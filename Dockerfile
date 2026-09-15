FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring

COPY --from=build /app/target/blog-0.0.1-SNAPSHOT.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]