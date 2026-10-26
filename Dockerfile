FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Копіюємо все, що є в репозиторії
COPY . .

# Шукаємо де саме лежить pom.xml і збираємо проект
RUN find . -name "pom.xml" -execdir mvn clean package -DskipTests \;

FROM eclipse-temurin:17-jre
WORKDIR /app

# Знаходимо зібраний .jar файл у будь-якій папці target та копіюємо його
COPY --from=build /app/**/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
