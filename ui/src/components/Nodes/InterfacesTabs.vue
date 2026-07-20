<template>
  <div class="card">
    <div class="onms-col-12 title headline3">Node Interfaces</div>
    <Tabs v-model:value="activeTab">
      <TabList>
        <Tab :value="0">IP Interfaces</Tab>
        <Tab :value="1">SNMP Interfaces</Tab>
      </TabList>
      <TabPanels>
        <TabPanel :value="0">
          <IpInterfacesTable />
        </TabPanel>
        <TabPanel :value="1">
          <SnmpInterfacesTable />
        </TabPanel>
      </TabPanels>
    </Tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Tab from 'primevue/tab'
import TabList from 'primevue/tablist'
import TabPanel from 'primevue/tabpanel'
import TabPanels from 'primevue/tabpanels'
import Tabs from 'primevue/tabs'
import SnmpInterfacesTable from './SnmpInterfacesTable.vue'
import IpInterfacesTable from './IpInterfacesTable.vue'

const activeTab = ref(0)
</script>

<style lang="scss" scoped>
.card {
  padding: 15px;
  margin-bottom: 15px;
  .title {
    margin-bottom: 15px;
  }
}
</style>
