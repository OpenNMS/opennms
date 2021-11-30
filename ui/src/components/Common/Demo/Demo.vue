
<template>
  <p>InputText Demo</p>
  <p>Input Text :: {{ showInputText }}</p>
  <FeatherInput
    id="inputText01"
    class="test"
    data-ref-id="test"
    v-model="showInputText"
  >
  </FeatherInput>
  <hr />
  <p>InputNumber Demo</p>
  <FeatherInput
    v-model="showInputNumber"
    type="number"
    background
  />
  <p>Input Number :: {{ showInputNumber }}</p>
  <p>FieldSet Demo</p>
  <!-- <FeatherTextarea
    label="Fieldset Area"
    rows="5" /> -->
  <p>Dropdown Demo</p>
  <FeatherDropdown>
    <template v-slot:trigger :right="right" :cover="cover" :standard="standard">
      <FeatherButton link href="#" menu-trigger>
        {{selectedDropDownValue==="" ? "Select City Name": selectedDropDownValue}}
      </FeatherButton>
    </template>
    <FeatherDropdownItem v-for="city in cities" :key="city" @click="dropdownChange(city.name)">
      {{ city.name }}
    </FeatherDropdownItem>
  </FeatherDropdown>

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
import TimePicker from '../TimePicker.vue'
import { FeatherButton }   from '@featherds/button'
import { FeatherRadio, FeatherRadioGroup } from '@featherds/radio'
import { FeatherInput } from '@featherds/input'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'

const selectedDropDownValue = ref('')
const showInputText = ref('')
const showInputNumber = ref()
const showTimePicker = ref('')
const cityGroupName = ref('')
const right = ref(false)
const cover = ref(false)
const standard = ref(false)
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

const dropdownChange = (val: string) => {
  selectedDropDownValue.value = val
}

</script>
