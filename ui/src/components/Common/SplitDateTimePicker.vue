<template>
  <span class="split-date-time">
    <label
      class="visually-hidden"
      :for="`${idPrefix}-date`"
    >{{ label }} date</label>
    <OnmsDatePicker
      :modelValue="dayToDate(datePart)"
      :inputId="`${idPrefix}-date`"
      class="date-input"
      :data-test="`${dataTest}-date`"
      @update:modelValue="v => emit('update:modelValue', join(dateToDay(v), timePart))"
    />
    <label
      class="visually-hidden"
      :for="`${idPrefix}-time`"
    >{{ label }} time</label>
    <OnmsDatePicker
      :modelValue="clockToDate(timePart)"
      :inputId="`${idPrefix}-time`"
      timeOnly
      hourFormat="12"
      class="time-input"
      :data-test="`${dataTest}-time`"
      @update:modelValue="v => emit('update:modelValue', join(datePart, dateToClock(v)))"
    />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { OnmsDatePicker } from '@opennms/onms-ui'

import { clockToDate, dateToClock, dateToDay, dayToDate } from '@/lib/pickerTime'

// A wall-clock date and time ('yyyy-MM-ddTHH:mm') picked as a date and a separate time. One date-and-time picker works
// on a real browser-local Date, so it can't hold a time the browser's clocks skip that day for daylight saving
// (02:30 on a spring-forward day), though the server's zone may well have it. Picked apart, the date is a day and the
// time a clock time, and neither shifts. Either part may be empty while the other is picked; the caller checks the result.
const props = defineProps<{
  modelValue: string
  // prefixes the input ids, which must be unique on the page
  idPrefix: string
  // names the inputs for screen readers: "<label> date", "<label> time"
  label: string
  // the date picker gets `<dataTest>-date`, the time picker `<dataTest>-time`
  dataTest: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

// split on the T, not by position: either side may be empty ('T08:00', '2026-03-08T')
const datePart = computed(() => props.modelValue.split('T')[0] ?? '')
const timePart = computed(() => (props.modelValue.split('T')[1] ?? '').slice(0, 5))

const join = (day: string, time: string) => `${day}T${time}`
</script>

<style lang="scss" scoped>
.split-date-time {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.date-input {
  width: 9rem;
}

.time-input {
  width: 7.5rem;
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}
</style>
