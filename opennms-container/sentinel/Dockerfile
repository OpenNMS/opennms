##
# Use Java base image and setup required RPMS as cacheable image.
##
ARG BASE_IMAGE="opennms/openjdk"
ARG BASE_IMAGE_VERSION="11"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION} as sentinel-base

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

##
# Install and setup OpenNMS Sentinel RPMS
##
FROM sentinel-base

# A default which installs the minimum required Minion packages
ARG SENTINEL_PACKAGES="opennms-sentinel"

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

ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG VERSION
ARG BUILD_NUMBER
ARG BUILD_URL
ARG BUILD_BRANCH
ARG BUILD_SHA1

LABEL maintainer="The OpenNMS Group" \
      license="AGPLv3" \
      name="Sentinel" \
      version="${VERSION}" \
      vendor="OpenNMS Community" \
      cicd.build.date="${BUILD_DATE}" \
      cicd.build.number="${BUILD_NUMBER}" \
      cicd.build.url="${BUILD_URL}" \
      cicd.build.branch="${BUILD_BRANCH}" \
      cicd.build.sha1="${CIRCLE_SHA1}"

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
