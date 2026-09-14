<template>
  <InputText
    :modelValue="modelValue"
    :invalid="invalid"
    :disabled="disabled"
    :placeholder="placeholder"
    :fluid="fluid"
    :pt="unsafePt as never"
    @update:modelValue="onUpdate"
    @change="onChange"
  />
</template>

<script setup lang="ts">
import InputText from 'primevue/inputtext'

// Seam wrapper (NMS-20029) around PrimeVue InputText. Supports v-model and
// v-model.trim (trim is applied here — the inner component ignores modifiers).
// id, type, autocomplete, aria-*, data-* and native events fall through to the
// underlying <input>.
const props = withDefaults(defineProps<{
  modelValue?: string
  modelModifiers?: { trim?: boolean }
  invalid?: boolean
  disabled?: boolean
  placeholder?: string
  // fluid: undefined allows parent Fluid-component context to be inherited;
  // explicit false would break that inheritance
  fluid?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  modelModifiers: () => ({}),
  invalid: false,
  disabled: false,
  placeholder: undefined,
  fluid: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: string | undefined]
}>()

const onUpdate = (value: string | undefined) => {
  emit('update:modelValue', props.modelModifiers?.trim && typeof value === 'string' ? value.trim() : value)
}

// Native v-model.trim trims the visible DOM value on the change event so the
// screen can't disagree with the model at submit time; mirror that here.
const onChange = (event: Event) => {
  if (props.modelModifiers?.trim) {
    const el = event.target as HTMLInputElement
    const trimmed = el.value.trim()
    if (el.value !== trimmed) {
      el.value = trimmed
    }
  }
}
</script>
