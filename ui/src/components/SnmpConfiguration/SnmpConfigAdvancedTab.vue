<template>
  <div class="snmp-config-advanced-tab">
    <PTabs
      class="nested-tabs"
      v-model:value="activeAdvancedSubtab"
    >
      <PTabList>
        <PTab :value="0">Default Overrides</PTab>
        <PTab :value="1">Profiles</PTab>
        <PTab :value="2">Upload/Download</PTab>
      </PTabList>
      <PTabPanels>
        <PTabPanel :value="0">
          <SnmpConfigDefaultsPanel />
        </PTabPanel>
        <PTabPanel :value="1">
          <SnmpConfigProfilesTab />
        </PTabPanel>
        <PTabPanel :value="2">
          <SnmpConfigUploadDownloadTab />
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
import SnmpConfigDefaultsPanel from './SnmpConfigDefaultsPanel.vue'
import SnmpConfigProfilesTab from './SnmpConfigProfilesTab.vue'
import SnmpConfigUploadDownloadTab from './SnmpConfigUploadDownloadTab.vue'

const PTabs = Tabs
const PTabList = TabList
const PTab = Tab
const PTabPanels = TabPanels
const PTabPanel = TabPanel

const store = useSnmpConfigStore()

const activeAdvancedSubtab = computed({
  get: () => store.activeAdvancedSubtab,
  set: val => store.setActiveAdvancedSubtab(val)
})
</script>

<style lang="scss" scoped>
.snmp-config-advanced-tab {
  background: var(--p-content-background);
  width: 100%;
  padding: 0;
  border-radius: 5px;
  margin-top: 0;
  border: 1px solid var(--p-content-border-color);

  .main-section {
    padding: 1.5em;
  }

  .nested-tabs {
    margin-top: 1em;

    :deep(.p-tab) {
      text-transform: uppercase;
    }
  }
}
</style>
