<template>
  <Listbox
    :modelValue="modelValue"
    :options="options"
    :optionLabel="optionLabel"
    :dataKey="dataKey"
    :multiple="multiple"
    :checkmark="checkmark"
    :filter="filter"
    :filterPlaceholder="filterPlaceholder"
    :listStyle="listStyle"
    :scrollHeight="scrollHeight"
    :virtualScrollerOptions="virtualScrollerOptions"
    :emptyMessage="emptyMessage"
    :disabled="disabled"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event)"
    @change="emit('change', $event.value)"
  >
    <template
      v-if="$slots.option"
      #option="slotProps"
    >
      <slot
        name="option"
        v-bind="slotProps"
      />
    </template>
  </Listbox>
</template>

<script setup lang="ts">
import Listbox from 'primevue/listbox'

// Seam wrapper (NMS-20081) around PrimeVue Listbox. `change` fires with the
// selected value only (the seam does not expose PrimeVue's event object),
// matching the OnmsAutoComplete `optionSelect` precedent.
//
// The declared surface is kept close to what the app uses. PrimeVue's Listbox has
// more (option grouping, its own filter fields, metaKeySelection); those are left
// out until something needs them. `emptyMessage` and `disabled` are the exceptions
// — general enough that any listbox may want them, so they are exposed up front
// rather than waiting for the first consumer.
withDefaults(defineProps<{
  modelValue?: unknown
  options?: unknown[]
  optionLabel?: string
  dataKey?: string
  multiple?: boolean
  checkmark?: boolean
  filter?: boolean
  filterPlaceholder?: string
  listStyle?: string
  scrollHeight?: string
  /** Windowing for long option lists; requires a fixed `itemSize` (px). */
  virtualScrollerOptions?: { itemSize: number, [key: string]: unknown }
  /** Shown in place of the list when there are no options. */
  emptyMessage?: string
  disabled?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  options: () => [],
  optionLabel: undefined,
  dataKey: undefined,
  multiple: false,
  checkmark: false,
  filter: false,
  filterPlaceholder: undefined,
  listStyle: undefined,
  scrollHeight: undefined,
  virtualScrollerOptions: undefined,
  emptyMessage: undefined,
  disabled: false,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
  change: [value: unknown]
}>()

defineSlots<{
  option?: (props: { option: unknown, index: number, selected: boolean }) => unknown
}>()
</script>
