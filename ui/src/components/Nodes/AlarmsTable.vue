<template>
  <NodeDetailsPanel title="Recent Alarms">
    <template #actions>
      <OnmsIconButton
        aria-label="View Alarms for this Node"
        tooltip="View Alarms for this Node"
        data-test="view-alarms-button"
        :icon="IconViewDetails"
        @click="onViewAlarmsClick"
      />
      <NodeDownloadDropdown
        :onCsvDownload="onCsvDownload"
        :onJsonDownload="onJsonDownload"
      />
    </template>
    <OnmsTable
      lazy
      :value="alarms"
      paginator
      :rows="PAGE_SIZE"
      :first="first"
      :totalRecords="totalRecords"
      dataKey="id"
      data-test="alarms-table"
      @page="onPage"
    >
      <OnmsColumn field="id" header="ID">
        <template #body="{ data }">
          <a :href="alarmDetailLink(baseHref, data.id)">{{ data.id }}</a>
        </template>
      </OnmsColumn>
      <OnmsColumn field="severity" header="Severity">
        <template #body="{ data }">
          <OnmsTag :value="data.severity" :severity="severityTag(data.severity)" />
        </template>
      </OnmsColumn>
      <OnmsColumn field="lastEventTime" header="Last Event Time">
        <template #body="{ data }">
          <div
            v-if="data.lastEventTime"
            class="event-time"
            data-test="last-event-time"
          >
            <span>{{ dateAndTime(data.lastEventTime).date }}</span>
            <span>{{ dateAndTime(data.lastEventTime).time }}</span>
          </div>
        </template>
      </OnmsColumn>
      <OnmsColumn field="logMessage" header="Log Message">
        <template #body="{ data }">
          <!-- Two lines at most, ellipsized; the whole message, as text, on hover. -->
          <div
            v-onms-tooltip.top="{ value: htmlToText(data.logMessage) || undefined, class: 'alarm-message-tooltip' }"
            v-html="sanitizeHtml(data.logMessage)"
            class="log-message"
            data-test="log-message"
          />
        </template>
      </OnmsColumn>
      <template #empty>
        <EmptyList :content="emptyListContent" data-test="empty-list" />
      </template>
    </OnmsTable>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { OnmsColumn, OnmsIconButton, OnmsTable, OnmsTag } from '@opennms/onms-ui'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import useSnackbar from '@/composables/useSnackbar'
import useVisiblePolling from '@/composables/useVisiblePolling'
import { formatDateAndTimeInDisplayZone } from '@/lib/displayTimeZone'
import { alarmDetailLink, nodeAlarmListLink } from '@/lib/linkUtils'
import { sanitizeHtml } from '@/lib/sanitizeHtml'
import { htmlToText } from '@/lib/utils'
import API from '@/services'
import { withNodeFilter } from '@/services/serviceHelpers'
import { useAlarmStore } from '@/stores/alarmStore'
import { useMenuStore } from '@/stores/menuStore'
import { Alarm, SORT } from '@/types'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import NodeDownloadDropdown from './NodeDownloadDropdown.vue'
import { useRecordDownload } from './hooks/useRecordDownload'
import { severityTag } from './utils'
import useActiveNodeId from './hooks/useActiveNodeId'
import useNodeTablePaging from './hooks/useNodeTablePaging'

// The node's alarms, most recent first, a few at a time to fit the Main tab's column. Each page is
// fetched from the server -- the status banner above has its own summary -- and refreshed on the
// banner's cycle, since alarms come and go while the page is open.
const PAGE_SIZE = 5
const POLL_INTERVAL_MS = 60_000

const alarmStore = useAlarmStore()
const menuStore = useMenuStore()

const { showSnackBar } = useSnackbar()
const { downloadRecords } = useRecordDownload()

const nodeId = useActiveNodeId()
const baseHref = computed(() => menuStore.mainMenu.baseHref)

// The time on its own line under the date, so the column stays narrow.
const dateAndTime = (value: number) => formatDateAndTimeInDisplayZone(value)

// The slice may still be the previous node's while this one's is in flight.
const isThisNode = computed<boolean>(() => alarmStore.nodeAlarmsNodeId === nodeId.value)

const alarms = computed<Alarm[]>(() => (isThisNode.value ? alarmStore.nodeAlarms : []))
const totalRecords = computed<number>(() => (isThisNode.value ? alarmStore.nodeAlarmsTotalCount : 0))

// With no alarms on hand for this node, say why: still loading, failed to load, or truly none.
const emptyListContent = computed(() => {
  if (isThisNode.value) {
    return { msg: 'No alarms for this node.' }
  }

  return alarmStore.nodeAlarmsFailedNodeId === nodeId.value
    ? { msg: 'Unable to load alarms for this node.' }
    : { msg: 'Loading alarms…' }
})

const { first, fetchPage, onPage, firstPage } = useNodeTablePaging({
  pageSize: PAGE_SIZE,
  load: page => alarmStore.getNodeAlarms(nodeId.value, {
    ...page,
    orderBy: 'lastEventTime',
    order: SORT.DESCENDING
  }),
  shownPage: () => (isThisNode.value ? alarmStore.nodeAlarmsPage : undefined),
  total: () => totalRecords.value
})

useVisiblePolling(fetchPage, POLL_INTERVAL_MS)

watch(nodeId, firstPage, { immediate: true })

const onViewAlarmsClick = () => {
  window.location.assign(nodeAlarmListLink(baseHref.value, nodeId.value))
}

// Every alarm the node has, not just the page. Fetched only when asked for, and kept out of the
// store, so the table's page is left alone.
const onDownload = async (format: 'csv' | 'json') => {
  const resp = await API.getAlarms(withNodeFilter(nodeId.value, {
    limit: 0,
    orderBy: 'lastEventTime',
    order: SORT.DESCENDING
  }))

  if (!resp) {
    showSnackBar({
      msg: `Unable to load alarms for '${format}' download for this node`,
      error: true
    })

    return
  }

  if (resp.alarm.length === 0) {
    showSnackBar({
      msg: `No alarms found for '${format}' download for this node`,
      error: true
    })

    return
  }

  downloadRecords(resp.alarm, 'Alarms', format)
}

const onCsvDownload = () => {
  onDownload('csv')
}

const onJsonDownload = () => {
  onDownload('json')
}

defineExpose({ onPage, fetchPage })
</script>

<style lang="scss" scoped>
.event-time {
  display: flex;
  flex-direction: column;
  white-space: nowrap;
}

// Clamped to two lines with an ellipsis; the tooltip carries the rest.
.log-message {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  overflow: hidden;

  :deep(p) {
    display: inline;
    margin: 0;
  }
}
</style>

<style lang="scss">
// The tooltip teleports to <body>, so this cannot be scoped. Wider than the default, so a long log
// message reads as a few lines rather than a tall, narrow column over the rows above.
.alarm-message-tooltip {
  max-width: 30rem;
}
</style>
