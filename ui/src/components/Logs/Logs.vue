<template>
  <div class="logs-sidebar">
    <h3>Search Logs</h3>
    <PListbox
      v-model="selectedLog"
      :options="logs"
      filter
      filterPlaceholder="Search logs"
      class="logs-listbox"
      :listStyle="listStyle"
      @change="onChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import Listbox from 'primevue/listbox'
import { useLogStore } from '@/stores/logStore'

const PListbox = Listbox

const logStore = useLogStore()
const logs = computed(() => logStore.logs)
const selectedLog = ref(logStore.selectedLog)
const listStyle = 'max-height: calc(100vh - 260px)'

const onChange = (event: { value: string | null }) => {
  if (event.value) {
    logStore.getLog(event.value)
  }
}
</script>

<style lang="scss" scoped>
.logs-sidebar {
  h3 {
    margin: 0 0 0.5rem 0;
  }

  .logs-listbox {
    width: 100%;
  }
}
</style>
