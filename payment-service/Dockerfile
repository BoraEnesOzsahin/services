FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

