FROM maven:3.9.3-eclipse-temurin-17

WORKDIR /app

# Copy pom and source code
COPY pom.xml .
COPY src ./src

# Copy the .proto directory so protobuf plugin can see the proto files
COPY .proto ./proto

# Run the build
RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/rating-service.jar"]

