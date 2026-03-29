FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY operator-service/pom.xml operator-service/pom.xml
COPY operator-service/src operator-service/src

RUN mvn -B -ntp -f operator-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget

WORKDIR /app
COPY --from=builder /workspace/operator-service/target/operator-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","/app/app.jar"]
