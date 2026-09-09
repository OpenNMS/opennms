<template>
  <div class="card">
    <div class="title-row">
      <div class="title headline3">Recent Events</div>
      <div class="action-buttons-container">
        <OnmsIconButton
          aria-label="Events for this Node"
          tooltip="Events for this Node"
          data-test="events-for-node-button"
          :icon="IconViewDetails"
          @click="onEventsForNodeClick"
        />
        <NodeDownloadDropdown
          :onCsvDownload="onCsvDownload"
          :onJsonDownload="onJsonDownload"
        />
      </div>
    </div>
    <OnmsTable
      lazy
      :value="eventStore.events"
      paginator
      :rows="pageSize"
      :first="first"
      :totalRecords="eventStore.totalCount"
      :rowsPerPageOptions="[5, 10, 20, 50]"
      data-test="events-table"
      @page="onPage"
    >
      <OnmsColumn field="id" header="Id">
        <template #body="{ data }">
          <router-link :to="`/event/${data.id}`">{{ data.id }}</router-link>
        </template>
      </OnmsColumn>
      <OnmsColumn field="createTime" header="Created">
        <template #body="{ data }">
          <span v-date>{{ data.createTime }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn field="severity" header="Severity">
        <template #body="{ data }">
          <OnmsTag :value="data.severity" :severity="severityMap[data.severity?.toLowerCase()] ?? 'secondary'" />
        </template>
      </OnmsColumn>
      <OnmsColumn field="logMessage" header="Message">
        <template #body="{ data }">
          <span v-html="data.logMessage" class="log-message" />
        </template>
      </OnmsColumn>
      <template #empty>
        <EmptyList :content="emptyListContent" data-test="empty-list" />
      </template>
    </OnmsTable>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsIconButton, OnmsTable, OnmsTag, type OnmsTablePageEvent, type OnmsTagSeverity } from '@opennms/onms-ui'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import NodeDownloadDropdown from './NodeDownloadDropdown.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useEventStore } from '@/stores/eventStore'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeExport } from './hooks/useNodeExport'
import { Event, ServiceType } from '@/types'

// The legacy event list page, which accepts a node filter.
// Will need to replace with the Vue page once it's implemented.
const EVENT_LIST_PATH = 'event/list'

const eventStore = useEventStore()
const menuStore = useMenuStore()
const route = useRoute()

const { showSnackBar } = useSnackbar()
const { generateBlob, generateDownload } = useNodeExport()

const nodeId = computed(() => route.params.id)

const pageSize = ref(5)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const severityMap: Record<string, OnmsTagSeverity> = {
  critical: 'danger',
  major: 'danger',
  minor: 'warn',
  warning: 'warn',
  normal: 'success',
  cleared: 'success',
  indeterminate: 'secondary'
}

const queryParameters = ref({
  limit: 5,
  offset: 0,
  _s: `node.id==${route.params.id}`
})

const onEventsForNodeClick = () => {
  window.location.assign(`${menuStore.mainMenu.baseHref}${EVENT_LIST_PATH}?filter=node%3D${nodeId.value}`)
}

// The CSV columns are whatever fields the events actually carry, in the order the API returns
// them, rather than the four the table displays. Taken from the data and not from the Event type,
// which does not declare every field the API sends (serviceType among them).
const getCsvColumns = (events: Record<string, unknown>[]) => {
  const columns: string[] = []

  for (const event of events) {
    for (const field of Object.keys(event)) {
      if (!columns.includes(field)) {
        columns.push(field)
      }
    }
  }

  return columns
}

// serviceType arrives as an object; its name is the part worth a CSV cell. Anything else
// non-primitive keeps its JSON form rather than stringifying to '[object Object]'.
const flattenCsvValue = (field: string, value: unknown) => {
  if (value === null || value === undefined) {
    return ''
  }

  if (field === 'serviceType') {
    return (value as ServiceType).name ?? ''
  }

  return typeof value === 'object' ? JSON.stringify(value) : String(value)
}

// Quote a field only when it needs it, doubling any embedded quote, per RFC 4180. Event log
// messages carry markup and commas, so unquoted values would break the row apart.
const toCsvValue = (value: string) =>
  /[",\r\n]/.test(value) ? `"${value.replace(/"/g, '""')}"` : value

const buildCsv = (events: Event[]) => {
  // Read the records as plain field bags: the export covers whatever the API sent, including
  // the fields Event does not declare.
  const rows = events as unknown as Record<string, unknown>[]
  const columns = getCsvColumns(rows)

  return [
    columns.join(','),
    ...rows.map(row => columns.map(field => toCsvValue(flattenCsvValue(field, row[field]))).join(','))
  ].join('\n')
}

const buildJson = (events: Event[]) => JSON.stringify(events, null, 2)

const onDownload = async (format: 'csv' | 'json') => {
  // The paginator's current page: its limit/offset are already tracked in queryParameters, so
  // raising rows-per-page is how a user downloads more than the default 5.
  const events = await eventStore.getEventsForExport(queryParameters.value)

  if (!events || events.length === 0) {
    showSnackBar({
      msg: `No events found for '${format}' download for this node`,
      error: true
    })

    return
  }

  const contentType = format === 'json' ? 'application/json' : 'text/csv'
  const data = format === 'json' ? buildJson(events) : buildCsv(events)

  generateDownload(generateBlob(data, contentType), `Events.${format}`)
}

const onCsvDownload = async () => {
  return onDownload('csv')
}

const onJsonDownload = async () => {
  return onDownload('json')
}

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
  pageSize.value = event.rows
  queryParameters.value = {
    ...queryParameters.value,
    offset: event.first,
    limit: event.rows,
    _s: `node.id==${route.params.id}`
  }
  eventStore.getEvents(queryParameters.value)
}

onMounted(() => {
  eventStore.getEvents(queryParameters.value)
})

defineExpose({ onPage })
</script>

<style lang="scss" scoped>
.card {
  background: var(--p-content-background);
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.08);
  padding: 15px;
  margin-bottom: 15px;

  .title-row {
    display: flex;
    align-items: center;
    gap: 5px;
  }

  .action-buttons-container {
    display: flex;
    flex: 1;
    align-items: center;
    justify-content: flex-end;
    gap: 0.5rem;
  }
}

.log-message {
  p {
    margin: 0;
  }
}
</style>
