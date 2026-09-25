<template>
  <OnmsCard class="plugins-card" data-test="plugins-card">
    <template #title>
      <span class="card-title">Installed plugins</span>
    </template>
    <template #content>
      <OnmsTable :value="plugins" dataKey="karName" sortField="karName" :sortOrder="1" data-test="plugins-table">
        <template #empty>
          <span data-test="no-plugins">No plugins have been loaded through this page.</span>
        </template>
        <OnmsColumn field="karName" header="KAR name" sortable />
        <OnmsColumn header="Features">
          <template #body="{ data }">
            <span :title="data.features.join(', ')" data-test="plugin-features">{{ ellipsify(data.features.join(', '), 60) || NOT_SET }}</span>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Uploaded">
          <template #body="{ data }">
            <div class="uploaded-cell" data-test="plugin-uploaded">
              <span>{{ data.uploadedBy || NOT_SET }}</span>
              <small>{{ formatUploadedAt(data.uploadedAt) }}</small>
            </div>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Size">
          <template #body="{ data }">{{ formatSize(data.size) }}</template>
        </OnmsColumn>
        <OnmsColumn header="Status">
          <template #body="{ data }">
            <div class="status-cell">
              <OnmsTag :severity="statusOf(data.status).severity" :value="statusOf(data.status).label" :title="statusOf(data.status).title" :data-status="data.status" data-test="plugin-status" />
              <small v-if="statusOf(data.status).hint || data.pendingRestart" data-test="plugin-status-hint">{{ statusOf(data.status).hint || 'restart required' }}</small>
            </div>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Actions" style="text-align: right">
          <template #body="{ data }">
            <div class="action-container">
              <OnmsIconButton
                v-if="data.status !== 'unloaded'"
                :icon="Delete"
                severity="danger"
                :disabled="!containerAvailable"
                :title="containerAvailable ? `Unload ${data.karName}` : CONTAINER_UNAVAILABLE"
                data-test="unload-plugin"
                @click="emit('unload', data)"
              />
            </div>
          </template>
        </OnmsColumn>
      </OnmsTable>
    </template>
  </OnmsCard>
</template>

<script setup lang="ts">
import { OnmsCard, OnmsColumn, OnmsIconButton, OnmsTable, OnmsTag } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import { ellipsify } from '@/lib/utils'
import { PluginEntry } from '@/types/pluginManagement'
import { CONTAINER_UNAVAILABLE, NOT_SET, formatSize, formatUploadedAt, statusOf } from './pluginDisplay'

defineProps<{
  plugins: PluginEntry[]
  containerAvailable: boolean
}>()

const emit = defineEmits<{
  (e: 'unload', plugin: PluginEntry): void
}>()
</script>

<style lang="scss" scoped>
.plugins-card {
  padding: 25px;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.uploaded-cell,
.status-cell {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;

  small {
    color: var(--p-text-muted-color);
  }
}

.action-container {
  display: flex;
  justify-content: flex-end;
}
</style>
