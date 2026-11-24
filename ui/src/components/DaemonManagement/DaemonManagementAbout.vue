<template>
  <div data-test="daemon-management-about">
    <p>
      Reloading makes a daemon re-read its configuration without restarting OpenNMS or interrupting the
      other daemons. This page lists the daemons that handle the reload event. What each reload covers is
      described in the table; some daemons also restart their schedules or listeners as part of it.
    </p>
    <p>
      The request is sent as the <em>uei.opennms.org/internal/reloadDaemonConfig</em> event and handled
      asynchronously. Most daemons answer with a <em>reloadDaemonConfigSuccessful</em> or
      <em>reloadDaemonConfigFailed</em> event, both visible in the event list.
      <span data-test="about-not-reported">{{ notReported }} send neither event</span>, and
      <span data-test="about-success-only">{{ successOnly }} reports success even when the reload fails</span>;
      for these, check the daemon's log to confirm the result.
    </p>
    <p>
      Some components that are not daemons also accept targeted reloads and are not listed here: provisioning
      adapters (addressed as <em>Provisiond.&lt;AdapterName&gt;</em>), correlation engines, northbound
      interfaces, and token authentication (<em>TokenAuth</em>). Starting and stopping daemons, and live daemon
      status, are not yet available from this page.
    </p>
  </div>
</template>

<script setup lang="ts">
import { RELOADABLE_DAEMONS, ReloadOutcome } from '@/services/daemonService'

const labelsWith = (outcome: ReloadOutcome): string => {
  const labels = RELOADABLE_DAEMONS.filter(d => d.outcome === outcome).map(d => d.label)
  return labels.length > 1 ? `${labels.slice(0, -1).join(', ')} and ${labels[labels.length - 1]}` : labels.join('')
}

const notReported = labelsWith('none')
const successOnly = labelsWith('successOnly')
</script>
