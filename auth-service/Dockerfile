FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY settings.xml .
RUN mvn dependency:go-offline -s settings.xml -B

COPY src src
RUN mvn package -DskipTests -s settings.xml -B

FROM eclipse-temurin:21-jre
WORKDIR /app

# User creation removed – container runs as root

COPY --from=builder /app/target/*.jar app.jar

# USER appuser
EXPOSE 8091

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
