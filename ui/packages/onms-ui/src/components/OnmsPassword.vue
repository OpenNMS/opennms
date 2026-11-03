<template>
  <Password
    :modelValue="modelValue"
    :inputId="inputId"
    :invalid="invalid"
    :disabled="disabled"
    :toggleMask="toggleMask"
    :feedback="false"
    :fluid="fluid"
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
  // fluid: undefined allows parent Fluid-component context to be inherited;
  // explicit false would break that inheritance
  fluid?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  inputId: undefined,
  invalid: false,
  disabled: false,
  toggleMask: true,
  fluid: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: string | undefined]
}>()
</script>
