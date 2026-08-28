FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

RUN cp build/libs/*.jar application.jar && \
    java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:21-jre-alpine
WORKDIR /application

RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

RUN addgroup -S app && adduser -S app -G app

COPY --from=builder --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=builder --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=app:app /workspace/extracted/application/ ./

USER app
EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=70.0", \
  "-XX:+UseSerialGC", \
  "-jar", "application.jar"]
