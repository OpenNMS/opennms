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
          the first matching definition wins; use the Up and Down actions to change the order. Values may be
          metadata placeholders such as <code>${requisition:wsman:username}</code> or
          <code>${scv:alias:password}</code>, resolved per node when collecting. Passwords are shown only as
          set or not set, and saving without entering one keeps the stored password. Changes are written to
          <code>wsman-config.xml</code> and picked up by the daemons without a restart. <strong>Data
          Collection</strong> manages the collections, system definitions and groups the collector merges from
          <code>wsman-datacollection-config.xml</code> and <code>wsman-datacollection.d/</code>; each object is
          saved back to the file it lives in, and rewriting a file drops any XML comments it held. Changes are
          picked up within a few seconds and used from the next collection cycle; only a new collection name
          wired into <code>collectd-configuration.xml</code> needs a Collectd configuration reload.
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
