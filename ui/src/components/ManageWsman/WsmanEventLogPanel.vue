<template>
  <div class="event-log" data-test="wsman-event-log">
    <p class="intro">
      Windows event logs read over WS-Man by the WsManEventLogd daemon, from <code>wsman-eventlog-configuration.xml</code>.
      A node is read when an interface matching the package filter carries the <em>WS-Man</em> service; each record becomes an event.
      Changes are applied on save without a restart.
    </p>

    <OnmsCard v-for="pkg in config.packages" :key="pkg.name" class="section" :data-test="`package-${pkg.name}`">
      <template #title>
        <div class="card-header">
          <span class="card-title">Package {{ pkg.name }}</span>
          <span class="filter" :title="pkg.filter">Filter: <code>{{ pkg.filter }}</code></span>
        </div>
      </template>
      <template #content>
        <div class="table-header">
          <span class="section-title">Logs ({{ pkg.logs.length }})</span>
          <OnmsButton variant="outlined" label="Add log" icon="pi pi-plus" data-test="add-log" @click="emit('addLog', pkg.name)" />
        </div>
        <OnmsTable :value="pkg.logs" dataKey="name" data-test="logs-table">
          <template #empty><span>No logs are read for this package.</span></template>
          <OnmsColumn header="Enabled" style="width: 6rem">
            <template #body="{ data }">
              <OnmsToggleSwitch
                :modelValue="data.enabled"
                :aria-label="`Enable ${data.name}`"
                data-test="log-enabled"
                @update:modelValue="(value: boolean) => emit('toggleLog', pkg.name, data.name, value)"
              />
            </template>
          </OnmsColumn>
          <OnmsColumn field="name" header="Log" sortable />
          <OnmsColumn header="Frequency">
            <template #body="{ data }">{{ formatInterval(data.interval) }}</template>
          </OnmsColumn>
          <OnmsColumn header="Levels">
            <template #body="{ data }">{{ data.levels || 'All' }}</template>
          </OnmsColumn>
          <OnmsColumn header="Mode">
            <template #body="{ data }"><OnmsTag :value="data.mode === 'shell' ? 'Get-WinEvent' : 'WQL'" severity="secondary" /></template>
          </OnmsColumn>
          <OnmsColumn header="Max records">
            <template #body="{ data }">{{ data.maxRecords }}</template>
          </OnmsColumn>
          <OnmsColumn header="First poll">
            <template #body="{ data }">last {{ data.lookback }}</template>
          </OnmsColumn>
          <OnmsColumn header="Actions" style="text-align: right">
            <template #body="{ data }">
              <div class="action-container">
                <OnmsIconButton :icon="Edit" :title="`Edit ${data.name}`" :aria-label="`Edit ${data.name}`" data-test="edit-log" @click="emit('editLog', pkg.name, data)" />
                <OnmsIconButton :icon="Delete" severity="danger" :title="`Delete ${data.name}`" :aria-label="`Delete ${data.name}`" data-test="delete-log" @click="emit('deleteLog', pkg.name, data)" />
              </div>
            </template>
          </OnmsColumn>
        </OnmsTable>

        <div class="table-header mappings">
          <span class="section-title">Event mappings ({{ pkg.eventMappings.length }})</span>
          <OnmsButton variant="outlined" label="Add mapping" icon="pi pi-plus" data-test="add-mapping" @click="emit('addMapping', pkg.name)" />
        </div>
        <OnmsTable :value="pkg.eventMappings" data-test="mappings-table">
          <template #empty><span>Every record uses the default UEI for its log.</span></template>
          <OnmsColumn header="Event ID" style="width: 7rem">
            <template #body="{ data }">{{ data.eventId }}</template>
          </OnmsColumn>
          <OnmsColumn header="Log">
            <template #body="{ data }">{{ data.logfile || 'Any' }}</template>
          </OnmsColumn>
          <OnmsColumn header="Source">
            <template #body="{ data }">{{ data.source || 'Any' }}</template>
          </OnmsColumn>
          <OnmsColumn field="uei" header="UEI" />
          <OnmsColumn header="Severity">
            <template #body="{ data }">{{ data.severity || 'From the level' }}</template>
          </OnmsColumn>
          <OnmsColumn header="Actions" style="text-align: right">
            <template #body="{ data, index }">
              <div class="action-container">
                <OnmsIconButton :icon="Edit" :title="`Edit mapping for ${data.eventId}`" :aria-label="`Edit mapping for ${data.eventId}`" data-test="edit-mapping" @click="emit('editMapping', pkg.name, index, data)" />
                <OnmsIconButton :icon="Delete" severity="danger" :title="`Delete mapping for ${data.eventId}`" :aria-label="`Delete mapping for ${data.eventId}`" data-test="delete-mapping" @click="emit('deleteMapping', pkg.name, index, data)" />
              </div>
            </template>
          </OnmsColumn>
        </OnmsTable>
      </template>
    </OnmsCard>
  </div>
</template>

<script setup lang="ts">
import { OnmsButton, OnmsCard, OnmsColumn, OnmsIconButton, OnmsTable, OnmsTag, OnmsToggleSwitch } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import { formatInterval } from './wsmanEventLogForm'
import { WsmanEventLogConfig, WsmanEventLogLog, WsmanEventLogMapping } from '@/types/wsmanAdmin'

defineProps<{
  config: WsmanEventLogConfig
}>()

const emit = defineEmits<{
  addLog: [packageName: string]
  editLog: [packageName: string, log: WsmanEventLogLog]
  deleteLog: [packageName: string, log: WsmanEventLogLog]
  toggleLog: [packageName: string, name: string, enabled: boolean]
  addMapping: [packageName: string]
  editMapping: [packageName: string, index: number, mapping: WsmanEventLogMapping]
  deleteMapping: [packageName: string, index: number, mapping: WsmanEventLogMapping]
}>()
</script>

<style lang="scss" scoped>
.event-log {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.intro {
  margin: 0;
  color: var(--p-text-muted-color);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 1rem;
  flex-wrap: wrap;
}

.card-title {
  font-weight: 600;
}

.filter {
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;

  &.mappings {
    margin-top: 1.25rem;
  }
}

.section-title {
  font-weight: 600;
}

.action-container {
  display: flex;
  justify-content: flex-end;
  gap: 0.25rem;
}
</style>
