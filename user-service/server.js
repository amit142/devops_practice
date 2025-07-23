const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { v4: uuidv4 } = require('uuid');

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// In-memory user storage
let users = [
  {
    id: '1',
    username: 'john_doe',
    email: 'john@example.com',
    firstName: 'John',
    lastName: 'Doe',
    createdAt: new Date().toISOString()
  },
  {
    id: '2',
    username: 'jane_smith',
    email: 'jane@example.com',
    firstName: 'Jane',
    lastName: 'Smith',
    createdAt: new Date().toISOString()
  }
];

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    service: 'user-service',
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

// Get all users
app.get('/users', (req, res) => {
  try {
    console.log('GET /users - Fetching all users');
    res.status(200).json({
      success: true,
      data: users,
      count: users.length
    });
  } catch (error) {
    console.error('Error fetching users:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

// Get user by ID
app.get('/users/:id', (req, res) => {
  try {
    const { id } = req.params;
    console.log(`GET /users/${id} - Fetching user by ID`);
    
    const user = users.find(u => u.id === id);
    
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }
    
    res.status(200).json({
      success: true,
      data: user
    });
  } catch (error) {
    console.error('Error fetching user:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

// Register new user
app.post('/register', (req, res) => {
  try {
    const { username, email, firstName, lastName } = req.body;
    console.log('POST /register - Registering new user:', { username, email });
    
    // Basic validation
    if (!username || !email || !firstName || !lastName) {
      return res.status(400).json({
        success: false,
        message: 'All fields are required: username, email, firstName, lastName'
      });
    }
    
    // Check if user already exists
    const existingUser = users.find(u => u.username === username || u.email === email);
    if (existingUser) {
      return res.status(409).json({
        success: false,
        message: 'User with this username or email already exists'
      });
    }
    
    // Create new user
    const newUser = {
      id: uuidv4(),
      username,
      email,
      firstName,
      lastName,
      createdAt: new Date().toISOString()
    };
    
    users.push(newUser);
    
    res.status(201).json({
      success: true,
      message: 'User registered successfully',
      data: newUser
    });
  } catch (error) {
    console.error('Error registering user:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

// Simple login endpoint (for demo purposes)
app.post('/auth/login', (req, res) => {
  try {
    const { username, email } = req.body;
    console.log('POST /auth/login - User login attempt:', { username, email });
    
    if (!username && !email) {
      return res.status(400).json({
        success: false,
        message: 'Username or email is required'
      });
    }
    
    const user = users.find(u => u.username === username || u.email === email);
    
    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Invalid credentials'
      });
    }
    
    // In a real app, you'd generate a JWT token here
    res.status(200).json({
      success: true,
      message: 'Login successful',
      data: {
        user: user,
        token: 'mock-jwt-token-' + user.id
      }
    });
  } catch (error) {
    console.error('Error during login:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Unhandled error:', err);
  res.status(500).json({
    success: false,
    message: 'Internal server error'
  });
});

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    success: false,
    message: 'Endpoint not found'
  });
});

// Start server
app.listen(PORT, () => {
  console.log(`User Service running on port ${PORT}`);
  console.log(`Health check: http://localhost:${PORT}/health`);
  console.log(`Available endpoints:`);
  console.log(`  GET    /users`);
  console.log(`  GET    /users/:id`);
  console.log(`  POST   /register`);
  console.log(`  POST   /auth/login`);
});

module.exports = app;
