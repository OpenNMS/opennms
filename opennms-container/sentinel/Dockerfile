ARG BASE_IMAGE="opennms/openjdk"
ARG BASE_IMAGE_VERSION="latest"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION}

ARG VERSION=${BASE_IMAGE_VERSION}
ARG BUILD_DATE="1970-01-01T00:00:00+0000"

ARG REPO_RELEASE="stable"
ARG REPO_HOST="yum.opennms.org"

ARG REPO_KEY_URL="https://${REPO_HOST}/OPENNMS-GPG-KEY"

ARG REPO_RPM="https://${REPO_HOST}/repofiles/opennms-repo-${REPO_RELEASE}-rhel7.noarch.rpm"

ARG PACKAGES="wget gettext"

ARG SENTINEL_PACKAGES="opennms-sentinel"

COPY ./rpms /tmp/rpms

SHELL ["/bin/bash", "-c"]

# Install repositories, system and OpenNMS packages and do some cleanup
RUN yum -y install epel-release && \
    rpm --import "${REPO_KEY_URL}" && \
    yum -y install ${REPO_RPM} && \
    yum -y install ${PACKAGES} && \
    if [ "$(ls -1 /tmp/rpms/*.rpm 2>/dev/null | wc -l)" != 0 ]; then yum -y localinstall /tmp/rpms/*.rpm; else yum -y install ${SENTINEL_PACKAGES}; fi && \
    rm -rf /tmp/rpms && \
    yum -y install ${PACKAGES} && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    chown -R ${USER}:${GROUP} /opt/sentinel

USER ${USER}

COPY ./assets/entrypoint.sh /

VOLUME [ "/opt/sentinel/deploy", "/opt/sentinel/etc", "/opt/sentinel/data" ]

LABEL maintainer="The OpenNMS Group" \
      license="AGPLv3" \
      name="Sentinel" \
      version="${VERSION}" \
      build.date="${BUILD_DATE}" \
      vendor="OpenNMS Community" \
      org.opennms.repo.release="${REPO_RELEASE}"

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-h" ]

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
