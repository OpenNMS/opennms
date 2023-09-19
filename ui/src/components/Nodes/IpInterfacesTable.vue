<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <table
        class="tl1 tl2 tl3"
        summary="IP Interfaces"
      >
        <thead>
          <tr>
            <th scope="col">IP Address</th>
            <th scope="col">IP Host Name</th>
            <th scope="col">SNMP ifIndex</th>
            <th scope="col">Managed</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="ipInterface in ipInterfaces"
            :key="ipInterface.id"
          >
            <td>{{ ipInterface.ipAddress }}</td>
            <td>{{ ipInterface.hostName || 'N/A' }}</td>
            <td>{{ ipInterface.ifIndex || 'N/A' }}</td>
            <td>{{ ipInterface.isManaged || 'N/A' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
  <Pagination
    :payload="payload"
    :parameters="queryParameters"
    @update-query-parameters="updateQueryParameters"
    :query="getNodeIpInterfaces"
    :getTotalCount="getIpInterfacesTotalCount"
  />
</template>

<script
  setup
  lang="ts"
>
import Pagination from '../Common/Pagination.vue'
import { useNodeStore } from '@/stores/nodeStore'
import useQueryParameters from '@/composables/useQueryParams'
import { QueryParameters } from '@/types'

const nodeStore = useNodeStore()
const route = useRoute()

const getNodeIpInterfaces = async (payload: QueryParameters) => {
  nodeStore.getNodeIpInterfaces({ id: route.params.id as string, queryParameters: payload })
}

const getIpInterfacesTotalCount = () => {
  return nodeStore.ipInterfacesTotalCount
}

const { queryParameters, updateQueryParameters, payload } = useQueryParameters({
  limit: 5,
  offset: 0,
  _s: 'isManaged==U,isManaged==P,isManaged==N,isManaged==M'
}, getNodeIpInterfaces)

const ipInterfaces = computed(() => nodeStore.ipInterfaces)
</script>

<style lang="scss">
@import "@featherds/table/scss/table";
table {
  @include table;
}
</style>
