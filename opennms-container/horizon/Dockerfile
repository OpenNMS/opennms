##
# Use Java base image and setup required RPMS as cacheable image.
##
ARG BASE_IMAGE="opennms/deploy-base"
ARG BASE_IMAGE_VERSION="jre-1.2.0.b105"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION} as horizon-base

ARG REQUIRED_DEBS="rrdtool jrrd2 jicmp jicmp6 r-base-core rsync libxml-twig-perl libwww-perl jq diffutils hostname sudo"

ARG CONFD_ARCH="linux-amd64"
ARG CONFD_VERSION="0.16.0"
ARG CONFD_URL="https://github.com/kelseyhightower/confd/releases/download/v${CONFD_VERSION}/confd-${CONFD_VERSION}-${CONFD_ARCH}"
ARG REPO_KEY_URL="https://debian.opennms.org/OPENNMS-GPG-KEY"

SHELL ["/bin/bash", "-c"]

# Collect generic steps in a layer for caching
RUN curl --retry 5 --fail -L ${CONFD_URL} -o /usr/bin/confd && \
    chmod +x /usr/bin/confd

# import keys
RUN apt-get update && \
    apt-get install -yq gnupg ca-certificates && \
    curl -fsSL ${REPO_KEY_URL} | apt-key add

COPY opennms.list /etc/apt/sources.list.d
RUN apt-get update && \
    apt-get install -yq ${REQUIRED_DEBS} && \
    apt-get clean

# Allow to send ICMP messages as non-root user
RUN setcap cap_net_raw+ep ${JAVA_HOME}/bin/java && \
    echo ${JAVA_HOME}/lib/jli > /etc/ld.so.conf.d/java-latest.conf && \
    ldconfig

# Create OpenNMS user with a specific group ID
RUN groupadd -g 10001 opennms && \
    adduser --uid 10001 --gid 10001 --no-create-home --home /opt/opennms --shell /usr/bin/bash opennms
##
# Install and setup OpenNMS RPMS
##
FROM horizon-base

# A default which installs the required OpenNMS Horizon packages
ARG ONMS_PACKAGES="opennms-common opennms-webapp-jetty opennms-webapp-hawtio libopennms-java libopennmsdeps-java"

# Allow to install optional packages via YUM
ARG ADD_DEB_PACKAGES

COPY ./debs /tmp/debs
COPY ./tarball /tmp/tarball

# Prevent setup prompt
ENV DEBIAN_FRONTEND=noninteractive

SHELL ["/bin/bash", "-c"]

# we want these to break the caching so yum will re-install now that
# we're not copying RPMs into /tmp/rpms by default
ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG REVISION

# Install repositories, system and OpenNMS packages and do some cleanup
# 1. Try to install from tarball
# 2. Try to install from local DEBS 
# 3. Try to install from public apt repository
#
# To avoid different behavior, between a debain to centos install.
# A symlink is created from /usr/share/opennms > /opt/opennms

RUN echo "installing packages for build ${REVISION} (${BUILD_DATE})"; \
    if [[ "$(ls -1 /tmp/tarball/*.tar.gz 2>/dev/null | wc -l)" != 0 ]]; then \
        mkdir -p /usr/share/opennms && \
        tar -xzf /tmp/tarball/opennms-*.tar.gz -C /usr/share/opennms && \
        chown -R 10001:0 /usr/share/opennms && \
        cp -r /usr/share/opennms/etc /usr/share/opennms/share/etc-pristine; \
    elif [[ "$(ls -1 /tmp/debs/*.deb 2>/dev/null | wc -l)" != 0 ]]; then \
        echo "installing local DEBs from filesystem" && \
        apt-get install -yq /tmp/debs/*.deb; \
    elif [[ -e /tmp/debs/opennms.list ]]; then \
        echo "installing local DEBs over HTTP" && \
        mv -f /tmp/debs/opennms.list /etc/apt/sources.list.d && \
        apt-get update && \
        apt-get install -yq ${ONMS_PACKAGES}; \
    else \
        echo "installing remote DEBs from stable" && \
        apt-get install -yq ${ONMS_PACKAGES}; \
    fi && \
    if [[ -n ${ADD_DEB_PACKAGES} ]]; then apt-get install -yq ${ADD_DEB_PACKAGES}; fi && \
    rm -rf /tmp/debs /tmp/tarball && \
    apt-get autoclean && \
    apt-get clean && \
    rm -rf /usr/share/opennms/share/rrd \
           /usr/share/opennms/share/reports \
           /usr/share/opennms/share/mibs && \
    mkdir -p /opt/opennms-etc-overlay \
             /opt/opennms-jetty-webinf-overlay \
             /opt/opennms-overlay && \
    mkdir -p /opennms-data/rrd \
             /opennms-data/reports \
             /opennms-data/mibs && \
    ln -s /opennms-data/rrd /usr/share/opennms/share/rrd && \
    ln -s /opennms-data/reports /usr/share/opennms/share/reports && \
    ln -s /opennms-data/mibs /usr/share/opennms/share/mibs && \
# Align with old opennms path
    ln -s /usr/share/opennms /opt && \
    chown 10001:10001 -R /usr/share/opennms \
                         /opennms-data \
                         /opt/opennms-overlay \
                         /opt/opennms-etc-overlay \
                         /opt/opennms-jetty-webinf-overlay && \
    chgrp -R 0 /usr/share/opennms \
               /opennms-data \
               /opt/opennms-overlay \
               /opt/opennms-etc-overlay \
               /opt/opennms-jetty-webinf-overlay && \
    chmod -R g=u /usr/share/opennms \
                 /opennms-data \
                 /opt/opennms-overlay \
                 /opt/opennms-etc-overlay \
                 /opt/opennms-jetty-webinf-overlay

# Add templates replaced at runtime and entrypoint
COPY --chown=10001:0 ./container-fs/confd /etc/confd
COPY --chown=10001:0 ./container-fs/entrypoint.sh /

# Arguments for labels should not invalidate caches
ARG VERSION
ARG SOURCE
ARG BUILD_JOB_ID
ARG BUILD_NUMBER
ARG BUILD_URL
ARG BUILD_BRANCH

LABEL org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.title="OpenNMS Horizon ${VERSION}" \
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

WORKDIR /opt/opennms

### Containers should NOT run as root as a good practice
USER 10001

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-h" ]

### Runtime information and not relevant at build time
ENV JAVA_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m"

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
## -- SNMP Trapd          1162/UDP
## -- Syslog Receiver    10514/UDP
EXPOSE 8980 8101 1162/udp 10514/udp
