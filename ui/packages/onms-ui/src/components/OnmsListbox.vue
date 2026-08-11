<template>
  <Listbox
    :modelValue="modelValue"
    :options="options"
    :optionLabel="optionLabel"
    :optionValue="optionValue"
    :optionDisabled="optionDisabled"
    :optionGroupLabel="optionGroupLabel"
    :optionGroupChildren="optionGroupChildren"
    :dataKey="dataKey"
    :multiple="multiple"
    :metaKeySelection="metaKeySelection"
    :checkmark="checkmark"
    :filter="filter"
    :filterPlaceholder="filterPlaceholder"
    :filterFields="filterFields"
    :listStyle="listStyle"
    :scrollHeight="scrollHeight"
    :virtualScrollerOptions="virtualScrollerOptions"
    :emptyMessage="emptyMessage"
    :emptyFilterMessage="emptyFilterMessage"
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
// `multiple` selection deliberately ships with `metaKeySelection` defaulting to
// false: in a multi-select picker plain clicking must toggle, and requiring
// ctrl/cmd (PrimeVue's own default) is the behaviour users report as broken.
withDefaults(defineProps<{
  modelValue?: unknown
  options?: unknown[]
  optionLabel?: string
  optionValue?: string
  optionDisabled?: string
  optionGroupLabel?: string
  optionGroupChildren?: string
  dataKey?: string
  multiple?: boolean
  metaKeySelection?: boolean
  checkmark?: boolean
  filter?: boolean
  filterPlaceholder?: string
  filterFields?: string[]
  listStyle?: string
  scrollHeight?: string
  /** Windowing for long option lists; requires a fixed `itemSize` (px). */
  virtualScrollerOptions?: { itemSize: number, [key: string]: unknown }
  emptyMessage?: string
  emptyFilterMessage?: string
  disabled?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  options: () => [],
  optionLabel: undefined,
  optionValue: undefined,
  optionDisabled: undefined,
  optionGroupLabel: undefined,
  optionGroupChildren: undefined,
  dataKey: undefined,
  multiple: false,
  metaKeySelection: false,
  checkmark: false,
  filter: false,
  filterPlaceholder: undefined,
  filterFields: undefined,
  listStyle: undefined,
  scrollHeight: undefined,
  virtualScrollerOptions: undefined,
  emptyMessage: undefined,
  emptyFilterMessage: undefined,
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
