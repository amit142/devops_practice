FROM python:3.11-slim

# Set working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better caching
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY . .

# Create non-root user
RUN addgroup --system --gid 1001 python
RUN adduser --system --uid 1001 --gid 1001 python

# Change ownership of the app directory
RUN chown -R python:python /app
USER python

# Expose port
EXPOSE 3002

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:3002/health')"

# Start the application
CMD ["gunicorn", "--bind", "0.0.0.0:3002", "--workers", "2", "--timeout", "60", "app:app"]
