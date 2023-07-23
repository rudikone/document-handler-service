# Используйте базовый образ с установленной JDK и Gradle
FROM gradle:7.3.3-jdk11 AS builder

# Установите рабочую директорию внутри контейнера
WORKDIR /app

# Скопируйте файлы сборки в контейнер
COPY . .

# Выполните сборку JAR-приложения
RUN gradle clean build --no-daemon

# Используйте базовый образ с установленной JRE
FROM openjdk:11

# Установите рабочую директорию внутри контейнера
WORKDIR /app

# Скопируйте JAR-файл из предыдущего этапа сборки
COPY --from=builder /app/build/libs/DocumentHandlerServiceApplication.jar .

# Запустите приложение при запуске контейнера
CMD ["java", "-jar", "DocumentHandlerServiceApplication.jar"]