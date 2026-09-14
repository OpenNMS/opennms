<template>
  <OnmsTable
    lazy
    :value="nodeStore.ipInterfaces"
    paginator
    :rows="pageSize"
    :first="first"
    :totalRecords="nodeStore.ipInterfacesTotalCount"
    :rowsPerPageOptions="[5, 10, 20, 50]"
    data-test="ip-interfaces-table"
    @page="onPage"
  >
    <OnmsColumn field="ipAddress" header="IP Address" />
    <OnmsColumn field="hostName" header="IP Host Name">
      <template #body="{ data }">{{ data.hostName || 'N/A' }}</template>
    </OnmsColumn>
    <OnmsColumn field="ifIndex" header="SNMP ifIndex">
      <template #body="{ data }">{{ data.ifIndex || 'N/A' }}</template>
    </OnmsColumn>
    <OnmsColumn field="isManaged" header="Managed">
      <template #body="{ data }">{{ data.isManaged || 'N/A' }}</template>
    </OnmsColumn>
    <template #empty>
      <EmptyList :content="emptyListContent" data-test="empty-list" />
    </template>
  </OnmsTable>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsTable, type OnmsTablePageEvent } from '@opennms/onms-ui'
import EmptyList from '@/components/Common/EmptyList.vue'
import { useNodeStore } from '@/stores/nodeStore'

const nodeStore = useNodeStore()
const route = useRoute()

const pageSize = ref(5)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const queryParameters = ref({
  limit: 5,
  offset: 0,
  _s: 'isManaged==U,isManaged==P,isManaged==N,isManaged==M'
})

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
  pageSize.value = event.rows
  queryParameters.value = { ...queryParameters.value, offset: event.first, limit: event.rows }
  nodeStore.getNodeIpInterfaces({ id: route.params.id as string, queryParameters: queryParameters.value })
}

onMounted(() => {
  nodeStore.getNodeIpInterfaces({ id: route.params.id as string, queryParameters: queryParameters.value })
})

defineExpose({ onPage })
</script>
