ARG BASE_IMAGE="opennms/openjdk"
ARG BASE_IMAGE_VERSION="latest"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION}

ARG VERSION=${BASE_IMAGE_VERSION}
ARG BUILD_DATE="1970-01-01T00:00:00+0000"

ARG USER="minion"

ARG REPO_RELEASE="stable"
ARG REPO_HOST="yum.opennms.org"

ARG REPO_KEY_URL="https://${REPO_HOST}/OPENNMS-GPG-KEY"

ARG REPO_RPM="https://${REPO_HOST}/repofiles/opennms-repo-${REPO_RELEASE}-rhel7.noarch.rpm"

ARG PACKAGES="wget gettext"

ARG MINION_PACKAGES="opennms-minion"

COPY ./rpms /tmp/rpms

SHELL ["/bin/bash", "-c"]

# Install repositories, system and OpenNMS packages and do some cleanup
RUN yum -y install epel-release && \
    rpm --import "${REPO_KEY_URL}" && \
    yum -y install ${REPO_RPM} && \
    yum -y install ${PACKAGES} && \
    if [ "$(ls -1 /tmp/rpms/*.rpm 2>/dev/null | wc -l)" != 0 ]; then yum -y localinstall /tmp/rpms/*.rpm; else yum -y install ${MINION_PACKAGES}; fi && \
    rm -rf /tmp/rpms && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    sed -r -i '/RUNAS/s/.*/export RUNAS=${USER}/' /etc/sysconfig/minion && \
    chgrp -R 0 /opt/minion && \
    chmod -R g=u /opt/minion

COPY ./assets/entrypoint.sh /

LABEL maintainer="The OpenNMS Group" \
      license="AGPLv3" \
      name="Minion" \
      version="${VERSION}" \
      build.date="${BUILD_DATE}" \
      vendor="OpenNMS Community" \
      org.opennms.repo.release="${REPO_RELEASE}"

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
