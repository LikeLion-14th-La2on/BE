# 1단계: 빌드 (레포의 gradlew 사용, 테스트는 PR 에서 이미 돌렸으니 건너뜀)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
# 의존성 먼저 받아 두면 코드만 바뀔 때 이 층을 다시 쓰므로 재빌드가 빠르다
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
# Windows 에서 받은 파일이면 줄바꿈(CRLF) 때문에 gradlew 가 안 돌아서 정리한다
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# 2단계: 실행 (jar 만 담아 가볍게)
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=Asia/Seoul
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
# ponytail: 1GB 서버(MySQL 과 같이 씀)라 힙·메타스페이스를 묶고 GC 도 가벼운 것으로. 서버를 키우면 올린다
ENTRYPOINT ["java", "-Xmx320m", "-XX:MaxMetaspaceSize=128m", "-XX:+UseSerialGC", "-jar", "app.jar"]
