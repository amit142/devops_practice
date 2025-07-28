# Use nginx for better performance and smaller image
FROM nginx:alpine

# Copy web files to nginx html directory
COPY index.html script.js style.css /usr/share/nginx/html/

# Create custom nginx config for SPA
RUN echo 'server { \
    listen 8000; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
    location /health { \
        access_log off; \
        return 200 "healthy\n"; \
        add_header Content-Type text/plain; \
    } \
}' > /etc/nginx/conf.d/default.conf

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf.bak 2>/dev/null || true

EXPOSE 8000

# Start nginx
CMD ["nginx", "-g", "daemon off;"]