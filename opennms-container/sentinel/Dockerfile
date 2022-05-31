##
# Use Java base image and setup required DEBS as cacheable image.
##
ARG BASE_IMAGE="opennms/deploy-base"
ARG BASE_IMAGE_VERSION="jre-1.2.0.b105"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION} as sentinel-base

ARG REQUIRED_DEBS="hostname wget gettext openssh-client"

ARG REPO_KEY_URL="https://debian.opennms.org/OPENNMS-GPG-KEY"

SHELL ["/bin/bash", "-c"]

# Collect generic steps in a layer for caching
# import keys & install required debs
RUN apt-get update && \
    apt-get install -y gnupg ca-certificates && \
    curl -fsSL ${REPO_KEY_URL} | apt-key add && \
    apt-get install -yq ${REQUIRED_DEBS}

# Allow to send ICMP messages as non-root user
RUN setcap cap_net_raw+ep ${JAVA_HOME}/bin/java && \
    echo ${JAVA_HOME}/lib/jli > /etc/ld.so.conf.d/java-latest.conf && \
    ldconfig

# Create Sentinel user with a specific group ID
RUN groupadd -g 10001 sentinel && \
    adduser --uid 10001 --gid 10001 --home /usr/share/sentinel --shell /usr/bin/bash sentinel

# Create SSH Key-Pair to use with the Karaf Shell
RUN mkdir /usr/share/sentinel/.ssh && \
    chmod 700 /usr/share/sentinel/.ssh && \
    ssh-keygen -t rsa -f /usr/share/sentinel/.ssh/id_rsa -q -N ""

##
# Install and setup OpenNMS Sentinel DEBS
##
FROM sentinel-base

# A default which installs the minimum required Minion packages
ARG SENTINEL_PACKAGES="opennms-sentinel"

# Allow to install optional packages via DEB
ARG ADD_DEB_PACKAGES

COPY ./debs /tmp/debs

# Prevent setup prompt
ENV DEBIAN_FRONTEND=noninteractive

SHELL ["/bin/bash", "-c"]

# we want these to break the caching so yum will re-install now that
# we're not copying DEBs into /tmp/debs by default
ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG REVISION

RUN id sentinel
RUN  getent group sentinel
RUN  getent passwd sentinel

# Install repositories, system and OpenNMS packages and do some cleanup
RUN echo "installing packages for build ${REVISION} (${BUILD_DATE})"; \
    if [[ "$(ls -1 /tmp/debs/*.deb 2>/dev/null | wc -l)" != 0 ]]; then \
        echo "installing local DEBs from filesystem" && \
        apt -y install /tmp/debs/*.deb; \
    elif [[ -e /tmp/debs/opennms.list ]]; then \
        echo "installing local DEBs over HTTP" && \
        mv -f /tmp/debs/opennms.list /etc/apt/sources.list.d && \
        apt-get update && \
        apt-get install -yq ${SENTINEL_PACKAGES}; \
    else \
        echo "installing remote DEBs from stable" && \
        apt-get install -yq ${SENTINEL_PACKAGES}; \
    fi && \
    if [[ -n ${ADD_DEB_PACKAGES} ]]; then apt-get install -yq ${ADD_DEB_PACKAGES}; fi && \
    rm -rf /tmp/debs && \
    apt-get autoclean && \
    apt-get clean && \
    # make sure entrypoint.sh able to do cp -r with original path
    rm /usr/share/sentinel/{data,etc,deploy} && \
    mv /var/lib/sentinel/{data,deploy} /usr/share/sentinel && \
    mv /etc/sentinel /usr/share/sentinel/etc && \
    ln -s /usr/share/sentinel/data /var/lib/sentinel/data && \
    ln -s /usr/share/sentinel/deploy /var/lib/sentinel/deploy && \
    ln -s /usr/share/sentinel/etc /etc/sentinel && \
    mkdir -p /usr/share/sentinel/data/{log,tmp} && \
    echo sentinel=$(cat /usr/share/sentinel/.ssh/id_rsa.pub | awk '{print $2}'),viewer > /usr/share/sentinel/etc/keys.properties && \
    echo "_g_\\:admingroup = group,admin,manager,viewer,systembundles,ssh" >> /usr/share/sentinel/etc/keys.properties && \
    chown 10001:10001 -R /usr/share/sentinel /var/lib/sentinel && \
    chgrp -R 0 /usr/share/sentinel && \
    chmod -R g=u /usr/share/sentinel && \
    chmod 600 /usr/share/sentinel/.ssh/id_rsa && \
    ln -s /usr/share/sentinel /opt/sentinel

COPY ./assets/* /

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
      org.opennms.image.base="${BASE_IMAGE}:${BASE_IMAGE_VERSION}" \
      org.opennme.cicd.jobid="${BUILD_JOB_ID}" \
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
