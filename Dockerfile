FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copie le JAR compilé par Maven
COPY target/demo-java-app-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]