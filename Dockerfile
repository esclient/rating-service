FROM maven:3.9.3-eclipse-temurin-17

WORKDIR /app

# Copy pom and source code
COPY pom.xml .
COPY src ./src




# Run the build
RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/rating-service.jar"]

