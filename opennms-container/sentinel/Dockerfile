## Set a fallback, so you can use just docker build without failing
ARG DEPLOY_BASE_IMAGE="quay.io/bluebird/deploy-base:2.0.3.b25"

FROM ${DEPLOY_BASE_IMAGE} AS sentinel-tarball

ADD --chown=10001:0 ./tarball-root/ /opt/sentinel/

# Install confd.io configuration files and scripts and ensure they are executable
COPY ./container-fs/confd/ /opt/sentinel/confd/
RUN chmod +x /opt/sentinel/confd/scripts/*

RUN chmod -R g-w /opt/sentinel && \
    chmod -R g=u \
        /opt/sentinel/etc \
        /opt/sentinel/data \
        && \
    chmod -R a+x /opt/sentinel/bin && \
    chmod g=u /opt/sentinel && \
    find \
        /opt/sentinel/data \
        /opt/sentinel/deploy \
        /opt/sentinel/system \
        -type d -print0 | xargs -0 chmod 2775

##
# Install Sentinel
##
FROM ${DEPLOY_BASE_IMAGE}

ARG DEPLOY_BASE_IMAGE

COPY --chown=10001:0 --from=sentinel-tarball /opt/sentinel /opt/sentinel

SHELL ["/bin/bash", "-c"]

# Create Sentinel user with a specific group ID
RUN groupadd \
        --gid 10001 \
        sentinel && \
    useradd \
        --system \
        --uid 10001 \
        --gid 10001 \
        --home-dir /opt/sentinel \
        --no-create-home \
        --shell /usr/bin/bash \
        sentinel

# Install entrypoint wrapper and health check script
COPY container-fs/entrypoint.sh /
COPY container-fs/health.sh /

VOLUME [ "/opt/sentinel/deploy", "/opt/sentinel/etc", "/opt/sentinel/data" ]

WORKDIR /opt/sentinel

### Containers should NOT run as root as a good practice
USER 10001

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-f" ]

# Arguments for labels should not invalidate caches
ARG BUILD_DATE
ARG VERSION
ARG SOURCE
ARG REVISION
ARG BUILD_JOB_ID
ARG RELEASE_BUILD_NUM
ARG BUILD_URL
ARG RELEASE_BRANCH

LABEL org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.title="BluebirdOps Sentinel ${VERSION}" \
      org.opencontainers.image.source="${SOURCE}" \
      org.opencontainers.image.revision="${REVISION}" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.vendor="Bluebird Community" \
      org.opencontainers.image.authors="Bluebird Community" \
      org.opencontainers.image.licenses="AGPL-3.0" \
      org.opennms.image.base="${DEPLOY_BASE_IMAGE}" \
      org.opennms.cicd.jobid="${BUILD_JOB_ID}" \
      org.opennms.cicd.buildurl="${BUILD_URL}" \
      org.opennms.cicd.branch="${RELEASE_BRANCH}"

### Runtime information and not relevant at build time

ENV SENTINEL_HOME="/opt/sentinel" \
    SENTINEL_ID="" \
    SENTINEL_LOCATION="SENTINEL" \
    OPENNMS_BROKER_URL="tcp://127.0.0.1:61616" \
    OPENNMS_HTTP_USER="minion" \
    OPENNMS_BROKER_USER="minion" \
    POSTGRES_HOST="localhost" \
    POSTGRES_PORT="5432" \
    POSTGRES_USER="postgres" \
    POSTGRES_DB="opennms"

##------------------------------------------------------------------------------
## EXPOSED PORTS
##------------------------------------------------------------------------------
## -- Sentinel Karaf Debug 5005/TCP
## -- Sentinel KARAF SSH   8301/TCP

EXPOSE 8301
