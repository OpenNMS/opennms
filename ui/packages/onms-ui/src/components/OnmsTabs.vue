<template>
  <Tabs
    :value="value"
    :pt="unsafePt as never"
    @update:value="emit('update:value', $event)"
  >
    <slot />
  </Tabs>
</template>

<script setup lang="ts">
import Tabs from 'primevue/tabs'

// Seam wrapper (NMS-20081) around PrimeVue Tabs. Compose with OnmsTabList /
// OnmsTab / OnmsTabPanels / OnmsTabPanel. `value` identifies the active tab
// (string or number); v-model:value supported. class / data-* fall through.
defineProps<{
  value: string | number
  unsafePt?: unknown
}>()

const emit = defineEmits<{
  'update:value': [value: string | number]
}>()
</script>
