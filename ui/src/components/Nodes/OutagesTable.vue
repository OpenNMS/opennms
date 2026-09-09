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
      :value="nodeStore.outages"
      paginator
      :rows="pageSize"
      :first="first"
      :totalRecords="nodeStore.outagesTotalCount"
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
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsIconButton, OnmsTable, OnmsTag, type OnmsTablePageEvent } from '@opennms/onms-ui'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import NodeDownloadDropdown from './NodeDownloadDropdown.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeListStore } from '@/stores/nodeListStore'
import { useNodeStore } from '@/stores/nodeStore'
import { useRecordDownload } from './hooks/useRecordDownload'
import { Outage } from '@/types'

// The legacy outage pages, which accept node/interface/service parameters.
// Will need to replace with the Vue pages once they are implemented.
const OUTAGE_LIST_PATH = 'outage/list.htm'

// outtype=both makes the legacy list show current AND resolved outages, matching this panel.
const OUTAGE_LIST_TYPE = 'both'
const OUTAGE_DETAIL_PATH = 'outage/detail.htm'
const INTERFACE_PATH = 'element/interface.jsp'
const SERVICE_PATH = 'element/service.jsp'

const menuStore = useMenuStore()
const nodeListStore = useNodeListStore()
const nodeStore = useNodeStore()
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

const onViewOutagesClick = () => {
  window.location.assign(
    `${baseHref.value}${OUTAGE_LIST_PATH}?filter=node%3D${nodeId.value}&outtype=${OUTAGE_LIST_TYPE}`)
}

const outageDetailLink = (outage: Outage) => `${baseHref.value}${OUTAGE_DETAIL_PATH}?id=${outage.id}`

// Both pages take the interface by address rather than by id, so the address is encoded (an
// IPv6 address is full of reserved characters).
const nodeAndInterfaceQuery = (outage: Outage) =>
  `?node=${nodeId.value}&intf=${encodeURIComponent(outage.ipAddress)}`

const interfaceLink = (outage: Outage) =>
  `${baseHref.value}${INTERFACE_PATH}${nodeAndInterfaceQuery(outage)}`

const serviceLink = (outage: Outage) =>
  `${baseHref.value}${SERVICE_PATH}${nodeAndInterfaceQuery(outage)}&service=${outage.serviceId}`

// An outage carries only its serviceId, so the display name comes from the service types the
// app loaded at startup. They arrive asynchronously, hence a computed read rather than a
// snapshot: the name fills in once they land.
const serviceName = (outage: Outage) => nodeListStore.getServiceTypeById(outage.serviceId)?.name

// Still down: the service was lost and has not come back, so the lost time is called out.
const isUnresolved = (outage: Outage) => !!outage.ifLostService && !outage.ifRegainedService

const onDownload = async (format: 'csv' | 'json') => {
  // The paginator's current page: its limit/offset are already tracked in queryParameters, so
  // raising rows-per-page is how a user downloads more than the default page.
  const outages = await nodeStore.getNodeOutagesForExport({
    id: nodeId.value,
    queryParameters: queryParameters.value
  })

  if (!outages || outages.length === 0) {
    showSnackBar({
      msg: `No outages found for '${format}' download for this node`,
      error: true
    })

    return
  }

  downloadRecords(outages, 'Outages', format)
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
  queryParameters.value = { ...queryParameters.value, offset: event.first, limit: event.rows }
  nodeStore.getNodeOutages({ id: route.params.id as string, queryParameters: queryParameters.value })
}

onMounted(() => {
  nodeStore.getNodeOutages({ id: route.params.id as string, queryParameters: queryParameters.value })
})

defineExpose({ onPage })
</script>
