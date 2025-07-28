from flask import Flask, jsonify, request
from flask_cors import CORS
import os
import time
from datetime import datetime

app = Flask(__name__)
CORS(app)

# Initialize start time
start_time = time.time()

# Mock product data
products = [
    {
        "id": "1",
        "name": "Laptop Pro",
        "description": "High-performance laptop for professionals",
        "price": 1299.99,
        "category": "electronics",
        "stock": 50,
        "createdAt": "2024-01-15T10:00:00Z"
    },
    {
        "id": "2",
        "name": "Wireless Headphones",
        "description": "Premium noise-cancelling headphones",
        "price": 299.99,
        "category": "electronics",
        "stock": 100,
        "createdAt": "2024-01-16T11:30:00Z"
    },
    {
        "id": "3",
        "name": "Python Programming Book",
        "description": "Complete guide to Python programming",
        "price": 49.99,
        "category": "books",
        "stock": 25,
        "createdAt": "2024-01-17T09:15:00Z"
    },
    {
        "id": "4",
        "name": "Cotton T-Shirt",
        "description": "Comfortable 100% cotton t-shirt",
        "price": 19.99,
        "category": "clothing",
        "stock": 200,
        "createdAt": "2024-01-18T14:20:00Z"
    },
    {
        "id": "5",
        "name": "Smart Watch",
        "description": "Feature-rich smartwatch with health tracking",
        "price": 399.99,
        "category": "electronics",
        "stock": 75,
        "createdAt": "2024-01-19T16:45:00Z"
    }
]

categories = [
    {
        "id": "electronics",
        "name": "Electronics",
        "description": "Electronic devices and gadgets"
    },
    {
        "id": "books",
        "name": "Books",
        "description": "Books and educational materials"
    },
    {
        "id": "clothing",
        "name": "Clothing",
        "description": "Apparel and fashion items"
    }
]

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'service': 'product-service',
        'timestamp': datetime.utcnow().isoformat() + 'Z',
        'uptime': time.time() - start_time
    }), 200

@app.route('/products', methods=['GET'])
def get_products():
    """Get all products with optional category filter"""
    try:
        category = request.args.get('category')
        
        print(f"GET /products - Fetching products (category: {category})")
        
        filtered_products = products
        if category:
            filtered_products = [p for p in products if p['category'].lower() == category.lower()]
        
        return jsonify({
            'success': True,
            'data': filtered_products,
            'count': len(filtered_products),
            'category': category
        }), 200
        
    except Exception as e:
        print(f"Error fetching products: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Internal server error'
        }), 500

@app.route('/products/<product_id>', methods=['GET'])
def get_product(product_id):
    """Get product by ID"""
    try:
        print(f"GET /products/{product_id} - Fetching product by ID")
        
        product = next((p for p in products if p['id'] == product_id), None)
        
        if not product:
            return jsonify({
                'success': False,
                'message': 'Product not found'
            }), 404
        
        return jsonify({
            'success': True,
            'data': product
        }), 200
        
    except Exception as e:
        print(f"Error fetching product: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Internal server error'
        }), 500

@app.route('/categories', methods=['GET'])
def get_categories():
    """Get all product categories"""
    try:
        print("GET /categories - Fetching all categories")
        
        return jsonify({
            'success': True,
            'data': categories,
            'count': len(categories)
        }), 200
        
    except Exception as e:
        print(f"Error fetching categories: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Internal server error'
        }), 500

@app.route('/products/<product_id>/stock', methods=['GET'])
def check_stock(product_id):
    """Check product stock availability"""
    try:
        print(f"GET /products/{product_id}/stock - Checking stock")
        
        product = next((p for p in products if p['id'] == product_id), None)
        
        if not product:
            return jsonify({
                'success': False,
                'message': 'Product not found'
            }), 404
        
        return jsonify({
            'success': True,
            'data': {
                'productId': product_id,
                'stock': product['stock'],
                'available': product['stock'] > 0
            }
        }), 200
        
    except Exception as e:
        print(f"Error checking stock: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Internal server error'
        }), 500

@app.route('/products/<product_id>/reserve', methods=['POST'])
def reserve_stock(product_id):
    """Reserve product stock (for order processing)"""
    try:
        data = request.get_json()
        quantity = data.get('quantity', 1)
        
        print(f"POST /products/{product_id}/reserve - Reserving {quantity} items")
        
        product = next((p for p in products if p['id'] == product_id), None)
        
        if not product:
            return jsonify({
                'success': False,
                'message': 'Product not found'
            }), 404
        
        if product['stock'] < quantity:
            return jsonify({
                'success': False,
                'message': 'Insufficient stock',
                'available': product['stock'],
                'requested': quantity
            }), 400
        
        # Reserve stock (in a real app, this would be more sophisticated)
        product['stock'] -= quantity
        
        return jsonify({
            'success': True,
            'message': 'Stock reserved successfully',
            'data': {
                'productId': product_id,
                'reserved': quantity,
                'remainingStock': product['stock']
            }
        }), 200
        
    except Exception as e:
        print(f"Error reserving stock: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Internal server error'
        }), 500

@app.errorhandler(404)
def not_found(error):
    return jsonify({
        'success': False,
        'message': 'Endpoint not found'
    }), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({
        'success': False,
        'message': 'Internal server error'
    }), 500

if __name__ == '__main__':
    start_time = time.time()
    port = int(os.environ.get('PORT', 3002))
    
    print(f"Product Service starting on port {port}")
    print(f"Health check: http://localhost:{port}/health")
    print("Available endpoints:")
    print("  GET    /products")
    print("  GET    /products/<id>")
    print("  GET    /categories")
    print("  GET    /products/<id>/stock")
    print("  POST   /products/<id>/reserve")
    
    app.run(host='0.0.0.0', port=port, debug=False)
