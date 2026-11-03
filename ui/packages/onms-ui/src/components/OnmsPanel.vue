<template>
  <Panel
    :header="header"
    :toggleable="toggleable"
    :collapsed="collapsed"
    :pt="unsafePt as never"
    @update:collapsed="emit('update:collapsed', $event)"
  >
    <template
      v-if="$slots.header"
      #header
    >
      <slot name="header" />
    </template>
    <slot />
  </Panel>
</template>

<script setup lang="ts">
import Panel from 'primevue/panel'

// Seam wrapper (NMS-20081) around PrimeVue Panel. class falls through.
withDefaults(defineProps<{
  header?: string
  toggleable?: boolean
  collapsed?: boolean
  unsafePt?: unknown
}>(), {
  header: undefined,
  toggleable: false,
  collapsed: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:collapsed': [value: boolean]
}>()
</script>
