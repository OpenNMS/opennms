
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
  <p>InputNumber Demo</p>
  <InputNumber
    id="inputNumber01"
    mode="decimal"
    showButtons
    :min="5"
    :max="10"
    :step="2"
    v-model="showInputNumber"
  />
  <p>Input Number :: {{ showInputNumber }}</p>
  <p>FieldSet Demo</p>
  <!-- <FeatherTextarea
    label="Fieldset Area"
    rows="5" /> -->
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

  <p>
    <FeatherButton primary>Save</FeatherButton>
  </p>

  <hr />
  <FeatherRadioGroup :label="'Favourite subject type?'" v-model="cityGroupName">
      <FeatherRadio
        v-for="item in categories"
        :value="item.name"
        :key="item.key"
        >{{ item.name }}</FeatherRadio
      >
    </FeatherRadioGroup>
</template>

<script setup lang="ts">

import { ref } from 'vue'
import InputText from '../InputText.vue'
import InputNumber from '../InputNumber.vue'
import DropDown from '../DropDown.vue'
import TimePicker from '../TimePicker.vue'
import { FeatherButton }   from '@featherds/button'
import { FeatherRadio, FeatherRadioGroup } from '@featherds/radio'
//import FeatherTextarea from '@featherds/textarea'

const city = ref('')
const showInputText = ref('')
const showInputNumber = ref()
const selectedCity1 = ref('')
const showTimePicker = ref('')
const cityGroupName = ref('')
const pcontent = ref('Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.')
const cities = ref([
  { name: 'New York', code: 'NY' },
  { name: 'Rome', code: 'RM' },
  { name: 'London', code: 'LDN' },
  { name: 'Istanbul', code: 'IST' },
  { name: 'Paris', code: 'PRS' }
])

const categories = ref([
  { name: 'Accounting', key: 'A' },
  { name: 'Marketing', key: 'M' },
  { name: 'Production', key: 'P' },
  { name: 'Research', key: 'R' }
])

const selectedValue = (val: string) => {
  cityGroupName.value = val
}

</script>
