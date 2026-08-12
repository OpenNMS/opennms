<template>
  <div class="logs-sidebar">
    <h3>Search Logs</h3>
    <OnmsListbox
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
import { computed, ref, watch } from 'vue'

import { OnmsListbox } from '@opennms/onms-ui'
import { useLogStore } from '@/stores/logStore'

const logStore = useLogStore()
const logs = computed(() => logStore.logs)
const selectedLog = ref(logStore.selectedLog)
const listStyle = 'max-height: calc(100vh - 260px)'

// Keep the Listbox highlight in sync with the store's selected log, including
// when it is refreshed or changed outside this component.
watch(() => logStore.selectedLog, (log) => {
  selectedLog.value = log
})

const onChange = (newValue: unknown) => {
  // PrimeVue Listbox single-select treats a click on the already-selected option
  // as a toggle: it emits update:modelValue with null (clearing selectedLog)
  // before emitting @change. Fall back to the currently loaded log so a re-click
  // reloads it and keeps the highlight, matching the old FeatherListItem behavior
  // that reloaded on every click.
  const value = newValue as string | null
  const log = value ?? logStore.selectedLog
  if (!log) {
    return
  }
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
