<template>
  <TogglePanel
    :collapsed="collapsed"
    class="wsman-help-panel"
    data-test="wsman-help-panel"
    @update:collapsed="(value: boolean) => (collapsed = value)"
  >
    <template #header>
      <span class="panel-header">
        <i class="pi pi-question-circle" aria-hidden="true" />
        About WS-Man
      </span>
    </template>
    <div class="help-columns">
      <div class="help-section">
        <div class="section-title">What WS-Man is</div>
        <p>
          WS-Management (WS-Man) is a SOAP-based protocol for querying and managing servers and devices,
          used by Windows hosts through WinRM and by hardware such as Dell iDRAC. OpenNMS can detect
          WS-Man agents during provisioning, poll them, and collect performance data from them.
        </p>
      </div>
      <div class="help-section">
        <div class="section-title">What this page shows</div>
        <p>
          <strong>Agent Defaults</strong> are the connection settings from the root of
          <code>wsman-config.xml</code> that apply to every agent. <strong>Server Definitions</strong> name the
          servers to collect from, by IP range, address or IPLIKE pattern, and override those settings for them;
          the first matching definition wins; use the Up and Down actions to change the order. The
          responding / servers column comes from the poller: servers carrying the WS-Man service that the
          definition matches, and how many of them have no open outage right now; a definition matching no
          servers usually means a range or pattern that does not cover them, or servers not yet provisioned.
          A "not polled" count means the servers are provisioned with the WS-Man service but no package in
          <code>poller-configuration.xml</code> includes that service, so the poller never checks them; the
          shipped configuration has none.
          Linking a definition to a <strong>requisition</strong> and pressing Sync provisions its specific
          addresses as nodes with the WS-Man service and adds its ranges as scheduled discovery ranges for that
          requisition; sync only ever adds, and the Requisition column shows how many addresses are provisioned. Values may be
          metadata placeholders such as <code>${requisition:wsman:username}</code> or
          <code>${scv:alias:password}</code>, resolved per node when collecting. Passwords are shown only as
          set or not set, and saving without entering one keeps the stored password. Changes are written to
          <code>wsman-config.xml</code> and picked up by the daemons without a restart. <strong>Data
          Collection</strong> shows the collections, system definitions and groups the collector merges from
          <code>wsman-datacollection-config.xml</code> and <code>wsman-datacollection.d/</code>. Existing
          objects can be edited or removed and are written back to the file they live in (rewriting a file drops
          any XML comments); adding new ones is not offered until these files move into the database. Every
          change is picked up automatically within a few seconds and used from the next poll or collection
          cycle: no restart is needed. Two exceptions: a collection name newly referenced from
          <code>collectd-configuration.xml</code> needs a Collectd configuration reload, and a server only
          gets the WS-Man service through provisioning, so newly matching servers need a requisition import or
          rescan.
        </p>
      </div>
    </div>
  </TogglePanel>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import TogglePanel from '@/components/Common/TogglePanel.vue'

const collapsed = ref(true)
</script>

<style lang="scss" scoped>
.panel-header {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;

  .pi-question-circle {
    color: var(--p-primary-color);
  }
}

.help-columns {
  display: flex;
  gap: 2.5rem;
  flex-wrap: wrap;

  .help-section {
    flex: 1;
    min-width: 320px;
  }
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

p {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  line-height: 1.5;
}
</style>
