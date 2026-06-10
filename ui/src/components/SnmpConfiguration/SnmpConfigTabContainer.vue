<template>
  <div class="snmp-config-tab-container">
    <FeatherTabContainer
      class="tabs"
      v-model="activeTab"
    >
      <template v-slot:tabs>
        <FeatherTab>Lookup</FeatherTab>
        <FeatherTab>Browse Definitions</FeatherTab>
        <FeatherTab>Advanced</FeatherTab>
      </template>
      <FeatherTabPanel>
        <SnmpConfigLookupTab />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <SnmpConfigDefinitionsTab />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <SnmpConfigAdvancedTab />
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import SnmpConfigLookupTab from './SnmpConfigLookupTab.vue'
import SnmpConfigAdvancedTab from './SnmpConfigAdvancedTab.vue'
import SnmpConfigDefinitionsTab from './SnmpConfigDefinitionsTab.vue'

const store = useSnmpConfigStore()

const activeTab = computed({
  get: () => store.activeTab,
  set: val => store.setActiveTab(val)
})
</script>

<style lang="scss" scoped>
</style>
