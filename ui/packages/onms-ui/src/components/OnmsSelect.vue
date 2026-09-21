<template>
  <Select
    :modelValue="modelValue"
    :options="options"
    :optionLabel="optionLabel"
    :optionValue="optionValue"
    :placeholder="placeholder"
    :inputId="inputId"
    :invalid="invalid"
    :disabled="disabled"
    :fluid="fluid"
    :showClear="showClear"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import Select from 'primevue/select'

// Seam wrapper (NMS-20029) around PrimeVue Select (single-select dropdown).
// Option rendering is driven by optionLabel/optionValue only; custom option
// slots are deliberately not part of the seam API yet.
withDefaults(defineProps<{
  modelValue?: unknown
  options?: unknown[]
  optionLabel?: string
  optionValue?: string
  placeholder?: string
  inputId?: string
  invalid?: boolean
  disabled?: boolean
  // fluid: undefined allows parent Fluid-component context to be inherited;
  // explicit false would break that inheritance
  fluid?: boolean
  showClear?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  options: () => [],
  optionLabel: undefined,
  optionValue: undefined,
  placeholder: undefined,
  inputId: undefined,
  invalid: false,
  disabled: false,
  fluid: undefined,
  showClear: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
}>()
</script>
