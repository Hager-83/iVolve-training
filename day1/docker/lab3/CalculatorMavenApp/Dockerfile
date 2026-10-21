
# Use the official Maven image as the base image
FROM maven:3.9-eclipse-temurin-17

# Set the working directory inside the container
WORKDIR /app

# Create a non-root user and prepare Maven's configuration directory
RUN useradd --create-home appuser \
    && mkdir -p /home/appuser/.m2 \
    && chown -R appuser:appuser /home/appuser

# Copy the Maven project files into the container
COPY pom.xml .
COPY src ./src

# Build the Maven project
RUN mvn package

# Run the application as the non-root user
USER appuser

# Set the home directory and Maven configuration directory
ENV HOME=/home/appuser
ENV MAVEN_CONFIG=/home/appuser/.m2

# Expose the port that the application will use
EXPOSE 8080

# Start the application
CMD ["java", "-jar", "target/calculator.jar"]
