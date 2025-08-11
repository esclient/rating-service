FROM maven:3.9.3-eclipse-temurin-17
WORKDIR /app

# Copy pom and source code
COPY pom.xml .
COPY src ./src

# Remove any existing proto directory to avoid conflicts
RUN rm -rf src/main/java/com/esclient/ratingservice/proto

# Create the expected directory structure
RUN mkdir -p src/main/java/com/esclient/ratingservice/proto

# Copy and transform the rating files if they exist
RUN if [ -d "src/main/java/rating" ]; then \
        echo "Copying rating files..."; \
        find src/main/java/rating -name "*.java" -exec cp {} src/main/java/com/esclient/ratingservice/proto/ \; && \
        echo "Fixing package references..."; \
        find src/main/java/com/esclient/ratingservice/proto -name "*.java" -exec sed -i \
            -e 's/package rating;/package com.esclient.ratingservice.proto;/g' \
            -e 's/rating\.Rating/com.esclient.ratingservice.proto.Rating/g' \
            {} \; && \
        echo "Package references fixed"; \
        rm -rf src/main/java/rating; \
    else \
        echo "ERROR: rating directory not found!"; \
        exit 1; \
    fi

# Fix imports in existing Java files - handle nested classes correctly
# This fixes the fully qualified class names in method signatures
RUN find src/main/java/com/esclient/ratingservice -name "*.java" -exec sed -i \
        -e 's/com\.esclient\.ratingservice\.proto\.RateModRequest/com.esclient.ratingservice.proto.Rating.RateModRequest/g' \
        -e 's/com\.esclient\.ratingservice\.proto\.RateModResponse/com.esclient.ratingservice.proto.Rating.RateModResponse/g' \
        -e 's/import com\.esclient\.ratingservice\.proto\.RateModRequest;/import com.esclient.ratingservice.proto.Rating.RateModRequest;/g' \
        -e 's/import com\.esclient\.ratingservice\.proto\.RateModResponse;/import com.esclient.ratingservice.proto.Rating.RateModResponse;/g' \
        -e 's/import rating\.Rating\.RateModRequest;/import com.esclient.ratingservice.proto.Rating.RateModRequest;/g' \
        -e 's/import rating\.Rating\.RateModResponse;/import com.esclient.ratingservice.proto.Rating.RateModResponse;/g' \
        {} \;

# Run the build
RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/rating-service-0.0.1-SNAPSHOT.jar"]