<template>
  <DataTable
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
    <Column field="ipAddress" header="IP Address" />
    <Column field="hostname" header="Host Name" />
    <Column field="serviceName" header="Service Name" />
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

const pageSize = ref(10)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const queryParameters = ref({
  limit: 10,
  offset: 0
})

const onPage = (event: DataTablePageEvent) => {
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
