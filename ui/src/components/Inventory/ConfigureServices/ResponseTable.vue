<template>
  <DataTable :value="snmpDetectResponses" responsiveLayout="scroll" v-if="snmpDetectResponses.length">
    <Column field="location" header="Location"></Column>
    <Column field="hostname" header="Hostname"></Column>
    <Column field="ipAddress" header="IP Address"></Column>
    <Column field="sysOID" header="SysOID"></Column>
    <Column field="communityString" header="Community String"></Column>
    <Column field="hasSNMPService" header="SNMP Available"></Column>
  </DataTable>
</template>

<script setup lang="ts">
import { computed } from "vue"
import { useStore } from "vuex"
import DataTable from "primevue/datatable"
import Column from "primevue/column"

const store = useStore();
const snmpDetectResponses = computed(
  () => store.state.inventoryModule.snmpDetectResponses
)
</script>   
