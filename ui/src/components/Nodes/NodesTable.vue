<template>
  <DataTable
    :value="nodes"
    showGridlines
    data-key="id"
    :loading="loading"
    responsiveLayout="scroll"
    @sort="sort"
    :lazy="true"
    class="nodes-table"
  >
    <!-- Search -->
    <template #header>
      <div class="flex-container space-between">
        <div>
          <h2 style="line-height: 0.7;">Nodes</h2>
        </div>
        <div>
          <span class="p-input-icon-left top-10">
            <i class="pi pi-search" />
            <InputText @input="searchFilterHandler" placeholder="Search node label" />
          </span>
        </div>
      </div>
    </template>

    <template #empty>No data found.</template>

    <template #loading>Loading data. Please wait.</template>

    <template #footer>
      <Pagination
        :parameters="queryParameters"
        @update-query-parameters="updateQueryParameters"
        moduleName="nodesModule"
        functionName="getNodes"
        totalCountStateName="totalCount"
      />
    </template>

    <Column field="label" header="Label" style="min-width:12rem" :sortable="true">
      <template #body="{ data }">
        <router-link :to="`/node/${data.id}`">{{ data.label }}</router-link>
      </template>
    </Column>

    <Column field="location" header="Location" style="min-width:12rem" :sortable="true">
      <template #body="{ data }">{{ data.location }}</template>
    </Column>

    <Column field="foreignSource" header="Foreign Source" style="min-width:12rem" :sortable="true">
      <template #body="{ data }">{{ data.foreignSource }}</template>
    </Column>

    <Column field="foreignId" header="Foreign Id" style="min-width:12rem" :sortable="true">
      <template #body="{ data }">{{ data.foreignId }}</template>
    </Column>
  </DataTable>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import Column from 'primevue/column'
import Pagination from './Pagination.vue'
import { useStore } from 'vuex'
import { QueryParameters } from '../../types'
import useQueryParameters from '../../hooks/useQueryParams'

const store = useStore()
const loading = ref(false)
const { queryParameters, updateQueryParameters, sort } = useQueryParameters({
  limit: 5,
  offset: 0,
  orderBy: 'label'
}, 'nodesModule/getNodes')
const searchFilterHandler = (e: any) => {
  const searchQueryParam: QueryParameters = { _s: `node.label==${e.target.value}*` }
  const updatedParams = { ...queryParameters.value, ...searchQueryParam }
  store.dispatch('nodesModule/getNodes', updatedParams)
  queryParameters.value = updatedParams
}
const nodes = computed(() => store.state.nodesModule.nodes)
</script>

<style lang="scss" scoped>
.nodes-table :deep(.p-datatable-header) {
  padding-top: 0px;
  padding-bottom: 0px;
  height: 60px;
}
</style>