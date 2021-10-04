<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <FeatherInput label="Input Demo" v-model="inputModel" />
      <p>Input Text :: {{ inputModel }}</p>
    </div>
  </div>

  <div class="feather-row">
    <div class="feather-col-12">
      <Button primary>Save</Button>
    </div>
  </div>

  <p>FieldSet Demo</p>
  <FieldSet>
    <p>
      Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
      incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,
      quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
      Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu
      fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident,
      sunt in culpa qui officia deserunt mollit anim id est laborum.
    </p>
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

  <hr />
  <FeatherRadioGroup :label="'Radio btn demo'" v-model="city">
    <RadioButton label="Chicago" value="Chicago" :disabled="true" />
    <RadioButton label="Los Angeles" value="Los Angeles" />
    <RadioButton label="New York" value="New York" />
    <RadioButton label="San Francisco" value="San Francisco" />
  </FeatherRadioGroup>

  <hr />
  <label>selected radio button value : {{ city }}</label>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import DropDown from '../DropDown.vue'
import FieldSet from '../FieldSet.vue'
import TimePicker from '../TimePicker.vue'
import Button from '../Button.vue'
import RadioButton from '../RadioButton.vue'
import { FeatherInput } from "@featherds/input"
import { FeatherRadioGroup } from "@featherds/radio"

const city = ref('Chicago')
const inputModel = ref('')
const selectedCity1 = ref('')
const showTimePicker = ref('')
const cityGroupName = ref('')
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
