FROM maven:3.9.3-eclipse-temurin-17
WORKDIR /app

# Copy pom and source code
COPY pom.xml .
COPY src ./src


# Build the project quietly
RUN mvn clean package -DskipTests -q

CMD ["java", "-jar", "target/rating-service-0.0.1-SNAPSHOT.jar"]