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
      :value="events"
      paginator
      :rows="pageSize"
      :first="first"
      :totalRecords="totalRecords"
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
          <OnmsTag :value="data.severity" :severity="severityTag(data.severity)" />
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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsIconButton, OnmsTable, OnmsTag, type OnmsTablePageEvent } from '@opennms/onms-ui'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import NodeDownloadDropdown from './NodeDownloadDropdown.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useEventStore } from '@/stores/eventStore'
import { useMenuStore } from '@/stores/menuStore'
import { useRecordDownload } from './hooks/useRecordDownload'
import { severityTag } from './utils'

// The legacy event list page, which accepts a node filter.
// Will need to replace with the Vue page once it's implemented.
const EVENT_LIST_PATH = 'event/list'

const eventStore = useEventStore()
const menuStore = useMenuStore()
const route = useRoute()

const { showSnackBar } = useSnackbar()
const { downloadRecords } = useRecordDownload()

const nodeId = computed(() => route.params.id as string)

const DEFAULT_PAGE_SIZE = 5

const pageSize = ref(DEFAULT_PAGE_SIZE)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const queryParameters = ref({
  limit: DEFAULT_PAGE_SIZE,
  offset: 0
})

// The store's node slice is only replaced on a successful fetch, so it can still hold the
// previous node's events -- after a failed fetch, or while this node's is in flight. Show it
// only when it is this node's.
const isThisNode = computed<boolean>(() => eventStore.nodeEventsNodeId === nodeId.value)
const events = computed(() => (isThisNode.value ? eventStore.nodeEvents : []))
const totalRecords = computed<number>(() => (isThisNode.value ? eventStore.nodeEventsTotalCount : 0))

const fetchEvents = () => {
  eventStore.getNodeEvents(nodeId.value, queryParameters.value)
}

const onEventsForNodeClick = () => {
  window.location.assign(`${menuStore.mainMenu.baseHref}${EVENT_LIST_PATH}?filter=node%3D${nodeId.value}`)
}

// The download is the page the paginator is showing, which the store already holds -- no
// second request, and no way for the file to disagree with the table. Raising rows-per-page is
// how a user takes more than the default page.
const onDownload = (format: 'csv' | 'json') => {
  if (events.value.length === 0) {
    showSnackBar({
      msg: `No events found for '${format}' download for this node`,
      error: true
    })

    return
  }

  downloadRecords(events.value, 'Events', format)
}

const onCsvDownload = () => {
  onDownload('csv')
}

const onJsonDownload = () => {
  onDownload('json')
}

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
  pageSize.value = event.rows
  queryParameters.value = {
    ...queryParameters.value,
    offset: event.first,
    limit: event.rows
  }
  fetchEvents()
}

onMounted(fetchEvents)

// The details page keeps one instance of this panel across node ids, so the id has to be
// followed rather than read once: otherwise the table, its "Events for this Node" link and its
// downloads would disagree about which node they are for. Back to the first page, since the
// page the user was on says nothing about the new node.
watch(nodeId, () => {
  first.value = 0
  queryParameters.value = {
    ...queryParameters.value,
    offset: 0
  }
  fetchEvents()
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
