// Service URLs
const SERVICES = {
    user: 'http://localhost:3001',
    product: 'http://localhost:3002',
    order: 'http://localhost:3003'
};

// Global data storage
let users = [];
let products = [];
let orders = [];

// Initialize dashboard
document.addEventListener('DOMContentLoaded', function() {
    checkAllServices();
    loadInitialData();
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    document.getElementById('user-form').addEventListener('submit', handleUserRegistration);
    document.getElementById('order-form').addEventListener('submit', handleOrderCreation);
}

// Logging function
function addLog(message, type = 'info') {
    const logsContainer = document.getElementById('logs-container');
    const logEntry = document.createElement('div');
    logEntry.className = `log-entry ${type}`;
    logEntry.textContent = `${new Date().toLocaleTimeString()} - ${message}`;
    logsContainer.appendChild(logEntry);
    logsContainer.scrollTop = logsContainer.scrollHeight;
}

// Display response in the output area
function displayResponse(data, title = 'API Response') {
    const output = document.getElementById('response-output');
    output.textContent = JSON.stringify(data, null, 2);
    addLog(`${title} displayed`, 'success');
}

// Check health of all services
async function checkAllServices() {
    addLog('Checking all services health...');
    
    for (const [serviceName, url] of Object.entries(SERVICES)) {
        await checkHealth(serviceName);
    }
}

// Check individual service health
async function checkHealth(serviceName) {
    const statusElement = document.getElementById(`${serviceName}-status`);
    statusElement.textContent = 'Checking...';
    statusElement.className = 'status-badge status-checking';
    
    try {
        let healthUrl;
        if (serviceName === 'order') {
            healthUrl = `${SERVICES[serviceName]}/orders/health`;
        } else {
            healthUrl = `${SERVICES[serviceName]}/health`;
        }
        
        const response = await fetch(healthUrl);
        const data = await response.json();
        
        if (response.ok && data.status === 'healthy') {
            statusElement.textContent = 'Healthy';
            statusElement.className = 'status-badge status-healthy';
            addLog(`${serviceName} service is healthy`, 'success');
        } else {
            throw new Error('Service unhealthy');
        }
    } catch (error) {
        statusElement.textContent = 'Unhealthy';
        statusElement.className = 'status-badge status-unhealthy';
        addLog(`${serviceName} service is unhealthy: ${error.message}`, 'error');
    }
}

// Load initial data
async function loadInitialData() {
    await getUsers();
    await getProducts();
    await getOrders();
    populateDropdowns();
}

// Get users
async function getUsers() {
    try {
        addLog('Fetching users...');
        const response = await fetch(`${SERVICES.user}/users`);
        const data = await response.json();
        
        if (response.ok && data.success) {
            users = data.data;
            displayResponse(data, 'Users Data');
            addLog(`Fetched ${users.length} users`, 'success');
        } else {
            throw new Error(data.message || 'Failed to fetch users');
        }
    } catch (error) {
        addLog(`Error fetching users: ${error.message}`, 'error');
        displayResponse({ error: error.message }, 'Users Error');
    }
}

// Get products
async function getProducts() {
    try {
        addLog('Fetching products...');
        const response = await fetch(`${SERVICES.product}/products`);
        const data = await response.json();
        
        if (response.ok && data.success) {
            products = data.data;
            displayResponse(data, 'Products Data');
            addLog(`Fetched ${products.length} products`, 'success');
        } else {
            throw new Error(data.message || 'Failed to fetch products');
        }
    } catch (error) {
        addLog(`Error fetching products: ${error.message}`, 'error');
        displayResponse({ error: error.message }, 'Products Error');
    }
}

// Get orders
async function getOrders() {
    try {
        addLog('Fetching orders...');
        const response = await fetch(`${SERVICES.order}/orders`);
        const data = await response.json();
        
        if (response.ok && data.success) {
            orders = data.data;
            displayResponse(data, 'Orders Data');
            addLog(`Fetched ${orders.length} orders`, 'success');
        } else {
            throw new Error(data.message || 'Failed to fetch orders');
        }
    } catch (error) {
        addLog(`Error fetching orders: ${error.message}`, 'error');
        displayResponse({ error: error.message }, 'Orders Error');
    }
}

// Populate dropdowns
function populateDropdowns() {
    // Populate user dropdown
    const userSelect = document.getElementById('orderUserId');
    userSelect.innerHTML = '<option value="">Select User</option>';
    users.forEach(user => {
        const option = document.createElement('option');
        option.value = user.id;
        option.textContent = `${user.firstName} ${user.lastName} (${user.username})`;
        userSelect.appendChild(option);
    });
    
    // Populate product dropdown
    const productSelect = document.getElementById('orderProductId');
    productSelect.innerHTML = '<option value="">Select Product</option>';
    products.forEach(product => {
        const option = document.createElement('option');
        option.value = product.id;
        option.textContent = `${product.name} - $${product.price}`;
        productSelect.appendChild(option);
    });
}

// Handle user registration
async function handleUserRegistration(event) {
    event.preventDefault();
    
    const formData = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value
    };
    
    try {
        addLog('Registering new user...');
        const response = await fetch(`${SERVICES.user}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            displayResponse(data, 'User Registration Success');
            addLog(`User registered successfully: ${formData.username}`, 'success');
            document.getElementById('user-form').reset();
            
            // Refresh users list
            await getUsers();
            populateDropdowns();
        } else {
            throw new Error(data.message || 'Failed to register user');
        }
    } catch (error) {
        addLog(`Error registering user: ${error.message}`, 'error');
        displayResponse({ error: error.message }, 'User Registration Error');
    }
}

// Handle order creation
async function handleOrderCreation(event) {
    event.preventDefault();
    
    const orderData = {
        userId: document.getElementById('orderUserId').value,
        items: [{
            productId: document.getElementById('orderProductId').value,
            quantity: parseInt(document.getElementById('orderQuantity').value)
        }]
    };
    
    try {
        addLog('Creating new order...');
        const response = await fetch(`${SERVICES.order}/orders`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(orderData)
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            displayResponse(data, 'Order Creation Success');
            addLog(`Order created successfully: ${data.data.id}`, 'success');
            document.getElementById('order-form').reset();
            
            // Refresh orders list
            await getOrders();
        } else {
            throw new Error(data.message || 'Failed to create order');
        }
    } catch (error) {
        addLog(`Error creating order: ${error.message}`, 'error');
        displayResponse({ error: error.message }, 'Order Creation Error');
    }
}

// Get categories
async function getCategories() {
    try {
        addLog('Fetching categories...');
        const response = await fetch(`${SERVICES.product}/categories`);
        const data = await response.json();
        
        if (response.ok && data.success) {
            displayResponse(data, 'Categories Data');
            addLog(`Fetched ${data.data.length} categories`, 'success');
        } else {
            throw new Error(data.message || 'Failed to fetch categories');
        }
    } catch (error) {
        addLog(`Error fetching categories: ${error.message}`, 'error');
        displayResponse({ error: error.message }, 'Categories Error');
    }
}

// Check product stock
async function checkStock(productId) {
    try {
        addLog(`Checking stock for product ${productId}...`);
        const response = await fetch(`${SERVICES.product}/products/${productId}/stock`);
        const data = await response.json();
        
        if (response.ok && data.success) {
            displayResponse(data, 'Stock Check');
            addLog(`Stock check completed for product ${productId}`, 'success');
        } else {
            throw new Error(data.message || 'Failed to check stock');
        }
    } catch (error) {
        addLog(`Error checking stock: ${error.message}`, 'error');
        displayResponse({ error: error.message }, 'Stock Check Error');
    }
}

// Auto-refresh services status every 30 seconds
setInterval(checkAllServices, 30000);