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
        >
        <span class="to">to</span>
        <input
          v-model="row.end"
          type="time"
          class="time-input"
          aria-label="End time"
          :data-test="`duty-${index}-end`"
        >
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
import { ref, watch } from 'vue'

import { OnmsButton, OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'

import Cancel from '@opennms/onms-ui/icons/navigation/Cancel.vue'

const props = defineProps<{
  modelValue: string[]
}>()

const emit = defineEmits<{
  'update:modelValue': [string[]]
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
    return { id: nextId++, days: [], begin: '', end: '', raw: value }
  }
  return {
    id: nextId++,
    days: match[1].match(/../g) ?? [],
    begin: toHHMM(match[2]),
    end: toHHMM(match[3]),
    raw: null
  }
}

const serialize = (): string[] =>
  rows.value
    .map((row) => {
      if (row.raw !== null) {
        return row.raw.trim()
      }
      // an incomplete row (no day, or a missing time) is not a valid schedule
      // yet; keep it visible for editing but leave it out of the value
      if (!row.days.length || !row.begin || !row.end) {
        return ''
      }
      const days = DAYS.filter(day => row.days.includes(day)).join('')
      return `${days}${toMilitary(row.begin)}-${toMilitary(row.end)}`
    })
    .filter(Boolean)

// rebuild rows only when the incoming value differs from what we already
// represent, so emitting our own serialization can't clobber in-progress edits
watch(
  () => props.modelValue,
  (value) => {
    if (JSON.stringify(serialize()) !== JSON.stringify(value ?? [])) {
      rows.value = (value ?? []).map(parse)
    }
  },
  { immediate: true, deep: true }
)

watch(rows, () => emit('update:modelValue', serialize()), { deep: true })

const toggleDay = (row: Row, day: string) => {
  const at = row.days.indexOf(day)
  if (at >= 0) {
    row.days.splice(at, 1)
  } else {
    row.days.push(day)
  }
}

const addRow = () => {
  rows.value.push({ id: nextId++, days: [], begin: '09:00', end: '17:00', raw: null })
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

  .to {
    color: var(--p-text-muted-color);
  }

  .raw-input {
    flex: 1;
    min-width: 12rem;
  }
}
</style>
