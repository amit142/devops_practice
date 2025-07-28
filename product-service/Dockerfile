# Use slim instead of alpine for faster builds
FROM python:3.11-slim

WORKDIR /app

# Copy requirements and install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY app.py .

# Create non-root user
RUN groupadd -r python && useradd -r -g python python && \
    chown -R python:python /app

USER python

EXPOSE 3002

# Start with single worker for lighter resource usage
CMD ["gunicorn", "--bind", "0.0.0.0:3002", "--workers", "1", "--threads", "2", "--timeout", "60", "app:app"]
