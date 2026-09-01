<template>
  <DatePicker
    :modelValue="modelValue"
    :showTime="showTime"
    :timeOnly="timeOnly"
    :hourFormat="hourFormat"
    :showSeconds="showSeconds"
    :stepHour="stepHour"
    :stepMinute="stepMinute"
    :stepSecond="stepSecond"
    :placeholder="placeholder"
    :minDate="minDate ?? undefined"
    :maxDate="maxDate ?? undefined"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event as Date | null)"
    @show="emit('show')"
    @hide="emit('hide')"
  />
</template>

<script setup lang="ts">
import DatePicker from 'primevue/datepicker'
import type { OnmsHourFormat } from '../types'

// Seam wrapper (NMS-20081) around PrimeVue DatePicker.
//
// Time support (NMS-20280): showTime / timeOnly / hourFormat / showSeconds are
// generic enough to survive a framework swap, so they are declared rather than
// left to attribute fallthrough. `timeOnly` covers the time-only case on its
// own -- PrimeVue gates the time panel on `showTime || timeOnly` and the date
// panel on `!timeOnly` -- so there is no separate OnmsTimePicker.
//
// hourFormat is typed as the owned OnmsHourFormat rather than PrimeVue's
// `HintedString<'12' | '24'>` (seam rule 4: no PrimeVue types in a public
// signature). All defaults below match installed primevue@4.5.5's own, so
// declaring these props changes no existing consumer's rendering.
//
// PrimeVue-reality: minDate/maxDate are typed `Date | undefined` there, and any
// non-undefined value is treated as a live bound. Consumers hold bounds in
// nullable refs (TimeControls' startDateRef/endDateRef), so this wrapper
// accepts null and collapses it to undefined instead of pushing that `?? undefined`
// onto every call site.
withDefaults(defineProps<{
  modelValue?: Date | null
  showTime?: boolean
  timeOnly?: boolean
  hourFormat?: OnmsHourFormat
  showSeconds?: boolean
  stepHour?: number
  stepMinute?: number
  stepSecond?: number
  placeholder?: string
  minDate?: Date | null
  maxDate?: Date | null
  unsafePt?: unknown
}>(), {
  modelValue: null,
  showTime: false,
  timeOnly: false,
  hourFormat: '24',
  showSeconds: false,
  stepHour: 1,
  stepMinute: 1,
  stepSecond: 1,
  placeholder: undefined,
  minDate: undefined,
  maxDate: undefined,
  unsafePt: undefined
})

// show/hide report overlay visibility in framework-neutral terms. TimeControls
// needs them so a click elsewhere in its popover can dismiss an open picker
// instead of being treated as a range selection; without them a consumer's only
// route is reaching into PrimeVue internals.
const emit = defineEmits<{
  'update:modelValue': [value: Date | null]
  show: []
  hide: []
}>()
</script>
