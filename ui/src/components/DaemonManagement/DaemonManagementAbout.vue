<template>
  <div data-test="daemon-management-about">
    <p>
      Reloading makes a daemon re-read its configuration files without restarting OpenNMS or interrupting the
      other daemons. This page lists the daemons that handle the reload event.
    </p>
    <p>
      The request is sent as the <em>uei.opennms.org/internal/reloadDaemonConfig</em> event and handled
      asynchronously. The daemon answers with a <em>reloadDaemonConfigSuccessful</em> or
      <em>reloadDaemonConfigFailed</em> event, both visible in the event list. A failed reload leaves the
      previous configuration in effect.
    </p>
    <p>
      Some specialized components — provisioning adapters (addressed as <em>Provisiond.&lt;AdapterName&gt;</em>)
      and correlation engines — also accept targeted reloads but are configured through their own subsystems and
      are not listed here. Starting and stopping daemons, and live daemon status, are not yet available from this
      page.
    </p>
  </div>
</template>

<script setup lang="ts">
</script>
