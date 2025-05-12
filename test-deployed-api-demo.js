/**
 * Test Script for Deployed Staff Tracker API (DEMO Mode)
 * 
 * This script tests the deployed API at https://appp-bx2c.onrender.com
 * which is running in DEMO mode
 * 
 * Run with: node test-deployed-api-demo.js
 */

const https = require('https');

// Configuration
const API_URL = 'https://appp-bx2c.onrender.com';

/**
 * Make HTTP/HTTPS request
 */
function makeRequest(url, method = 'GET') {
  return new Promise((resolve, reject) => {
    const parsedUrl = new URL(url);
    
    const options = {
      hostname: parsedUrl.hostname,
      path: parsedUrl.pathname + parsedUrl.search,
      method: method
    };
    
    const req = https.request(options, (res) => {
      let data = '';
      
      res.on('data', (chunk) => {
        data += chunk;
      });
      
      res.on('end', () => {
        try {
          const jsonData = JSON.parse(data);
          resolve({
            statusCode: res.statusCode,
            body: jsonData
          });
        } catch (e) {
          resolve({
            statusCode: res.statusCode,
            body: data
          });
        }
      });
    });
    
    req.on('error', (error) => {
      reject(error);
    });
    
    req.end();
  });
}

/**
 * Test API root endpoint
 */
async function testRootEndpoint() {
  try {
    console.log('Testing root endpoint...');
    
    const response = await makeRequest(API_URL);
    
    console.log(`Status code: ${response.statusCode}`);
    console.log(`Response: ${JSON.stringify(response.body)}`);
    
    if (response.statusCode === 200) {
      console.log('✅ Root endpoint is working correctly');
      return true;
    } else {
      console.log('❌ Root endpoint returned an error');
      return false;
    }
  } catch (error) {
    console.error(`❌ Error testing root endpoint: ${error.message}`);
    return false;
  }
}

/**
 * Test mock staff data
 */
async function testMockStaff() {
  // Since we're in DEMO mode, we can test against the predefined mock staff IDs
  const staffIds = ['staff1', 'staff2'];
  
  for (const staffId of staffIds) {
    try {
      console.log(`\nTesting mock staff data for ${staffId}...`);
      
      // We're in DEMO mode, so we might not have authentication
      // Let's see what happens when we try to access the reports endpoint directly
      const response = await makeRequest(`${API_URL}/api/reports/${staffId}`);
      
      console.log(`Status code: ${response.statusCode}`);
      
      if (response.statusCode === 200) {
        console.log(`✅ Staff ${staffId} data is accessible`);
      } else if (response.statusCode === 401) {
        console.log(`ℹ️ Authentication required for staff ${staffId} data`);
      } else {
        console.log(`❌ Failed to access staff ${staffId} data: ${JSON.stringify(response.body)}`);
      }
    } catch (error) {
      console.error(`❌ Error testing staff ${staffId} data: ${error.message}`);
    }
  }
}

/**
 * Run all tests
 */
async function runTests() {
  console.log('🧪 Starting tests for Staff Tracker API in DEMO Mode');
  console.log(`🌐 API URL: ${API_URL}\n`);
  
  // Test root endpoint
  const rootEndpointResult = await testRootEndpoint();
  
  if (rootEndpointResult) {
    // If the root endpoint is working, test mock staff data
    await testMockStaff();
  }
  
  console.log('\n🏁 Testing completed');
  
  // Instructions for next steps
  console.log('\n📋 Next steps:');
  console.log('1. Verify your Android app is configured with the correct API URL:');
  console.log(`   ${API_URL}`);
  console.log('2. Update your Firebase configuration if needed');
  console.log('3. Use the "keep-alive.js" script to prevent the free service from sleeping');
}

// Run the tests
runTests(); 