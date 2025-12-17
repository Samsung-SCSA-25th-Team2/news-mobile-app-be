# 1. Runtime 이미지 (권장)
FROM eclipse-temurin:17-jre

# 2. 작업 디렉토리
WORKDIR /app

# 3. JAR 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 4. Cloud Run / Docker 공통 포트
EXPOSE 8080

# 5. 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
