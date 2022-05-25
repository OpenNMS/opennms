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
              :key="outage.id"
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
      moduleName="nodesModule"
      functionName="getNodeOutages"
      totalCountStateName="outagesTotalCount"
    />
  </div>
</template>

<script
  setup
  lang="ts"
>
import Pagination from '../Common/Pagination.vue'
import { useStore } from 'vuex'
import useQueryParameters from '@/composables/useQueryParams'
const store = useStore()
const route = useRoute()
const payload = { id: route.params.id }
const { queryParameters, updateQueryParameters } = useQueryParameters({
  limit: 10,
  offset: 0,
}, 'nodesModule/getNodeOutages', payload)
const outages = computed(() => store.state.nodesModule.outages)
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

