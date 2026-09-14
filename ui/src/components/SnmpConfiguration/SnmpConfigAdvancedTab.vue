<template>
  <div class="snmp-config-advanced-tab">
    <OnmsTabs
      class="nested-tabs"
      v-model:value="activeAdvancedSubtab"
    >
      <OnmsTabList>
        <OnmsTab :value="0">Default Overrides</OnmsTab>
        <OnmsTab :value="1">Profiles</OnmsTab>
        <OnmsTab :value="2">Upload/Download</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel :value="0">
          <SnmpConfigDefaultsPanel />
        </OnmsTabPanel>
        <OnmsTabPanel :value="1">
          <SnmpConfigProfilesTab />
        </OnmsTabPanel>
        <OnmsTabPanel :value="2">
          <SnmpConfigUploadDownloadTab />
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>
   </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'
import SnmpConfigDefaultsPanel from './SnmpConfigDefaultsPanel.vue'
import SnmpConfigProfilesTab from './SnmpConfigProfilesTab.vue'
import SnmpConfigUploadDownloadTab from './SnmpConfigUploadDownloadTab.vue'

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
  }
}
</style>
