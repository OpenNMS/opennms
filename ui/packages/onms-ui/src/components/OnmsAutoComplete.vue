<template>
  <AutoComplete
    ref="autoCompleteRef"
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
    :dropdownMode="dropdownMode"
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
import { ref } from 'vue'

// Seam wrapper around PrimeVue AutoComplete. `complete` fires with
// the raw query string (the seam does not expose PrimeVue's event object);
// `optionSelect` fires with the selected value. `clearInput()` is exposed for
// callers that offer their own clear affordance.
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
  // dropdownMode: 'blank' clears the query on dropdown click, 'current' keeps it
  // and searches from the current value; undefined preserves PrimeVue's default.
  dropdownMode?: 'blank' | 'current'
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
  dropdownMode: undefined,
  multiple: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
  complete: [query: string]
  optionSelect: [value: unknown]
}>()

// PrimeVue's AutoComplete type doesn't declare $el, so narrow to what's used.
const autoCompleteRef = ref<{ $el?: HTMLElement } | null>(null)

const inputElement = () => {
  const el = autoCompleteRef.value?.$el

  return el instanceof HTMLInputElement ? el : el?.querySelector('input') ?? null
}

// Clears the text the user has typed but not yet turned into a selection.
// In `multiple` mode that text has no model to reset — PrimeVue's inner input is
// uncontrolled there (it binds `value` only in single mode), so the query lives
// in the DOM and a caller cannot reach it through `modelValue`. Resetting the
// selection is still the caller's job, via `modelValue`.
//
// The input event is dispatched rather than just assigning `value`: PrimeVue
// schedules its own suggestion fetch on a `delay` timer (300ms) from its `onInput`
// handler, and only that handler cancels it. A silent assignment leaves the timer
// armed, so a clear within the delay window still fires `complete` with the stale
// query and the caller re-runs the search it just cancelled. Going through
// `onInput` with an empty value clears the timer and closes the overlay too.
const clearInput = () => {
  const input = inputElement()

  if (input) {
    input.value = ''
    input.dispatchEvent(new Event('input', { bubbles: true }))
  }
}

const focus = () => inputElement()?.focus()

defineExpose({ clearInput, focus })
</script>
