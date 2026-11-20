# 1단계: 빌드 (레포의 gradlew 사용, 테스트는 PR 에서 이미 돌렸으니 건너뜀)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

# 2단계: 실행 (jar 만 담아 가볍게)
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=Asia/Seoul
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
# ponytail: 1GB 서버라 힙을 512MB 로 묶어 둔다. 서버를 키우면 올린다
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
