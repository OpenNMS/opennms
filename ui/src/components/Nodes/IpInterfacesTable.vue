<template>
  <DataTable
    :value="ipInterfaces"
    showGridlines
    data-key="id"
    :loading="loading"
    responsiveLayout="scroll"
    @sort="sort"
    :lazy="true"
  >
    <template #empty>There are no IP interfaces for this node</template>

    <template #loading>Loading data. Please wait.</template>

    <template #footer>
      <Pagination
        :payload="payload"
        :parameters="queryParameters"
        @update-query-parameters="updateQueryParameters"
        moduleName="nodesModule"
        functionName="getNodeIpInterfaces"
        totalCountStateName="ipInterfacesTotalCount"
      />
    </template>

    <Column field="ipAddress" header="IP Address" :sortable="true">
      <template #body="{ data }">{{ data.ipAddress }}</template>
    </Column>

    <Column field="hostName" header="IP Host Name">
      <template #body="{ data }">{{ data.hostName || 'N/A' }}</template>
    </Column>

    <Column field="ifIndex" header="SNMP ifIndex">
      <template #body="{ data }">{{ data.ifIndex || 'N/A' }}</template>
    </Column>

    <Column field="managed" header="Managed">
      <template #body="{ data }">{{ data.isManaged }}</template>
    </Column>
  </DataTable>
</template>
  
<script setup lang="ts">
import { ref, computed } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Pagination from './Pagination.vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'
import useQueryParameters from '../../hooks/useQueryParams'

const store = useStore()
const route = useRoute()
const optionalPayload = { id: route.params.id }
const { queryParameters, updateQueryParameters, sort, payload } = useQueryParameters({
  limit: 5,
  offset: 0,
  _s: 'isManaged==U,isManaged==P,isManaged==N,isManaged==M'
}, 'nodesModule/getNodeIpInterfaces', optionalPayload)
const loading = ref(false)
const ipInterfaces = computed(() => store.state.nodesModule.ipInterfaces)
</script>
  