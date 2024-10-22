
#FROM openjdk:17-jdk-slim
#
#WORKDIR /app
#
#COPY target/mc-account-0.0.1-SNAPSHOT.jar app.jar
#
#ENTRYPOINT ["java", "-jar", "app.jar"]


# Используем образ Maven для сборки
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Теперь используем образ OpenJDK для запуска
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/mc-account-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
