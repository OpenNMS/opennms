<template>
  <div class="card">
    <div class="onms-col-12 title headline3">Node Interfaces</div>
    <OnmsTabs v-model:value="activeTab">
      <OnmsTabList>
        <OnmsTab :value="0">IP Interfaces</OnmsTab>
        <OnmsTab :value="1">SNMP Interfaces</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel :value="0">
          <IpInterfacesTable />
        </OnmsTabPanel>
        <OnmsTabPanel :value="1">
          <SnmpInterfacesTable />
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'
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
