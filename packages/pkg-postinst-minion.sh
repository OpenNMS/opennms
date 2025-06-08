#!/usr/bin/env bash

if ! getent group minion >/dev/null 2>&1; then
  groupadd --system minion
fi

if ! getent passwd minion >/dev/null 2>&1; then
  useradd --system \
          --home-dir /opt/minion \
          --no-create-home \
          --shell /usr/sbin/nologin \
          --gid minion \
          --no-user-group \
          minion
fi

ln -s /var/lib/minion/deploy /opt/minion/deploy
ln -s /var/log/minion /opt/minion/data/log
chown -R minion:minion /var/lib/minion
chown -R minion:minion /var/log/minion
systemctl daemon-reload
