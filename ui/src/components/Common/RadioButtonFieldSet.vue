<template>
  <div v-for="category of radioValues" :key="category.key" class="p-field-radiobutton">
    <RadioButton
      :id="category.key"
      :name="name"
      :value="category.name"
      v-model="selectedVal"
      @change="onSelect"
    />
    <label :for="category.key">{{ category.name }}</label>
  </div>
</template> 

<script setup lang="ts">

import { ref } from "vue"
import RadioButton from "./RadioButton.vue"

const selectedVal = ref('')
const props = defineProps({
  name: {
    type: String,
    required: true
  },
  radioValues: {
    type: Object,
    required: true
  }
})
const emit = defineEmits(['selectedRadioVal'])
const onSelect = () => {
  emit("selectedRadioVal", selectedVal.value)
}
</script> 
