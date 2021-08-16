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
    
<script lang="ts">
import { defineComponent } from 'vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import SnmpInterfacesTable from './SnmpInterfacesTable.vue'
import IpInterfacesTable from './IpInterfacesTable.vue'
export default defineComponent({
  name: 'Interface Tabs',
  components: {
    TabView,
    TabPanel,
    SnmpInterfacesTable,
    IpInterfacesTable
  }
})
</script>
  