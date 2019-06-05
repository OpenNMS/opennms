##
# Use Java base image and setup required RPMS as cacheable image.
##
ARG BASE_IMAGE="opennms/openjdk"
ARG BASE_IMAGE_VERSION="11"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION} as minion-base

ARG REQUIRED_RPMS="wget gettext jicmp jicmp6"

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
# Install and setup OpenNMS Minion RPMS
##
FROM minion-base

ARG USER="minion"

# A default which installs the minimum required Minion packages
ARG MINION_PACKAGES="opennms-minion opennms-minion-container opennms-minion-features-core opennms-minion-features-default"

# Allow to install optional packages via YUM
ARG ADD_YUM_PACKAGES

COPY ./rpms /tmp/rpms

SHELL ["/bin/bash", "-c"]

# Install repositories, system and OpenNMS packages and do some cleanup
RUN if [[ "$(ls -1 /tmp/rpms/*.rpm 2>/dev/null | wc -l)" != 0 ]]; then yum -y localinstall /tmp/rpms/*.rpm; else yum install -y ${MINION_PACKAGES}; fi && \
    if [[ -n ${ADD_YUM_PACKAGES} ]]; then yum -y install ${ADD_YUM_PACKAGES}; fi && \
    rm -rf /tmp/rpms && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    sed -r -i '/RUNAS/s/.*/export RUNAS=${USER}/' /etc/sysconfig/minion && \
    chgrp -R 0 /opt/minion && \
    chmod -R g=u /opt/minion

COPY ./assets/entrypoint.sh /

ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG VERSION=${BASE_IMAGE_VERSION}
ARG BUILD_NUMBER
ARG BUILD_URL
ARG BUILD_BRANCH
ARG BUILD_SHA1

LABEL maintainer="The OpenNMS Group" \
      license="AGPLv3" \
      name="Minion" \
      version="${VERSION}" \
      vendor="OpenNMS Community" \
      cicd.build.date="${BUILD_DATE}" \
      cicd.build.number="${BUILD_NUMBER}" \
      cicd.build.url="${BUILD_URL}" \
      cicd.build.branch="${BUILD_BRANCH}" \
      cicd.build.sha1="${CIRCLE_SHA1}"

WORKDIR /opt/minion

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-f" ]

### Containers should NOT run as root as a good practice
USER 10001

### Runtime information and not relevant at build time
ENV MINION_ID="00000000-0000-0000-0000-deadbeef0001" \
    MINION_LOCATION="MINION" \
    OPENNMS_BROKER_URL="tcp://127.0.0.1:61616" \
    OPENNMS_HTTP_URL="http://127.0.0.1:8980/opennms" \
    OPENNMS_HTTP_USER="minion" \
    OPENNMS_HTTP_PASS="minion" \
    OPENNMS_BROKER_USER="minion" \
    OPENNMS_BROKER_PASS="minion"

##------------------------------------------------------------------------------
## EXPOSED PORTS
##------------------------------------------------------------------------------
## -- OpenNMS KARAF SSH    8201/TCP
## -- OpenNMS JMX         18980/TCP
## -- SNMP Trapd           1162/UDP
## -- Syslog               1514/UDP
EXPOSE 8201/tcp 1162/udp 1514/udp
