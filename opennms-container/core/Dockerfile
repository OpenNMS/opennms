##
# Use Java base image and setup required RPMs as cacheable image.
##
ARG BASE_IMAGE="opennms/deploy-base:ubi9-3.2.0.b247-jre-17"

FROM ${BASE_IMAGE} as core-tarball

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
FROM ${BASE_IMAGE} as core-plugins

COPY plugins.sh /tmp/plugins.sh 
RUN chmod +x /tmp/plugins.sh  && cd /tmp && ./plugins.sh && rm ./plugins.sh


FROM ${BASE_IMAGE} as core-base

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
        --home /usr/share/opennms \
        --no-create-home \
        --shell /usr/bin/bash \
        opennms

# If you copy from /usr/share/opennms to /usr/share/opennms the permissions are not preserved
# We would have 755 for opennms:root instead of 775 and prevents writing lock files in /usr/share/opennms
COPY --chown=10001:0 --from=core-tarball /opt/usr-share /usr/share

COPY --chown=10001:0 --from=core-plugins /opt/usr-plugins /usr/share/opennms/deploy

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
RUN rm -rf /usr/share/opennms/share/rrd \
           /usr/share/opennms/share/reports \
           /usr/share/opennms/share/mibs \
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
    ln -s /opennms-data/rrd /usr/share/opennms/share/rrd && \
    ln -s /opennms-data/reports /usr/share/opennms/share/reports && \
    ln -s /opennms-data/mibs /usr/share/opennms/share/mibs && \
# Compatibility with other OpenNMS paths
    ln -s /usr/share/opennms /opt/opennms && \
    ln -s /usr/share/opennms/etc /etc/opennms && \
    ln -s /usr/share/opennms/logs /var/log/opennms && \
    ln -s /usr/share/opennms/share /var/lib/opennms && \
# Create etc-pristine
    rsync -ar /opt/opennms/etc/ /opt/opennms/share/etc-pristine/

# Add templates replaced at runtime and entrypoint
COPY --chown=10001:0 ./container-fs/confd /etc/confd
COPY --chown=10001:0 ./container-fs/entrypoint.sh /
COPY --chown=10001:0 ./container-fs/health.sh /

# Allow users in the root group to access them with the same authorization as the directory and file owner
RUN chmod -R g=u /usr/share/opennms/etc
RUN find /usr/share/opennms/data /usr/share/opennms/system /opennms-data -type d -print0 | xargs -0 chmod g=u

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

# We pass an --arg and then reference it later to make sure it is JSON quoted properly
RUN jq --null-input \
    --arg created "${BUILD_DATE}" \
    --arg title "OpenNMS Horizon ${VERSION}" \
    --arg source "${SOURCE}" \
    --arg revision "${REVISION}" \
    --arg version "${VERSION}" \
    --arg vendor "The OpenNMS Group, Inc." \
    --arg authors "OpenNMS Community" \
    --arg licenses "AGPL-3.0" \
    --arg image_base "${BASE_IMAGE}" \
    --arg jobid "${BUILD_JOB_ID}" \
    --arg buildnumber "${BUILD_NUMBER}" \
    --arg buildurl "${BUILD_URL}" \
    --arg branch "${BUILD_BRANCH}" \
    '{ \
        "org.opencontainers.image.created": $created, \
        "org.opencontainers.image.title": $title, \
        "org.opencontainers.image.source": $source, \
        "org.opencontainers.image.revision": $revision, \
        "org.opencontainers.image.version": $version, \
        "org.opencontainers.image.vendor": $vendor, \
        "org.opencontainers.image.authors": $authors, \
        "org.opencontainers.image.licenses": $licenses, \
        "org.opennms.image.base": $image_base, \
        "org.opennms.cicd.jobid": $jobid, \
        "org.opennms.cicd.buildnumber": $buildnumber, \
        "org.opennms.cicd.buildurl": $buildurl, \
        "org.opennms.cicd.branch": $branch, \
    }' \
    > /usr/share/opennms/.container-labels.json

WORKDIR /usr/share/opennms

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
