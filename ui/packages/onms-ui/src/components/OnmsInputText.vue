<template>
  <InputText
    :modelValue="modelValue"
    :invalid="invalid"
    :disabled="disabled"
    :placeholder="placeholder"
    :pt="unsafePt as never"
    @update:modelValue="onUpdate"
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
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  modelModifiers: () => ({}),
  invalid: false,
  disabled: false,
  placeholder: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: string | undefined]
}>()

const onUpdate = (value: string | undefined) => {
  emit('update:modelValue', props.modelModifiers?.trim && typeof value === 'string' ? value.trim() : value)
}
</script>
