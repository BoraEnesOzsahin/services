# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jdk-jammy

# Set the working directory in the container
WORKDIR /app

# Copy the executable JAR from the host to the container
COPY target/UserAuthenticationService-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8083

# Command to run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
