<template>
  <RadioButton :id="id" :name="name" :value="value" />
</template> 

<script setup lang="ts">

import RadioButton from 'primevue/radiobutton'

const props = defineProps({
  id: {
    type: String,
    required: true
  },
  name: {
    type: String,
    default: 'text',
    required: true
  },
  value: {
    type: String,
    required: true
  }
})
</script> 
