/**
 * Keep Alive Script
 * 
 * This script pings your deployed application at regular intervals to prevent
 * it from sleeping on free hosting services like Render and Railway.
 * 
 * Usage:
 * 1. Deploy this script separately (e.g., on a different free service)
 * 2. Set the TARGET_URL environment variable to your deployed API URL
 * 3. Set the PING_INTERVAL environment variable (in minutes, default is 10)
 */

const https = require('https');
const http = require('http');

// Configuration
const TARGET_URL = process.env.TARGET_URL || 'http://localhost:3000';
const PING_INTERVAL = parseInt(process.env.PING_INTERVAL || '10', 10); // in minutes
const PORT = process.env.PORT || 3001;

// Utility to make HTTP/HTTPS requests
function pingService() {
  const url = new URL(TARGET_URL);
  const options = {
    hostname: url.hostname,
    port: url.port || (url.protocol === 'https:' ? 443 : 80),
    path: '/',
    method: 'GET',
    headers: {
      'User-Agent': 'StaffTracker-KeepAlive'
    }
  };
  
  const client = url.protocol === 'https:' ? https : http;
  
  const req = client.request(options, (res) => {
    console.log(`[${new Date().toISOString()}] Ping to ${TARGET_URL} - Status: ${res.statusCode}`);
    
    let data = '';
    res.on('data', (chunk) => {
      data += chunk;
    });
    
    res.on('end', () => {
      if (res.statusCode === 200) {
        console.log('✅ Service is running');
      } else {
        console.log(`⚠️ Service returned status code ${res.statusCode}`);
      }
    });
  });
  
  req.on('error', (error) => {
    console.error(`❌ Error pinging ${TARGET_URL}: ${error.message}`);
  });
  
  req.end();
}

// Create a simple server for the keep-alive script itself
const server = http.createServer((req, res) => {
  res.writeHead(200, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({
    status: 'running',
    target: TARGET_URL,
    interval: `${PING_INTERVAL} minutes`,
    lastPing: new Date().toISOString()
  }));
});

// Start the keep-alive service
server.listen(PORT, () => {
  console.log(`Keep-alive service running on port ${PORT}`);
  console.log(`Target: ${TARGET_URL}`);
  console.log(`Ping interval: ${PING_INTERVAL} minutes`);
  
  // Initial ping
  pingService();
  
  // Schedule regular pings
  setInterval(pingService, PING_INTERVAL * 60 * 1000);
  
  console.log('Ping scheduler started');
}); 