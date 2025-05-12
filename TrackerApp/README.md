# Staff Tracker App

A comprehensive staff tracking application with admin and user interfaces for monitoring staff locations.

## Features

### User (Staff) Features
- Login with email and password
- Check-in and check-out functionality
- Background location tracking during working hours
- Clean and intuitive UI

### Admin Features
- Dashboard with real-time view of active staff
- Staff management (add, edit, view staff)
- Reports generation (daily, weekly, monthly, quarterly, yearly)
- Export location data to Excel

## Architecture

The application consists of:

1. **Android App** - Native application written in Kotlin
2. **Firebase Backend** - Authentication, Firestore, and Storage
3. **Reports API** - Node.js service for generating Excel reports

## Technology Stack

### Android App
- Kotlin
- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Google Maps & Location Services
- WorkManager for background tasks
- MVVM Architecture

### Backend
- Firebase (serverless)
- Node.js + Express for reports API
- Excel generation using excel4node

## Setup

### Prerequisites
- Android Studio
- Node.js and npm (for backend)
- Firebase account

### Android App Setup
1. Clone the repository
2. Open the `android` folder in Android Studio
3. Create a Firebase project and add the `google-services.json` file
4. Build and run the app

### Backend Setup
1. Navigate to the `backend` directory
2. Install dependencies: `npm install`
3. Create a `.env` file (see `.env.example`)
4. Add your Firebase service account key
5. Start the server: `npm run dev`

## Deployment

### Android App
- Generate signed APK or App Bundle
- Publish to Google Play Store or distribute internally

### Backend API
- Deploy to Railway, Render, or any other Node.js hosting
- Set environment variables in the hosting platform

## Firebase Setup

1. Create a Firebase project
2. Enable Authentication (Email/Password)
3. Create Firestore Database with collections:
   - `staff`
   - `locations`
   - `sessions`
4. Set up Storage for staff photos
5. Add security rules for proper data access

## License

This project is licensed under the MIT License - see the LICENSE file for details. 