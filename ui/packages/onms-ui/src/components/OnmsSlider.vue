<template>
  <Slider
    :modelValue="modelValue"
    :min="min"
    :max="max"
    :step="step"
    :disabled="disabled"
    :ariaLabel="ariaLabel"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event as number)"
    @change="emit('change', $event as number)"
  />
</template>

<script setup lang="ts">
import Slider from 'primevue/slider'

// Seam wrapper (NMS-20029) around PrimeVue Slider, single-value only (the
// `range` two-handle mode is not exposed — no OpenNMS usage needs it, and it
// would widen modelValue to number | number[] for every consumer).
//
// `ariaLabel` is a declared prop rather than a fall-through DOM attr: the
// element carrying role="slider" is the handle, not the root, so an
// `aria-label` left to fall through would land on the wrong element.
withDefaults(defineProps<{
  modelValue?: number
  min?: number
  max?: number
  step?: number
  disabled?: boolean
  ariaLabel?: string
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  min: 0,
  max: 100,
  step: undefined,
  disabled: false,
  ariaLabel: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: number]
  change: [value: number]
}>()
</script>
