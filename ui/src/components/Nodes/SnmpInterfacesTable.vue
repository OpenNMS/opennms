<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <table
        class="tl1 tl2 tl3, tl4, tl5"
        summary="SNMP Interfaces"
      >
        <thead>
          <tr>
            <th scope="col">SNMP ifIndex</th>
            <th scope="col">SNMP ifDescr</th>
            <th scope="col">SNMP ifName</th>
            <th scope="col">SNMP ifAlias</th>
            <th scope="col">SNMP ifSpeed</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="snmpInterface in snmpInterfaces"
            :key="snmpInterface.id"
          >
            <td>{{ snmpInterface.ifIndex }}</td>
            <td>{{ snmpInterface.ifDescr || 'N/A' }}</td>
            <td>{{ snmpInterface.ifName || 'N/A' }}</td>
            <td>{{ snmpInterface.ifAlias || 'N/A' }}</td>
            <td>
              <span v-html="snmpInterface.ifSpeed"></span>
            </td>
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
    functionName="getNodeSnmpInterfaces"
    totalCountStateName="snmpInterfacesTotalCount"
  />
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
const optionalPayload = { id: route.params.id }
const { queryParameters, updateQueryParameters, payload } = useQueryParameters({
  limit: 5,
  offset: 0,
}, 'nodesModule/getNodeSnmpInterfaces', optionalPayload)
const snmpInterfaces = computed(() => store.state.nodesModule.snmpInterfaces)
</script>

<style lang="scss">
@import "@featherds/table/scss/table";
table {
  @include table;
}
</style>

