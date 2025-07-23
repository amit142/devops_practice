FROM python:3.10-alpine

# Set working directory
WORKDIR /app

# Copy web files
COPY . .

# Create non-root user
RUN addgroup -g 1001 -S webuser && \
    adduser -S webuser -u 1001 -G webuser

# Change ownership
RUN chown -R webuser:webuser /app
USER webuser

# Expose port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD python3 -c "import urllib.request; urllib.request.urlopen('http://localhost:8000')" || exit 1

# Start the web server
CMD ["python3", "-m", "http.server", "8000"]