<template>
  <RadioButton
    :modelValue="modelValue"
    :value="value"
    :inputId="inputId"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import RadioButton from 'primevue/radiobutton'

// Seam wrapper (NMS-20081) around PrimeVue RadioButton. Widened to `unknown`
// (not `string`) — pre-implementation verification of the two current call
// sites found ProvisionDForm.vue's `config.rescanBehavior` is a `number`
// (rescanItems values are 1/0/2), not a string like SystemDefinitionCreationDrawer's
// `oidType`. Callers must cast at the handler.
withDefaults(defineProps<{
  modelValue?: unknown
  value?: unknown
  inputId?: string
  unsafePt?: unknown
}>(), {
  modelValue: undefined,
  value: undefined,
  inputId: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
}>()
</script>
