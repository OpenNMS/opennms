##
# Use Java base image and setup required RPMS as cacheable image.
##
ARG BASE_IMAGE="opennms/openjdk"
ARG BASE_IMAGE_VERSION="11"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION} as horizon-base

ARG REQUIRED_RPMS="rrdtool jrrd2 jicmp jicmp6 R-core rsync perl-XML-Twig perl-libwww-perl"

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
# Install and setup OpenNMS RPMS
##
FROM horizon-base

# A default which installs the required OpenNMS Horizon packages
ARG ONMS_PACKAGES="opennms-core opennms-webapp-jetty opennms-webapp-remoting opennms-webapp-hawtio"

# Allow to install optional packages via YUM
ARG ADD_YUM_PACKAGES

COPY ./rpms /tmp/rpms

SHELL ["/bin/bash", "-c"]

# Install repositories, system and OpenNMS packages and do some cleanup
RUN if [[ "$(ls -1 /tmp/rpms/*.rpm 2>/dev/null | wc -l)" != 0 ]]; then yum -y localinstall /tmp/rpms/*.rpm; else yum install -y ${ONMS_PACKAGES}; fi && \
    if [[ -n ${ADD_YUM_PACKAGES} ]]; then yum -y install ${ADD_YUM_PACKAGES}; fi && \
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

# Arguments for labels should not invalidate caches
ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG VERSION=${BASE_IMAGE_VERSION}
ARG BUILD_NUMBER
ARG BUILD_URL
ARG BUILD_BRANCH
ARG BUILD_SHA1

LABEL maintainer="The OpenNMS Group" \
      license="AGPLv3" \
      name="Horizon" \
      version="${VERSION}" \
      vendor="OpenNMS Community" \
      cicd.build.date="${BUILD_DATE}" \
      cicd.build.number="${BUILD_NUMBER}" \
      cicd.build.url="${BUILD_URL}" \
      cicd.build.branch="${BUILD_BRANCH}" \
      cicd.build.sha1="${CIRCLE_SHA1}"

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
