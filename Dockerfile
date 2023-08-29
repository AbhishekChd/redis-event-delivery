FROM eclipse-temurin:17-jdk-focal
WORKDIR /app

# Copy all the source files
COPY ./gradle ./gradle
COPY gradlew build.gradle ./
COPY src ./src

# Application endpoint
EXPOSE 8080

ENTRYPOINT ["./gradlew", "bootRun", "--no-daemon"]