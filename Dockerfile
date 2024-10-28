FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/mc-account-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]



