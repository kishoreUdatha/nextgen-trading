# services/poison-replayer/Dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
# expects the jar to be built on the host first (target/…jar exists)
COPY target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/app/app.jar"]
