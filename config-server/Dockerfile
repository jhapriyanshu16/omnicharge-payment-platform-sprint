FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY config-server/src config-server/src

RUN mvn -B -ntp -f config-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget

WORKDIR /app
COPY --from=builder /workspace/config-server/target/config-server-*.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java","-jar","/app/app.jar"]
