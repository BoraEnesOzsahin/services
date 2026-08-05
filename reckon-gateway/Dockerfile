FROM maven:3.9.6-eclipse-temurin-21-jammy
ARG JAR_FILE=target/reckon-gateway-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app.jar"]
