<template>
  <div data-test="daemon-management-about">
    <p>
      Most OpenNMS daemons can re-read their configuration while running. Reloading applies changes made to a
      daemon's configuration files (directly or through the configuration pages) without restarting OpenNMS or
      interrupting the other daemons.
    </p>
    <p>
      A reload request is delivered as the <em>uei.opennms.org/internal/reloadDaemonConfig</em> event and handled
      asynchronously: the daemon answers with a <em>reloadDaemonConfigSuccessful</em> or
      <em>reloadDaemonConfigFailed</em> event, which you can find in the event list. A failed reload leaves the
      previous configuration in effect.
    </p>
    <p>
      Only the daemons listed here support config reload. Starting and stopping daemons, and live daemon status,
      are not yet available from this page.
    </p>
  </div>
</template>

<script setup lang="ts">
</script>
