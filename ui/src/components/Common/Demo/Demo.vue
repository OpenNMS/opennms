<template>
  <p>InputText Demo</p>
  <p>Input Text :: {{ showInputText }}</p>
  <InputText
    id="inputText01"
    type="text"
    placeholder="Please Enter Text"
    primeClass="lg"
    v-model="showInputText"
  />
  <hr />
  <p>TimePicker Demo</p>
  <p>Selected Time :: {{ showTimePicker }}</p>
  <TimePicker id="timepicker01" v-model="showTimePicker" />
  <hr />
</template>

<script setup lang="ts">

import { ref } from 'vue'
import InputText from '../InputText.vue'
import TimePicker from '../TimePicker.vue'

const showInputText = ref('');
const showTimePicker = ref('');

</script>