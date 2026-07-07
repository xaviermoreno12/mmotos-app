FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/mmotos-app-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
