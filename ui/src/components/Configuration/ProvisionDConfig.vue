<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">
        <FeatherTabContainer>
          <template v-slot:tabs>
            <FeatherTab @click="$router.push('/provisionDConfig')">Requisition Definitions</FeatherTab>
            <FeatherTab @click="$router.push('/threadPools')">Thread Pools</FeatherTab>
          </template>
          <FeatherTabPanel><router-view></router-view></FeatherTabPanel>
          <FeatherTabPanel><router-view></router-view></FeatherTabPanel>
        </FeatherTabContainer>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">

import { onMounted } from 'vue';
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import router from '@/router';

onMounted(() => {
  router.push('/provisionDConfig')
})

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
