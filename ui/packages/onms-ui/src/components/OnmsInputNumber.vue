<template>
  <InputNumber
    :modelValue="modelValue"
    :min="min"
    :max="max"
    :step="step"
    :useGrouping="useGrouping"
    :maxFractionDigits="maxFractionDigits"
    :invalid="invalid"
    :disabled="disabled"
    :fluid="fluid"
    :inputId="inputId"
    :pt="pt as never"
    @update:modelValue="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import InputNumber from 'primevue/inputnumber'

// Seam wrapper (NMS-20081) around PrimeVue InputNumber. Grouping separators
// are OFF by design (every OpenNMS numeric field is a port/count/interval —
// PrimeVue's own default is true). inputProps carries plain DOM attrs
// (data-test, aria-label) to the inner <input>.
//
// PrimeVue-reality: installed primevue@4.5.5's InputNumber does not declare
// an `inputProps` prop at all (confirmed against
// node_modules/primevue/inputnumber/index.mjs — that prop exists only on
// CascadeSelect, InputChips, Password and TreeSelect). Binding it directly as
// `:inputProps="inputProps"` would be silently inert: InputNumber's render
// function passes an explicit, fixed prop list to its internally-nested
// InputText, with no $attrs spread, so the object would only ever land as a
// useless `inputprops="[object Object]"` attribute on the wrapper's root
// <span> (verified via DOM probe). InputNumber does forward
// `pt.pcInputText` straight through to that nested InputText though, and
// InputText's own `ptmi('root', ...)` merges pt.root onto its rendered
// <input> element — so routing inputProps through `pt.pcInputText.root` is
// the PrimeVue-native way to land plain DOM attrs on the real <input>
// (verified: `data-test`/`aria-label` passed this way appear on the <input>,
// not the outer span).
const props = withDefaults(defineProps<{
  modelValue?: number | null
  min?: number
  max?: number
  step?: number
  useGrouping?: boolean
  maxFractionDigits?: number
  invalid?: boolean
  disabled?: boolean
  // fluid: undefined preserves parent Fluid-component context inheritance
  fluid?: boolean
  inputId?: string
  inputProps?: Record<string, unknown>
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  min: undefined,
  max: undefined,
  step: undefined,
  useGrouping: false,
  maxFractionDigits: undefined,
  invalid: false,
  disabled: false,
  fluid: undefined,
  inputId: undefined,
  inputProps: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: number | null]
}>()

const pt = computed(() => {
  if (props.inputProps === undefined && props.unsafePt === undefined) {
    return undefined
  }

  if (props.inputProps === undefined) {
    return props.unsafePt
  }

  // Deep-merge inputProps into unsafePt.pcInputText.root instead of clobbering
  // it, so a caller's unsafePt.pcInputText survives alongside inputProps.
  // inputProps wins on key collisions within root.
  const base = props.unsafePt as Record<string, unknown> | undefined
  const existing = (base?.pcInputText as Record<string, unknown> | undefined)?.root
  return {
    ...base,
    pcInputText: {
      ...(base?.pcInputText as Record<string, unknown> | undefined),
      root: { ...(existing as Record<string, unknown> | undefined), ...props.inputProps }
    }
  }
})
</script>
