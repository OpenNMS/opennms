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
      <OnmsColumn field="outageId" header="ID">
        <template #body="{ data }">
          {{ data.id }}
        </template>
      </OnmsColumn>
      <OnmsColumn field="ipAddress" header="IP Address">
        <template #body="{ data }">
          {{ data.ipAddress || 'N/A' }}
        </template>
      </OnmsColumn>
      <OnmsColumn field="serviceName" header="Service Name">
        <template #body="{ data }">
          {{ data.serviceName || 'N/A' }}
        </template>
      </OnmsColumn>
      <OnmsColumn field="ifLostService" header="Lost">
        <template #body="{ data }">
          {{ data.ifLostService || 'N/A' }}
        </template>
      </OnmsColumn>
      <OnmsColumn field="ifRegainedService" header="Regained">
        <template #body="{ data }">
          {{ data.ifRegainedService || 'N/A' }}
        </template>
      </OnmsColumn>
      <OnmsColumn field="hostname" header="Host Name">
        <template #body="{ data }">
          {{ data.hostname || 'N/A' }}
        </template>
      </OnmsColumn>
      <template #empty>
        <EmptyList :content="emptyListContent" data-test="empty-list" />
      </template>
    </OnmsTable>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsIconButton, OnmsTable, type OnmsTablePageEvent } from '@opennms/onms-ui'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import NodeDownloadDropdown from './NodeDownloadDropdown.vue'
import { useNodeStore } from '@/stores/nodeStore'

const nodeStore = useNodeStore()
const route = useRoute()

const pageSize = ref(10)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const queryParameters = ref({
  limit: 10,
  offset: 0
})

// Placeholders for the next set of work: the outages equivalents of the Recent Events panel's
// "Events for this Node" link and CSV/JSON downloads.
const onViewOutagesClick = () => {
  // TODO: link to the outages list filtered to this node
}

const onCsvDownload = () => {
  // TODO: download this node's outages as CSV
}

const onJsonDownload = () => {
  // TODO: download this node's outages as JSON
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
