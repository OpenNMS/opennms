<template>
  <TabPanel
    :value="value"
    :pt="unsafePt as never"
  >
    <slot />
  </TabPanel>
</template>

<script setup lang="ts">
import TabPanel from 'primevue/tabpanel'

// Seam wrapper (NMS-20081) around PrimeVue TabPanel.
withDefaults(defineProps<{
  value: string | number
  unsafePt?: unknown
}>(), {
  unsafePt: undefined
})
</script>
