<template>
  <ToggleSwitch
    :modelValue="modelValue"
    :inputId="inputId"
    :disabled="disabled"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import ToggleSwitch from 'primevue/toggleswitch'

// Seam wrapper (NMS-20081) around PrimeVue ToggleSwitch. Boolean-only
// (trueValue/falseValue are not exposed). data-* / aria-* fall through.
withDefaults(defineProps<{
  modelValue?: boolean
  inputId?: string
  disabled?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  inputId: undefined,
  disabled: false,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>
