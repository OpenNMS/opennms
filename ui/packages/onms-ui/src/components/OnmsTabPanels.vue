<template>
  <TabPanels :pt="unsafePt as never">
    <slot />
  </TabPanels>
</template>

<script setup lang="ts">
import TabPanels from 'primevue/tabpanels'

// Seam wrapper (NMS-20081) around PrimeVue TabPanels.
withDefaults(defineProps<{
  unsafePt?: unknown
}>(), {
  unsafePt: undefined
})
</script>
