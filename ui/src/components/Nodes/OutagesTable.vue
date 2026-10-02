<template>
  <NodeDetailsPanel title="Recent Outages">
    <template #actions>
      <OnmsIconButton
        aria-label="View Outages for this Node"
        tooltip="View Outages for this Node"
        data-test="view-outages-button"
        :icon="IconViewDetails"
        @click="onViewOutagesClick"
      />
      <NodeDownloadDropdown
        :onCsvDownload="onCsvDownload"
        :onJsonDownload="onJsonDownload"
      />
    </template>
    <OnmsTable
      lazy
      :value="outages"
      paginator
      :rows="pageSize"
      :first="first"
      :totalRecords="totalRecords"
      :rowsPerPageOptions="[5, 10, 20, 50]"
      data-test="outages-table"
      @page="onPage"
    >
      <OnmsColumn field="id" header="ID">
        <template #body="{ data }">
          <a :href="outageDetailLink(data)">{{ data.id }}</a>
        </template>
      </OnmsColumn>
      <OnmsColumn field="ipAddress" header="IP Address">
        <template #body="{ data }">
          <a v-if="data.ipAddress" :href="interfaceLink(data)">{{ data.ipAddress }}</a>
          <span v-else>N/A</span>
        </template>
      </OnmsColumn>
      <OnmsColumn field="serviceName" header="Service Name">
        <template #body="{ data }">
          <a v-if="serviceName(data) && data.ipAddress" :href="serviceLink(data)">{{ serviceName(data) }}</a>
          <span v-else>{{ serviceName(data) || 'N/A' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn field="ifLostService" header="Lost">
        <template #body="{ data }">
          <OnmsTag v-if="isUnresolved(data)" severity="danger">
            <span v-date>{{ data.ifLostService }}</span>
          </OnmsTag>
          <span v-else-if="data.ifLostService" v-date>{{ data.ifLostService }}</span>
          <span v-else>N/A</span>
        </template>
      </OnmsColumn>
      <OnmsColumn field="ifRegainedService" header="Regained">
        <template #body="{ data }">
          <span v-if="data.ifRegainedService" v-date>{{ data.ifRegainedService }}</span>
          <span v-else>N/A</span>
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
import { useMenuStore } from '@/stores/menuStore'
import { useOutageStore } from '@/stores/outageStore'
import { useRecordDownload } from './hooks/useRecordDownload'
import {
  OUTAGE_LIST_TYPE_BOTH,
  interfaceLink as buildInterfaceLink,
  nodeOutageListLink,
  outageDetailLink as buildOutageDetailLink,
  serviceLink as buildServiceLink
} from '@/lib/linkUtils'
import { Outage } from '@/types'

const menuStore = useMenuStore()
const outageStore = useOutageStore()
const route = useRoute()

const { showSnackBar } = useSnackbar()
const { downloadRecords } = useRecordDownload()

const nodeId = computed(() => route.params.id as string)
const baseHref = computed(() => menuStore.mainMenu.baseHref)

const DEFAULT_PAGE_SIZE = 5

const pageSize = ref(DEFAULT_PAGE_SIZE)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const queryParameters = ref({
  limit: DEFAULT_PAGE_SIZE,
  offset: 0
})

// The store's node slice is only replaced on a successful fetch, so it can still hold the
// previous node's outages -- after a failed fetch, or while this node's is in flight. Show it
// only when it is this node's.
const isThisNode = computed<boolean>(() => outageStore.nodeOutagesNodeId === nodeId.value)
const outages = computed<Outage[]>(() => (isThisNode.value ? outageStore.nodeOutages : []))
const totalRecords = computed<number>(() => (isThisNode.value ? outageStore.nodeOutagesTotalCount : 0))

// outtype=both so the legacy list shows current AND resolved outages, matching this panel.
const onViewOutagesClick = () => {
  window.location.assign(nodeOutageListLink(baseHref.value, nodeId.value, OUTAGE_LIST_TYPE_BOTH))
}

const outageDetailLink = (outage: Outage) => buildOutageDetailLink(baseHref.value, outage.id)

const interfaceLink = (outage: Outage) =>
  buildInterfaceLink(baseHref.value, nodeId.value, outage.ipAddress)

const serviceLink = (outage: Outage) =>
  buildServiceLink(baseHref.value, nodeId.value, outage.ipAddress, outage.serviceId)

// Every outage carries its service inline, so the name needs no lookup: OnmsMonitoredService
// requires a serviceType, and the outage row nests it.
const serviceName = (outage: Outage) => outage.monitoredService?.serviceType?.name

// Still down: the service was lost and has not come back, so the lost time is called out.
const isUnresolved = (outage: Outage) => !!outage.ifLostService && !outage.ifRegainedService

// The download is the page the paginator is showing, which the store already holds -- no
// second request, and no way for the file to disagree with the table. Raising rows-per-page is
// how a user takes more than the default page.
const onDownload = (format: 'csv' | 'json') => {
  if (outages.value.length === 0) {
    showSnackBar({
      msg: `No outages found for '${format}' download for this node`,
      error: true
    })

    return
  }

  downloadRecords(outages.value, 'Outages', format)
}

const onCsvDownload = () => {
  onDownload('csv')
}

const onJsonDownload = () => {
  onDownload('json')
}

const fetchOutages = () => {
  outageStore.getNodeOutages(nodeId.value, queryParameters.value)
}

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
  pageSize.value = event.rows
  queryParameters.value = { ...queryParameters.value, offset: event.first, limit: event.rows }
  fetchOutages()
}

onMounted(fetchOutages)

// The details page keeps one instance of this panel across node ids, so the id has to be
// followed rather than read once: otherwise the table, its "View Outages for this Node" link
// and its downloads would disagree about which node they are for. Back to the first page,
// since the page the user was on says nothing about the new node.
watch(nodeId, () => {
  first.value = 0
  queryParameters.value = { ...queryParameters.value, offset: 0 }
  fetchOutages()
})

defineExpose({ onPage })
</script>
