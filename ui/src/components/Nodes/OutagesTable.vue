<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-12 headline3">Recent Outages</div>
    </div>
    <div class="feather-row">
      <div class="feather-col-12">
        <table
          class="tl1 tl2 tl3"
          summary="Outages"
        >
          <thead>
            <tr>
              <th scope="col">IP Address</th>
              <th scope="col">Host Name</th>
              <th scope="col">Service Name</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="outage in outages"
              :key="outage.outageId"
            >
              <td>{{ outage.ipAddress }}</td>
              <td>{{ outage.hostname }}</td>
              <td>{{ outage.serviceName }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <Pagination
      :payload="payload"
      :parameters="queryParameters"
      @update-query-parameters="updateQueryParameters"
      :query="getNodeOutages"
      :getTotalCount="getOutagesTotalCount"
    />
  </div>
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

const getNodeOutages = async (payload: QueryParameters) => {
  nodeStore.getNodeOutages({ id: route.params.id as string, queryParameters: payload })
}

const getOutagesTotalCount = () => {
  return nodeStore.outagesTotalCount
}

const { queryParameters, updateQueryParameters, payload } = useQueryParameters({
  limit: 10,
  offset: 0
}, getNodeOutages)

const outages = computed(() => nodeStore.outages)
</script>

<style
  lang="scss"
  scoped
>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  padding: 15px;
  margin-bottom: 15px;
}
table {
  @include table;
}
</style>
