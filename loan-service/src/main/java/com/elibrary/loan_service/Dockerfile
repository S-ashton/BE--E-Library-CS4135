FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

EXPOSE 8083

CMD ["java", "-jar", "target/loan-service-0.0.1-SNAPSHOT.jar"]