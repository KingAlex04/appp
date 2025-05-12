/**
 * Test Script for Deployed Staff Tracker API
 * 
 * This script tests the deployed API at https://appp-bx2c.onrender.com
 * Run with: node test-deployed-api.js
 */

const https = require('https');
const http = require('http');
const fs = require('fs');
const path = require('path');

// Configuration
const API_URL = 'https://appp-bx2c.onrender.com';
const ADMIN_USERNAME = 'admin';
const ADMIN_PASSWORD = 'admin123';
const USER_USERNAME = 'user';
const USER_PASSWORD = 'user123';

// Global token for authentication
let authToken = null;

/**
 * Make HTTP/HTTPS request
 */
function makeRequest(options, data = null) {
  return new Promise((resolve, reject) => {
    const url = new URL(options.url);
    const isHttps = url.protocol === 'https:';
    
    const requestOptions = {
      hostname: url.hostname,
      path: url.pathname + url.search,
      method: options.method || 'GET',
      headers: options.headers || {}
    };
    
    if (options.port) {
      requestOptions.port = options.port;
    }
    
    const client = isHttps ? https : http;
    
    const req = client.request(requestOptions, (res) => {
      const chunks = [];
      
      res.on('data', (chunk) => {
        chunks.push(chunk);
      });
      
      res.on('end', () => {
        const body = Buffer.concat(chunks);
        
        // Handle different content types
        if (res.headers['content-type']?.includes('application/json')) {
          try {
            resolve({
              statusCode: res.statusCode,
              headers: res.headers,
              body: JSON.parse(body.toString())
            });
          } catch (e) {
            resolve({
              statusCode: res.statusCode,
              headers: res.headers,
              body: body.toString()
            });
          }
        } else if (res.headers['content-type']?.includes('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')) {
          // Excel file
          resolve({
            statusCode: res.statusCode,
            headers: res.headers,
            body: body,
            isFile: true
          });
        } else {
          resolve({
            statusCode: res.statusCode,
            headers: res.headers,
            body: body.toString()
          });
        }
      });
    });
    
    req.on('error', (error) => {
      reject(error);
    });
    
    if (data) {
      req.write(typeof data === 'string' ? data : JSON.stringify(data));
    }
    
    req.end();
  });
}

/**
 * Login to API
 */
async function login(username, password) {
  try {
    console.log(`🔑 Logging in as ${username}...`);
    
    const response = await makeRequest({
      url: `${API_URL}/api/login`,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    }, {
      username,
      password
    });
    
    if (response.statusCode === 200) {
      authToken = response.body.token;
      console.log('✅ Login successful');
      console.log(`User: ${response.body.user.username} (${response.body.user.role})`);
      console.log(`Token: ${authToken.substring(0, 15)}...`);
      return true;
    } else {
      console.log(`❌ Login failed: ${JSON.stringify(response.body)}`);
      return false;
    }
  } catch (error) {
    console.error(`❌ Error during login: ${error.message}`);
    return false;
  }
}

/**
 * Test API root endpoint
 */
async function testRootEndpoint() {
  try {
    console.log('\n📡 Testing root endpoint...');
    
    const response = await makeRequest({
      url: API_URL
    });
    
    if (response.statusCode === 200) {
      console.log('✅ Root endpoint is working');
      console.log(`Response: ${JSON.stringify(response.body)}`);
      return true;
    } else {
      console.log(`❌ Root endpoint failed: ${JSON.stringify(response.body)}`);
      return false;
    }
  } catch (error) {
    console.error(`❌ Error testing root endpoint: ${error.message}`);
    return false;
  }
}

/**
 * Test reports endpoint
 */
async function testReportsEndpoint(staffId = 'staff1', period = 'daily') {
  try {
    if (!authToken) {
      console.log('❌ Not authenticated. Please login first.');
      return false;
    }
    
    console.log(`\n📊 Testing reports endpoint for staff ${staffId} (${period})...`);
    
    const response = await makeRequest({
      url: `${API_URL}/api/reports/${staffId}?period=${period}`,
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
    
    if (response.statusCode === 200) {
      console.log('✅ Reports endpoint is working');
      
      if (response.isFile) {
        // Save the Excel file
        const fileName = `staff_${staffId}_${period}_report.xlsx`;
        fs.writeFileSync(fileName, response.body);
        console.log(`📋 Report downloaded to ${fileName}`);
      } else {
        console.log(`Response: ${JSON.stringify(response.body)}`);
      }
      
      return true;
    } else {
      console.log(`❌ Reports endpoint failed: ${JSON.stringify(response.body)}`);
      return false;
    }
  } catch (error) {
    console.error(`❌ Error testing reports endpoint: ${error.message}`);
    return false;
  }
}

/**
 * Run all tests
 */
async function runTests() {
  console.log('🧪 Starting tests for Staff Tracker API');
  console.log(`🌐 API URL: ${API_URL}`);
  
  // Test root endpoint
  const rootEndpointWorking = await testRootEndpoint();
  
  if (!rootEndpointWorking) {
    console.log('❌ Root endpoint test failed. Aborting further tests.');
    return;
  }
  
  // Test login as admin
  const adminLoginSuccessful = await login(ADMIN_USERNAME, ADMIN_PASSWORD);
  
  if (adminLoginSuccessful) {
    // Test reports endpoint as admin
    await testReportsEndpoint();
  }
  
  // Test login as regular user
  const userLoginSuccessful = await login(USER_USERNAME, USER_PASSWORD);
  
  if (userLoginSuccessful) {
    // Test reports endpoint as regular user
    await testReportsEndpoint('staff2', 'weekly');
  }
  
  console.log('\n🏁 All tests completed!');
}

// Run the tests
runTests(); 