FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY discovery-server/src discovery-server/src

RUN mvn -B -ntp -f discovery-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget

WORKDIR /app
COPY --from=builder /workspace/discovery-server/target/discovery-server-*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java","-jar","/app/app.jar"]
