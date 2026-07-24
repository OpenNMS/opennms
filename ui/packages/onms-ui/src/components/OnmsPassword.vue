<template>
  <Password
    :modelValue="modelValue"
    :inputId="inputId"
    :invalid="invalid"
    :disabled="disabled"
    :toggleMask="toggleMask"
    :feedback="false"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import Password from 'primevue/password'

// Seam wrapper (NMS-20029) around PrimeVue Password: a masked input with an
// optional reveal toggle. Password-strength feedback is deliberately not part
// of the seam API.
withDefaults(defineProps<{
  modelValue?: string
  inputId?: string
  invalid?: boolean
  disabled?: boolean
  toggleMask?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  inputId: undefined,
  invalid: false,
  disabled: false,
  toggleMask: true,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: string | undefined]
}>()
</script>
