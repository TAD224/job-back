# Étape 1 : Build de l'application avec Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Étape 2 : Exécution de l'application avec Java 21
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/plateforme_emploi-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]