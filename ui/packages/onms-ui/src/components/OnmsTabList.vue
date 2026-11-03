<template>
  <TabList :pt="unsafePt as never">
    <slot />
  </TabList>
</template>

<script setup lang="ts">
import TabList from 'primevue/tablist'

// Seam wrapper (NMS-20081) around PrimeVue TabList.
withDefaults(defineProps<{
  unsafePt?: unknown
}>(), {
  unsafePt: undefined
})
</script>
