const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
const xl = require('excel4node');
const moment = require('moment');
const path = require('path');
const dotenv = require('dotenv');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');

// Load environment variables
dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'your_jwt_secret_key';

// Middleware
app.use(cors());
app.use(express.json());

// Initialize Firebase Admin (or use mock in development)
let db;
let firebaseInitialized = false;

try {
  // Try to use environment variables for Firebase config in production
  if (process.env.FIREBASE_SERVICE_ACCOUNT) {
    const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    firebaseInitialized = true;
  } else {
    // Fallback to local file for development
    try {
      const serviceAccount = require('./serviceAccountKey.json');
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
      });
      firebaseInitialized = true;
    } catch (err) {
      console.warn('Failed to initialize Firebase with service account file:', err.message);
      console.warn('Running in DEMO mode without Firebase');
    }
  }
  
  if (firebaseInitialized) {
    db = admin.firestore();
    console.log('Firebase initialized successfully');
  }
} catch (error) {
  console.error('Error initializing Firebase:', error);
  console.warn('Running in DEMO mode without Firebase');
}

// Mock data for demo mode
const mockStaff = {
  "staff1": { id: "staff1", name: "John Doe", email: "john.doe@example.com", contactNumber: "123-456-7890", address: "123 Main St" },
  "staff2": { id: "staff2", name: "Jane Smith", email: "jane.smith@example.com", contactNumber: "987-654-3210", address: "456 Oak Ave" }
};

const mockSessions = [
  { id: "session1", staffId: "staff1", checkInTime: new Date("2023-05-01T09:00:00"), checkOutTime: new Date("2023-05-01T17:00:00"), 
    checkInLocation: { latitude: 40.7128, longitude: -74.0060 }, checkOutLocation: { latitude: 40.7129, longitude: -74.0061 } },
  { id: "session2", staffId: "staff2", checkInTime: new Date("2023-05-01T08:30:00"), checkOutTime: new Date("2023-05-01T16:30:00"),
    checkInLocation: { latitude: 34.0522, longitude: -118.2437 }, checkOutLocation: { latitude: 34.0523, longitude: -118.2438 } }
];

const mockLocations = [
  { id: "loc1", staffId: "staff1", sessionId: "session1", timestamp: new Date("2023-05-01T10:00:00"), latitude: 40.7130, longitude: -74.0062, accuracy: 10, provider: "GPS" },
  { id: "loc2", staffId: "staff1", sessionId: "session1", timestamp: new Date("2023-05-01T12:00:00"), latitude: 40.7132, longitude: -74.0064, accuracy: 8, provider: "GPS" },
  { id: "loc3", staffId: "staff2", sessionId: "session2", timestamp: new Date("2023-05-01T09:30:00"), latitude: 34.0524, longitude: -118.2439, accuracy: 12, provider: "GPS" }
];

// Mock users for authentication
const users = {
  "admin": {
    id: "admin1",
    username: "admin",
    password: "$2b$10$i8.6UubDvZUWUW9O9CvHLeRKQO5HnQR/pYWNZMMs92jgZ1iNO573O", // hashed "admin123"
    role: "admin"
  },
  "user": {
    id: "user1",
    username: "user",
    password: "$2b$10$3o3RjAzOvIBJu9SQiQlg5eH8ok6o6.97T1yOIhESM97UVA.95UzRS", // hashed "user123"
    role: "user"
  }
};

// Authentication middleware
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];
  
  if (!token) {
    return res.status(401).json({ error: 'Unauthorized: No token provided' });
  }
  
  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ error: 'Forbidden: Invalid token' });
    }
    req.user = user;
    next();
  });
};

// Login endpoint
app.post('/api/login', async (req, res) => {
  const { username, password } = req.body;
  
  if (!username || !password) {
    return res.status(400).json({ error: 'Username and password are required' });
  }
  
  const user = users[username];
  
  if (!user) {
    return res.status(401).json({ error: 'Invalid username or password' });
  }
  
  try {
    const passwordMatch = await bcrypt.compare(password, user.password);
    
    if (!passwordMatch) {
      return res.status(401).json({ error: 'Invalid username or password' });
    }
    
    // Generate JWT token
    const token = jwt.sign({
      id: user.id,
      username: user.username,
      role: user.role
    }, JWT_SECRET, { expiresIn: '24h' });
    
    res.json({
      message: 'Authentication successful',
      token,
      user: {
        id: user.id,
        username: user.username,
        role: user.role
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Register new user (admin only)
app.post('/api/register', authenticateToken, async (req, res) => {
  // Check if requester is admin
  if (req.user.role !== 'admin') {
    return res.status(403).json({ error: 'Forbidden: Only admins can register new users' });
  }
  
  const { username, password, role } = req.body;
  
  if (!username || !password || !role) {
    return res.status(400).json({ error: 'Username, password, and role are required' });
  }
  
  if (users[username]) {
    return res.status(409).json({ error: 'Username already exists' });
  }
  
  try {
    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);
    
    // Create new user
    const newUser = {
      id: `user${Object.keys(users).length + 1}`,
      username,
      password: hashedPassword,
      role
    };
    
    // Add to users object
    users[username] = newUser;
    
    res.status(201).json({
      message: 'User registered successfully',
      user: {
        id: newUser.id,
        username: newUser.username,
        role: newUser.role
      }
    });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Routes
app.get('/', (req, res) => {
  res.json({ message: 'Staff Tracker API is running', mode: firebaseInitialized ? 'PRODUCTION' : 'DEMO' });
});

// Protected routes - require authentication
// Generate report for a staff member
app.get('/api/reports/:staffId', authenticateToken, async (req, res) => {
  try {
    const { staffId } = req.params;
    const { period, startDate, endDate } = req.query;
    
    if (!staffId) {
      return res.status(400).json({ error: 'Staff ID is required' });
    }
    
    let staff, locations, sessions;
    
    // Get data either from Firebase or mock data
    if (firebaseInitialized) {
      // Get staff info from Firebase
      const staffDoc = await db.collection('staff').doc(staffId).get();
      if (!staffDoc.exists) {
        return res.status(404).json({ error: 'Staff not found' });
      }
      
      staff = { id: staffDoc.id, ...staffDoc.data() };
      
      // Calculate date range based on period
      let start, end;
      if (startDate && endDate) {
        start = moment(startDate);
        end = moment(endDate);
      } else {
        switch (period) {
          case 'daily':
            start = moment().startOf('day');
            end = moment().endOf('day');
            break;
          case 'weekly':
            start = moment().startOf('week');
            end = moment().endOf('week');
            break;
          case 'monthly':
            start = moment().startOf('month');
            end = moment().endOf('month');
            break;
          case 'quarterly':
            start = moment().startOf('quarter');
            end = moment().endOf('quarter');
            break;
          case 'yearly':
            start = moment().startOf('year');
            end = moment().endOf('year');
            break;
          default:
            // Default to last 30 days
            start = moment().subtract(30, 'days').startOf('day');
            end = moment().endOf('day');
        }
      }
      
      // Get all locations for this staff in the date range from Firebase
      const locationsSnapshot = await db.collection('locations')
        .where('staffId', '==', staffId)
        .where('timestamp', '>=', admin.firestore.Timestamp.fromDate(start.toDate()))
        .where('timestamp', '<=', admin.firestore.Timestamp.fromDate(end.toDate()))
        .orderBy('timestamp', 'asc')
        .get();
      
      locations = locationsSnapshot.docs.map(doc => {
        const data = doc.data();
        // Convert Firestore timestamp to JavaScript Date
        return {
          id: doc.id,
          ...data,
          timestamp: data.timestamp.toDate()
        };
      });
      
      // Get all sessions for this staff in the date range from Firebase
      const sessionsSnapshot = await db.collection('sessions')
        .where('staffId', '==', staffId)
        .where('checkInTime', '>=', admin.firestore.Timestamp.fromDate(start.toDate()))
        .where('checkInTime', '<=', admin.firestore.Timestamp.fromDate(end.toDate()))
        .orderBy('checkInTime', 'asc')
        .get();
      
      sessions = sessionsSnapshot.docs.map(doc => {
        const data = doc.data();
        // Convert Firestore timestamps to JavaScript Dates
        return {
          id: doc.id,
          ...data,
          checkInTime: data.checkInTime.toDate(),
          checkOutTime: data.checkOutTime ? data.checkOutTime.toDate() : null
        };
      });
    } else {
      // Use mock data
      staff = mockStaff[staffId];
      if (!staff) {
        return res.status(404).json({ error: 'Staff not found' });
      }
      
      // Filter mock sessions and locations by staff ID
      sessions = mockSessions.filter(s => s.staffId === staffId);
      locations = mockLocations.filter(l => l.staffId === staffId);
    }
    
    // Generate Excel file
    const wb = new xl.Workbook();
    
    // Add a staffInfo worksheet
    const staffInfoSheet = wb.addWorksheet('Staff Info');
    
    // Add styles
    const headerStyle = wb.createStyle({
      font: {
        bold: true,
        color: '#FFFFFF'
      },
      fill: {
        type: 'pattern',
        patternType: 'solid',
        fgColor: '#4472C4'
      }
    });
    
    const dateStyle = wb.createStyle({
      numberFormat: 'yyyy-mm-dd hh:mm:ss'
    });
    
    // Staff Info sheet
    staffInfoSheet.cell(1, 1).string('Staff ID').style(headerStyle);
    staffInfoSheet.cell(1, 2).string('Name').style(headerStyle);
    staffInfoSheet.cell(1, 3).string('Email').style(headerStyle);
    staffInfoSheet.cell(1, 4).string('Contact Number').style(headerStyle);
    staffInfoSheet.cell(1, 5).string('Address').style(headerStyle);
    
    staffInfoSheet.cell(2, 1).string(staff.id);
    staffInfoSheet.cell(2, 2).string(staff.name || '');
    staffInfoSheet.cell(2, 3).string(staff.email || '');
    staffInfoSheet.cell(2, 4).string(staff.contactNumber || '');
    staffInfoSheet.cell(2, 5).string(staff.address || '');
    
    // Add a sessions worksheet
    const sessionsSheet = wb.addWorksheet('Sessions');
    
    sessionsSheet.cell(1, 1).string('Session ID').style(headerStyle);
    sessionsSheet.cell(1, 2).string('Check In Time').style(headerStyle);
    sessionsSheet.cell(1, 3).string('Check Out Time').style(headerStyle);
    sessionsSheet.cell(1, 4).string('Duration (hours)').style(headerStyle);
    sessionsSheet.cell(1, 5).string('Check In Location').style(headerStyle);
    sessionsSheet.cell(1, 6).string('Check Out Location').style(headerStyle);
    
    sessions.forEach((session, index) => {
      const rowIndex = index + 2;
      
      sessionsSheet.cell(rowIndex, 1).string(session.id);
      sessionsSheet.cell(rowIndex, 2).date(session.checkInTime).style(dateStyle);
      
      if (session.checkOutTime) {
        sessionsSheet.cell(rowIndex, 3).date(session.checkOutTime).style(dateStyle);
        
        // Calculate duration in hours
        const duration = moment(session.checkOutTime).diff(moment(session.checkInTime), 'hours', true);
        sessionsSheet.cell(rowIndex, 4).number(parseFloat(duration.toFixed(2)));
      } else {
        sessionsSheet.cell(rowIndex, 3).string('Still active');
        sessionsSheet.cell(rowIndex, 4).string('N/A');
      }
      
      // Format locations
      const checkInLocation = session.checkInLocation ? 
        `Lat: ${session.checkInLocation.latitude}, Lng: ${session.checkInLocation.longitude}` : 'N/A';
      sessionsSheet.cell(rowIndex, 5).string(checkInLocation);
      
      const checkOutLocation = session.checkOutLocation ? 
        `Lat: ${session.checkOutLocation.latitude}, Lng: ${session.checkOutLocation.longitude}` : 'N/A';
      sessionsSheet.cell(rowIndex, 6).string(checkOutLocation);
    });
    
    // Add a locations worksheet
    const locationsSheet = wb.addWorksheet('Locations');
    
    locationsSheet.cell(1, 1).string('Time').style(headerStyle);
    locationsSheet.cell(1, 2).string('Latitude').style(headerStyle);
    locationsSheet.cell(1, 3).string('Longitude').style(headerStyle);
    locationsSheet.cell(1, 4).string('Accuracy').style(headerStyle);
    locationsSheet.cell(1, 5).string('Provider').style(headerStyle);
    locationsSheet.cell(1, 6).string('Session ID').style(headerStyle);
    
    locations.forEach((location, index) => {
      const rowIndex = index + 2;
      
      locationsSheet.cell(rowIndex, 1).date(location.timestamp).style(dateStyle);
      locationsSheet.cell(rowIndex, 2).number(location.latitude);
      locationsSheet.cell(rowIndex, 3).number(location.longitude);
      locationsSheet.cell(rowIndex, 4).number(location.accuracy || 0);
      locationsSheet.cell(rowIndex, 5).string(location.provider || 'Unknown');
      locationsSheet.cell(rowIndex, 6).string(location.sessionId || '');
    });
    
    // Generate timestamp for filename
    const timestamp = moment().format('YYYY-MM-DD_HH-mm-ss');
    const reportName = `${staff.name.replace(/\s+/g, '_')}_${period || 'custom'}_${timestamp}.xlsx`;
    const filePath = path.join(__dirname, 'reports', reportName);
    
    // Ensure reports directory exists
    const fs = require('fs');
    if (!fs.existsSync(path.join(__dirname, 'reports'))) {
      fs.mkdirSync(path.join(__dirname, 'reports'), { recursive: true });
    }
    
    // Write to file and send response
    wb.write(filePath, (err, stats) => {
      if (err) {
        console.error('Error generating Excel file:', err);
        return res.status(500).json({ error: 'Failed to generate report' });
      }
      
      // Send file as download
      res.download(filePath, reportName, (err) => {
        if (err) {
          console.error('Error sending file:', err);
          return res.status(500).json({ error: 'Failed to send report' });
        }
        
        // Delete file after sending
        fs.unlink(filePath, (err) => {
          if (err) console.error('Error deleting temporary file:', err);
        });
      });
    });
  } catch (error) {
    console.error('Error generating report:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Helper function to generate password hashes (for reference)
app.get('/generate-password-hash/:password', (req, res) => {
  const { password } = req.params;
  bcrypt.hash(password, 10)
    .then(hash => {
      res.json({ hash });
    })
    .catch(err => {
      res.status(500).json({ error: err.message });
    });
});

// Start server
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  console.log(`Demo credentials:`);
  console.log(`Admin: username=admin, password=admin123`);
  console.log(`User: username=user, password=user123`);
}); 