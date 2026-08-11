<template>
  <div class="onms-row">
    <div class="onms-col-12 wrapper">
      <OnmsButton
        variant="text"
        class="graph-controls"
        aria-haspopup="true"
        @click="toggleMenu"
      >
        {{ selectedTime }} &nbsp;
        <OnmsIcon :icon="ArrowDropDown" />
      </OnmsButton>

      <OnmsPopover
        ref="menu"
        class="graph-controls-panel"
      >
        <div class="menu-content">
          <ul class="onms-list options-col">
            <li
              class="list-item"
              v-for="option in TIME_RANGE_OPTIONS"
              :key="option.label"
              @click="selectOption(option)"
            >{{ option.label }}</li>
          </ul>

          <div class="custom-col">
            <FormField label="Start Date" class="date-input">
              <OnmsDatePicker v-model="startDateRef" />
            </FormField>
            <FormField label="Start Time">
              <OnmsSelect
                :options="HOUR_OPTIONS"
                v-model="startTimeRef"
                optionLabel="label"
              />
            </FormField>
            <FormField label="End Date" class="date-input">
              <OnmsDatePicker v-model="endDateRef" />
            </FormField>
            <FormField label="End Time">
              <OnmsSelect
                :options="HOUR_OPTIONS"
                v-model="endTimeRef"
                optionLabel="label"
              />
            </FormField>
            <OnmsButton
              :disabled="disableCustomTimeBtn"
              variant="text"
              @click="applyCustomTime"
            >Apply custom time</OnmsButton>
          </div>
        </div>
      </OnmsPopover>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import { OnmsButton, OnmsDatePicker, OnmsIcon, OnmsPopover, OnmsSelect } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { add, sub, getUnixTime, differenceInHours, fromUnixTime } from 'date-fns'
import ArrowDropDown from '@/components/icons/navigation/ArrowDropDown.vue'
import { HOUR_OPTIONS, TIME_RANGE_OPTIONS, TimeOption } from './utils/timeRangeOptions'

const emit = defineEmits(['updateTime'])

const menu = ref()
const startDateRef = ref()
const startTimeRef = ref<TimeOption>({ label: '1 PM', time: { hours: 13 }})
const endDateRef = ref()
const endTimeRef = ref<TimeOption>({ label: '1 PM', time: { hours: 13 }})

const selectedTime = ref('Last Day')

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

  // end - start, as Dates. This previously passed unix SECONDS in the wrong order,
  // so the difference was always negative and every custom range was labelled as
  // minutes.
  const difference = differenceInHours(fromUnixTime(endTime), fromUnixTime(startTime))

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
@import '@/styles/onms-typography';
.wrapper {
  height: 70px;
  .graph-controls {
    padding: 8px;
    max-height: 35px;
  }
}
</style>

<style lang="scss">
@import '@/styles/onms-typography';

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
      @include onms-body-small;
    }
  }
}
</style>
