<template>
  <NodeDetailsPanel title="Recent Events">
    <template #actions>
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
    </template>
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
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsIconButton, OnmsTable, OnmsTag, type OnmsTablePageEvent, type OnmsTagSeverity } from '@opennms/onms-ui'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import NodeDownloadDropdown from './NodeDownloadDropdown.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useEventStore } from '@/stores/eventStore'
import { useMenuStore } from '@/stores/menuStore'
import { useRecordDownload } from './hooks/useRecordDownload'

// The legacy event list page, which accepts a node filter.
// Will need to replace with the Vue page once it's implemented.
const EVENT_LIST_PATH = 'event/list'

const eventStore = useEventStore()
const menuStore = useMenuStore()
const route = useRoute()

const { showSnackBar } = useSnackbar()
const { downloadRecords } = useRecordDownload()

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

  downloadRecords(events, 'Events', format)
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
.log-message {
  p {
    margin: 0;
  }
}
</style>
