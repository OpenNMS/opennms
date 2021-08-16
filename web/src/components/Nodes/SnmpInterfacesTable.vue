<template>
  <DataTable
    :value="snmpInterfaces"
    showGridlines
    data-key="id"
    :loading="loading"
    responsiveLayout="scroll"
    @sort="sort"
    :lazy="true"
  >
    <template #empty>No data found.</template>

    <template #loading>Loading data. Please wait.</template>

    <template #footer>
      <Pagination
        :payload="payload"
        :parameters="queryParameters"
        @update-query-parameters="updateQueryParameters"
        moduleName="nodesModule"
        functionName="getNodeSnmpInterfaces"
        totalCountStateName="snmpInterfacesTotalCount"
      />
    </template>

    <Column field="ifIndex" header="SNMP ifIndex" :sortable="true">
      <template #body="{ data }">{{ data.ifIndex }}</template>
    </Column>

    <Column field="ifDescr" header="SNMP ifDescr" :sortable="true">
      <template #body="{ data }">{{ data.idDescr || 'N/A' }}</template>
    </Column>

    <Column field="ifName" header="SNMP ifName" :sortable="true">
      <template #body="{ data }">{{ data.ifName || 'N/A' }}</template>
    </Column>

    <Column field="ifAlias" header="SNMP ifAlias" :sortable="true">
      <template #body="{ data }">{{ data.ifAlias || 'N/A' }}</template>
    </Column>

    <Column field="ifSpeed" header="SNMP ifSpeed" :sortable="true">
      <template #body="{ data }">
        <span v-html="data.ifSpeed"></span>
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import Column from 'primevue/column'
import Pagination from './Pagination.vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'
import useQueryParameters from '../../hooks/useQueryParams'
export default defineComponent({
  name: 'SNMP Interfaces Table',
  components: {
    DataTable,
    InputText,
    Column,
    Pagination
  },
  setup() {
    const store = useStore()
    const route = useRoute()
    const loading = ref(false)
    const optionalPayload = { id: route.params.id }
    const { queryParameters, updateQueryParameters, payload, sort } = useQueryParameters({
      limit: 5,
      offset: 0,
    }, 'nodesModule/getNodeSnmpInterfaces', optionalPayload)
    const snmpInterfaces = computed(() => store.state.nodesModule.snmpInterfaces)
    return {
      sort,
      loading,
      payload,
      snmpInterfaces,
      queryParameters,
      updateQueryParameters
    }
  }
})
</script>