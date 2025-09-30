FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN addgroup --system app && adduser --system --ingroup app app

COPY --from=build /app/target/rating-service-0.0.1-SNAPSHOT.jar /app/app.jar

RUN mkdir -p /app/logs && chown -R app:app /app

USER app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
