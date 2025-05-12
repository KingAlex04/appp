# Staff Tracker Application

A comprehensive staff tracking application with admin and user interfaces. The application allows tracking employee locations, managing check-in/check-out, and generating detailed reports.

## Components

### 1. Android App (Kotlin)
- Authentication system for admin and staff login
- Staff dashboard with check-in/check-out functionality
- Admin dashboard to monitor active staff
- Background location tracking service
- MVVM architecture implementation

### 2. Backend API (Node.js)
- Authentication with JWT tokens
- Report generation service (Excel exports)
- Support for various reporting periods
- Mock data mode for demonstration purposes

### 3. Firebase Integration
- Authentication for users
- Firestore database for storing user data and location logs
- Cloud Storage for staff photos

## Setup Instructions

### Backend Setup
1. Navigate to the backend directory:
   ```
   cd TrackerApp/backend
   ```

2. Install dependencies:
   ```
   npm install
   ```

3. Run the deployment script:
   ```
   powershell -ExecutionPolicy Bypass -File deploy.ps1
   ```

4. Follow the prompts to set up Firebase credentials or run in demo mode

### Android App Setup
1. Open the project in Android Studio
2. Update the backend API URL in the app configuration
3. Build and run the application

## Authentication

The backend includes default users for testing:
- Admin: username=`admin`, password=`admin123`
- User: username=`user`, password=`user123`

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

KingAlex04 