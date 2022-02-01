<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">
        <div class="feather-row">
          <div class="feather-col-3">
            <Logs />
          </div>
          <div :class="`feather-col-9`">
            <Editor />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useStore } from 'vuex'
import Editor from '@/components/Logs/Editor.vue'
import Logs from '@/components/Logs/Logs.vue'
const store = useStore()
onMounted(() => store.dispatch('logsModule/getLogs'))
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  background: var($surface);
  padding: 15px;
  position: relative;
}
.feather-row {
  flex-wrap: nowrap;
}
</style>
