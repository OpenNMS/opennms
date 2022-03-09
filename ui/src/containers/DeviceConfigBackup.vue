<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-12">
        <p class="title">Device Configuration Backup</p>
        <DCBTable />
      </div>
    </div>
  </div>
</template>

<script setup lang=ts>
import { onMounted } from 'vue'
import { useStore } from 'vuex'
import DCBTable from '@/components/Device/DCBTable.vue'

const store = useStore()

onMounted(() => store.dispatch('deviceModule/getDeviceConfigBackups'))
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
.card {
  @include elevation(2);
  background: var($surface);
  padding: 50px;

  .title {
    @include headline4;
    margin-left: 19px;
  }
}
</style>
