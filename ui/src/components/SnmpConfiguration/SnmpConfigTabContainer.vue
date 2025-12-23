<template>
  <div class="snmp-config-tab-container">
    <FeatherTabContainer
      class="tabs"
      v-model="store.activeTab"
    >
      <template v-slot:tabs>
        <FeatherTab>Lookup</FeatherTab>
        <FeatherTab>Definitions</FeatherTab>
        <FeatherTab>Profiles</FeatherTab>
      </template>
      <FeatherTabPanel>
        <SnmpConfigLookup />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <SnmpConfigDefinitionsTable />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <SnmpConfigProfilesTable />
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script lang="ts" setup>
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import SnmpConfigLookup from './SnmpConfigLookup.vue'
import SnmpConfigDefinitionsTable from './SnmpConfigDefinitionsTable.vue'
import SnmpConfigProfilesTable from './SnmpConfigProfilesTable.vue'

const store = useSnmpConfigStore()

</script>

<style lang="scss" scoped></style>
