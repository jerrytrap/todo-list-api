# 1단계: 빌드
FROM eclipse-temurin:17-jdk as builder
WORKDIR /app

# 빌드 설정 파일 복사
COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew build --no-daemon

# 2단계: 실행
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]