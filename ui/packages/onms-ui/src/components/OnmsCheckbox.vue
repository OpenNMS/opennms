<template>
  <Checkbox
    :modelValue="modelValue"
    binary
    :inputId="inputId"
    :invalid="invalid"
    :disabled="disabled"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import Checkbox from 'primevue/checkbox'

// Seam wrapper (NMS-20029) around PrimeVue Checkbox, fixed to binary
// (boolean v-model) — the only mode OpenNMS uses. Pair with an external
// <label :for="inputId">.
withDefaults(defineProps<{
  modelValue?: boolean
  inputId?: string
  invalid?: boolean
  disabled?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: false,
  inputId: undefined,
  invalid: false,
  disabled: false,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>
