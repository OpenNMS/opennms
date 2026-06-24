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

// PrimeVue Listbox single-select treats a click on the already-selected option
// as a toggle: it emits update:modelValue with null (clearing selectedLog)
// before emitting @change. We track the loaded log separately so a re-click
// reloads the same log and keeps the highlight, matching the old
// FeatherListItem behavior that reloaded on every click.
let loadedLog: string | null = logStore.selectedLog || null

const onChange = (event: { value: string | null }) => {
  const log = event.value ?? loadedLog
  if (!log) {
    return
  }
  loadedLog = log
  selectedLog.value = log
  logStore.getLog(log)
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
