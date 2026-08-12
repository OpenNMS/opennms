<template>
  <OnmsTable
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
    <OnmsColumn field="ifIndex" header="SNMP ifIndex" />
    <OnmsColumn field="ifDescr" header="SNMP ifDescr">
      <template #body="{ data }">{{ data.ifDescr || 'N/A' }}</template>
    </OnmsColumn>
    <OnmsColumn field="ifName" header="SNMP ifName">
      <template #body="{ data }">{{ data.ifName || 'N/A' }}</template>
    </OnmsColumn>
    <OnmsColumn field="ifAlias" header="SNMP ifAlias">
      <template #body="{ data }">{{ data.ifAlias || 'N/A' }}</template>
    </OnmsColumn>
    <OnmsColumn field="ifSpeed" header="SNMP ifSpeed">
      <template #body="{ data }"><span v-html="data.ifSpeed" /></template>
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
  offset: 0
})

const onPage = (event: OnmsTablePageEvent) => {
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
