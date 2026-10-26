FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
# Копіюємо jar із папки target (Maven за замовчуванням кладе його туди)
COPY --from=build /app/target/LvivPartyBot-1.0-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]
