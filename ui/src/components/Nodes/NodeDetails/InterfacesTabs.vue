<template>
  <NodeDetailsPanel title="Node Interfaces">
    <OnmsTabs v-model:value="activeTab">
      <OnmsTabList>
        <OnmsTab :value="0">IP Interfaces</OnmsTab>
        <OnmsTab :value="1">SNMP Interfaces</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel :value="0">
          <IpInterfacesTable :active="activeTab === 0" />
        </OnmsTabPanel>
        <OnmsTabPanel :value="1">
          <SnmpInterfacesTable :active="activeTab === 1" />
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
