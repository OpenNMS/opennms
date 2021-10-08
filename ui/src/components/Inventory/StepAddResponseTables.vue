<template>
  <template v-for="ipRangeResponse of ipRangeResponses" :key="ipRangeResponse.location">
    <h3>{{ ipRangeResponse.location }}</h3>
    <DataTable :value="ipRangeResponse.scanResults" responsiveLayout="scroll">
      <Column field="hostname" header="Hostname"></Column>
      <Column field="ipAddress" header="IP Address"></Column>
      <Column field="rtt" header="RTT"></Column>
    </DataTable>
  </template>
</template>

<script setup lang="ts">
import { computed } from "vue"
import { useStore } from "vuex"
import DataTable from "primevue/datatable"
import Column from "primevue/column"

const store = useStore();
const ipRangeResponses = computed(
  () => store.state.inventoryModule.ipRangeResponses
)
</script>   
