<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">
        <TabMenu :model="TabMenuItem" />
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">

import TabMenu from 'primevue/tabmenu'

const TabMenuItem = [
  { label: 'Requisition Definitions', to: '/provisionDConfig' },
  { label: 'Thread Pools', to: '/threadPools' },
]
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  padding: 15px;
  margin-top: 15px;
}
</style>
