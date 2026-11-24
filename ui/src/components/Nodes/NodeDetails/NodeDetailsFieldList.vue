<template>
  <div class="node-details-field-list">
    <div
      v-for="field in fields"
      :key="field.label"
      class="onms-row"
      data-test="field"
    >
      <div class="onms-col-4">
        <span class="label">{{ field.label }}</span>
      </div>
      <div class="onms-col-8">
        <span>{{ field.text }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { NodeDetailsField } from './nodeEnlinkdFields'

// Label/value rows for a Node Details panel, laid out as the node info dialog's are.
defineProps<{
  fields: NodeDetailsField[]
}>()
</script>

<style scoped lang="scss">
// Each field is its own .onms-row, i.e. its own grid, so the grid's gap only separates label from
// value; the flex column puts the spacing back between rows (see NodeDetailsDialog).
.node-details-field-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.label {
  font-weight: bold;
}
</style>
