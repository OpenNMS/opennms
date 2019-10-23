##
# Use Java base image and setup required RPMS as cacheable image.
##
ARG BASE_IMAGE="opennms/openjdk"
ARG BASE_IMAGE_VERSION="11"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION} as meridian-sentinel-base

ARG REQUIRED_RPMS="wget gettext"

ARG REPO_KEY_URL="https://yum.opennms.org/OPENNMS-GPG-KEY"
ARG REPO_RPM="https://yum.opennms.org/repofiles/opennms-repo-stable-rhel7.noarch.rpm"

SHELL ["/bin/bash", "-c"]

# Collect generic steps in a layer for caching
RUN rpm --import "${REPO_KEY_URL}" && \
    yum install -y epel-release && \
    yum -y install "${REPO_RPM}" && \
    yum -y install ${REQUIRED_RPMS} && \
    yum clean all -y && \
    rm -rf /var/cache/yum

# Allow to send ICMP messages as non-root user
RUN setcap cap_net_raw+ep ${JAVA_HOME}/bin/java && \
    echo ${JAVA_HOME}/lib/jli > /etc/ld.so.conf.d/java-latest.conf && \
    ldconfig

##
# Install and setup OpenNMS Sentinel RPMS
##
FROM meridian-sentinel-base

# A default which installs the minimum required Sentinel packages
ARG SENTINEL_PACKAGES="meridian-sentinel"

# Allow to install optional packages via YUM
ARG ADD_YUM_PACKAGES

COPY ./rpms /tmp/rpms

SHELL ["/bin/bash", "-c"]

# Install repositories, system and OpenNMS packages and do some cleanup
RUN if [[ "$(ls -1 /tmp/rpms/*.rpm 2>/dev/null | wc -l)" != 0 ]]; then yum -y localinstall /tmp/rpms/*.rpm; else yum install -y ${SENTINEL_PACKAGES}; fi && \
    if [[ -n ${ADD_YUM_PACKAGES} ]]; then yum -y install ${ADD_YUM_PACKAGES}; fi && \
    rm -rf /tmp/rpms && \
    yum clean all && \
    rm -rf /var/cache/yum

COPY ./assets/entrypoint.sh /

VOLUME [ "/opt/sentinel/deploy", "/opt/sentinel/etc", "/opt/sentinel/data" ]

WORKDIR /opt/sentinel

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-f" ]

# Arguments for labels should not invalidate caches
ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG VERSION
ARG SOURCE
ARG REVISION
ARG BUILD_JOB_ID
ARG BUILD_NUMBER
ARG BUILD_URL
ARG BUILD_BRANCH

LABEL org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.title="OpenNMS Meridian Sentinel ${VERSION}" \
      org.opencontainers.image.source="${SOURCE}" \
      org.opencontainers.image.revision="${REVISION}" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.vendor="The OpenNMS Group, Inc." \
      org.opencontainers.image.authors="OpenNMS Community" \
      org.opencontainers.image.licenses="AGPL-3.0" \
      org.opennms.image.base="${BASE_IMAGE}:${BASE_IMAGE_VERSION}" \
      org.opennme.cicd.jobid="${BUILD_JOB_ID}" \
      org.opennms.cicd.buildnumber="${BUILD_NUMBER}" \
      org.opennms.cicd.buildurl="${BUILD_URL}" \
      org.opennms.cicd.branch="${BUILD_BRANCH}"

### Runtime information and not relevant at build time

# TODO MVR SENTINEL_LOCATION is not used at the moment
ENV SENTINEL_HOME="/opt/sentinel" \
    SENTINEL_ID="" \
    SENTINEL_LOCATION="SENTINEL" \
    OPENNMS_BROKER_URL="tcp://127.0.0.1:61616" \
    OPENNMS_HTTP_URL="http://127.0.0.1:8980/opennms" \
    OPENNMS_HTTP_USER="minion" \
    OPENNMS_HTTP_PASS="minion" \
    OPENNMS_BROKER_USER="minion" \
    OPENNMS_BROKER_PASS="minion" \
    POSTGRES_HOST="localhost" \
    POSTGRES_PORT="5432" \
    POSTGRES_USER="postgres" \
    POSTGRES_PASSWORD="" \
    POSTGRES_DB="opennms"

##------------------------------------------------------------------------------
## EXPOSED PORTS
##------------------------------------------------------------------------------
## -- Sentinel Karaf Debug 5005/TCP
## -- Sentinel KARAF SSH   8301/TCP

EXPOSE 8301
