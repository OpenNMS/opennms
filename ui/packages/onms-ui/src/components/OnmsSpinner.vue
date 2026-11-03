<template>
  <ProgressSpinner
    :style="{ width: size, height: size }"
    :strokeWidth="strokeWidth"
    :pt="unsafePt as never"
  />
</template>

<script setup lang="ts">
import ProgressSpinner from 'primevue/progressspinner'

// Seam wrapper (NMS-20029) around PrimeVue ProgressSpinner. Single
// primary-colored stroke (the PrimeVue four-color cycle is disabled).
// Presentational only — visibility/overlay logic belongs to the consumer.
withDefaults(defineProps<{
  size?: string
  strokeWidth?: string
  unsafePt?: unknown
}>(), {
  size: '2.5rem',
  strokeWidth: '4',
  unsafePt: undefined
})
</script>

<style lang="scss" scoped>
// FeatherSpinner used a single primary-colored stroke. PrimeVue's
// ProgressSpinner cycles through four colors via the p-progressspinner-color
// animation; pin the stroke to the primary color and drop the color-cycle
// animation (keep the dash animation for the spinning arc).
:deep(.p-progressspinner-circle) {
  stroke: var(--p-primary-color);
  animation: p-progressspinner-dash 1.5s ease-in-out infinite;
}
</style>
