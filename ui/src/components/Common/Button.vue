<template>
  <Button :label="label" :icon="icon" :badge="badge" :class="selectedClass"></Button>
</template> 

<script setup lang="ts">

import Button from 'primevue/button'
import { computed } from "vue"

const props = defineProps({
  label: {
    type: String,
    required: true
  },
  primeClass: {
    type: String,
    required: false
  },
  icon: {
    type: String,
    required: false
  },
  badge: {
    type: String,
    required: false
  }
})

//computed
const selectedClass = computed(() => {
  switch (props.primeClass) {
    case 'sm':
      return 'p-button-sm'
    case 'lg':
      return 'p-button-lg'
    case 'rounded':
      return 'p-button-rounded'
    case 'raised':
      return 'p-button-raised'
    default:
      return 'p-button'
  }
})


</script> 
