##
# Use Java base image and setup required RPMS as cacheable image.
##
ARG BASE_IMAGE="opennms/openjdk"
ARG BASE_IMAGE_VERSION="11.0.5.10-b3108"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION} as minion-base

ARG REQUIRED_RPMS="wget gettext jicmp jicmp6 openssh-clients"

ARG REPO_KEY_URL="https://yum.opennms.org/OPENNMS-GPG-KEY"
ARG REPO_RPM="https://yum.opennms.org/repofiles/opennms-repo-stable-rhel8.noarch.rpm"

SHELL ["/bin/bash", "-c"]

# Collect generic steps in a layer for caching
RUN rpm --import "${REPO_KEY_URL}" && \
    dnf -y install epel-release && \
    dnf -y install "${REPO_RPM}" && \
    dnf -y install ${REQUIRED_RPMS} && \
    dnf clean all && \
    rm -rf /var/cache/yum

# Allow to send ICMP messages as non-root user
RUN setcap cap_net_raw+ep ${JAVA_HOME}/bin/java && \
    echo ${JAVA_HOME}/lib/jli > /etc/ld.so.conf.d/java-latest.conf && \
    ldconfig

# Create Minion user with a specific group ID
RUN groupadd -g 10001 minion && \
    adduser -u 10001 -g 10001 -d /opt/minion -s /usr/bin/bash minion

# Create SSH Key-Pair to use with the Karaf Shell
RUN mkdir /opt/minion/.ssh && \
    chmod 700 /opt/minion/.ssh && \
    ssh-keygen -t rsa -f /opt/minion/.ssh/id_rsa -q -N ""

##
# Install and setup OpenNMS Minion RPMS
##
FROM minion-base

ARG USER="minion"

# A default which installs the minimum required Minion packages
ARG MINION_PACKAGES="opennms-minion"

# Allow to install optional packages via YUM
ARG ADD_YUM_PACKAGES

COPY ./rpms /tmp/rpms

SHELL ["/bin/bash", "-c"]

# we want these to break the caching so yum will re-install now that
# we're not copying RPMs into /tmp/rpms by default
ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG REVISION

# Install repositories, system and OpenNMS packages and do some cleanup
RUN echo "installing packages for build ${REVISION} (${BUILD_DATE})"; \
    if [[ "$(ls -1 /tmp/rpms/*.rpm 2>/dev/null | wc -l)" != 0 ]]; then \
        echo "installing local RPMs from filesystem" && \
        dnf -y install /tmp/rpms/*.rpm; \
    elif [[ -e /tmp/rpms/opennms-docker.repo ]]; then \
        echo "installing local RPMs over HTTP" && \
        dnf -y install 'dnf-command(config-manager)' && \
        dnf -y config-manager --add-repo /tmp/rpms/*.repo && \
        dnf -y config-manager --set-enabled opennms-repo-docker-common && \
        dnf -y --disablerepo=opennms-repo-stable-common install ${MINION_PACKAGES}; \
    else \
        echo "installing remote RPMs from stable" && \
        dnf -y install ${MINION_PACKAGES}; \
    fi && \
    if [[ -n ${ADD_YUM_PACKAGES} ]]; then dnf -y install ${ADD_YUM_PACKAGES}; fi && \
    rm -rf /tmp/rpms && \
    dnf clean all && \
    rm -rf /var/cache/yum && \
    sed -r -i '/RUNAS/s/.*/export RUNAS=${USER}/' /etc/sysconfig/minion && \
    echo minion=$(cat /opt/minion/.ssh/id_rsa.pub | awk '{print $2}'),viewer > /opt/minion/etc/keys.properties && \
    echo "_g_\\:admingroup = group,admin,manager,viewer,systembundles,ssh" >> /opt/minion/etc/keys.properties && \
    chown 10001:10001 -R /opt/minion && \
    chgrp -R 0 /opt/minion && \
    chmod -R g=u /opt/minion && \
    chmod 600 /opt/minion/.ssh/id_rsa

COPY ./assets/* /

# Arguments for labels should not invalidate caches
ARG VERSION
ARG SOURCE
ARG BUILD_JOB_ID
ARG BUILD_NUMBER
ARG BUILD_URL
ARG BUILD_BRANCH

LABEL org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.title="OpenNMS Minion ${VERSION}" \
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

WORKDIR /opt/minion

### Containers should NOT run as root as a good practice
USER 10001

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-f" ]

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
