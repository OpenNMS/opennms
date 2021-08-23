<template>
  <p>InputText Demo</p>
  <p>Input Text :: {{ showInputText }}</p>
  <InputText
    id="inputText01"
    type="text"
    placeholder="Please Enter Text"
    v-model="showInputText"
  />
</template>

<script setup lang="ts">

import { ref } from 'vue'
import InputText from '../InputText.vue'

const showInputText = ref('');

</script>