# E-Commerce Microservices Dashboard

A modern web interface to visualize and interact with the E-Commerce microservices platform.

## Features

### 🔍 Service Monitoring
- Real-time health checks for all three microservices
- Visual status indicators (Healthy/Unhealthy/Checking)
- Auto-refresh every 30 seconds

### 📊 Data Visualization
- View all users, products, and orders
- JSON response viewer with syntax highlighting
- Real-time activity logs

### 🛠️ Interactive Forms
- **User Registration**: Create new users with validation
- **Order Creation**: Place orders by selecting users and products
- Dropdown menus populated with live data

### 🎨 Modern UI
- Responsive design that works on desktop and mobile
- Clean, professional interface with hover effects
- Color-coded status indicators and logs

## Services Integration

The dashboard integrates with all three microservices:

- **User Service** (Port 3001) - Node.js/Express
- **Product Service** (Port 3002) - Python/Flask  
- **Order Service** (Port 3003) - Java/Spring Boot

## Usage

1. **Access the Dashboard**: Open http://localhost:8000 in your browser
2. **Monitor Services**: Check the status cards at the top
3. **View Data**: Click "Get Users", "Get Products", or "Get Orders" buttons
4. **Create Users**: Fill out the registration form
5. **Place Orders**: Select a user and product, then create an order
6. **Monitor Activity**: Watch the activity log for real-time updates

## API Responses

All API responses are displayed in the JSON viewer with:
- Formatted JSON with proper indentation
- Scrollable container for large responses
- Error handling and display

## Activity Logging

The dashboard logs all activities with timestamps:
- 🟢 Success operations (green)
- 🔴 Error operations (red)  
- 🔵 Info operations (blue)

## Technical Details

- **Frontend**: Vanilla JavaScript, HTML5, CSS3
- **Styling**: Modern CSS Grid and Flexbox
- **Icons**: Font Awesome 6
- **Server**: Python HTTP server (port 8000)
- **CORS**: Enabled on all microservices for cross-origin requests

## Browser Compatibility

- Chrome/Chromium (recommended)
- Firefox
- Safari
- Edge

## Troubleshooting

If services show as "Unhealthy":
1. Ensure all microservices are running
2. Check the activity log for specific error messages
3. Verify CORS is enabled on all services
4. Check network connectivity to localhost ports