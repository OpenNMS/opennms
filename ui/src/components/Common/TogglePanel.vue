<template>
  <OnmsPanel
    :header="header"
    toggleable
    :collapsed="collapsed"
    :unsafe-pt="{ header: { onClick: onHeaderClick, style: { cursor: 'pointer' } } }"
    class="toggle-panel"
    @update:collapsed="emit('update:collapsed', $event)"
  >
    <template
      v-if="$slots.header"
      #header
    >
      <slot name="header" />
    </template>
    <slot />
  </OnmsPanel>
</template>

<script setup lang="ts">
import { OnmsPanel } from '@opennms/onms-ui'

const props = defineProps<{
  header?: string
  collapsed: boolean
}>()

const emit = defineEmits<{
  (e: 'update:collapsed', value: boolean): void
}>()

// Toggle when clicking anywhere in the header, not just the chevron. The
// built-in toggle button manages its own click, so ignore clicks that
// originate from it to avoid double-toggling.
//
// Use composedPath() rather than event.target.closest(): the toggle button's
// icon swaps (Plus <-> Minus) when it toggles, which can detach the clicked
// node from the DOM before this bubbled handler runs. A detached node's
// closest() returns null, so the guard would miss it and fire a second,
// unwanted toggle (rapid expand-then-collapse). composedPath is captured at
// dispatch time and stays stable through propagation.
const onHeaderClick = (event: MouseEvent) => {
  const fromToggleButton = event.composedPath().some(
    el => el instanceof Element &&
      (el.classList.contains('p-panel-header-actions') || el.classList.contains('p-panel-toggle-button'))
  )
  if (fromToggleButton) {
    return
  }
  emit('update:collapsed', !props.collapsed)
}
</script>

<style lang="scss" scoped>
.toggle-panel {
  // shade the header to match the DataTable header (auto-switches in dark mode)
  :deep(.p-panel-header) {
    background: var(--p-datatable-header-cell-background);
  }
}
</style>
