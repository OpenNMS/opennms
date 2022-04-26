<template>
  <div class="scv-container">
    <div class="list"><SCVListVue /></div>
    <div class="form"><SCVFormVue /></div>
  </div>
</template>

<script setup lang="ts">
import SCVListVue from '@/components/SCV/SCVList.vue'
import SCVFormVue from '@/components/SCV/SCVForm.vue'
import { useStore } from 'vuex'

const store = useStore()
onMounted(() => store.dispatch('scvModule/getAliases'))
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";

.scv-container {
  padding: 25px;
  background: var($surface);
  display: flex;
  flex-grow: 1;
  justify-content: center;
  gap: 50px;

  .list {
    width: 300px;
  }
  .form {
    width: 500px;
  }
}
</style>
