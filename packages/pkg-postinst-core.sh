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

ln -s /var/log/opennms /opt/opennms/logs
ln -s /var/lib/opennms/rrd /opt/opennms/share/rrd
ln -s /var/lib/opennms/reports /opt/opennms/share/reports
ln -s /etc/opennms /opt/opennms/etc
chown -R opennms:opennms /opt/opennms
chown -R opennms:opennms /var/log/opennms
chown -R opennms:opennms /var/lib/opennms
chown -R opennms:opennms /etc/opennms
systemctl daemon-reload
