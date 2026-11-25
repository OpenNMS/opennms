<template>
  <OnmsCard class="activity-card" data-test="activity-card">
    <template #title>
      <div class="card-header">
        <span class="card-title">Activity log</span>
        <div class="header-actions">
          <OnmsButton variant="outlined" label="Refresh" :disabled="refreshing" data-test="refresh-log" @click="refresh" />
          <OnmsButton variant="outlined" label="Download" :disabled="downloading" :loading="downloading" data-test="download-log" @click="download" />
        </div>
      </div>
    </template>
    <template #content>
      <p class="card-note">
        The most recent entries of <code>plugin-management.log</code>, newest first. Download saves the whole file.
      </p>
      <div class="filters">
        <FormField class="filter search-filter">
          <OnmsSearchInput v-model="search" inputId="plugin-log-search" placeholder="Search" ariaLabel="Search the activity log" dataTest="log-search" />
        </FormField>
        <FormField class="filter" label="Plugin" for="plugin-log-kar">
          <OnmsSelect v-model="karFilter" inputId="plugin-log-kar" :options="karOptions" placeholder="All" showClear data-test="log-filter-kar" />
        </FormField>
        <FormField class="filter" label="Action" for="plugin-log-action">
          <OnmsSelect v-model="actionFilter" inputId="plugin-log-action" :options="LOG_ACTIONS" placeholder="All" showClear data-test="log-filter-action" />
        </FormField>
        <FormField class="filter" label="Outcome" for="plugin-log-outcome">
          <OnmsSelect v-model="outcomeFilter" inputId="plugin-log-outcome" :options="LOG_OUTCOMES" placeholder="All" showClear data-test="log-filter-outcome" />
        </FormField>
        <FormField class="filter lines-filter" label="Lines" for="plugin-log-lines">
          <OnmsSelect :modelValue="store.logLines" inputId="plugin-log-lines" :options="LOG_LINE_OPTIONS" data-test="log-lines" @update:modelValue="(value) => changeLines(value as number)" />
        </FormField>
      </div>
      <p v-if="downloadError" class="error" role="alert" data-test="download-error">The log could not be downloaded.</p>
      <p v-if="store.log === null" class="error" data-test="log-error">The log could not be read.</p>
      <p v-else-if="store.log === undefined" class="placeholder" data-test="log-loading">Loading…</p>
      <OnmsTable
        v-else
        :value="filtered"
        dataKey="id"
        paginator
        :rows="25"
        :rowsPerPageOptions="[25, 50, 100]"
        :rowClass="rowClass"
        :unsafePt="tablePt"
        size="small"
        data-test="activity-log"
      >
        <template #empty>
          <span v-if="entries.length === 0" data-test="log-empty">No activity has been logged yet.</span>
          <span v-else data-test="log-no-match">No entries match the filters.</span>
        </template>
        <OnmsColumn header="Time">
          <template #body="{ data }">
            <code v-if="!data.parsed" class="raw-line" data-test="log-raw">{{ data.raw }}</code>
            <span v-else class="time" data-test="log-time">{{ data.time }}</span>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Level">
          <template #body="{ data }">
            <OnmsTag v-if="data.parsed" :severity="levelSeverity(data.level)" :value="data.level" data-test="log-level" />
          </template>
        </OnmsColumn>
        <OnmsColumn header="Action">
          <template #body="{ data }">
            <span v-if="data.parsed" data-test="log-action">{{ data.action }}</span>
          </template>
        </OnmsColumn>
        <OnmsColumn header="User">
          <template #body="{ data }">
            <span v-if="data.parsed" :title="data.remote ? `from ${data.remote}` : undefined" data-test="log-user">{{ data.user || NOT_SET }}</span>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Plugin">
          <template #body="{ data }">
            <span v-if="data.parsed" :title="data.sha256 ? `SHA-256 ${data.sha256}` : undefined" data-test="log-kar">{{ data.kar || NOT_SET }}</span>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Outcome">
          <template #body="{ data }">
            <OnmsTag v-if="data.parsed" :severity="outcomeSeverity(data.outcome)" :value="data.outcome" data-test="log-outcome" />
          </template>
        </OnmsColumn>
        <OnmsColumn header="Detail">
          <template #body="{ data }">
            <span v-if="data.parsed" class="detail" data-test="log-detail">{{ data.detail }}</span>
          </template>
        </OnmsColumn>
      </OnmsTable>
    </template>
  </OnmsCard>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { OnmsButton, OnmsCard, OnmsColumn, OnmsSearchInput, OnmsSelect, OnmsTable, OnmsTag } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import { LOG_LINE_OPTIONS } from '@/services/pluginManagementService'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { NOT_SET } from './pluginDisplay'
import { LOG_ACTIONS, LOG_OUTCOMES, PluginLogEntry, distinctKars, entryMatches, isFailedEntry, levelSeverity, outcomeSeverity, parsePluginLog } from './pluginLog'

const COLUMN_COUNT = 7
const LOG_FILE_NAME = 'plugin-management.log'

const store = usePluginManagementStore()
const refreshing = ref(false)
const downloading = ref(false)
const downloadError = ref(false)
const search = ref('')
const karFilter = ref<string | null>(null)
const actionFilter = ref<string | null>(null)
const outcomeFilter = ref<string | null>(null)

const entries = computed<PluginLogEntry[]>(() => (typeof store.log === 'string' ? parsePluginLog(store.log) : []))
const karOptions = computed(() => distinctKars(entries.value))

const filtered = computed(() => entries.value.filter(entry =>
  entryMatches(entry, search.value)
  && (!karFilter.value || entry.kar === karFilter.value)
  && (!actionFilter.value || entry.action === actionFilter.value)
  && (!outcomeFilter.value || entry.outcome === outcomeFilter.value)))

const rowClass = (entry: PluginLogEntry) => ({ 'log-row--failed': isFailedEntry(entry), 'log-row--raw': !entry.parsed })

// an unparsed line spans the row: its first cell takes every column and the
// others are hidden, since a column template cannot change the cell itself
const tablePt = {
  column: {
    bodyCell: ({ parent, context }: { parent?: { props?: { rowData?: PluginLogEntry }}, context?: { index?: number }}) => {
      const entry = parent?.props?.rowData
      if (!entry || entry.parsed) {
        return undefined
      }
      return context?.index === 0 ? { colspan: COLUMN_COUNT } : { style: { display: 'none' }}
    }
  }
}

const refresh = async () => {
  refreshing.value = true
  try {
    await store.refreshLog()
  } finally {
    refreshing.value = false
  }
}

const changeLines = async (lines: number) => {
  if (!lines || lines === store.logLines) {
    return
  }
  refreshing.value = true
  try {
    await store.setLogLines(lines)
  } finally {
    refreshing.value = false
  }
}

const saveText = (text: string, fileName: string) => {
  const url = URL.createObjectURL(new Blob([text], { type: 'text/plain' }))
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = fileName
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(url)
}

const download = async () => {
  downloading.value = true
  downloadError.value = false
  try {
    const text = await store.downloadLog()
    if (text === null) {
      downloadError.value = true
      return
    }
    saveText(text, LOG_FILE_NAME)
  } finally {
    downloading.value = false
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

.header-actions {
  display: flex;
  gap: 0.5rem;
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

.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 0.75rem 1rem;
  margin-bottom: 1rem;

  .filter {
    min-width: 11rem;
  }

  .search-filter {
    flex: 1 1 18rem;
  }

  .lines-filter {
    min-width: 7rem;
  }
}

.error {
  margin: 0 0 1rem 0;
  color: var(--p-error-color);
}

.placeholder {
  margin: 0;
  color: var(--p-text-muted-color);
}

.time {
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.detail,
.raw-line {
  word-break: break-word;
}

.raw-line {
  font-size: 0.8rem;
}

:deep(.log-row--failed) > td {
  background: color-mix(in srgb, var(--p-error-color) 8%, transparent);
}
</style>
