FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY payment-service/src payment-service/src

RUN mvn -B -ntp -f payment-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget

WORKDIR /app
COPY --from=builder /workspace/payment-service/target/payment-service-*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java","-jar","/app/app.jar"]
