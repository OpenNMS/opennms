<template>
  <OnmsCard class="plugins-card" data-test="plugins-card">
    <template #title>
      <span class="card-title">Installed plugins</span>
    </template>
    <template #content>
      <OnmsTable :value="plugins" dataKey="karName" sortField="karName" :sortOrder="1" data-test="plugins-table">
        <template #empty>
          <span data-test="no-plugins">No plugins are loaded.</span>
        </template>
        <OnmsColumn field="karName" header="KAR name" sortable />
        <OnmsColumn header="Features">
          <template #body="{ data }">
            <span :title="featureTitle(data)" :data-diagnostics="Object.keys(data.diagnostics ?? {}).length || undefined" data-test="plugin-features">{{ ellipsify(data.features.join(', '), 60) || NOT_SET }}</span>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Source">
          <template #body="{ data }">
            <OnmsTag
              v-if="sourceOf(data.source).manual"
              severity="info"
              :value="sourceOf(data.source).label"
              :title="sourceOf(data.source).title"
              data-test="plugin-source"
            />
            <span v-else :title="sourceOf(data.source).title" data-test="plugin-source">{{ sourceOf(data.source).label }}</span>
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
            <OnmsTag
              :severity="statusOf(data.status, data.pendingRestart, data.health).severity"
              :value="statusOf(data.status, data.pendingRestart, data.health).label"
              :data-health="data.health"
              :title="statusTitle(data)"
              :data-status="data.status"
              data-test="plugin-status"
            />
          </template>
        </OnmsColumn>
        <OnmsColumn header="Actions" style="text-align: right">
          <template #body="{ data }">
            <div class="action-container">
              <OnmsIconButton
                v-if="data.status !== 'unloaded'"
                :icon="Refresh"
                :disabled="!canRestart(data, containerAvailable)"
                :title="restartTitle(data, containerAvailable)"
                data-test="restart-plugin"
                @click="emit('restart', data)"
              />
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
import Refresh from '@opennms/onms-ui/icons/navigation/Refresh.vue'
import { ellipsify } from '@/lib/utils'
import { PluginEntry } from '@/types/pluginManagement'
import { CONTAINER_UNAVAILABLE, NOT_SET, canRestart, featureTitle, formatSize, formatUploadedAt, restartTitle, sourceOf, statusOf, statusTitle } from './pluginDisplay'

defineProps<{
  plugins: PluginEntry[]
  containerAvailable: boolean
}>()

const emit = defineEmits<{
  (e: 'unload', plugin: PluginEntry): void
  (e: 'restart', plugin: PluginEntry): void
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

.uploaded-cell {
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
  gap: 0.25rem;
}
</style>
