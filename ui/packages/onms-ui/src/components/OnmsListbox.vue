<template>
  <Listbox
    :modelValue="modelValue"
    :options="options"
    :filter="filter"
    :filterPlaceholder="filterPlaceholder"
    :listStyle="listStyle"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event)"
    @change="emit('change', $event.value)"
  />
</template>

<script setup lang="ts">
import Listbox from 'primevue/listbox'

// Seam wrapper (NMS-20081) around PrimeVue Listbox. `change` fires with the
// selected value only (the seam does not expose PrimeVue's event object),
// matching the OnmsAutoComplete `optionSelect` precedent.
withDefaults(defineProps<{
  modelValue?: unknown
  options?: unknown[]
  filter?: boolean
  filterPlaceholder?: string
  listStyle?: string
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  options: () => [],
  filter: false,
  filterPlaceholder: undefined,
  listStyle: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
  change: [value: unknown]
}>()
</script>
