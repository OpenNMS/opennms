<template>
  <Search />
  <div class="logs-sidebar">
    <p
      :class="{ 'selected': log === selectedLog }"
      class="pointer"
      v-for="(log, index) in logs"
      :key="log"
      @click="getLog(log)"
    >
      <span class="subtitle1">
        {{ Number(index) + 1 }}:&nbsp
      </span>
      <span class="subtitle2">
        {{ log }}
      </span>
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useStore } from 'vuex'
import Search from './Search.vue'

const store = useStore()
const selectedLog = computed(() => store.state.logsModule.selectedLog)
const logs = computed(() => store.getters['logsModule/getFilteredLogs'])
const getLog = (log: string) => store.dispatch('logsModule/getLog', log)
</script>

<style lang="scss" scoped>
p {
  margin: 0px;
  padding: 5px;
  padding-left: 10px;
}
.logs-sidebar {
  overflow-y: scroll;
  overflow-x: hidden;
  height: calc(100vh - 182px);
  word-break: break-all;
  border: 1px solid var(--feather-border-on-surface)
}
.selected {
  background: var(--feather-shade-3);
  span {
    color: var(--feather-primary);
  }
}
</style>
