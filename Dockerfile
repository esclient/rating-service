FROM maven:3.9.3-eclipse-temurin-17
WORKDIR /app

# Copy pom and source code
COPY pom.xml .
COPY src ./src

# Fix all incorrect imports in Java files
# Change from com.esclient.ratingservice.proto to rating package
RUN find src/main/java/com/esclient/ratingservice -name "*.java" -exec sed -i \
    -e 's/import com\.esclient\.ratingservice\.proto\.Rating\./import rating.Rating./g' \
    -e 's/import com\.esclient\.ratingservice\.proto\.RatingServiceGrpc/import rating.RatingServiceGrpc/g' \
    -e 's/com\.esclient\.ratingservice\.proto\.Rating\./rating.Rating./g' \
    -e 's/com\.esclient\.ratingservice\.proto\.RatingServiceGrpc/rating.RatingServiceGrpc/g' \
    {} \;

# Show what we fixed (for debugging)
RUN echo "=== Fixed imports ===" && \
    grep -r "import rating\." src/main/java/com/esclient/ratingservice/ || echo "No rating imports found" && \
    echo "=== End fixed imports ==="

# Build the project quietly
RUN mvn clean package -DskipTests -q

CMD ["java", "-jar", "target/rating-service-0.0.1-SNAPSHOT.jar"]