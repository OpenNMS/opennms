<template>
  <TimeSafeDatePicker
    :modelValue="modelValue"
    :showTime="showTime"
    :timeOnly="timeOnly"
    :hourFormat="hourFormat"
    :showSeconds="showSeconds"
    :stepHour="stepHour"
    :stepMinute="stepMinute"
    :stepSecond="stepSecond"
    :placeholder="placeholder"
    :inputId="inputId"
    :minDate="minDate ?? undefined"
    :maxDate="maxDate ?? undefined"
    :pt="unsafePt as never"
    @update:modelValue="emit('update:modelValue', $event as Date | null)"
    @show="emit('show')"
    @hide="emit('hide')"
  />
</template>

<script setup lang="ts">
import { type ComponentOptions, defineComponent } from 'vue'
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
// inputId lands on the rendered <input>, not the wrapper element. A plain `id`
// falling through would settle on PrimeVue's wrapper, out of reach of a
// FormField label's `for`, so the association needs this dedicated prop --
// the same reason OnmsSelect, OnmsInputNumber and friends declare one.
//
// PrimeVue-reality: minDate/maxDate are typed `Date | undefined` there, and any
// non-undefined value is treated as a live bound. Consumers hold bounds in
// nullable refs (TimeControls' startDateRef/endDateRef), so this wrapper
// accepts null and collapses it to undefined instead of pushing that `?? undefined`
// onto every call site.
// Time-only and daylight saving (NMS-20534): a time-only value is a Date whose date part means nothing, but PrimeVue
// builds it on today's date: a typed time with `new Date()` (parseDateTime), and a spinner step on an empty picker
// from today too (viewDate). On the day the browser's clocks go forward, 02:30 does not exist, and the Date becomes
// 03:30. In time-only mode this subclass puts both on a fixed day instead (10 January 2000, on which no zone changes
// its clocks), so every clock time can be picked. A spinner step on a picker that has a value already keeps that
// value's date, which the consumer chose. PrimeVue-reality: these are internals of primevue@4.5.5's DatePicker, pinned
// by the daylight-saving tests in tests/onms-ui/OnmsDatePicker.test.ts.
const TIME_ONLY_DAY: [number, number, number] = [2000, 0, 10]

type DatePickerInternals = {
  timeOnly: boolean
  rawValue: unknown
}
const base = DatePicker as unknown as {
  render: unknown
  methods: { populateTime: (this: DatePickerInternals, value: Date, timeString: string, ampm?: string) => void }
  computed: { viewDate: (this: DatePickerInternals) => Date }
}

const TimeSafeDatePicker = defineComponent({
  // still PrimeVue's DatePicker in devtools and tests
  name: 'DatePicker',
  extends: DatePicker as unknown as ComponentOptions,
  render: base.render as never,
  computed: {
    viewDate(this: DatePickerInternals): Date {
      const date = base.computed.viewDate.call(this)
      if (!this.timeOnly || this.rawValue instanceof Date) {
        return date
      }
      return new Date(...TIME_ONLY_DAY, date.getHours(), date.getMinutes(), date.getSeconds())
    }
  },
  methods: {
    populateTime(this: DatePickerInternals, value: Date, timeString: string, ampm?: string): void {
      if (this.timeOnly) {
        value.setFullYear(...TIME_ONLY_DAY)
      }
      base.methods.populateTime.call(this, value, timeString, ampm)
    }
  }
  // the same props, emits and slots as the DatePicker it extends, so the template below is still type-checked
}) as unknown as typeof DatePicker

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
  inputId?: string
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
  inputId: undefined,
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
