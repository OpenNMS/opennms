##
# Use Java base image and setup required RPMS as cacheable image.
##
ARG BASE_IMAGE="opennms/deploy-base:jre-2.0.6.b165"

FROM ${BASE_IMAGE} as horizon-base

ADD --chown=10001:10001 ./tarball-root/ /usr/share/opennms/

ARG REQUIRED_DEBS="rrdtool jrrd2 r-base-core rsync libxml-twig-perl libwww-perl jq diffutils hostname sudo"

ARG REPO_KEY_URL="https://debian.opennms.org/OPENNMS-GPG-KEY"

SHELL ["/bin/bash", "-c"]

# Prevent setup prompt
ENV DEBIAN_FRONTEND=noninteractive

# import keys
RUN apt-get update && \
    apt-get \
    --no-install-recommends \
    -yq \
    install \
    gnupg \
    ca-certificates && \
    curl -fsSL ${REPO_KEY_URL} | apt-key add && \
    echo "deb https://debian.opennms.org stable main" > /etc/apt/sources.list.d/opennms.list && \
    apt-get update && \
    apt-get \
    --no-install-recommends \
    -yq \
    install \
    ${REQUIRED_DEBS} && \
    apt-get -y autoclean && \
    apt-get -y clean && \
    rm -rf /var/cache/apt

# Allow to send ICMP messages as non-root user
RUN setcap cap_net_raw+ep ${JAVA_HOME}/bin/java && \
    echo ${JAVA_HOME}/lib/jli > /etc/ld.so.conf.d/java-latest.conf && \
    ldconfig

# Create OpenNMS user with a specific group ID
RUN groupadd \
    -g 10001 \
    opennms && \
    adduser \
    --uid 10001 \
    --gid 10001 \
    --home /opt/opennms \
    --no-create-home \
    --shell /usr/bin/bash \
    opennms

##
# Install and setup OpenNMS RPMS
##
FROM horizon-base

# Allow to install optional packages via YUM
ARG ADD_DEB_PACKAGES

SHELL ["/bin/bash", "-c"]

# we want these to break the caching so yum will re-install now that
# we're not copying RPMs into /tmp/rpms by default
ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG REVISION
ARG ADD_DEB_PACKAGES

# Install any spare packages and create some compatibility links
RUN if [[ -n "${ADD_DEB_PACKAGES}" ]]; then \
    apt-get --no-install-recommends install -yq ${ADD_DEB_PACKAGES}; \
    fi && \
    apt-get -y autoclean && \
    apt-get -y clean && \
    rm -rf /usr/share/opennms/share/rrd \
    /usr/share/opennms/share/reports \
    /usr/share/opennms/share/mibs \
    /var/cache/apt && \
    install -d -m 755 -g 10001 -o 10001 \
    /opt/opennms-etc-overlay \
    /opt/opennms-jetty-webinf-overlay \
    /opt/opennms-overlay \
    /opennms-data && \
    install -d -m 755 -g 10001 -o 10001 \
    /opennms-data/rrd \
    /opennms-data/reports \
    /opennms-data/mibs && \
    ln -s /opennms-data/rrd /usr/share/opennms/share/rrd && \
    ln -s /opennms-data/reports /usr/share/opennms/share/reports && \
    ln -s /opennms-data/mibs /usr/share/opennms/share/mibs && \
    # Align with old opennms path
    ln -s /usr/share/opennms /opt/opennms && \
    # Create etc-pristine
    rsync -ar /opt/opennms/etc/ /opt/opennms/share/etc-pristine/

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
    org.opennms.image.base="${BASE_IMAGE}" \
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
