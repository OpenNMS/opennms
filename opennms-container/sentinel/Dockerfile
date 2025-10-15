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
# Use Java base image and setup required RPMs as cacheable image.
##
ARG BASE_IMAGE="opennms/deploy-base:ubi9-3.6.1.b295-jre-17"

FROM ${BASE_IMAGE} AS sentinel-tarball

ADD --chown=10001:0 ./tarball-root/ /opt/usr-share/sentinel/
RUN chmod -R g-w /opt/usr-share/sentinel && \
    chmod -R g=u \
        /opt/usr-share/sentinel/etc \
        /opt/usr-share/sentinel/data \
        && \
    chmod -R a+x /opt/usr-share/sentinel/bin && \
    chmod g=u /opt/usr-share/sentinel && \
    find \
        /opt/usr-share/sentinel/data \
        /opt/usr-share/sentinel/deploy \
        /opt/usr-share/sentinel/system \
        /opt/usr-share/sentinel/repositories \
        -type d -print0 | xargs -0 chmod 2775

FROM ${BASE_IMAGE} AS sentinel-base

ARG REQUIRED_RPMS="hostname uuid"

SHELL ["/bin/bash", "-c"]

# Collect generic steps in a layer for caching

# install required RPMs
RUN microdnf -y install ${REQUIRED_RPMS} && \
    rm -rf /var/cache/yum


##
# Install and setup OpenNMS Sentinel
##
FROM sentinel-base

# If you copy from /usr/share/sentinel to /usr/share/sentinel the permissions are not preserved
# We would have 755 for sentinel:root instead of 775 and prevents writing lock files in /usr/share/sentinel
COPY --chown=10001:0 --from=sentinel-tarball /opt/usr-share /usr/share

SHELL ["/bin/bash", "-c"]

# Create Sentinel user with a specific group ID
RUN groupadd \
        --gid 10001 \
        sentinel && \
    useradd \
        --system \
        --uid 10001 \
        --gid 10001 \
        --home-dir /usr/share/sentinel \
        --no-create-home \
        --shell /usr/bin/bash \
        sentinel

ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG REVISION

RUN id sentinel
RUN  getent group sentinel
RUN  getent passwd sentinel

# Install any spare packages and create some compatibility links
RUN install -d -o 10001 -g 0 -m 775 \
        /var/lib/sentinel \
        /usr/share/sentinel/data/{log,tmp} && \
    ln -s /usr/share/sentinel/data /var/lib/sentinel/data && \
    ln -s /usr/share/sentinel/deploy /var/lib/sentinel/deploy && \
    ln -s /usr/share/sentinel/etc /etc/sentinel && \
    ln -s /usr/share/sentinel /opt/sentinel

# Install entrypoint wrapper and health check script
COPY container-fs/entrypoint.sh /
COPY container-fs/health.sh /

# Install a default JMX Prometheus configuration
COPY container-fs/prom-jmx-default-config.yaml /opt/prom-jmx-exporter/config.yaml

# Install confd.io configuration files and scripts and ensure they are executable
COPY ./container-fs/confd/ /opt/sentinel/confd/
RUN chmod +x /opt/sentinel/confd/scripts/*

VOLUME [ "/usr/share/sentinel/deploy", "/usr/share/sentinel/etc", "/usr/share/sentinel/data" ]

WORKDIR /usr/share/sentinel

### Containers should NOT run as root as a good practice
USER 10001

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-f" ]

# Arguments for labels should not invalidate caches
ARG VERSION
ARG SOURCE
ARG BUILD_JOB_ID
ARG BUILD_NUMBER
ARG BUILD_URL
ARG BUILD_BRANCH

LABEL org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.title="OpenNMS Sentinel ${VERSION}" \
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

### Runtime information and not relevant at build time

# TODO MVR SENTINEL_LOCATION is not used at the moment
ENV SENTINEL_HOME="/usr/share/sentinel" \
    SENTINEL_ID="" \
    SENTINEL_LOCATION="SENTINEL" \
    OPENNMS_BROKER_URL="tcp://127.0.0.1:61616" \
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
