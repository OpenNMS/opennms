<template>
  <AutoComplete
    :modelValue="modelValue"
    :suggestions="suggestions"
    :optionLabel="optionLabel"
    :placeholder="placeholder"
    :inputId="inputId"
    :invalid="invalid"
    :disabled="disabled"
    :forceSelection="forceSelection"
    :fluid="fluid"
    :dropdown="dropdown"
    :multiple="multiple"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event)"
    @complete="emit('complete', $event.query)"
    @option-select="emit('optionSelect', $event.value)"
  >
    <template
      v-if="$slots.empty"
      #empty
    >
      <slot name="empty" />
    </template>
    <template
      v-if="$slots.footer"
      #footer
    >
      <slot name="footer" />
    </template>
    <template
      v-if="$slots.option"
      #option="slotProps"
    >
      <slot
        name="option"
        v-bind="slotProps"
      />
    </template>
  </AutoComplete>
</template>

<script setup lang="ts">
import AutoComplete from 'primevue/autocomplete'

// Seam wrapper around PrimeVue AutoComplete. `complete` fires with
// the raw query string (the seam does not expose PrimeVue's event object);
// `optionSelect` fires with the selected value.
withDefaults(defineProps<{
  modelValue?: unknown
  suggestions?: unknown[]
  optionLabel?: string
  placeholder?: string
  inputId?: string
  invalid?: boolean
  disabled?: boolean
  forceSelection?: boolean
  // fluid: undefined allows parent Fluid-component context to be inherited;
  // explicit false would break that inheritance
  fluid?: boolean
  dropdown?: boolean
  // multiple: undefined preserves PrimeVue's default; when true, the model
  // switches to an array of selected values (chips mode)
  multiple?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  suggestions: () => [],
  optionLabel: undefined,
  placeholder: undefined,
  inputId: undefined,
  invalid: false,
  disabled: false,
  forceSelection: false,
  fluid: undefined,
  dropdown: undefined,
  multiple: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
  complete: [query: string]
  optionSelect: [value: unknown]
}>()
</script>
