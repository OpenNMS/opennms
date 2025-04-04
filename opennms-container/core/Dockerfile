## Set a fallback, so you can use just docker build without failing
ARG DEPLOY_BASE_IMAGE="quay.io/bluebird/deploy-base:2.0.0.b16"

FROM ${DEPLOY_BASE_IMAGE} AS core-tarball

ADD --chown=10001:0 ./tarball-root/ /opt/opennms/
RUN chmod -R g-w /opt/opennms && \
    chmod -R g=u \
        /opt/opennms/etc \
        /opt/opennms/data \
        /opt/opennms/jetty-webapps/*/WEB-INF \
        /opt/opennms/logs \
        /opt/opennms/share \
        && \
    chmod g=u /opt/opennms && \
    find \
        /opt/opennms/data \
        /opt/opennms/deploy \
        /opt/opennms/logs \
        /opt/opennms/system \
        -type d -print0 | xargs -0 chmod 2775

##
# Install and setup OpenNMS
##
FROM ${DEPLOY_BASE_IMAGE}

## Share the image variable between stages
ARG DEPLOY_BASE_IMAGE

SHELL ["/bin/bash", "-c"]

# Create OpenNMS user with a specific group ID
RUN groupadd \
        --gid 10001 \
        opennms && \
    useradd \
        --uid 10001 \
        --gid 10001 \
        --home /opt/opennms \
        --no-create-home \
        --shell /usr/bin/bash \
        opennms

COPY --chown=10001:0 --from=core-tarball /opt/opennms /opt/opennms

RUN install -d -m 775 -o 10001 -g 0 \
            /opt/opennms-etc-overlay \
            /opt/opennms-jetty-webinf-overlay \
            /opt/opennms-overlay \
            /opt/opennms/data/tmp \
            /opt/opennms/etc_archive \
            /opennms-data && \
    install -d -m 775 -o 10001 -g 0 \
            /opennms-data/rrd \
            /opennms-data/reports \
            /opennms-data/mibs && \
    ln -s /opennms-data/rrd /opt/opennms/share/rrd && \
    ln -s /opennms-data/reports /opt/opennms/share/reports && \
    ln -s /opennms-data/mibs /opt/opennms/share/mibs && \
    cp -a /opt/opennms/etc/ /opt/opennms/share/etc-pristine/

# Add templates replaced at runtime and entrypoint
COPY --chown=10001:0 ./container-fs/confd /etc/confd
COPY --chown=10001:0 ./container-fs/entrypoint.sh /
COPY --chown=10001:0 ./container-fs/health.sh /

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
      org.opencontainers.image.title="BluebirdOps ${VERSION}" \
      org.opencontainers.image.source="${SOURCE}" \
      org.opencontainers.image.revision="${REVISION}" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.vendor="Bluebird Community" \
      org.opencontainers.image.authors="Bluebird Community" \
      org.opencontainers.image.licenses="AGPL-3.0" \
      org.opennms.image.base="${DEPLOY_BASE_IMAGE}" \
      org.opennms.cicd.jobid="${BUILD_JOB_ID}" \
      org.opennms.cicd.buildnumber="${RELEASE_BUILD_NUM}" \
      org.opennms.cicd.buildurl="${BUILD_URL}" \
      org.opennms.cicd.branch="${RELEASE_BRANCH}"

WORKDIR /opt/opennms

### Containers should NOT run as root as a good practice
USER 10001

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-h" ]

### Runtime information and not relevant at build time
# Set conservative default memory usage options
ENV JAVA_OPTS="-Xmx2g -XX:MaxMetaspaceSize=1g"
# Allow OpenNMS to detect whether it is run in a container
ENV OPENNMS_EXECUTION_ENVIRONMENT=container

# Volumes for storing data outside of the container
# Suggestions how to use the following volumes:
#   * /opt/opennms/etc: Persistence for etc configuration files
#   * /opt/opennms-etc-overlay: Inject and override files relative to /opt/opennms/etc
#   * /opt/opennms-jetty-webinf-overlay: Inject and override files relative to /opt/opennms/jetty-webapps/opennms/WEB-INF
#   * /opt/opennms-overlay: Inject and override custom files relative to /opt/opennms
# Order (higher number takes precedence):
#   1. shipped files
#   2. confd
#   3. opt-overlay
#   4. etc-overlay
#   5. jetty-overlay
VOLUME /opennms-data \
       /opt/opennms/etc \
       /opt/opennms-overlay \
       /opt/opennms-etc-overlay \
       /opt/opennms-jetty-webinf-overlay

##------------------------------------------------------------------------------
## EXPOSED PORTS
##------------------------------------------------------------------------------
## -- OpenNMS HTTP        8980/TCP
## -- OpenNMS JMX        18980/TCP
## -- OpenNMS KARAF RMI   1099/TCP
## -- OpenNMS KARAF SSH   8101/TCP
## -- OpenNMS MQ         61616/TCP
## -- OpenNMS Eventd      5817/TCP
## -- SNMP Trapd          1162/UDP
## -- Syslog Receiver    10514/UDP
EXPOSE 8980 8101 \
       1162/udp \
       10514/udp
