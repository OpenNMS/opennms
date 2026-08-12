<template>
  <DatePicker
    :modelValue="modelValue"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event as Date | null)"
  />
</template>

<script setup lang="ts">
import DatePicker from 'primevue/datepicker'

// Seam wrapper (NMS-20081) around PrimeVue DatePicker.
withDefaults(defineProps<{
  modelValue?: Date | null
  unsafePt?: unknown
}>(), {
  modelValue: null,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: Date | null]
}>()
</script>
