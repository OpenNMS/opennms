<template>
  <MultiSelect
    :modelValue="modelValue"
    :options="options"
    :optionLabel="optionLabel"
    :dataKey="dataKey"
    :filter="filter"
    :display="display"
    :placeholder="placeholder"
    :inputId="inputId"
    :fluid="fluid"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import MultiSelect from 'primevue/multiselect'

// Seam wrapper (NMS-20081) around PrimeVue MultiSelect.
withDefaults(defineProps<{
  modelValue?: unknown[]
  options?: unknown[]
  optionLabel?: string
  dataKey?: string
  filter?: boolean
  display?: 'comma' | 'chip'
  placeholder?: string
  // PrimeVue MultiSelect exposes inputId (not labelId); forward it so a paired
  // <label for> can associate with the control.
  inputId?: string
  // fluid: undefined lets a parent Fluid context be inherited; explicit false
  // would break that inheritance.
  fluid?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  options: () => [],
  optionLabel: undefined,
  dataKey: undefined,
  filter: false,
  display: 'comma',
  placeholder: undefined,
  inputId: undefined,
  fluid: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown[]]
}>()
</script>
