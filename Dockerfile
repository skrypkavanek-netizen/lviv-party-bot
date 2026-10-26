# Етап 1: Збірка проєкту через Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Копіюємо pom.xml та папки з кодом у корінь контейнера
COPY pom.xml .
COPY src ./src

# Збираємо проєкт
RUN mvn clean package -DskipTests

# Етап 2: Запуск готового бота
FROM eclipse-temurin:17-jre
WORKDIR /app

# Копіюємо готовий jar із результатів збірки
COPY --from=build /app/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
