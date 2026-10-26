FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# Знаходимо папку, де лежить pom.xml, переходимо туди і запускаємо збірку
RUN cd $(dirname $(find . -name "pom.xml" | head -n 1)) && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
# Знаходимо та копіюємо готовий jar-файл з будь-якої підпапки target
COPY --from=build /app/**/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
