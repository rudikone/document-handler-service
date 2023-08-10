# document-handler-service

![Логотип проекта](doc/logo.jpg)

## Описание

document-handler-service - это приложение, которое предоставляет возможность сохранять документы,
такие как текстовые файлы, изображения, аудио и видео файлы в базу данных GridFs MongoDb.

## Локальный запуск и разработка

1. Запустить IntellijIdea runConfiguration "infrastructure" или командой docker-compose up
2. Запустите сервер
3. Сервер принимает запросы: `http://localhost:8080/dhs`

## Использование

1. Сохраните документ

## Инструменты

- Kotlin 1.7.22
- Spring Boot 3.0.5
- Spring Data MongoDb 3.0.6
- CycloneDX 1.7.4
- Detekt 1.22.0
- OpenApi 2.1.0

