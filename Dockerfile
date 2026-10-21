FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN sed -i 's/\r$//' gradlew
COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=secret,id=build_ca \
    if [ -f /run/secrets/build_ca ]; then \
        cp "$JAVA_HOME/lib/security/cacerts" /tmp/build-cacerts && \
        keytool -importcert -noprompt -alias build-ca -file /run/secrets/build_ca \
            -keystore /tmp/build-cacerts -storepass changeit && \
        export JAVA_TOOL_OPTIONS="-Djavax.net.ssl.trustStore=/tmp/build-cacerts"; \
    fi && \
    sh ./gradlew --no-daemon test bootJar && \
    find build/libs -name '*.jar' ! -name '*-plain.jar' -exec cp {} /app/app.jar \;

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd --gid 10001 app && useradd --uid 10001 --gid app --no-create-home app
WORKDIR /app
COPY --from=build --chown=app:app /app/app.jar ./app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]