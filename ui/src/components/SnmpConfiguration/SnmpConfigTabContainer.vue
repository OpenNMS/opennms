<template>
  <div class="snmp-config-tab-container">
    <OnmsTabs
      class="tabs"
      v-model:value="activeTab"
    >
      <OnmsTabList>
        <OnmsTab :value="0">Lookup</OnmsTab>
        <OnmsTab :value="1">Browse Definitions</OnmsTab>
        <OnmsTab :value="2">Advanced</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel :value="0">
          <SnmpConfigLookupTab />
        </OnmsTabPanel>
        <OnmsTabPanel :value="1">
          <SnmpConfigDefinitionsTab />
        </OnmsTabPanel>
        <OnmsTabPanel :value="2">
          <SnmpConfigAdvancedTab />
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'
import SnmpConfigLookupTab from './SnmpConfigLookupTab.vue'
import SnmpConfigAdvancedTab from './SnmpConfigAdvancedTab.vue'
import SnmpConfigDefinitionsTab from './SnmpConfigDefinitionsTab.vue'

const store = useSnmpConfigStore()

const activeTab = computed({
  get: () => store.activeTab,
  set: val => store.setActiveTab(val)
})
</script>
