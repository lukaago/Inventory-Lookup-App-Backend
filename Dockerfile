FROM eclipse-temurin:24
WORKDIR /app
COPY ./target/Shelfy-V1-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]