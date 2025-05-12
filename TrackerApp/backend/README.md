# Staff Tracker Backend

This is a Node.js backend service for the Staff Tracker application. It provides API endpoints for generating Excel reports of staff location data and authentication features.

## Features

- User authentication with JWT tokens
- Admin and regular user roles
- Generate Excel reports for staff location tracking
- Support for different report periods (daily, weekly, monthly, quarterly, yearly)
- Customizable date ranges for reports
- Excel reports include staff details, sessions, and location data
- Demo mode when Firebase credentials are unavailable

## Setup

1. Install dependencies:
   ```
   npm install
   ```

2. Configure environment variables:
   - Copy `.env.example` to `.env` (or use the deployment script to create one)
   - Update the Firebase service account information in the `.env` file or provide a `serviceAccountKey.json` file

3. Start the server:
   - For development: `npm run dev`
   - For production: `npm start`

## Authentication

The system comes with two default users:
- Admin: username=`admin`, password=`admin123`
- User: username=`user`, password=`user123`

### Authentication Endpoints

- **Login**: `POST /api/login`
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
  Response includes a JWT token to use for authentication.

- **Register New User** (Admin only): `POST /api/register`
  ```json
  {
    "username": "newuser",
    "password": "password123",
    "role": "user"
  }
  ```
  Requires authentication with admin privileges.

## API Endpoints

### Generate Report (Authenticated)

```
GET /api/reports/:staffId
```

Parameters:
- `staffId` - ID of the staff member (required)
- `period` - Report period (daily, weekly, monthly, quarterly, yearly)
- `startDate` - Custom start date (format: YYYY-MM-DD)
- `endDate` - Custom end date (format: YYYY-MM-DD)

Headers:
- `Authorization: Bearer YOUR_JWT_TOKEN`

Response:
- Excel file download

## Deployment

1. Run the deployment script:
   ```
   pwsh deploy.ps1
   ```
   or
   ```
   powershell -ExecutionPolicy Bypass -File deploy.ps1
   ```

2. Follow the interactive prompts to:
   - Install dependencies
   - Set up Firebase credentials (optional)
   - Configure environment variables
   - Select deployment method

### Free Hosting Options

This service can be deployed to:

- **Railway** (https://railway.app)
- **Render** (https://render.com)
- **Heroku** (https://heroku.com)
- Any other Node.js hosting service

For deployment, make sure to set the environment variables properly:
- `PORT`: The port to run the server on (usually set by the hosting service)
- `NODE_ENV`: Set to `production` for production environments
- `JWT_SECRET`: A secret key for signing JWT tokens
- `FIREBASE_SERVICE_ACCOUNT`: JSON string of Firebase service account credentials (if not using a separate file)

## Firebase Setup

The backend requires a Firebase service account key to connect to Firestore. Follow these steps to obtain it:

1. Go to the Firebase Console
2. Navigate to Project Settings > Service Accounts
3. Click "Generate New Private Key"
4. Save the JSON file as `serviceAccountKey.json` in the project root or use its contents in the environment variable

## Testing the API

Use the included test utility:

```
node test-auth.js
```

This utility allows you to:
1. Test login functionality
2. Test protected API endpoints
3. Register new users (admin only)
4. Configure API URL for testing deployed instances 