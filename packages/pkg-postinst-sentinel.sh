#!/usr/bin/env bash

if ! getent group sentinel >/dev/null 2>&1; then
  groupadd --system sentinel
fi

if ! getent passwd sentinel >/dev/null 2>&1; then
  useradd --system \
          --home-dir /opt/sentinel \
          --no-create-home \
          --shell /usr/sbin/nologin \
          --gid sentinel \
          --no-user-group \
          sentinel
fi

ln -s /var/lib/sentinel/deploy /opt/sentinel/deploy
ln -s /var/log/sentinel /opt/sentinel/data/log
chown -R sentinel:sentinel /opt/sentinel
chown -R sentinel:sentinel /var/lib/sentinel
chown -R sentinel:sentinel /var/log/sentinel
systemctl daemon-reload
