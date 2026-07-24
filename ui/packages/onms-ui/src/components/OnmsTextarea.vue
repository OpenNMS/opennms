<template>
  <Textarea
    :modelValue="modelValue"
    :rows="rows"
    :invalid="invalid"
    :disabled="disabled"
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
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  rows: 3,
  invalid: false,
  disabled: false,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: string | undefined]
}>()
</script>
