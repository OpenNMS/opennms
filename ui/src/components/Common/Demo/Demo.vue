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
  <p>FieldSet Demo</p>
  <FieldSet>
    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor 
      incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, 
      quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. 
      Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu 
      fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident,
       sunt in culpa qui officia deserunt mollit anim id est laborum. </p>
  </FieldSet>

  <p>Dropdown Demo</p>
  <DropDown
    v-model="selectedCity1"
    :options="cities"
    optionLabel="name"
    optionValue="code"
    placeholder="City Name"
    :editable="true"
    :filter="true"
  ></DropDown>

  <hr />
  <p>TimePicker Demo</p>
  <p>Selected Time :: {{ showTimePicker }}</p>
  <TimePicker id="timepicker01" v-model="showTimePicker" />
  <hr />
</template>

<script setup lang="ts">

import { ref } from 'vue'
import InputText from '../InputText.vue'
import DropDown from '../DropDown.vue'
import FieldSet from '../FieldSet.vue';
import TimePicker from '../TimePicker.vue'

const showInputText = ref('');
const selectedCity1 = ref('');
const pcontent = ref('Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.')
const cities = ref( [
  { name: 'New York', code: 'NY' },
  { name: 'Rome', code: 'RM' },
  { name: 'London', code: 'LDN' },
  { name: 'Istanbul', code: 'IST' },
  { name: 'Paris', code: 'PRS' }
]);
const showInputText = ref('');
const showTimePicker = ref('');

</script>