<template>
  <OnmsCard class="activity-card" data-test="activity-card">
    <template #title>
      <div class="card-header">
        <span class="card-title">Activity log</span>
        <OnmsButton variant="outlined" label="Refresh" :disabled="refreshing" data-test="refresh-log" @click="refresh" />
      </div>
    </template>
    <template #content>
      <p class="card-note">
        The most recent {{ LOG_LINES }} entries of <code>plugin-management.log</code>, newest first.
      </p>
      <p v-if="store.log === null" class="error" data-test="log-error">The log could not be read.</p>
      <p v-else-if="store.log === undefined" class="placeholder" data-test="log-loading">Loading…</p>
      <p v-else-if="store.log === ''" class="placeholder" data-test="log-empty">No activity has been logged yet.</p>
      <pre v-else class="log" data-test="activity-log">{{ store.log }}</pre>
    </template>
  </OnmsCard>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { OnmsButton, OnmsCard } from '@opennms/onms-ui'

import { LOG_LINES } from '@/services/pluginManagementService'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'

const store = usePluginManagementStore()
const refreshing = ref(false)

const refresh = async () => {
  refreshing.value = true
  try {
    await store.refreshLog()
  } finally {
    refreshing.value = false
  }
}
</script>

<style lang="scss" scoped>
.activity-card {
  padding: 25px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.card-note {
  margin: 0 0 1rem 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}

.error {
  margin: 0;
  color: var(--p-red-500, #c62828);
}

.placeholder {
  margin: 0;
  color: var(--p-text-muted-color);
}

.log {
  margin: 0;
  max-height: 24rem;
  overflow: auto;
  padding: 0.75rem;
  border-radius: 4px;
  background: var(--p-content-hover-background, rgba(0, 0, 0, 0.05));
  font-size: 0.8rem;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
