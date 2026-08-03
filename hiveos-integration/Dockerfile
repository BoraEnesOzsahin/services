# -----------------------------------------------------------------------
# Single stage: Runtime only.
# The JAR is built on the host machine (mvnw package -DskipTests)
# and copied directly into the image. This avoids SSL/network issues
# that can occur when building inside Docker on Windows.
# -----------------------------------------------------------------------
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system appgroup \
    && useradd --system --gid appgroup --no-create-home appuser

COPY target/*.jar app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
