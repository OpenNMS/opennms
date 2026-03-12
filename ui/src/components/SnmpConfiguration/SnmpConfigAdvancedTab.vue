<template>
  <div class="snmp-config-advanced-tab">
    <div class="main-section">
      <SnmpConfigDefaultsPanel />
    </div>
  </div>
</template>

<script lang="ts" setup>

import SnmpConfigDefaultsPanel from './SnmpConfigDefaultsPanel.vue'

</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

.snmp-config-advanced-tab {
  background: var(variables.$surface);
  width: 100%;
  padding: 0;
  border-radius: 5px;
  margin-top: 0;
  border: 1px solid var(variables.$border-on-surface);

  .main-section {
    padding: 16px;
  }
}
</style>
