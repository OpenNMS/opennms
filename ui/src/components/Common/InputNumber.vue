<template>
  <PrimeVueInputNumber :id="id" :mode="mode" :min="min" :max="max" :step="step" />
</template> 

<script setup lang="ts">

import PrimeVueInputNumber from 'primevue/inputnumber'
interface InputNumberProps {
  id?: string
  mode?: string
  min?: number
  max?: number
  step?: number
}

const props = withDefaults(defineProps<InputNumberProps>(), {
  mode: "decimal"
})

</script> 
