#!/bin/bash
# Staff Tracker Unified Deployment Script

echo "=== Staff Tracker Deployment Tool ==="
echo ""

# Check for dependencies
check_dependency() {
  if ! command -v $1 &> /dev/null; then
    echo "❌ $1 is not installed. Please install it before continuing."
    exit 1
  fi
}

check_dependency "node"
check_dependency "npm"

# Install dependencies
echo "📦 Installing dependencies..."
npm install

# Create required directories
echo "📁 Setting up directories..."
mkdir -p reports

# Check for Firebase credentials
if [ -f "serviceAccountKey.json" ]; then
  echo "✅ Firebase service account key found"
else
  echo "⚠️ No Firebase service account key found"
  echo "The application will run in DEMO mode"
  
  read -p "Do you want to set up Firebase credentials now? (y/n): " setup_firebase
  
  if [ "$setup_firebase" = "y" ]; then
    echo "Enter Firebase service account details:"
    read -p "Project ID: " project_id
    read -p "Private Key ID: " private_key_id
    read -p "Client Email: " client_email
    read -p "Client ID: " client_id
    
    # Create a basic template
    cat > serviceAccountKey.json << EOL
{
  "type": "service_account",
  "project_id": "$project_id",
  "private_key_id": "$private_key_id",
  "private_key": "-----BEGIN PRIVATE KEY-----\\nYOUR_PRIVATE_KEY_HERE\\n-----END PRIVATE KEY-----\\n",
  "client_email": "$client_email",
  "client_id": "$client_id",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/$client_email"
}
EOL
    
    echo "✅ Created serviceAccountKey.json template"
    echo "⚠️ IMPORTANT: You need to replace the private key with your actual private key"
  fi
fi

# Create .env file if it doesn't exist
if [ ! -f ".env" ]; then
  echo "📝 Creating .env file..."
  
  # Generate random string for JWT secret
  JWT_SECRET=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1)
  
  cat > .env << EOL
PORT=3000
NODE_ENV=development
JWT_SECRET=$JWT_SECRET
EOL
  
  echo "✅ Created .env file with random JWT secret"
else
  echo "✅ .env file already exists"
fi

# Select deployment method
echo ""
echo "📋 Select deployment method:"
echo "1) Start as local server"
echo "2) Deploy to Render.com (Free)"
echo "3) Deploy to Railway.app (Free tier)"
echo "4) Deploy to Heroku (Requires account)"
echo "5) Deploy with Docker"
read -p "Select option (1-5): " deploy_method

case $deploy_method in
  1)
    echo "🚀 Starting local server..."
    echo "The server will be available at http://localhost:3000"
    echo "Demo credentials:"
    echo "  Admin: username=admin, password=admin123"
    echo "  User: username=user, password=user123"
    echo "Press Ctrl+C to stop the server"
    node index.js
    ;;
    
  2)
    echo "🔄 Preparing for Render.com deployment..."
    echo ""
    echo "📋 Render.com Deployment Steps:"
    echo "1. Sign up for a free account at render.com"
    echo "2. Create a new Web Service and connect your GitHub repository:"
    echo "   https://github.com/KingAlex04/appp"
    echo "3. Configure the following settings:"
    echo "   - Root Directory: TrackerApp/backend"
    echo "   - Environment: Node"
    echo "   - Build Command: npm install"
    echo "   - Start Command: node index.js"
    echo "4. Add environment variables:"
    echo "   - PORT: 10000 (Render will override this)"
    echo "   - NODE_ENV: production"
    echo "   - JWT_SECRET: (copy from your .env file)"
    echo ""
    echo "🔗 Render Dashboard: https://dashboard.render.com/"
    ;;
    
  3)
    echo "🔄 Preparing for Railway.app deployment..."
    echo ""
    echo "📋 Railway.app Deployment Steps:"
    echo "1. Sign up for Railway at railway.app"
    echo "2. Create a new project and connect your GitHub repository:"
    echo "   https://github.com/KingAlex04/appp"
    echo "3. Configure Root Directory: TrackerApp/backend"
    echo "4. Add environment variables:"
    echo "   - PORT: 3000"
    echo "   - NODE_ENV: production"
    echo "   - JWT_SECRET: (copy from your .env file)"
    echo ""
    echo "🔗 Railway Dashboard: https://railway.app/dashboard"
    ;;
    
  4)
    check_dependency "heroku"
    echo "🔄 Preparing for Heroku deployment..."
    
    read -p "Have you already created a Heroku app? (y/n): " heroku_app_exists
    
    if [ "$heroku_app_exists" = "n" ]; then
      read -p "Enter app name (e.g., staff-tracker-backend): " heroku_app_name
      echo "Creating Heroku app $heroku_app_name..."
      heroku create $heroku_app_name
    else
      read -p "Enter your Heroku app name: " heroku_app_name
    fi
    
    echo "Setting up environment variables..."
    heroku config:set NODE_ENV=production -a $heroku_app_name
    
    # Get JWT_SECRET from .env file or generate a new one
    if [ -f ".env" ]; then
      JWT_SECRET=$(grep JWT_SECRET .env | cut -d '=' -f2)
    else
      JWT_SECRET=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1)
    fi
    
    heroku config:set JWT_SECRET=$JWT_SECRET -a $heroku_app_name
    
    echo "🚀 Ready to deploy to Heroku!"
    echo "To deploy, commit your changes and run:"
    echo "git subtree push --prefix TrackerApp/backend heroku main"
    ;;
    
  5)
    check_dependency "docker"
    echo "🔄 Preparing Docker deployment..."
    echo ""
    echo "1) Run locally with Docker"
    echo "2) Build Docker image only"
    read -p "Select option (1-2): " docker_option
    
    if [ "$docker_option" = "1" ]; then
      if command -v docker-compose &> /dev/null; then
        echo "🚀 Starting application with Docker Compose..."
        docker-compose up
      else
        echo "🚀 Starting application with Docker..."
        docker build -t staff-tracker-backend .
        docker run -p 3000:3000 staff-tracker-backend
      fi
    else
      echo "🔨 Building Docker image..."
      docker build -t staff-tracker-backend .
      echo "✅ Docker image built successfully"
      echo ""
      echo "To run the container, use:"
      echo "docker run -p 3000:3000 staff-tracker-backend"
    fi
    ;;
    
  *)
    echo "❌ Invalid option selected"
    exit 1
    ;;
esac

echo "✅ Deployment script completed" 