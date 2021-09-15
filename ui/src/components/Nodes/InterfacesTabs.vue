<template>
  <TabView>
    <TabPanel header="IP Interfaces">
      <IpInterfacesTable />
    </TabPanel>
    <TabPanel header="SNMP Interfaces">
      <SnmpInterfacesTable />
    </TabPanel>
  </TabView>
</template>
    
<script setup lang="ts">
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import SnmpInterfacesTable from './SnmpInterfacesTable.vue'
import IpInterfacesTable from './IpInterfacesTable.vue'
</script>
  