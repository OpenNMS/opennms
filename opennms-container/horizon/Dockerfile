##
# Use Java base image and setup required RPMS as cacheable image.
##
ARG BASE_IMAGE="opennms/openjdk"
ARG BASE_IMAGE_VERSION="11"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION} as horizon-base

ARG REQUIRED_RPMS="rrdtool jrrd2 jicmp jicmp6 R-core rsync perl-XML-Twig perl-libwww-perl gettext"

ARG REPO_KEY_URL="https://yum.opennms.org/OPENNMS-GPG-KEY"
ARG REPO_RPM="https://yum.opennms.org/repofiles/opennms-repo-stable-rhel7.noarch.rpm"

SHELL ["/bin/bash", "-c"]

# Collect generic steps in a layer for caching
RUN rpm --import "${REPO_KEY_URL}" && \
    yum install -y epel-release && \
    yum -y install "${REPO_RPM}" && \
    yum -y install ${REQUIRED_RPMS} && \
    yum clean all -y && \
    rm -rf /var/cache/yum

# Allow to send ICMP messages as non-root user
RUN setcap cap_net_raw+ep ${JAVA_HOME}/bin/java && \
    echo ${JAVA_HOME}/lib/jli > /etc/ld.so.conf.d/java-latest.conf && \
    ldconfig

# Set gid=0 and make group perms==owner perms
RUN groupadd -g 10001 opennms && \
    adduser -u 10001 -g 10001 -d /opt/opennms -s /usr/bin/bash opennms
##
# Install and setup OpenNMS RPMS
##
FROM horizon-base

# A default which installs the required OpenNMS Horizon packages
ARG ONMS_PACKAGES="opennms-core opennms-webapp-jetty opennms-webapp-remoting opennms-webapp-hawtio"

# Allow to install optional packages via YUM
ARG ADD_YUM_PACKAGES

COPY ./rpms /tmp/rpms
COPY ./tarball /tmp/tarball

SHELL ["/bin/bash", "-c"]

# Install repositories, system and OpenNMS packages and do some cleanup
# 1. Try to install from tarball
# 2. Try to install from local RPMS
# 3. Try to install from public yum repository
#
# To avoid different behavior, between a RPM or tarball installation,
# the directory structure is unified, the links for /opt/opennms/share
# and /opt/opennms/logs introduced by RPM installs are removed.
RUN if [[ "$(ls -1 /tmp/tarball/*.tar.gz 2>/dev/null | wc -l)" != 0 ]]; then \
        mkdir -p /opt/opennms && tar xzf /tmp/tarball/opennms-*.tar.gz -C /opt/opennms && \
        cp -r /opt/opennms/etc /opt/opennms/share/etc-pristine; \
    elif [[ "$(ls -1 /tmp/rpms/*.rpm 2>/dev/null | wc -l)" != 0 ]]; then \
        yum -y localinstall /tmp/rpms/*.rpm && \
        rm -rf /opt/opennms/logs \
               /opt/opennms/share && \
        mv /var/opennms /opt/opennms/share && \
        mv /var/log/opennms /opt/opennms/logs; \
    else \
        yum install -y ${ONMS_PACKAGES} && \
        rm -rf /opt/opennms/logs \
               /opt/opennms/share && \
        mv /var/opennms /opt/opennms/share && \
        mv /var/log/opennms /opt/opennms/logs; \
    fi && \
    if [[ -n ${ADD_YUM_PACKAGES} ]]; then yum -y install ${ADD_YUM_PACKAGES}; fi && \
    rm -rf /tmp/rpms /tmp/tarball && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    rm -rf /opt/opennms/share/rrd \
           /opt/opennms/share/reports \
           /opt/opennms/share/mibs && \
    mkdir -p /opt/opennms-etc-overlay \
             /opt/opennms-jetty-webinf-overlay \
             /opt/opennms-overlay && \
    mkdir -p /opennms-data/rrd \
             /opennms-data/reports \
             /opennms-data/mibs && \
    ln -s /opennms-data/rrd /opt/opennms/share/rrd && \
    ln -s /opennms-data/reports /opt/opennms/share/reports && \
    ln -s /opennms-data/mibs /opt/opennms/share/mibs && \
    chown 10001:10001 -R /opt/opennms \
                         /opennms-data \
                         /opt/opennms-overlay \
                         /opt/opennms-etc-overlay \
                         /opt/opennms-jetty-webinf-overlay && \
    chgrp -R 0 /opt/opennms \
               /opennms-data \
               /opt/opennms-overlay \
               /opt/opennms-etc-overlay \
               /opt/opennms-jetty-webinf-overlay && \
    chmod -R g=u /opt/opennms \
                 /opennms-data \
                 /opt/opennms-overlay \
                 /opt/opennms-etc-overlay \
                 /opt/opennms-jetty-webinf-overlay

# Add templates replaced at runtime and entrypoint
COPY --chown=10001:0 ./assets/*.tpl /tmp/
COPY --chown=10001:0 ./assets/entrypoint.sh /

# Arguments for labels should not invalidate caches
ARG BUILD_DATE="1970-01-01T00:00:00+0000"
ARG VERSION=${BASE_IMAGE_VERSION}
ARG BUILD_NUMBER
ARG BUILD_URL
ARG BUILD_BRANCH
ARG BUILD_SHA1

LABEL maintainer="The OpenNMS Group" \
      license="AGPLv3" \
      name="Horizon" \
      version="${VERSION}" \
      vendor="OpenNMS Community" \
      cicd.build.date="${BUILD_DATE}" \
      cicd.build.number="${BUILD_NUMBER}" \
      cicd.build.url="${BUILD_URL}" \
      cicd.build.branch="${BUILD_BRANCH}" \
      cicd.build.sha1="${CIRCLE_SHA1}"

WORKDIR /opt/opennms

USER 10001

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-h" ]

### Runtime information and not relevant at build time

ENV OPENNMS_KARAF_SSH_HOST="0.0.0.0" \
    OPENNMS_KARAF_SSH_PORT="8101" \
    JAVA_OPTS="" \
    OPENNMS_TRAPD_PORT=1162

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
