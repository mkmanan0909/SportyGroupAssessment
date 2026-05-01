# Multi-stage: build reproducible Fat JAR, run on JRE 11 (matches pom).
FROM maven:3.9.9-eclipse-temurin-11-alpine AS build
WORKDIR /src
COPY pom.xml .
COPY src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
