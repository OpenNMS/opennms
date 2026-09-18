<template>
  <div class="event-log" data-test="wsman-event-log">
    <p class="intro">
      Windows event logs read over WS-Man by the WsManEventLogd daemon, from <code>wsman-eventlog-configuration.xml</code>.
      A node is read when an interface matching the package filter carries the <em>WS-Man</em> service; each record becomes an event.
      Changes are applied on save without a restart.
    </p>

    <div class="toolbar">
      <OnmsButton variant="outlined" label="Add package" icon="pi pi-plus" data-test="add-package" @click="emit('addPackage')" />
      <OnmsButton variant="outlined" label="Refresh status" data-test="refresh-status" @click="emit('refreshStatus')" />
    </div>
    <p v-if="!config.packages.length" class="intro" data-test="no-packages">No packages: no Windows event logs are read. Add a package to start.</p>

    <OnmsCard v-for="pkg in config.packages" :key="pkg.name" class="section" :data-test="`package-${pkg.name}`">
      <template #title>
        <div class="card-header">
          <span class="card-title">Package {{ pkg.name }}</span>
          <span class="filter" :title="pkg.filter">Filter: <code>{{ pkg.filter }}</code></span>
          <span class="action-container">
            <OnmsIconButton :icon="Edit" :title="`Edit package ${pkg.name}`" :aria-label="`Edit package ${pkg.name}`" data-test="edit-package" @click="emit('editPackage', pkg)" />
            <OnmsIconButton :icon="Delete" severity="danger" :title="`Delete package ${pkg.name}`" :aria-label="`Delete package ${pkg.name}`" data-test="delete-package" @click="emit('deletePackage', pkg)" />
          </span>
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
          <OnmsColumn header="Definition">
            <template #body="{ data }">
              <span v-if="definitions === null" data-test="definition-unknown">—</span>
              <span v-else-if="definitions[data.uei]?.exists" data-test="definition-label">{{ definitions[data.uei].label }}</span>
              <OnmsTag v-else value="Missing" severity="warn" data-test="definition-missing" />
            </template>
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

        <div class="table-header mappings">
          <span class="section-title">Read status ({{ statusFor(pkg.name).length }})</span>
          <span v-if="status === null" class="filter" data-test="status-unavailable">Status could not be loaded.</span>
        </div>
        <OnmsTable :value="statusFor(pkg.name)" paginator :rows="10" :rowsPerPageOptions="[10, 25, 50]" sortField="nodeLabel" :sortOrder="1" data-test="status-table">
          <template #empty><span>Nothing read yet: the daemon records a row after its first poll of each node and log.</span></template>
          <OnmsColumn field="nodeLabel" header="Node" sortable>
            <template #body="{ data }"><span :title="data.ipAddress">{{ data.nodeLabel }}</span></template>
          </OnmsColumn>
          <OnmsColumn field="log" header="Log" sortable />
          <OnmsColumn header="State">
            <template #body="{ data }">
              <OnmsTag :value="stateOf(data).label" :severity="stateOf(data).severity" :title="data.lastError ?? ''" data-test="status-state" />
            </template>
          </OnmsColumn>
          <OnmsColumn field="lastSuccess" header="Last read" sortable>
            <template #body="{ data }">{{ formatWhen(data.lastSuccess) }}</template>
          </OnmsColumn>
          <OnmsColumn field="recordsRead" header="Records" sortable />
          <OnmsColumn field="eventsPublished" header="Events" sortable />
          <OnmsColumn header="Cursor">
            <template #body="{ data }">{{ data.cursor ?? '—' }}</template>
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
import { type OnmsTagSeverity } from '@opennms/onms-ui'
import { formatInterval, formatWhen } from './wsmanEventLogForm'
import { WsmanEventLogConfig, WsmanEventLogDefinition, WsmanEventLogLog, WsmanEventLogMapping, WsmanEventLogPackage, WsmanEventLogStatusRow } from '@/types/wsmanAdmin'

const props = defineProps<{
  config: WsmanEventLogConfig
  // null when the status request failed, which must read differently from "nothing yet"
  status: WsmanEventLogStatusRow[] | null
  // the definition behind each mapping UEI; null when the request failed
  definitions: Record<string, WsmanEventLogDefinition> | null
}>()

const statusFor = (packageName: string) => (props.status ?? []).filter(r => r.packageName === packageName)

const stateOf = (row: WsmanEventLogStatusRow): { label: string; severity: OnmsTagSeverity } => {
  if (row.backingOff) {
    return { label: `Backing off (${row.consecutiveFailures} failed)`, severity: 'danger' }
  }
  if (row.consecutiveFailures > 0) {
    return { label: `Failing (${row.consecutiveFailures})`, severity: 'warn' }
  }
  if (row.lastSuccess) {
    return { label: 'OK', severity: 'success' }
  }
  return { label: 'Never read', severity: 'secondary' }
}

const emit = defineEmits<{
  addPackage: []
  editPackage: [pkg: WsmanEventLogPackage]
  deletePackage: [pkg: WsmanEventLogPackage]
  refreshStatus: []
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
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;

  .filter {
    flex: 1;
  }
}

.toolbar {
  display: flex;
  gap: 0.5rem;
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
