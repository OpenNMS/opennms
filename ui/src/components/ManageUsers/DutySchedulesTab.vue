<template>
  <div class="duty-schedules-tab" data-test="duty-schedules">
    <p class="hint">
      Duty schedules limit when this user is notified. A user with no schedules is available at all times.
      Overnight windows (end before start) are valid and span midnight.
    </p>

    <div class="add-row">
      <FormField label="Day of Week" for="duty-add-day">
        <OnmsSelect
          v-model="dayToAdd"
          inputId="duty-add-day"
          :options="DAY_OPTIONS"
          optionLabel="label"
          optionValue="value"
          data-test="duty-add-day"
        />
      </FormField>
      <FormField label="Start Time" for="duty-add-start">
        <OnmsDatePicker
          v-model="startToAdd"
          inputId="duty-add-start"
          timeOnly
          hourFormat="24"
          data-test="duty-add-start"
        />
      </FormField>
      <FormField label="End Time" for="duty-add-end">
        <OnmsDatePicker
          v-model="endToAdd"
          inputId="duty-add-end"
          timeOnly
          hourFormat="24"
          data-test="duty-add-end"
        />
      </FormField>
      <OnmsButton
        label="Add"
        icon="pi pi-plus"
        class="add-button"
        :disabled="!startToAdd || !endToAdd"
        data-test="duty-add-button"
        @click="addEntry"
      />
    </div>
    <small v-if="duplicateNote" class="duplicate-note" data-test="duty-duplicate-note">{{ duplicateNote }}</small>

    <OnmsTable
      v-if="entries.length"
      :value="entries"
      dataKey="key"
      data-test="duty-table"
    >
      <OnmsColumn header="Day(s)">
        <template #body="{ data }">
          <span v-if="data.raw === null">{{ data.days.map(fullDayName).join(', ') }}</span>
          <!-- a hand-edited entry the day/time form can't represent is shown
               verbatim so it is never silently dropped -->
          <code v-else :data-test="'duty-raw'">{{ data.raw }}</code>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Start">
        <template #body="{ data }">{{ data.raw === null ? data.begin : '—' }}</template>
      </OnmsColumn>
      <OnmsColumn header="End">
        <template #body="{ data }">{{ data.raw === null ? data.end : '—' }}</template>
      </OnmsColumn>
      <OnmsColumn header="">
        <template #body="{ index }">
          <OnmsIconButton
            severity="danger"
            :icon="Delete"
            :aria-label="`Remove duty schedule ${index + 1}`"
            :data-test="`remove-duty-${index}`"
            @click="removeEntry(index)"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>
    <p v-else class="no-schedules" data-test="no-duty-schedules">
      No duty schedules; this user is available at all times.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsColumn, OnmsDatePicker, OnmsIconButton, OnmsSelect, OnmsTable } from '@opennms/onms-ui'

import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import FormField from '@/components/Common/FormField.vue'

// Duty schedules per NMS-20281: one Day of Week / Start / End row with an Add
// button, and a table of the entries with a delete control. Entries are only
// added or removed, never edited in place, so every existing schedule string
// round-trips byte-identically — including multi-day entries (MoTuWe0800-1700,
// shown as one row listing all their days) and hand-edited strings the
// structured form can't represent.

const props = defineProps<{
  modelValue: string[]
}>()

const emit = defineEmits<{
  'update:modelValue': [string[]]
}>()

const DAYS = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su']
const FULL_DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
const DAY_OPTIONS = DAYS.map((value, i) => ({ value, label: FULL_DAYS[i] }))
const SCHEDULE = /^((?:Mo|Tu|We|Th|Fr|Sa|Su){1,7})(\d{1,4})-(\d{1,4})$/

interface Entry {
  key: string
  days: string[]
  begin: string
  end: string
  // non-null only for strings the structured form can't represent
  raw: string | null
  // the incoming string, serialized back verbatim
  original: string
}

const fullDayName = (day: string) => FULL_DAYS[DAYS.indexOf(day)] ?? day

// military HMM/HHMM -> HH:MM
const toHHMM = (military: string): string => {
  const n = Number.parseInt(military, 10)
  return `${String(Math.floor(n / 100)).padStart(2, '0')}:${String(n % 100).padStart(2, '0')}`
}

const parse = (value: string, index: number): Entry => {
  const trimmed = value.trim()
  const match = SCHEDULE.exec(trimmed)
  if (!match) {
    return { key: `${index}:${trimmed}`, days: [], begin: '', end: '', raw: trimmed, original: trimmed }
  }
  return {
    key: `${index}:${trimmed}`,
    days: match[1].match(/../g) ?? [],
    begin: toHHMM(match[2]),
    end: toHHMM(match[3]),
    raw: null,
    original: trimmed
  }
}

const entries = computed(() => (props.modelValue ?? []).map(parse))

const dayToAdd = ref('Mo')
const startToAdd = ref<Date | null>(defaultTime(9, 0))
const endToAdd = ref<Date | null>(defaultTime(17, 0))
const duplicateNote = ref('')

function defaultTime(hours: number, minutes: number): Date {
  const date = new Date()
  date.setHours(hours, minutes, 0, 0)
  return date
}

const military = (date: Date): string => String(date.getHours() * 100 + date.getMinutes())

const addEntry = () => {
  if (!startToAdd.value || !endToAdd.value) {
    return
  }
  const serialized = `${dayToAdd.value}${military(startToAdd.value)}-${military(endToAdd.value)}`
  if ((props.modelValue ?? []).includes(serialized)) {
    duplicateNote.value = 'That schedule is already in the list.'
    return
  }
  duplicateNote.value = ''
  emit('update:modelValue', [...(props.modelValue ?? []), serialized])
}

const removeEntry = (index: number) => {
  duplicateNote.value = ''
  emit('update:modelValue', entries.value.filter((_, i) => i !== index).map(e => e.original))
}

// a duplicate warning about a list that has since changed is stale
watch(() => props.modelValue, () => {
  duplicateNote.value = ''
})
</script>

<style lang="scss" scoped>
.duty-schedules-tab {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.hint {
  margin: 0;
  color: var(--p-text-muted-color);
}

.add-row {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
  flex-wrap: wrap;

  .add-button {
    margin-bottom: 2px;
  }
}

.duplicate-note {
  color: var(--p-text-muted-color);
  font-style: italic;
}

.no-schedules {
  margin: 0;
  color: var(--p-text-muted-color);
}
</style>
