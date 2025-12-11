<template>
  <div class="snmp-config-tab-container">
    <FeatherTabContainer
      class="tabs"
      v-model="store.activeTab"
    >
      <template v-slot:tabs>
        <FeatherTab>View</FeatherTab>
        <FeatherTab>Tab Two</FeatherTab>
      </template>
      <FeatherTabPanel>
        <div>Tab One</div>
      </FeatherTabPanel>
      <FeatherTabPanel>
        <div>Tab Two</div>
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script lang="ts" setup>
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'

const store = useSnmpConfigStore()
</script>

<style lang="scss" scoped></style>
