<template>
  <div class="onms-row">
    <div class="onms-col-12 wrapper">
      <PButton
        text
        class="graph-controls"
        aria-haspopup="true"
        @click="toggleMenu"
      >
        {{ selectedTime }} &nbsp;
        <FeatherIcon :icon="ArrowDropDown" />
      </PButton>

      <PPopover
        ref="menu"
        class="graph-controls-panel"
      >
        <div class="menu-content">
          <ul class="onms-list options-col">
            <li
              class="list-item"
              v-for="option in options"
              :key="option.label"
              @click="selectOption(option)"
            >{{ option.label }}</li>
          </ul>

          <div class="custom-col">
            <FormField label="Start Date" class="date-input">
              <PDatePicker v-model="startDateRef" />
            </FormField>
            <FormField label="Start Time">
              <PSelect
                :options="times"
                v-model="startTimeRef"
                optionLabel="label"
              />
            </FormField>
            <FormField label="End Date" class="date-input">
              <PDatePicker v-model="endDateRef" />
            </FormField>
            <FormField label="End Time">
              <PSelect
                :options="times"
                v-model="endTimeRef"
                optionLabel="label"
              />
            </FormField>
            <PButton
              :disabled="disableCustomTimeBtn"
              text
              @click="applyCustomTime"
            >Apply custom time</PButton>
          </div>
        </div>
      </PPopover>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import Button from 'primevue/button'
import Popover from 'primevue/popover'
import DatePicker from 'primevue/datepicker'
import Select from 'primevue/select'
import FormField from '@/components/Common/FormField.vue'
import { add, sub, getUnixTime, differenceInHours } from 'date-fns'
import { FeatherIcon } from '@featherds/icon'
import ArrowDropDown from '@featherds/icon/navigation/ArrowDropDown'

const PButton = Button
const PPopover = Popover
const PDatePicker = DatePicker
const PSelect = Select

interface TimeOption {
  label: string
  time: Record<string, unknown>
}

const emit = defineEmits(['updateTime'])

const menu = ref()
const startDateRef = ref()
const startTimeRef = ref<TimeOption>({ label: '1 PM', time: { hours: '1' }})
const endDateRef = ref()
const endTimeRef = ref<TimeOption>({ label: '1 PM', time: { hours: '1' }})

const selectedTime = ref('Last Day')
const options = [
  { label: 'Last hour', time: { minutes: '60' }},
  { label: 'Last 2 hours', time: { hours: '2' }},
  { label: 'Last 4 hours', time: { hours: '4' }},
  { label: 'Last 8 hours', time: { hours: '5' }},
  { label: 'Last 12 hours', time: { hours: '12' }},
  { label: 'Last day', time: { hours: '24' }},
  { label: 'Last two days', time: { hours: '48' }},
  { label: 'Last week', time: { days: '7' }},
  { label: 'Last month', time: { months: '1' }},
  { label: 'Last three months', time: { months: '3' }},
  { label: 'Last six months', time: { months: '6' }},
  { label: 'Last year', time: { years: '1' }}
]

const times = [
  { label: '12 AM', time: { hours: '0' }},
  { label: '1 AM', time: { hours: '1' }},
  { label: '2 AM', time: { hours: '2' }},
  { label: '3 AM', time: { hours: '3' }},
  { label: '4 AM', time: { hours: '4' }},
  { label: '5 AM', time: { hours: '5' }},
  { label: '6 AM', time: { hours: '6' }},
  { label: '7 AM', time: { hours: '7' }},
  { label: '8 AM', time: { hours: '8' }},
  { label: '9 AM', time: { hours: '9' }},
  { label: '10 AM', time: { hours: '10' }},
  { label: '11 AM', time: { hours: '11' }},
  { label: '12 PM', time: { hours: '12' }},
  { label: '1 PM', time: { hours: '13' }},
  { label: '2 PM', time: { hours: '14' }},
  { label: '3 PM', time: { hours: '15' }},
  { label: '4 PM', time: { hours: '16' }},
  { label: '5 PM', time: { hours: '17' }},
  { label: '6 PM', time: { hours: '18' }},
  { label: '7 PM', time: { hours: '19' }},
  { label: '8 PM', time: { hours: '20' }},
  { label: '9 PM', time: { hours: '21' }},
  { label: '10 PM', time: { hours: '22' }},
  { label: '11 PM', time: { hours: '23' }}
]

const disableCustomTimeBtn = computed(() => Boolean(!startDateRef.value || !startTimeRef.value || !endDateRef.value || !endTimeRef.value))

const toggleMenu = (event: Event) => menu.value.toggle(event)

const selectOption = (option: TimeOption) => {
  selectedTime.value = option.label
  const now = new Date()
  const startTime = getUnixTime(sub(now, option.time))
  const endTime = getUnixTime(now)
  const format = Object.keys(option.time)[0]

  emit('updateTime', {
    startTime,
    endTime,
    format
  })

  menu.value.hide()
}

const applyCustomTime = () => {
  let format = 'hours'
  const startTime = getUnixTime(add(startDateRef.value, startTimeRef.value.time))
  const endTime = getUnixTime(add(endDateRef.value, endTimeRef.value.time))

  const difference = differenceInHours(startTime, endTime)
  if (difference < 1) {
    format = 'minutes'
  }
  if (difference > 24) {
    format = 'days'
  }
  if (difference > 8766) {
    format = 'years'
  }

  emit('updateTime', {
    startTime,
    endTime,
    format
  })

  selectedTime.value = 'Custom Time'
  menu.value.hide()
}
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
.wrapper {
  height: 70px;
  .graph-controls {
    padding: 8px;
    max-height: 35px;
  }
}
</style>

<style lang="scss">
@import "@featherds/styles/mixins/typography";

.graph-controls-panel {
  .menu-content {
    display: flex;
    gap: 2rem;
    min-width: 40em;
    max-width: 550px;
  }

  .options-col {
    flex: 0 0 40%;
    list-style: none;
    padding: 0;
    margin: 0;

    .list-item {
      padding: 0.5rem 0.75rem;
      cursor: pointer;

      &:hover {
        background: var(--p-highlight-background);
      }
    }
  }

  .custom-col {
    flex: 1;

    .date-input {
      @include body-small;
    }
  }
}
</style>
