<template>
  <OnmsCard class="wsman-definitions" data-test="wsman-definitions">
    <template #title>
      <span class="card-title">Definitions</span>
    </template>
    <template #content>
      <p class="card-note">
        Each definition applies its settings to the addresses it matches; the first match wins and
        anything it leaves unset comes from the agent defaults.
      </p>
      <OnmsTable
        :value="rows"
        dataKey="index"
        data-test="wsman-definitions-table"
      >
        <template #empty>
          <span data-test="no-definitions">No definitions. Every agent uses the defaults.</span>
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
      </OnmsTable>
    </template>
  </OnmsCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OnmsCard, OnmsColumn, OnmsTable } from '@opennms/onms-ui'
import { WsmanDefinition } from '@/types/wsmanAdmin'
import { NOT_SET, SETTING_ROWS, formatSetting } from './wsmanDisplay'

const props = defineProps<{
  definitions: WsmanDefinition[]
}>()

const rows = computed(() => props.definitions.map((definition, index) => ({ index, definition })))

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

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
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
