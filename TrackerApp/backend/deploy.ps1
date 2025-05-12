# Staff Tracker Backend Deployment Script for Windows
Write-Host "=== Staff Tracker Backend Deployment ===" -ForegroundColor Green

# Step 1: Install dependencies
Write-Host "Step 1: Installing dependencies..." -ForegroundColor Yellow
npm install

# Step 2: Create required directories
Write-Host "Step 2: Creating required directories..." -ForegroundColor Yellow
if (-not (Test-Path -Path "reports")) {
    New-Item -ItemType Directory -Path "reports" | Out-Null
    Write-Host "  - Created reports directory" -ForegroundColor Gray
} else {
    Write-Host "  - Reports directory already exists" -ForegroundColor Gray
}

# Step 3: Check for Firebase credentials
Write-Host "Step 3: Checking Firebase credentials..." -ForegroundColor Yellow
if (Test-Path -Path "serviceAccountKey.json") {
    Write-Host "  - Firebase service account key found" -ForegroundColor Green
} else {
    Write-Host "  - WARNING: No Firebase service account key found" -ForegroundColor Red
    Write-Host "  - The application will run in DEMO mode" -ForegroundColor Yellow
    
    # Prompt for Firebase configuration
    $setupFirebase = Read-Host "Do you want to set up Firebase credentials now? (y/n)"
    
    if ($setupFirebase -eq "y") {
        Write-Host "Enter Firebase service account details:"
        $projectId = Read-Host "Project ID"
        $privateKeyId = Read-Host "Private Key ID"
        $clientEmail = Read-Host "Client Email"
        $clientId = Read-Host "Client ID"
        
        # Create a basic template - this is not a valid key but will help the user set up later
        $serviceAccountTemplate = @{
            "type" = "service_account"
            "project_id" = $projectId
            "private_key_id" = $privateKeyId
            "private_key" = "-----BEGIN PRIVATE KEY-----\nYOUR_PRIVATE_KEY_HERE\n-----END PRIVATE KEY-----\n"
            "client_email" = $clientEmail
            "client_id" = $clientId
            "auth_uri" = "https://accounts.google.com/o/oauth2/auth"
            "token_uri" = "https://oauth2.googleapis.com/token"
            "auth_provider_x509_cert_url" = "https://www.googleapis.com/oauth2/v1/certs"
            "client_x509_cert_url" = "https://www.googleapis.com/robot/v1/metadata/x509/$($clientEmail -replace '@', '%40' -replace '\\.', '%2E')"
        }
        
        $serviceAccountTemplate | ConvertTo-Json | Out-File -FilePath "serviceAccountKey.json" -Encoding UTF8
        Write-Host "  - Created serviceAccountKey.json template" -ForegroundColor Green
        Write-Host "  - IMPORTANT: You need to replace the private key with your actual private key" -ForegroundColor Yellow
    }
}

# Step 4: Create .env file if it doesn't exist
Write-Host "Step 4: Setting up environment variables..." -ForegroundColor Yellow
if (-not (Test-Path -Path ".env")) {
    Write-Host "  - Creating .env file" -ForegroundColor Gray
    $jwtSecret = -join ((65..90) + (97..122) + (48..57) | Get-Random -Count 32 | ForEach-Object {[char]$_})
    @"
PORT=3000
NODE_ENV=development
JWT_SECRET=$jwtSecret
"@ | Out-File -FilePath ".env" -Encoding UTF8
    
    Write-Host "  - Created .env file with random JWT secret" -ForegroundColor Green
} else {
    Write-Host "  - .env file already exists" -ForegroundColor Gray
}

# Step 5: Prompt for deployment method
Write-Host "Step 5: Select deployment method..." -ForegroundColor Yellow
Write-Host "1. Start as a local server"
Write-Host "2. Deploy to a free hosting service (instructions)"
$deployMethod = Read-Host "Select deployment method (1-2)"

if ($deployMethod -eq "1") {
    Write-Host "Starting local server..." -ForegroundColor Green
    Write-Host "The server will be available at http://localhost:3000" -ForegroundColor Green
    Write-Host "Demo credentials:" -ForegroundColor Cyan
    Write-Host "  Admin: username=admin, password=admin123" -ForegroundColor Cyan
    Write-Host "  User: username=user, password=user123" -ForegroundColor Cyan
    Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
    node index.js
} elseif ($deployMethod -eq "2") {
    Write-Host "=== Deployment Instructions ===" -ForegroundColor Magenta
    Write-Host "Option 1: Deploy to Render.com (Free)" -ForegroundColor Cyan
    Write-Host "1. Sign up for a free account at render.com"
    Write-Host "2. Create a new Web Service and connect your GitHub repository"
    Write-Host "3. Configure the following settings:"
    Write-Host "   - Build Command: npm install"
    Write-Host "   - Start Command: node index.js"
    Write-Host "   - Add environment variables from your .env file"
    Write-Host "   - If using Firebase, add FIREBASE_SERVICE_ACCOUNT environment variable"
    Write-Host ""
    
    Write-Host "Option 2: Deploy to Railway.app (Free tier available)" -ForegroundColor Cyan
    Write-Host "1. Sign up for a free account at railway.app"
    Write-Host "2. Create a new project and connect your GitHub repository"
    Write-Host "3. Add the required environment variables"
    Write-Host "4. Deploy your application"
    Write-Host ""
    
    Write-Host "Option 3: Deploy to Heroku (Credit card required but free tier available)" -ForegroundColor Cyan
    Write-Host "1. Install Heroku CLI and login"
    Write-Host "2. Run these commands in your project directory:"
    Write-Host "   heroku create staff-tracker-backend"
    Write-Host "   git push heroku main"
    Write-Host "   heroku config:set NODE_ENV=production JWT_SECRET=your_secret_key"
    Write-Host "   If using Firebase, add FIREBASE_SERVICE_ACCOUNT config var"
    
    Write-Host ""
    Write-Host "After deployment, your API will be available at the URL provided by your hosting service" -ForegroundColor Green
    Write-Host "Remember to update your mobile app to use the new API endpoint" -ForegroundColor Yellow
}

Write-Host "Deployment script completed" -ForegroundColor Green 