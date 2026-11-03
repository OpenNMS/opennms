<template>
  <SelectButton
    :modelValue="modelValue"
    :options="options"
    :optionLabel="optionLabel"
    :optionValue="optionValue"
    :allowEmpty="allowEmpty"
    :disabled="disabled"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event)"
    @change="emit('change', $event.value)"
  />
</template>

<script setup lang="ts">
import SelectButton from 'primevue/selectbutton'

// Seam wrapper (NMS-20029) around PrimeVue SelectButton: a segmented
// single-choice control. `allowEmpty` defaults to false (PrimeVue's own
// default is true) — a segmented control that can deselect its last option
// leaves the UI in a state with no mode chosen, which no OpenNMS usage wants.
// `change` emits the selected value directly rather than PrimeVue's
// { originalEvent, value } object, matching the OnmsListbox precedent.
withDefaults(defineProps<{
  modelValue?: unknown
  options?: unknown[]
  optionLabel?: string
  optionValue?: string
  allowEmpty?: boolean
  disabled?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  options: () => [],
  optionLabel: undefined,
  optionValue: undefined,
  allowEmpty: false,
  disabled: false,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
  change: [value: unknown]
}>()
</script>
