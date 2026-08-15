FROM eclipse-temurin:21-jdk AS dev

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends inotify-tools \
    && rm -rf /var/lib/apt/lists/*

COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x ./mvnw \
    && ./mvnw -q -DskipTests dependency:go-offline

COPY src src

EXPOSE 8080

CMD ["bash", "-lc", "while true; do ./mvnw spring-boot:run & app_pid=$!; inotifywait -r -e modify,create,delete,move src pom.xml & watch_pid=$!; wait -n $app_pid $watch_pid; kill $app_pid $watch_pid 2>/dev/null || true; wait $app_pid $watch_pid 2>/dev/null || true; sleep 1; done"]
