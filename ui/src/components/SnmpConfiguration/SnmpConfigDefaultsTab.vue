<template>
  <div class="snmp-config-defaults-tab">
    <div class="main-section">
      <SnmpConfigDefaultsPanel />
    </div>
  </div>
</template>

<script setup lang="ts">
import SnmpConfigDefaultsPanel from './SnmpConfigDefaultsPanel.vue'

</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

.snmp-config-defaults-tab {
  .main-section {
    padding: 16px;
  }
}
</style>
