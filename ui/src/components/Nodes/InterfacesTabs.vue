<template>
  <div class="card">
    <div class="feather-col-12 title headline3">Node Interfaces</div>
    <FeatherTabContainer>
      <template v-slot:tabs>
        <FeatherTab>IP Interfaces</FeatherTab>
        <FeatherTab>SNMP Interfaces</FeatherTab>
      </template>

      <FeatherTabPanel>
        <IpInterfacesTable />
      </FeatherTabPanel>

      <FeatherTabPanel>
        <SnmpInterfacesTable />
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>
    
<script setup lang="ts">
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import SnmpInterfacesTable from './SnmpInterfacesTable.vue'
import IpInterfacesTable from './IpInterfacesTable.vue'
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  padding: 15px;
  margin-bottom: 15px;
  .title {
    margin-bottom: 15px;
  }
}
</style>
  