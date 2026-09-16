<template>
  <NodeDetailsPanel title="Node Interfaces">
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
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import SnmpInterfacesTable from './SnmpInterfacesTable.vue'
import IpInterfacesTable from './IpInterfacesTable.vue'

const activeTab = ref(0)
</script>
