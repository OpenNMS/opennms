<template>
  <Chip
    :label="label"
    :removable="removable"
    :pt="unsafePt as never"
    @remove="emit('remove', $event)"
  >
    <template
      v-if="$slots.default"
      #default
    >
      <slot />
    </template>
  </Chip>
</template>

<script setup lang="ts">
import Chip from 'primevue/chip'

// Seam wrapper (NMS-20081) around PrimeVue Chip. Content via `label` or the
// default slot. class / v-tooltip / native @click fall through (chips are
// used both as removable filter tokens and as clickable navigation pills).
withDefaults(defineProps<{
  label?: string
  removable?: boolean
  unsafePt?: unknown
}>(), {
  label: undefined,
  removable: false,
  unsafePt: undefined
})

const emit = defineEmits<{
  remove: [event: Event]
}>()
</script>
