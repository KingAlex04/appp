/**
 * Keep-Alive Script for Staff Tracker API
 * 
 * This script pings the deployed API at regular intervals to keep it from sleeping.
 * Run with: node keep-appp-alive.js
 */

const https = require('https');

// Configuration
const TARGET_URL = 'https://appp-bx2c.onrender.com';
const PING_INTERVAL = 10; // minutes

/**
 * Ping the service
 */
function pingService() {
  const now = new Date();
  console.log(`[${now.toISOString()}] Pinging ${TARGET_URL}...`);
  
  const req = https.get(TARGET_URL, (res) => {
    let data = '';
    
    res.on('data', (chunk) => {
      data += chunk;
    });
    
    res.on('end', () => {
      if (res.statusCode === 200) {
        console.log(`✅ [${now.toISOString()}] Service is running (Status: ${res.statusCode})`);
      } else {
        console.log(`⚠️ [${now.toISOString()}] Service returned status code ${res.statusCode}`);
      }
    });
  });
  
  req.on('error', (error) => {
    console.error(`❌ [${now.toISOString()}] Error pinging service: ${error.message}`);
  });
  
  req.end();
}

// Initial ping
console.log('🔄 Starting keep-alive service for Staff Tracker API');
console.log(`🌐 Target URL: ${TARGET_URL}`);
console.log(`⏱️ Ping interval: ${PING_INTERVAL} minutes`);
console.log('Press Ctrl+C to stop\n');

pingService();

// Schedule regular pings
setInterval(pingService, PING_INTERVAL * 60 * 1000);

// Keep the script running
process.on('SIGINT', () => {
  console.log('\n🛑 Keep-alive service stopped');
  process.exit();
}); 