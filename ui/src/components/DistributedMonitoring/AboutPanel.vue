<template>
  <TogglePanel
    :collapsed="collapsed"
    class="about-panel"
    data-test="about-panel"
    @update:collapsed="(value: boolean) => (collapsed = value)"
  >
    <template #header>
      <span class="panel-header">
        <i class="pi pi-question-circle" aria-hidden="true" />
        About Minions and Locations
      </span>
    </template>
    <div class="help-columns">
      <div class="help-column">
        <div class="section-heading">Monitoring locations</div>
        <div class="help-section">
          <div class="section-title">Monitoring location</div>
          <p>Where Minions run and where nodes are monitored from. Default means the core server itself.</p>
        </div>
        <div class="help-section">
          <div class="section-title">Created automatically</div>
          <p>A requisition or Minion that uses a new name creates it. Adding one here is rarely needed.</p>
        </div>
        <div class="help-section">
          <div class="section-title">Delete</div>
          <p>Only possible with no nodes or Minions assigned, after typing its name. Its perspective outage history is removed with it.</p>
        </div>
      </div>
      <div class="help-column">
        <div class="section-heading">Minions</div>
        <div class="help-section">
          <div class="section-title">Status</div>
          <p>Up when heartbeats arrive, down when they stop, unknown when the Minion has not been heard from since the core started.</p>
        </div>
        <div class="help-section">
          <div class="section-title">Monitoring location and version</div>
          <p>Reported by the Minion itself. Change the location in its <code>org.opennms.minion.controller</code> config.</p>
        </div>
        <div class="help-section">
          <div class="section-title">Delete</div>
          <p>Meant for decommissioned Minions, so a Minion heard from in the last two minutes cannot be deleted. Its alarms are removed, and a Minion that is still running registers again.</p>
        </div>
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

  .help-column {
    flex: 1;
    min-width: 320px;
  }
}

.section-heading {
  font-size: 1.05rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.help-section + .help-section {
  margin-top: 0.75rem;
}

p {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  line-height: 1.5;
}
</style>
