FROM eclipse-temurin:21-jre-alpine
COPY target/cart*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
# This line for test cart ci 999