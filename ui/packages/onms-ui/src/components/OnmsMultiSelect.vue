<template>
  <MultiSelect
    :modelValue="modelValue"
    :options="options"
    :optionLabel="optionLabel"
    :dataKey="dataKey"
    :filter="filter"
    :display="display"
    :placeholder="placeholder"
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
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  options: () => [],
  optionLabel: undefined,
  dataKey: undefined,
  filter: false,
  display: 'comma',
  placeholder: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown[]]
}>()
</script>
