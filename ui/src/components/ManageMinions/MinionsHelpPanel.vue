<template>
  <TogglePanel
    :collapsed="collapsed"
    class="minions-help-panel"
    data-test="minions-help-panel"
    @update:collapsed="(value: boolean) => (collapsed = value)"
  >
    <template #header>
      <span class="panel-header">
        <i class="pi pi-question-circle" aria-hidden="true" />
        About Minions
      </span>
    </template>
    <div class="help-columns">
      <div class="help-section">
        <div class="section-title">What Minions are</div>
        <p>
          A Minion is a lightweight remote process that performs polling, data
          collection and flow/trap reception on behalf of the main OpenNMS
          system from a monitoring location. Each Minion registers itself and
          reports its <strong>status</strong>, <strong>version</strong> and last
          check-in time; those, along with its type, are maintained by the
          system and shown here read-only.
        </p>
      </div>
      <div class="help-section">
        <div class="section-title">How to use this page</div>
        <p>
          <strong>Edit</strong> changes a Minion's label, its
          <strong>location</strong>, and its key/value <strong>properties</strong>.
          <strong>Delete</strong> removes a Minion and its auto-created
          requisition node; a Minion whose process is still running will
          re-register on its next check-in. The same operations are available to
          tooling through the <code>/api/v2/minions</code> REST API.
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
