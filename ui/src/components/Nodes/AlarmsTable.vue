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
      :value="alarms"
      paginator
      :rows="PAGE_SIZE"
      :first="first"
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
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsIconButton, OnmsTable, OnmsTag, type OnmsTablePageEvent } from '@opennms/onms-ui'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import useSnackbar from '@/composables/useSnackbar'
import { formatDateAndTimeInDisplayZone } from '@/lib/displayTimeZone'
import { alarmDetailLink, nodeAlarmListLink } from '@/lib/linkUtils'
import { sanitizeHtml } from '@/lib/sanitizeHtml'
import { htmlToText } from '@/lib/utils'
import { useAlarmStore } from '@/stores/alarmStore'
import { useMenuStore } from '@/stores/menuStore'
import { Alarm } from '@/types'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import NodeDownloadDropdown from './NodeDownloadDropdown.vue'
import { useRecordDownload } from './hooks/useRecordDownload'
import { severityTag } from './utils'

// The node's alarms, most recent first, a few at a time to fit the Main tab's column.
//
// It makes no request of its own: the status banner on the same tab fetches every alarm the node
// has into alarmStore's node slice and refreshes it every minute, so this pages through that slice
// in the browser -- and its counts always agree with the banner's.
const PAGE_SIZE = 5

const alarmStore = useAlarmStore()
const menuStore = useMenuStore()
const route = useRoute()

const { showSnackBar } = useSnackbar()
const { downloadRecords } = useRecordDownload()

const nodeId = computed(() => route.params.id as string)
const baseHref = computed(() => menuStore.mainMenu.baseHref)

// The time on its own line under the date, so the column stays narrow.
const dateAndTime = (value: number) => formatDateAndTimeInDisplayZone(value)

const first = ref(0)
const emptyListContent = { msg: 'No alarms for this node.' }

// The slice may still be the previous node's while this one's is in flight.
const alarms = computed<Alarm[]>(() => {
  if (alarmStore.nodeAlarmsNodeId !== nodeId.value) {
    return []
  }

  return [...alarmStore.nodeAlarms].sort((a, b) => (b.lastEventTime ?? 0) - (a.lastEventTime ?? 0))
})

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
}

// A new node starts on its first page.
watch(nodeId, () => {
  first.value = 0
})

// A refresh can leave fewer alarms than the page shown -- step back to the last page that exists.
watch(() => alarms.value.length, (count) => {
  if (first.value >= count && first.value > 0) {
    first.value = Math.max(0, Math.floor((count - 1) / PAGE_SIZE) * PAGE_SIZE)
  }
})

const onViewAlarmsClick = () => {
  window.location.assign(nodeAlarmListLink(baseHref.value, nodeId.value))
}

// Every alarm the node has, not just the page: they are all on hand already.
const onDownload = (format: 'csv' | 'json') => {
  if (alarms.value.length === 0) {
    showSnackBar({
      msg: `No alarms found for '${format}' download for this node`,
      error: true
    })

    return
  }

  downloadRecords(alarms.value, 'Alarms', format)
}

const onCsvDownload = () => {
  onDownload('csv')
}

const onJsonDownload = () => {
  onDownload('json')
}

defineExpose({ onPage })
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
