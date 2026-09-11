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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsTable, type OnmsTablePageEvent } from '@opennms/onms-ui'
import EmptyList from '@/components/Common/EmptyList.vue'
import { useNodeStore } from '@/stores/nodeStore'

const nodeStore = useNodeStore()
const route = useRoute()

const nodeId = computed(() => route.params.id as string)

const DEFAULT_PAGE_SIZE = 5

const pageSize = ref(DEFAULT_PAGE_SIZE)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const queryParameters = ref({
  limit: DEFAULT_PAGE_SIZE,
  offset: 0
})

const fetchInterfaces = () => {
  nodeStore.getNodeSnmpInterfaces({ id: nodeId.value, queryParameters: queryParameters.value })
}

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
  pageSize.value = event.rows
  queryParameters.value = { ...queryParameters.value, offset: event.first, limit: event.rows }
  fetchInterfaces()
}

onMounted(fetchInterfaces)

// The details page keeps one instance of this table across node ids, so the id has to be
// followed rather than read once, or the tab keeps showing the SNMP interfaces of the node the user
// navigated away from. Back to the first page, since the page the user was on says nothing
// about the new node.
watch(nodeId, () => {
  first.value = 0
  queryParameters.value = { ...queryParameters.value, offset: 0 }
  fetchInterfaces()
})

defineExpose({ onPage })
</script>
