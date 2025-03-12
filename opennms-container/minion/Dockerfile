#
# Licensed to The OpenNMS Group, Inc (TOG) under one or more
# contributor license agreements.  See the LICENSE.md file
# distributed with this work for additional information
# regarding copyright ownership.
#
# TOG licenses this file to You under the GNU Affero General
# Public License Version 3 (the "License") or (at your option)
# any later version.  You may not use this file except in
# compliance with the License.  You may obtain a copy of the
# License at:
#
#      https://www.gnu.org/licenses/agpl-3.0.txt
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
# either express or implied.  See the License for the specific
# language governing permissions and limitations under the
# License.
#

##
# Pre-stage image to extract and manipulate Minion directory structure
# Normally we install to /opt/minion and not /opt/minion-XX.X.X-SNAPSHOT
# To avoid issues, we rearrange the directories in pre-stage to avoid injecting these
# as additional layers into the final image.
##
ARG BASE_IMAGE="opennms/deploy-base:ubi9-3.5.2.b283-jre-17"

FROM ${BASE_IMAGE} AS minion-base

ADD --chown=10001:0 ./tarball-root/ /opt/minion/
RUN rm /opt/minion/etc/org.opennms.features.telemetry.listeners-single-port-flows.cfg
RUN chmod -R g-w /opt/minion && \
    chmod -R g=u \
        /opt/minion/etc \
        /opt/minion/data \
        && \
    chmod -R a+x /opt/minion/bin && \
    chmod g=u /opt/minion && \
    find \
        /opt/minion/data \
        /opt/minion/deploy \
        /opt/minion/system \
        /opt/minion/repositories \
        -type d -print0 | xargs -0 chmod 2775

##
# Download plugins
##
FROM ${BASE_IMAGE} AS minion-plugins

COPY plugins.sh /tmp/plugins.sh 
RUN chmod +x /tmp/plugins.sh  && cd /tmp && ./plugins.sh && rm ./plugins.sh

##
# Prod image with minimal image size
##
FROM ${BASE_IMAGE}

ARG REQUIRED_RPMS="hostname uuid"

# Collect generic steps in a layer for caching

# install required RPMs
RUN microdnf -y install ${REQUIRED_RPMS} && \
    rm -rf /var/cache/yum

RUN groupadd \
        --gid 10001 \
        minion && \
    useradd \
        --system \
        --uid 10001 \
        --gid 10001 \
        --home-dir /opt/minion \
        --no-create-home \
        --shell /usr/bin/bash \
        minion

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
COPY --chown=10001:0 --from=minion-plugins /opt/usr-plugins /opt/minion/deploy

# Install confd.io configuration files and scripts and ensure they are executable
COPY ./container-fs/confd/ /opt/minion/confd/
RUN chmod +x /opt/minion/confd/scripts/*
COPY ./minion-config-schema.yml /opt/minion/confd/

# Create the directory for server certificates
RUN install -d -m 750 /opt/minion/server-certs

# Create prom-jmx-exporter folder
RUN install -d -m 770 /opt/prom-jmx-exporter && chown 10001:0 /opt/prom-jmx-exporter

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
    org.opennms.cicd.jobid="${BUILD_JOB_ID}" \
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
