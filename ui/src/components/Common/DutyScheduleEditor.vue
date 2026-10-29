<template>
  <div class="duty-schedule-editor" data-test="duty-schedules">
    <p
      v-if="!rows.length"
      class="no-schedules"
      data-test="no-duty-schedules"
    >No duty schedules; this user is available at all times.</p>
    <div
      v-for="(row, index) in rows"
      :key="row.id"
      class="duty-row"
      :data-test="`duty-row-${index}`"
    >
      <template v-if="row.raw === null">
        <div
          class="day-toggles"
          role="group"
          aria-label="Days of the week"
        >
          <OnmsButton
            v-for="day in DAYS"
            :key="day"
            :variant="row.days.includes(day) ? undefined : 'outlined'"
            size="small"
            :label="day"
            :aria-pressed="row.days.includes(day)"
            :data-test="`duty-${index}-day-${day}`"
            @click="toggleDay(row, day)"
          />
        </div>
        <input
          v-model="row.begin"
          type="time"
          class="time-input"
          aria-label="Begin time"
          :data-test="`duty-${index}-begin`"
          @input="markEdited(row)"
        >
        <span class="to">to</span>
        <input
          v-model="row.end"
          type="time"
          class="time-input"
          aria-label="End time"
          :data-test="`duty-${index}-end`"
          @input="markEdited(row)"
        >
        <small
          v-if="!hasDay(row)"
          class="row-error"
          role="alert"
          :data-test="`duty-${index}-error`"
        >Pick at least one day</small>
        <small
          v-if="isOvernight(row)"
          :class="overnightBlocks(row) ? 'row-error' : 'row-warning'"
          :data-test="`duty-${index}-overnight`"
        >The begin time must be before the end time. Split overnight coverage into two schedules.</small>
        <small
          v-else-if="shortWindow(row) !== null"
          class="row-warning"
          :data-test="`duty-${index}-warning`"
        >This window is only {{ shortWindow(row) }} minutes long.</small>
      </template>
      <!-- a hand-edited entry the day/time form can't represent stays editable
           as raw text so it is never silently dropped -->
      <OnmsInputText
        v-else
        v-model="row.raw"
        class="raw-input"
        fluid
        :data-test="`duty-${index}-raw`"
      />
      <OnmsIconButton
        severity="danger"
        :icon="Cancel"
        :aria-label="`Remove duty schedule ${index + 1}`"
        :data-test="`remove-duty-${index}`"
        @click="removeRow(index)"
      />
    </div>
    <OnmsButton
      variant="outlined"
      size="small"
      icon="pi pi-plus"
      label="Add Schedule"
      data-test="add-duty-button"
      @click="addRow"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'

import Cancel from '@opennms/onms-ui/icons/navigation/Cancel.vue'

const props = defineProps<{
  modelValue: string[]
}>()

const emit = defineEmits<{
  'update:modelValue': [string[]]
  'update:valid': [boolean]
}>()

// canonical day order; a schedule string concatenates a 1-7 subset of these
const DAYS = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su']
const SCHEDULE = /^((?:Mo|Tu|We|Th|Fr|Sa|Su){1,7})(\d{1,4})-(\d{1,4})$/

interface Row {
  id: number
  days: string[]
  begin: string
  end: string
  // non-null only for entries the structured form can't represent
  raw: string | null
  // the untouched incoming string; serialized back byte-identically until the
  // user edits the row, so merely opening the dialog can never rewrite a
  // schedule the server only grandfathers in its original form
  original: string | null
}

let nextId = 0
const rows = ref<Row[]>([])

// military HMM/HHMM -> HH:MM for the native time input
const toHHMM = (military: string): string => {
  const n = Number.parseInt(military, 10)
  const hours = Math.floor(n / 100)
  const minutes = n % 100
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`
}

// HH:MM -> the hour*100+minute form the schedule string uses
const toMilitary = (hhmm: string): string => {
  const [hours, minutes] = hhmm.split(':')
  return String(Number.parseInt(hours, 10) * 100 + Number.parseInt(minutes, 10))
}

const parse = (value: string): Row => {
  const match = SCHEDULE.exec(value.trim())
  if (!match) {
    return { id: nextId++, days: [], begin: '', end: '', raw: value, original: null }
  }
  return {
    id: nextId++,
    days: match[1].match(/../g) ?? [],
    begin: toHHMM(match[2]),
    end: toHHMM(match[3]),
    raw: null,
    original: value.trim()
  }
}

// any user edit drops the pristine string so the row re-serializes from fields
const markEdited = (row: Row) => {
  row.original = null
}

const serializeRow = (row: Row): string => {
  if (row.raw !== null) {
    return row.raw.trim()
  }
  if (row.original !== null) {
    return row.original
  }
  // an incomplete row (no day, or a missing time) is not a valid schedule
  // yet; keep it visible for editing but leave it out of the value
  if (!row.days.length || !row.begin || !row.end) {
    return ''
  }
  const days = DAYS.filter(day => row.days.includes(day)).join('')
  return `${days}${toMilitary(row.begin)}-${toMilitary(row.end)}`
}

const serialize = (): string[] => rows.value.map(serializeRow).filter(Boolean)

// the schedules the parent handed us; the server grandfathers these unchanged
// even when they are overnight, so they never block saving
let stored = new Set<string>()

// rebuild rows only when the incoming value differs from what we already
// represent, so emitting our own serialization can't clobber in-progress edits
watch(
  () => props.modelValue,
  (value) => {
    if (JSON.stringify(serialize()) !== JSON.stringify(value ?? [])) {
      stored = new Set((value ?? []).map(v => v.trim()))
      rows.value = (value ?? []).map(parse)
    }
  },
  { immediate: true, deep: true }
)

// no emit when the serialization matches the incoming value: opening the
// dialog must not rewrite anything, only real edits may
watch(rows, () => {
  const next = serialize()
  if (JSON.stringify(next) !== JSON.stringify(props.modelValue ?? [])) {
    emit('update:modelValue', next)
  }
}, { deep: true })

// a form row (not raw text) with no day selected can't be serialized
const hasDay = (row: Row): boolean => row.raw !== null || row.days.length > 0

const minutesOf = (hhmm: string) => {
  const [hours, minutes] = hhmm.split(':')
  return Number.parseInt(hours, 10) * 60 + Number.parseInt(minutes, 10)
}

// v2 and notifd compare within one calendar day, so begin after end never
// matches; the server rejects it for new or changed entries
const isOvernight = (row: Row): boolean =>
  row.raw === null && !!row.begin && !!row.end && minutesOf(row.begin) > minutesOf(row.end)

const overnightBlocks = (row: Row): boolean =>
  isOvernight(row) && !stored.has(serializeRow(row))

// the window length in minutes when it is suspiciously short (5 or fewer), else
// null. Advisory only.
const SHORT_WINDOW_MINUTES = 5
const shortWindow = (row: Row): number | null => {
  if (row.raw !== null || !row.begin || !row.end || isOvernight(row)) {
    return null
  }
  const length = minutesOf(row.end) - minutesOf(row.begin)
  return length <= SHORT_WINDOW_MINUTES ? length : null
}

const valid = computed(() => rows.value.every(row => hasDay(row) && !overnightBlocks(row)))

watch(valid, value => emit('update:valid', value), { immediate: true })

const toggleDay = (row: Row, day: string) => {
  markEdited(row)
  const at = row.days.indexOf(day)
  if (at >= 0) {
    row.days.splice(at, 1)
  } else {
    row.days.push(day)
  }
}

const addRow = () => {
  rows.value.push({ id: nextId++, days: [], begin: '09:00', end: '17:00', raw: null, original: null })
}

const removeRow = (index: number) => {
  rows.value.splice(index, 1)
}
</script>

<style lang="scss" scoped>
.duty-schedule-editor {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.no-schedules {
  margin: 0;
  color: var(--p-text-muted-color);
}

.duty-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;

  .day-toggles {
    display: flex;
    gap: 0.25rem;
  }

  .time-input {
    padding: 0.35rem 0.5rem;
    border: 1px solid var(--p-inputtext-border-color, var(--p-content-border-color));
    border-radius: var(--p-inputtext-border-radius, 4px);
    background: var(--p-inputtext-background, transparent);
    color: var(--p-inputtext-color, inherit);
    color-scheme: light dark;
  }

  .row-error {
    color: var(--p-red-500, #d32f2f);
  }

  .row-warning {
    color: var(--p-orange-500, #ed6c02);
  }

  .to {
    color: var(--p-text-muted-color);
  }

  .raw-input {
    flex: 1;
    min-width: 12rem;
  }
}
</style>
