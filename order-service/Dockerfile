# Build stage - use lighter maven image
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# Runtime stage - use distroless for minimal attack surface
FROM gcr.io/distroless/java17-debian11:nonroot

WORKDIR /app

# Copy only the JAR file
COPY --from=builder /app/target/order-service-0.0.1-SNAPSHOT.jar ./app.jar

# Expose port
EXPOSE 3003

# Start with optimized JVM settings for containers
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70.0", "-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-jar", "app.jar"]