<template>
  <div class="snmp-config-tab-container">
    <FeatherTabContainer
      class="tabs"
      v-model="store.activeTab"
    >
      <template v-slot:tabs>
        <FeatherTab>Lookup</FeatherTab>
        <FeatherTab>Browse Configurations</FeatherTab>
        <FeatherTab>Advanced</FeatherTab>
      </template>
      <FeatherTabPanel>
        <SnmpConfigLookupTab />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <SnmpConfigViewTab />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <SnmpConfigAdvancedTab />
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script lang="ts" setup>
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import SnmpConfigLookupTab from './SnmpConfigLookupTab.vue'
import SnmpConfigViewTab from './SnmpConfigViewTab.vue'
import SnmpConfigAdvancedTab from './SnmpConfigAdvancedTab.vue'

const store = useSnmpConfigStore()

</script>

<style lang="scss" scoped>
</style>
