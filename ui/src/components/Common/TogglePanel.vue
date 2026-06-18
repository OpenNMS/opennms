<template>
  <PPanel
    :header="header"
    toggleable
    :collapsed="collapsed"
    :pt="{ header: { onClick: onHeaderClick, style: { cursor: 'pointer' } } }"
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
  </PPanel>
</template>

<script setup lang="ts">
import Panel from 'primevue/panel'

const PPanel = Panel

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
const onHeaderClick = (event: MouseEvent) => {
  if ((event.target as HTMLElement).closest('button')) {
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
