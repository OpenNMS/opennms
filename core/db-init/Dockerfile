FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 10001 opennms && \
    adduser -u 10001 -G opennms -D opennms

COPY --chown=opennms:opennms target/db-init-*.jar /app/db-init.jar

USER opennms

# JAVA_TOOL_OPTIONS is read natively by the JVM — no shell wrapper needed.
# Override via env var in docker-compose: JAVA_TOOL_OPTIONS=-Xms256m -Xmx512m
ENTRYPOINT ["java", "-jar", "/app/db-init.jar"]
