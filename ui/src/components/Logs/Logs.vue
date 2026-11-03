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
// Cap the scrolling list so this column cannot outgrow the editor beside it and
// push the app footer off the bottom of the window. Same subtraction as the
// editor in Logs/Editor.vue — masthead (--onms-header-height), the footer band
// (--onms-footer-height), this page's 51px breadcrumb row and the card's 30px of
// padding — plus what sits above the list inside this column: the 40px "Search
// Logs" heading and the Listbox's 62px filter box.
const listStyle = 'max-height: calc(100vh - var(--onms-header-height, 3.75rem) - var(--onms-footer-height, 41px) - 51px - 30px - 102px)'

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
