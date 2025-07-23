FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Create non-root user
RUN addgroup --system --gid 1001 spring
RUN adduser --system --uid 1001 --gid 1001 spring

# Change ownership of the app directory
RUN chown -R spring:spring /app
USER spring

# Expose port
EXPOSE 3003

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:3003/orders/health || exit 1

# Start the application
CMD ["java", "-jar", "target/order-service-0.0.1-SNAPSHOT.jar"]