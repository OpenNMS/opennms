<template>
  <OnmsCard class="wsman-definitions" data-test="wsman-definitions">
    <template #title>
      <div class="card-header">
        <span class="card-title">Server Definitions</span>
        <OnmsButton label="Add Server Definition" data-test="add-definition" @click="emit('add')" />
      </div>
    </template>
    <template #content>
      <p class="card-note">
        A server definition names the servers to collect from, by IP range, address or IPLIKE pattern,
        and the connection settings to use for them. The first matching definition wins and anything it
        leaves unset comes from the agent defaults. The last column counts the servers carrying the WS-Man
        service that the definition matches, and how many of them the poller currently sees responding.
      </p>
      <OnmsTable
        :value="rows"
        dataKey="index"
        data-test="wsman-definitions-table"
      >
        <template #empty>
          <span data-test="no-definitions">No server definitions. Every server uses the agent defaults.</span>
        </template>
        <OnmsColumn header="Applies to">
          <template #body="{ data }">
            <ul class="match-list" :data-test="`definition-${data.index}-matches`">
              <li v-for="r in data.definition.ranges" :key="`r-${r.begin}-${r.end}`">{{ r.begin }} – {{ r.end }}</li>
              <li v-for="ip in data.definition.specifics" :key="`s-${ip}`">{{ ip }}</li>
              <li v-for="m in data.definition.ipMatches" :key="`m-${m}`">IPLIKE {{ m }}</li>
            </ul>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Endpoint">
          <template #body="{ data }">{{ endpointSummary(data.definition) }}</template>
        </OnmsColumn>
        <OnmsColumn header="Credentials">
          <template #body="{ data }">{{ credentialSummary(data.definition) }}</template>
        </OnmsColumn>
        <OnmsColumn header="Overrides">
          <template #body="{ data }">{{ overrideSummary(data.definition) }}</template>
        </OnmsColumn>
        <OnmsColumn header="Responding / servers">
          <template #body="{ data }"><WsmanStatusCell :bucket="statusFor(data.index)" /></template>
        </OnmsColumn>
        <OnmsColumn header="Actions">
          <template #body="{ data }">
            <div class="action-container">
              <OnmsButton variant="text" label="Up" :disabled="data.index === 0" :title="'Move up (matched earlier)'" data-test="move-up" @click="emit('move', data.index, -1)" />
              <OnmsButton variant="text" label="Down" :disabled="data.index === rows.length - 1" :title="'Move down (matched later)'" data-test="move-down" @click="emit('move', data.index, 1)" />
              <OnmsIconButton :icon="Edit" :title="`Edit definition ${data.index + 1}`" :aria-label="`Edit definition ${data.index + 1}`" data-test="edit-definition" @click="emit('edit', data.index)" />
              <OnmsIconButton :icon="Delete" severity="danger" :title="`Delete definition ${data.index + 1}`" :aria-label="`Delete definition ${data.index + 1}`" data-test="delete-definition" @click="emit('delete', data.index)" />
            </div>
          </template>
        </OnmsColumn>
      </OnmsTable>
    </template>
  </OnmsCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OnmsButton, OnmsCard, OnmsColumn, OnmsIconButton, OnmsTable } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import { WsmanDefinition, WsmanStatus } from '@/types/wsmanAdmin'
import WsmanStatusCell from './WsmanStatusCell.vue'
import { NOT_SET, SETTING_ROWS, formatSetting } from './wsmanDisplay'

const props = defineProps<{
  definitions: WsmanDefinition[]
  status?: WsmanStatus | null
}>()

const emit = defineEmits<{
  (e: 'add'): void
  (e: 'edit', index: number): void
  (e: 'delete', index: number): void
  (e: 'move', index: number, delta: number): void
}>()

const rows = computed(() => props.definitions.map((definition, index) => ({ index, definition })))

const statusFor = (index: number) => props.status?.definitions.find(d => d.index === index) ?? null

const endpointSummary = (d: WsmanDefinition): string => {
  const parts: string[] = []
  if (d.ssl !== null) {
    parts.push(d.ssl ? 'https' : 'http')
  }
  if (d.port !== null) {
    parts.push(`port ${d.port}`)
  }
  if (d.path) {
    parts.push(d.path)
  }
  return parts.length ? parts.join(' · ') : NOT_SET
}

const credentialSummary = (d: WsmanDefinition): string => {
  const parts: string[] = []
  if (d.username) {
    parts.push(d.username)
  }
  parts.push(d.hasPassword ? 'password set' : 'no password')
  if (d.gssAuth) {
    parts.push('GSS')
  }
  return parts.join(' · ')
}

// everything not already shown in the endpoint/credential columns
const OVERRIDE_KEYS = SETTING_ROWS
  .map(r => r.key)
  .filter(k => !['username', 'hasPassword', 'ssl', 'port', 'path', 'gssAuth'].includes(k))

const overrideSummary = (d: WsmanDefinition): string => {
  const parts = OVERRIDE_KEYS
    .filter(k => d[k] !== null && d[k] !== undefined && d[k] !== '')
    .map(k => `${SETTING_ROWS.find(r => r.key === k)?.label}: ${formatSetting(d, k)}`)
  return parts.length ? parts.join(' · ') : NOT_SET
}
</script>

<style lang="scss" scoped>
.wsman-definitions {
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

.action-container {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.card-note {
  margin: 0 0 1rem 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}

.match-list {
  margin: 0;
  padding-left: 1rem;
}
</style>
