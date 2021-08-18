<template>
  <DataTable
    :value="outages"
    showGridlines
    data-key="id"
    :loading="loading"
    responsiveLayout="scroll"
    @sort="sort"
    :lazy="true"
  >
    <template #header>Recent Outages</template>

    <template #empty>There have been no outages on this node in the last 24 hours.</template>

    <template #loading>Loading data. Please wait.</template>

    <template #footer>
      <Pagination
        :payload="payload"
        :parameters="queryParameters"
        @update-query-parameters="updateQueryParameters"
        moduleName="nodesModule"
        functionName="getNodeOutages"
        totalCountStateName="outagesTotalCount"
      />
    </template>

    <Column field="ipAddress" header="IP Address">
      <template #body="{ data }">{{ data.ipAddress }}</template>
    </Column>

    <Column field="hostname" header="Host Name">
      <template #body="{ data }">{{ data.hostname }}</template>
    </Column>

    <Column field="serviceName" header="Service Name">
      <template #body="{ data }">{{ data.serviceName }}</template>
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
import useQueryParameters from '@/hooks/useQueryParams'
const store = useStore()
const route = useRoute()
const payload = { id: route.params.id }
const loading = ref(false)
const { queryParameters, sort, updateQueryParameters } = useQueryParameters({
  limit: 5,
  offset: 0,
}, 'nodesModule/getNodeOutages', payload)
const outages = computed(() => store.state.nodesModule.outages)
</script>