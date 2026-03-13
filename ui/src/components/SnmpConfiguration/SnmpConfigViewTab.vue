<template>
  <div class="snmp-config-view-tab">
    <FeatherTabContainer
      class="nested-tabs"
      v-model="activeViewConfigurationsTab"
    >
      <template v-slot:tabs>
        <FeatherTab>Definitions</FeatherTab>
        <FeatherTab>Profiles</FeatherTab>
      </template>
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
import SnmpConfigDefinitionsTab from './SnmpConfigDefinitionsTab.vue'
import SnmpConfigProfilesTab from './SnmpConfigProfilesTab.vue'

const store = useSnmpConfigStore()

const activeViewConfigurationsTab = computed({
  get: () => store.activeViewConfigurationsTab,
  set: (val) => store.setActiveViewConfigurationsTab(val)
})
</script>

<style lang="scss" scoped>
.snmp-config-view-tab {
  width: 100%;

  .nested-tabs {
    margin-top: 1em;

    :deep(li .tab) {
      height: 2.25rem;
    }
  }
}
</style>
