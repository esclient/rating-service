FROM maven:3.9.4-eclipse-temurin-21

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -q

CMD ["java", "-jar", "target/rating-service-0.0.1-SNAPSHOT.jar"]
