##
# Pre-stage image to extract and manipulate Minion directory structure
# Normally we install to /opt/minion and not /opt/minion-XX.X.X-SNAPSHOT
# To avoid issues, we rearrange the directories in pre-stage to avoid injecting these
# as addtional layers into the final image.
##
ARG BASE_IMAGE="opennms/deploy-base:jre-1.0.0.b39"

FROM ${BASE_IMAGE} as minion-base

ADD ./tarball/minion.tar.gz /opt/

# Organize files based on the build home dir /opt/minion
RUN mv /opt/minion-* /opt/minion

# We set the correct permissions in this stage, so we don't have this as an addtional
# layer in our deploy image.
RUN groupadd --gid 10001 minion && \
    useradd --system --uid 10001 --gid minion minion --home-dir /opt/minion && \
    chown 10001 /opt/minion -R && \
    chgrp -R 0 /opt/minion && \
    chmod -R g=u /opt/minion

##
# Prod image with minimal image size
##
FROM ${BASE_IMAGE}

# Create user to be able to run as non root with a known user and group id.
RUN groupadd --gid 10001 minion && \
    useradd --system --uid 10001 --gid minion minion --home-dir /opt/minion

# https://issues.opennms.org/browse/NMS-12635
# It is possible to set sysctls: net.ipv4.ping_group_range=0 10001 which allows the container using sockets. If we run on
# infrastructure which doesn't allow whitelisting net.ipv4.ping_group_range as a safe sysctl (Kubernetes < 1.18) the
# minimal solution is giving the Java binary the cap_net_raw+ep capabilities.
RUN setcap cap_net_raw+ep $(readlink -f /usr/bin/java)

# Install entrypoint wrapper and health check script
COPY container-fs/entrypoint.sh /
COPY container-fs/health.sh /

# If you copy from /opt/minion to /opt/minion the permissions are not preserved
# We would have 755 for minion:root instead of 775 and prevents writing lock files in /opt/minion
COPY --chown=10001:0 --from=minion-base /opt /opt

# Install confd.io configuration files and scripts and ensure they are executable
COPY ./container-fs/confd/ /opt/minion/confd/
RUN chmod +x /opt/minion/confd/scripts/*

# Create SSH Key-Pair to use with the Karaf Shell
# This is a workaround to be able to use our health:check which does not work with the karaf/bin/client command
RUN mkdir /opt/minion/.ssh && \
    ssh-keygen -t rsa -f /opt/minion/.ssh/id_rsa -q -N "" && \
    chmod 700 /opt/minion/.ssh && \
    chmod 600 /opt/minion/.ssh/id_rsa && \
    chown 10001 /opt/minion/.ssh -R && \
    echo minion=$(cat /opt/minion/.ssh/id_rsa.pub | awk '{print $2}'),viewer > /opt/minion/etc/keys.properties && \
    echo "_g_\\:admingroup = group,admin,manager,viewer,systembundles,ssh" >> /opt/minion/etc/keys.properties

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
      org.opencontainers.image.title="OpenNMS Minion ${VERSION}" \
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

WORKDIR /opt/minion

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
## -- SNMP Trapd           1162/UDP
## -- Syslog               1514/UDP
EXPOSE 8201/tcp 1162/udp 1514/udp
