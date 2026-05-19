FROM maven:3.8.1-openjdk-21 as builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:21-slim

WORKDIR /app

COPY --from=builder /app/target/urban-intelligence-platform-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
