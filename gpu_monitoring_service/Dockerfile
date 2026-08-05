FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY .mvn .mvn
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src src
RUN mvn package -DskipTests -B

FROM ubuntu:22.04
WORKDIR /app

# Install Java
RUN apt-get update && \
    apt-get install -y openjdk-21-jre && \
    rm -rf /var/lib/apt/lists/*

# --- GPU Tool Installation ---
# Add your GPU diagnostic tool installation commands here.
# This is highly dependent on the specific GPU and driver version.

# Example for NVIDIA:
# RUN apt-get update && apt-get install -y nvidia-utils-510

# Example for AMD (this is a placeholder, you will need to find the correct package):
# RUN apt-get update && apt-get install -y amd-gpu-tools

# Example for Intel (this is a placeholder):
# RUN apt-get update && apt-get install -y intel-gpu-tools

RUN groupadd --system appgroup \
    && useradd --system --gid appgroup --no-create-home appuser

COPY --from=builder /app/target/*.jar app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
