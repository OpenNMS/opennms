<template>
  <div class="snmp-config-tab-container">
    <PTabs
      class="tabs"
      v-model:value="activeTab"
    >
      <PTabList>
        <PTab :value="0">Lookup</PTab>
        <PTab :value="1">Browse Definitions</PTab>
        <PTab :value="2">Advanced</PTab>
      </PTabList>
      <PTabPanels>
        <PTabPanel :value="0">
          <SnmpConfigLookupTab />
        </PTabPanel>
        <PTabPanel :value="1">
          <SnmpConfigDefinitionsTab />
        </PTabPanel>
        <PTabPanel :value="2">
          <SnmpConfigAdvancedTab />
        </PTabPanel>
      </PTabPanels>
    </PTabs>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'
import SnmpConfigLookupTab from './SnmpConfigLookupTab.vue'
import SnmpConfigAdvancedTab from './SnmpConfigAdvancedTab.vue'
import SnmpConfigDefinitionsTab from './SnmpConfigDefinitionsTab.vue'

const PTabs = Tabs
const PTabList = TabList
const PTab = Tab
const PTabPanels = TabPanels
const PTabPanel = TabPanel

const store = useSnmpConfigStore()

const activeTab = computed({
  get: () => store.activeTab,
  set: val => store.setActiveTab(val)
})
</script>

<style lang="scss" scoped>
.snmp-config-tab-container {
  .tabs {
    :deep(.p-tab) {
      text-transform: uppercase;
    }
  }
}
</style>
