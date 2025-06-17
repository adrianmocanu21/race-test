# Use a lightweight JDK base image
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR into the container
COPY target/race-test-0.0.1-SNAPSHOT.jar race-test.jar

# Run the JAR
ENTRYPOINT ["java", "-jar", "race-test.jar"]
