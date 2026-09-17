<template>
  <TogglePanel
    :collapsed="collapsed"
    class="locations-help-panel"
    data-test="locations-help-panel"
    @update:collapsed="(value: boolean) => (collapsed = value)"
  >
    <template #header>
      <span class="panel-header">
        <i class="pi pi-question-circle" aria-hidden="true" />
        About Monitoring Locations
      </span>
    </template>
    <div class="help-columns">
      <div class="help-section">
        <div class="section-title">What monitoring locations are for</div>
        <p>
          A monitoring location groups the OpenNMS main system and its Minions
          into a monitoring area, so polling and data collection can run from a
          specific place. Nodes and Minions reference a location by its
          <strong>name</strong>; the optional geolocation, latitude and
          longitude place it on the geographic map, and the
          <strong>priority</strong> orders locations (lower first).
        </p>
      </div>
      <div class="help-section">
        <div class="section-title">How to use this page</div>
        <p>
          <strong>Add New Location</strong> creates a location; <strong>Edit</strong>
          changes its area, geolocation, coordinates and priority. The name is the
          identifier and cannot be changed — recreate the location to rename it.
          Deleting a location that nodes or Minions still reference leaves them
          without a valid location, so reassign them first. The same operations
          are available to tooling through the <code>/api/v2/monitoringLocations</code>
          REST API.
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
