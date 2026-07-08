<template>
  <DataTable
    lazy
    :value="nodeStore.snmpInterfaces"
    paginator
    :rows="pageSize"
    :first="first"
    :totalRecords="nodeStore.snmpInterfacesTotalCount"
    :rowsPerPageOptions="[5, 10, 20, 50]"
    data-test="snmp-interfaces-table"
    @page="onPage"
  >
    <Column field="ifIndex" header="SNMP ifIndex" />
    <Column field="ifDescr" header="SNMP ifDescr">
      <template #body="{ data }">{{ data.ifDescr || 'N/A' }}</template>
    </Column>
    <Column field="ifName" header="SNMP ifName">
      <template #body="{ data }">{{ data.ifName || 'N/A' }}</template>
    </Column>
    <Column field="ifAlias" header="SNMP ifAlias">
      <template #body="{ data }">{{ data.ifAlias || 'N/A' }}</template>
    </Column>
    <Column field="ifSpeed" header="SNMP ifSpeed">
      <template #body="{ data }"><span v-html="data.ifSpeed" /></template>
    </Column>
    <template #empty>
      <EmptyList :content="emptyListContent" data-test="empty-list" />
    </template>
  </DataTable>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { type DataTablePageEvent } from 'primevue/datatable'
import EmptyList from '@/components/Common/EmptyList.vue'
import { useNodeStore } from '@/stores/nodeStore'

const nodeStore = useNodeStore()
const route = useRoute()

const pageSize = ref(5)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const queryParameters = ref({
  limit: 5,
  offset: 0
})

const onPage = (event: DataTablePageEvent) => {
  first.value = event.first
  pageSize.value = event.rows
  queryParameters.value = { ...queryParameters.value, offset: event.first, limit: event.rows }
  nodeStore.getNodeSnmpInterfaces({ id: route.params.id as string, queryParameters: queryParameters.value })
}

onMounted(() => {
  nodeStore.getNodeSnmpInterfaces({ id: route.params.id as string, queryParameters: queryParameters.value })
})

defineExpose({ onPage })
</script>
