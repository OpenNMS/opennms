<template>
  <div class="snmp-config-tab-container">
    <FeatherTabContainer
      class="tabs"
      v-model="store.activeTab"
    >
      <template v-slot:tabs>
        <FeatherTab>Lookup by IP</FeatherTab>
        <FeatherTab>Definitions</FeatherTab>
        <FeatherTab>Profiles</FeatherTab>
      </template>
      <FeatherTabPanel>
        <SnmpConfigLookupTab />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <SnmpConfigDefinitionsTab />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <SnmpConfigProfilesTab />
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script lang="ts" setup>
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import SnmpConfigLookupTab from './SnmpConfigLookupTab.vue'
import SnmpConfigDefinitionsTab from './SnmpConfigDefinitionsTab.vue'
import SnmpConfigProfilesTab from './SnmpConfigProfilesTab.vue'

const store = useSnmpConfigStore()

</script>

<style lang="scss" scoped>
</style>
