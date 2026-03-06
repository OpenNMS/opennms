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
# Generic OpenNMS Daemon container (Karaf-only, no Manager/Eventd).
# The specific daemon to run is selected by featuresBoot overlay.
##
ARG BASE_IMAGE="opennms/deploy-base:ubi9-3.6.3.b335-jre-17"

FROM ${BASE_IMAGE} AS daemon-tarball

ADD --chown=10001:0 ./tarball-root/ /opt/usr-share/daemon/
RUN chmod -R g-w /opt/usr-share/daemon && \
    chmod -R g=u \
        /opt/usr-share/daemon/etc \
        /opt/usr-share/daemon/data \
        && \
    chmod -R a+x /opt/usr-share/daemon/bin && \
    chmod g=u /opt/usr-share/daemon && \
    find \
        /opt/usr-share/daemon/data \
        /opt/usr-share/daemon/deploy \
        /opt/usr-share/daemon/system \
        -type d -print0 | xargs -0 chmod 2775

FROM ${BASE_IMAGE} AS daemon-base

ARG REQUIRED_RPMS="hostname uuid"

SHELL ["/bin/bash", "-c"]

# install required RPMs
RUN microdnf -y install ${REQUIRED_RPMS} && \
    rm -rf /var/cache/yum


##
# Install and setup OpenNMS Daemon
##
FROM daemon-base

# If you copy from /opt/daemon to /opt/daemon the permissions are not preserved
COPY --chown=10001:0 --from=daemon-tarball /opt/usr-share /opt/

# Add entrypoint script
COPY --chown=10001:0 container-fs/entrypoint.sh /opt/daemon/bin/entrypoint.sh
RUN chmod 755 /opt/daemon/bin/entrypoint.sh

SHELL ["/bin/bash", "-c"]

# Create onmsd user with a specific group ID
RUN groupadd \
        --gid 10001 \
        onmsd && \
    useradd \
        --system \
        --uid 10001 \
        --gid 10001 \
        --home-dir /opt/daemon \
        --no-create-home \
        --shell /usr/bin/bash \
        onmsd

ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG REVISION

RUN install -d -o 10001 -g 0 -m 775 \
        /var/lib/daemon \
        /opt/daemon/data/{log,tmp} \
        /opt/daemon-etc-overlay \
        /opt/daemon-overlay

VOLUME [ "/opt/daemon/deploy", "/opt/daemon/etc", "/opt/daemon/data" ]

WORKDIR /opt/daemon

### Containers should NOT run as root as a good practice
USER 10001

ENTRYPOINT [ "/opt/daemon/bin/entrypoint.sh" ]

STOPSIGNAL SIGTERM

# Arguments for labels should not invalidate caches
ARG VERSION
ARG SOURCE
ARG BUILD_JOB_ID
ARG BUILD_NUMBER
ARG BUILD_URL
ARG BUILD_BRANCH

LABEL org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.title="OpenNMS Daemon ${VERSION}" \
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

ENV OPENNMS_HOME="/opt/daemon" \
    POSTGRES_HOST="localhost" \
    POSTGRES_PORT="5432" \
    POSTGRES_USER="postgres" \
    POSTGRES_PASSWORD="" \
    POSTGRES_DB="opennms" \
    JAVA_OPTS=""

##------------------------------------------------------------------------------
## EXPOSED PORTS
##------------------------------------------------------------------------------
## -- Daemon Karaf SSH   8201/TCP

EXPOSE 8201
