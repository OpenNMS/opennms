<template>
  <Tab
    :value="value"
    :pt="unsafePt as never"
  >
    <slot />
  </Tab>
</template>

<script setup lang="ts">
import Tab from 'primevue/tab'

// Seam wrapper (NMS-20081) around PrimeVue Tab. Tab labels render uppercase
// by design (previously re-declared per-site as :deep(.p-tab) rules).
// Native events (@click) fall through.
withDefaults(defineProps<{
  value: string | number
  unsafePt?: unknown
}>(), {
  unsafePt: undefined
})
</script>

<style lang="scss" scoped>
.p-tab {
  text-transform: uppercase;
}
</style>
