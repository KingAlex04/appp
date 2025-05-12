/**
 * Staff Tracker API Authentication Test Utility
 * 
 * This script helps test the authentication endpoints of the Staff Tracker API.
 * Run with: node test-auth.js
 */

const readline = require('readline');
const https = require('https');
const http = require('http');

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

// Store the token for subsequent requests
let authToken = null;

// Default to localhost, but can be changed
let apiUrl = 'http://localhost:3000';

function makeRequest(options, data = null) {
  return new Promise((resolve, reject) => {
    const client = options.protocol === 'https:' ? https : http;
    
    const req = client.request(options, (res) => {
      let responseData = '';
      
      res.on('data', (chunk) => {
        responseData += chunk;
      });
      
      res.on('end', () => {
        try {
          const jsonResponse = JSON.parse(responseData);
          resolve({ status: res.statusCode, data: jsonResponse });
        } catch (error) {
          resolve({ status: res.statusCode, data: responseData });
        }
      });
    });
    
    req.on('error', (error) => {
      reject(error);
    });
    
    if (data) {
      req.write(JSON.stringify(data));
    }
    
    req.end();
  });
}

async function login(username, password) {
  try {
    const url = new URL('/api/login', apiUrl);
    
    const options = {
      hostname: url.hostname,
      port: url.port,
      path: url.pathname,
      method: 'POST',
      protocol: url.protocol,
      headers: {
        'Content-Type': 'application/json'
      }
    };
    
    const response = await makeRequest(options, { username, password });
    
    if (response.status === 200) {
      authToken = response.data.token;
      console.log('\n✅ Login successful');
      console.log('Token:', authToken);
      console.log('User:', response.data.user);
    } else {
      console.log(`\n❌ Login failed (${response.status}):`, response.data.error);
    }
    
    return response;
  } catch (error) {
    console.error('\n❌ Login request error:', error.message);
  }
}

async function testApiEndpoint(endpoint) {
  try {
    if (!authToken) {
      console.log('\n❌ Not authenticated. Please login first.');
      return;
    }
    
    const url = new URL(endpoint, apiUrl);
    
    const options = {
      hostname: url.hostname,
      port: url.port,
      path: url.pathname,
      method: 'GET',
      protocol: url.protocol,
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    };
    
    const response = await makeRequest(options);
    
    if (response.status === 200) {
      console.log(`\n✅ API Request to ${endpoint} successful`);
      console.log('Response:', response.data);
    } else {
      console.log(`\n❌ API Request to ${endpoint} failed (${response.status}):`, response.data.error);
    }
  } catch (error) {
    console.error(`\n❌ API Request to ${endpoint} error:`, error.message);
  }
}

function showMenu() {
  console.log('\n===== Staff Tracker API Test Menu =====');
  console.log('1. Set API URL (current:', apiUrl, ')');
  console.log('2. Login');
  console.log('3. Test Reports Endpoint');
  console.log('4. Register New User (Admin only)');
  console.log('5. Exit');
  
  rl.question('\nSelect an option (1-5): ', async (answer) => {
    switch(answer) {
      case '1':
        rl.question('Enter API URL (e.g., http://localhost:3000): ', (url) => {
          apiUrl = url;
          console.log(`API URL set to ${apiUrl}`);
          showMenu();
        });
        break;
        
      case '2':
        rl.question('Username: ', (username) => {
          rl.question('Password: ', async (password) => {
            await login(username, password);
            showMenu();
          });
        });
        break;
        
      case '3':
        rl.question('Staff ID (e.g., staff1): ', async (staffId) => {
          await testApiEndpoint(`/api/reports/${staffId}`);
          showMenu();
        });
        break;
        
      case '4':
        if (!authToken) {
          console.log('\n❌ Not authenticated. Please login first.');
          showMenu();
          break;
        }
        
        rl.question('New username: ', (username) => {
          rl.question('New password: ', async (password) => {
            rl.question('Role (admin/user): ', async (role) => {
              try {
                const url = new URL('/api/register', apiUrl);
                
                const options = {
                  hostname: url.hostname,
                  port: url.port,
                  path: url.pathname,
                  method: 'POST',
                  protocol: url.protocol,
                  headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${authToken}`
                  }
                };
                
                const response = await makeRequest(options, { username, password, role });
                
                if (response.status === 201) {
                  console.log('\n✅ User registration successful');
                  console.log('User:', response.data.user);
                } else {
                  console.log(`\n❌ User registration failed (${response.status}):`, response.data.error);
                }
              } catch (error) {
                console.error('\n❌ User registration error:', error.message);
              }
              
              showMenu();
            });
          });
        });
        break;
        
      case '5':
        console.log('Exiting...');
        rl.close();
        break;
        
      default:
        console.log('Invalid option. Please try again.');
        showMenu();
    }
  });
}

console.log('Staff Tracker API Test Utility');
console.log('==============================');
console.log('This utility helps you test the authentication and API endpoints.');

showMenu(); 