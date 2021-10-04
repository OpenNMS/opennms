<template>
  <FeatherRadio
    :label="label"
    :modelValue="modelValue"
    :hint="hint"
    :error="error"
    :vertical="vertical"
  />
</template> 

<script setup lang="ts">
import { FeatherRadio } from "@featherds/radio"
defineProps({
  label: {
    type: String,
    required: true
  },
  modelValue: {
    type: String,
  },
  hint: {
    type: String,
  },
  error: {
    type: String,
  },
  vertical: {
    type: Boolean,
  }
})
</script> 
