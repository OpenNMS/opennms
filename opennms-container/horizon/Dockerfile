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

ARG ONMS_PACKAGES="opennms-core opennms-webapp-jetty opennms-webapp-hawtio"

COPY ./rpms /tmp/rpms

SHELL ["/bin/bash", "-c"]

# Install repositories, system and OpenNMS packages and do some cleanup
RUN yum -y install epel-release && \
    rpm --import "${REPO_KEY_URL}" && \
    yum -y install ${REPO_RPM} && \
    yum -y install ${PACKAGES} && \
    if [ "$(ls -1 /tmp/rpms/*.rpm 2>/dev/null | wc -l)" != 0 ]; then yum -y localinstall /tmp/rpms/*.rpm; else yum -y install ${ONMS_PACKAGES}; fi && \
    rm -rf /tmp/rpms && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    rm -rf /opt/opennms/logs \
           /var/opennms/rrd \
           /var/opennms/reports && \
    mkdir -p /opennms-data/logs \
             /opennms-data/rrd \
             /opennms-data/reports && \
    ln -s /opennms-data/logs /opt/opennms/logs && \
    ln -s /opennms-data/rrd /var/opennms/rrd && \
    ln -s /opennms-data/reports /var/opennms/reports

# Add templates replaced at runtime and entrypoint
COPY ./assets/*.tpl /root/
COPY ./assets/entrypoint.sh /

LABEL maintainer="The OpenNMS Group" \
      license="AGPLv3" \
      name="Horizon" \
      version="${VERSION}" \
      build.date="${BUILD_DATE}" \
      vendor="OpenNMS Community" \
      org.opennms.repo.release="${REPO_RELEASE}"

WORKDIR /opt/opennms

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-h" ]

### Runtime information and not relevant at build time

ENV OPENNMS_KARAF_SSH_HOST="0.0.0.0" \
    OPENNMS_KARAF_SSH_PORT="8101" \
    JAVA_OPTS=""

# Volumes for storing data outside of the container
VOLUME [ "/opt/opennms/etc", "/opt/opennms-etc-overlay", "/opennms-data" ]

##------------------------------------------------------------------------------
## EXPOSED PORTS
##------------------------------------------------------------------------------
## -- OpenNMS HTTP        8980/TCP
## -- OpenNMS JMX        18980/TCP
## -- OpenNMS KARAF RMI   1099/TCP
## -- OpenNMS KARAF SSH   8101/TCP
## -- OpenNMS MQ         61616/TCP
## -- OpenNMS Eventd      5817/TCP
## -- SNMP Trapd           162/UDP
## -- Syslog Receiver      514/UDP
EXPOSE 8980 8101 61616 162/udp 514/udp
