#!/usr/bin/env bash

if ! getent group opennms >/dev/null 2>&1; then
  groupadd --system opennms
fi

if ! getent passwd opennms >/dev/null 2>&1; then
  useradd --system \
          --home-dir /opt/opennms \
          --no-create-home \
          --shell /usr/sbin/nologin \
          --gid opennms \
          --no-user-group \
          opennms
fi

[ ! -e /opt/opennms/logs ] && ln -s /var/log/opennms /opt/opennms/logs
[ ! -e /opt/opennms/share/rrd ] && ln -s /var/lib/opennms/rrd /opt/opennms/share/rrd
[ ! -e /opt/opennms/share/reports ] && ln -s /var/lib/opennms/reports /opt/opennms/share/reports
[ ! -e /opt/opennms/deploy ] && ln -s /var/lib/opennms/deploy /opt/opennms/deploy
chown -R opennms:opennms /opt/opennms
chown -R opennms:opennms /var/log/opennms
chown -R opennms:opennms /var/lib/opennms
systemctl daemon-reload
