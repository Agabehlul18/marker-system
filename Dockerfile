# 1. Base image olaraq Java 21 istifadə olunur
FROM eclipse-temurin:21-jdk-alpine AS build

# 2. İş qovluğunu təyin edirik
WORKDIR /app

# 3. Gradle wrapper və build.gradle fayllarını əlavə edirik
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x gradlew

# 4. Dependenciesləri yükləyirik (cache üçün ayrıca layer)
RUN ./gradlew dependencies || true

# 5. Mənbə kodunu əlavə edirik
COPY src src

# 6. Spring Boot jar yaratmaq
RUN ./gradlew bootJar --no-daemon

# 7. Run image (lightweight)
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# 8. Jar faylıni build image-dan copy edirik
COPY --from=build /app/build/libs/*.jar app.jar

# 9. Portu təyin edirik
EXPOSE 8080

# 10. Start command
ENTRYPOINT ["java","-jar","app.jar"]
