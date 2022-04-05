FROM openjdk:16-alpine3.13

WORKDIR /app
COPY target/mrc-0.0.1-SNAPSHOT.jar ./

EXPOSE 8080

CMD ["java", "-jar", "mrc-0.0.1-SNAPSHOT.jar"]
