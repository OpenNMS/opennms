## Set a fallback, so you can use just docker build without failing
ARG DEPLOY_BASE_IMAGE="quay.io/bluebird/deploy-base:2.0.3.b25"

FROM ${DEPLOY_BASE_IMAGE} AS minion-tarball

ADD --chown=10001:0 ./tarball-root/ /opt/minion/

RUN rm /opt/minion/etc/org.opennms.features.telemetry.listeners-single-port-flows.cfg

# Install confd.io configuration files and scripts and ensure they are executable
COPY ./container-fs/confd/ /opt/minion/confd/
COPY ./minion-config-schema.yml /opt/minion/confd/
RUN chmod +x /opt/minion/confd/scripts/*

# Create the directory for server certificates
RUN install -d -m 750 /opt/minion/server-certs

RUN chmod -R g-w /opt/minion && \
    chmod -R g=u \
        /opt/minion/etc \
        /opt/minion/data \
        && \
    chmod -R a+x /opt/minion/bin && \
    chmod g=u /opt/minion && \
    find \
        /opt/minion/data \
        /opt/minion/deploy \
        /opt/minion/system \
        /opt/minion/repositories \
        -type d -print0 | xargs -0 chmod 2775

##
# Prod image with minimal image size
##
FROM ${DEPLOY_BASE_IMAGE}

## Share the image variable between stages
ARG DEPLOY_BASE_IMAGE

RUN groupadd \
        --gid 10001 \
        minion && \
    useradd \
        --system \
        --uid 10001 \
        --gid 10001 \
        --home-dir /opt/minion \
        --no-create-home \
        --shell /usr/bin/bash \
        minion

# Install entrypoint wrapper and health check script
COPY container-fs/entrypoint.sh /
COPY container-fs/health.sh /

# Install a default JMX Prometheus configuration
COPY container-fs/prom-jmx-default-config.yaml /opt/prom-jmx-exporter/config.yaml

# If you copy from /opt/minion to /opt/minion the permissions are not preserved
# We would have 755 for minion:root instead of 775 and prevents writing lock files in /opt/minion
COPY --chown=10001:0 --from=minion-tarball /opt /opt

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
      org.opencontainers.image.title="BluebirdOps Minion ${VERSION}" \
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

WORKDIR /opt/minion

USER 10001

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-f" ]

### Runtime information and not relevant at build time
ENV MINION_ID="00000000-0000-0000-0000-deadbeef0001" \
    MINION_LOCATION="MINION" \
    OPENNMS_BROKER_URL="tcp://127.0.0.1:61616" \
    OPENNMS_HTTP_USER="minion" \
    OPENNMS_HTTP_PASS="minion" \
    OPENNMS_BROKER_USER="minion" \
    OPENNMS_BROKER_PASS="minion"

##------------------------------------------------------------------------------
## EXPOSED PORTS
##------------------------------------------------------------------------------
## -- OpenNMS KARAF SSH    8201/TCP
## -- SNMP Trapd           1162/UDP
## -- Syslog               1514/UDP
EXPOSE 8201/tcp \
       1162/udp \
       1514/udp
