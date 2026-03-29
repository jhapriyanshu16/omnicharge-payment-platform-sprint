FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY recharge-service/pom.xml recharge-service/pom.xml
COPY recharge-service/src recharge-service/src

RUN mvn -B -ntp -f recharge-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget

WORKDIR /app
COPY --from=builder /workspace/recharge-service/target/recharge-service-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","/app/app.jar"]
