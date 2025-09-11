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
ARG BASE_IMAGE="opennms/deploy-base:ubi9-3.6.0.b293-jre-17"

FROM ${BASE_IMAGE} AS core-tarball

ADD --chown=10001:0 ./tarball-root/ /opt/usr-share/opennms/
RUN chmod -R g-w /opt/usr-share/opennms && \
    chmod -R g=u \
        /opt/usr-share/opennms/etc \
        /opt/usr-share/opennms/data \
        /opt/usr-share/opennms/jetty-webapps/*/WEB-INF \
        /opt/usr-share/opennms/logs \
        /opt/usr-share/opennms/share \
        && \
    chmod g=u /opt/usr-share/opennms && \
    find \
        /opt/usr-share/opennms/data \
        /opt/usr-share/opennms/deploy \
        /opt/usr-share/opennms/logs \
        /opt/usr-share/opennms/system \
        -type d -print0 | xargs -0 chmod 2775

##
# Download plugins
##
FROM ${BASE_IMAGE} AS core-plugins

COPY plugins.sh /tmp/plugins.sh 
RUN chmod +x /tmp/plugins.sh  && cd /tmp && ./plugins.sh && rm ./plugins.sh


FROM ${BASE_IMAGE} AS core-base

ARG REQUIRED_RPMS="diffutils hostname jrrd2 jq rrdtool"

SHELL ["/bin/bash", "-c"]

RUN microdnf -y install ${REQUIRED_RPMS} && \
    rm -rf /var/cache/yum

# Create OpenNMS user with a specific group ID
RUN groupadd \
        --gid 10001 \
        opennms && \
    adduser \
        --uid 10001 \
        --gid 10001 \
        --home /opt/opennms \
        --no-create-home \
        --shell /usr/bin/bash \
        opennms

# If you copy from /opt/opennms to /opt/opennms the permissions are not preserved
# We would have 755 for opennms:root instead of 775 and prevents writing lock files in /opt/opennms
COPY --chown=10001:0 --from=core-tarball /opt/usr-share /usr/share

COPY --chown=10001:0 --from=core-plugins /opt/usr-plugins /opt/opennms/deploy

##
# Install and setup OpenNMS RPMS
##
FROM core-base

SHELL ["/bin/bash", "-c"]

# we want these to break the caching so yum will re-install now that
# we're not copying RPMs into /tmp/rpms by default
ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG REVISION

# Install any spare packages and create some compatibility links
RUN rm -rf /opt/opennms/share/rrd \
           /opt/opennms/share/reports \
           /opt/opennms/share/mibs \
           /var/cache/apt && \
    install -d -m 775 -o 10001 -g 0 \
            /opt/opennms-etc-overlay \
            /opt/opennms-jetty-webinf-overlay \
            /opt/opennms-overlay \
            /opennms-data \
            /var/log && \
    install -d -m 775 -o 10001 -g 0 \
            /opennms-data/rrd \
            /opennms-data/reports \
            /opennms-data/mibs && \
    ln -s /opennms-data/reports /opt/opennms/share/reports && \
    ln -s /opennms-data/mibs /opt/opennms/share/mibs && \
# Compatibility with other OpenNMS paths
    ln -s /opt/opennms /opt/opennms && \
    ln -s /opt/opennms/etc /etc/opennms && \
    ln -s /opt/opennms/logs /var/log/opennms && \
    ln -s /opt/opennms/share /var/lib/opennms && \
# Create etc-pristine
    rsync -ar /opt/opennms/etc/ /opt/opennms/share/etc-pristine/

# Add templates replaced at runtime and entrypoint
COPY --chown=10001:0 ./container-fs/confd /etc/confd
COPY --chown=10001:0 ./container-fs/entrypoint.sh /
COPY --chown=10001:0 ./container-fs/health.sh /

# Allow users in the root group to access them with the same authorization as the directory and file owner
RUN chmod -R g=u /opt/opennms/etc
RUN find /opt/opennms/data /opt/opennms/system /opennms-data -type d -print0 | xargs -0 chmod g=u

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
      org.opennms.cicd.jobid="${BUILD_JOB_ID}" \
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
# Allow OpenNMS to detect whether it is run in a container
ENV OPENNMS_EXECUTION_ENVIRONMENT=container

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
