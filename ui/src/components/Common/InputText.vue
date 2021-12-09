<template>
  <InputText :id="id" :type="type" :placeholder="placeholder" :class="selectedClass" />
</template> 

<script setup lang="ts">

import InputText from 'primevue/inputtext'
import { computed } from "vue"

const props = defineProps({
  id: {
    type: String,
    required: false
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    required: false
  },
  primeClass: {
    type: String,
    required: false
  }
})

//computed
const selectedClass = computed(() => {
  switch (props.primeClass) {
    case 'sm':
      return 'p-inputtext-sm'
    case 'lg':
      return 'p-inputtext-lg'
    default:
      return ''
  }
})


</script> 
