<template>
  <div class="feather-row" v-if="snmpDetectResponses.length">
    <div class="feather-col-12">
      <table class="tl1 tl2 tl3 tl4" summary="Recent Events">
        <thead>
          <tr>
            <th scope="col">Location</th>
            <th scope="col">Hostname</th>
            <th scope="col">IP Address</th>
            <th scope="col">SysOID</th>
            <th scope="col">Community String</th>
            <th scope="col">SNMP Available</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="response in snmpDetectResponses" :key="response.location">
            <td>{{ response.location }}</td>
            <td>{{ response.hostname }}</td>
            <td>{{ response.ipAddress }}</td>
            <td>{{ response.sysOID }}</td>
            <td>{{ response.communityString }}</td>
            <td>{{ response.hasSNMPService }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue"
import { useStore } from "vuex"

const store = useStore()
const snmpDetectResponses = computed(
  () => store.state.inventoryModule.snmpDetectResponses
)
</script>

<style lang="scss">
@import "@featherds/table/scss/table";
</style>
