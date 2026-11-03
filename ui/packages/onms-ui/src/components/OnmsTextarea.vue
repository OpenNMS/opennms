<template>
  <Textarea
    :modelValue="modelValue"
    :rows="rows"
    :invalid="invalid"
    :disabled="disabled"
    :autoResize="autoResize"
    :fluid="fluid"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import Textarea from 'primevue/textarea'

// Seam wrapper (NMS-20029) around PrimeVue Textarea. id, aria-*, data-* and
// native events fall through to the underlying <textarea>.
withDefaults(defineProps<{
  modelValue?: string
  rows?: number | string
  invalid?: boolean
  disabled?: boolean
  autoResize?: boolean
  // fluid: undefined allows parent Fluid-component context to be inherited;
  // explicit false would break that inheritance
  fluid?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  rows: 3,
  invalid: false,
  disabled: false,
  autoResize: undefined,
  fluid: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: string | undefined]
}>()
</script>
