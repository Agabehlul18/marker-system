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

# 4. Vacib qovluqları (abc, crops, data) konteynerə əlavə edirik
COPY abc abc
COPY crops crops
COPY data data

# 5. Dependenciesləri yükləyirik (cache üçün ayrıca layer)
RUN ./gradlew dependencies || true

# 6. Mənbə kodunu əlavə edirik
COPY src src

# 7. Spring Boot jar yaratmaq
RUN ./gradlew bootJar --no-daemon

# 8. Run image (lightweight)
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# 9. Jar faylını və lazım olan qovluqları build image-dan copy edirik
COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/abc abc
COPY --from=build /app/crops crops
COPY --from=build /app/data data

# 10. Portu təyin edirik
EXPOSE 8080

# 11. Start command
ENTRYPOINT ["java","-jar","app.jar"]