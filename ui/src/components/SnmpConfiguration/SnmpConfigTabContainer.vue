<template>
  <div class="snmp-config-tab-container">
    <FeatherTabContainer
      class="tabs"
      v-model="store.activeTab"
    >
      <template v-slot:tabs>
        <FeatherTab>Definitions</FeatherTab>
        <FeatherTab>Profiles</FeatherTab>
      </template>
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
import SnmpConfigDefinitionsTable from './SnmpConfigDefinitionsTable.vue'
import SnmpConfigProfilesTable from './SnmpConfigProfilesTable.vue'

const store = useSnmpConfigStore()

</script>

<style lang="scss" scoped></style>
