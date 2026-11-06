<template>
  <div class="calendar-tab">
    <p
      v-if="timeZone"
      class="zone"
      data-test="calendar-zone"
    >Times are in the server time zone: {{ timeZone }}</p>
    <div class="month-nav">
      <OnmsIconButton
        :icon="ChevronLeft"
        title="Previous month"
        aria-label="Previous month"
        data-test="calendar-prev"
        @click="shiftMonth(-1)"
      />
      <span class="month-label" data-test="calendar-month">{{ monthLabel }}</span>
      <OnmsIconButton
        :icon="ChevronRight"
        title="Next month"
        aria-label="Next month"
        data-test="calendar-next"
        @click="shiftMonth(1)"
      />
    </div>
    <div
      v-if="settingsMissing"
      class="calendar-error"
      role="alert"
      data-test="calendar-settings-missing"
    >{{ SETTINGS_MISSING_MESSAGE }}</div>
    <div
      v-else-if="errorText"
      class="calendar-error"
      role="alert"
      data-test="calendar-error"
    >{{ errorText }}</div>
    <div
      v-else
      class="grid"
      :class="{ loading }"
    >
      <div v-for="w in WEEKDAY_HEADERS" :key="w" class="weekday">{{ w }}</div>
      <div
        v-for="(cell, i) in cells"
        :key="i"
        class="cell"
        :class="{ outside: !cell }"
        data-test="calendar-cell"
        :data-date="cell?.date"
      >
        <template v-if="cell">
          <button
            type="button"
            class="day-number"
            :title="`Add a time on ${cell.date}`"
            data-test="calendar-day"
            @click="openDay(cell.date)"
          >{{ cell.day }} <span aria-hidden="true">+</span></button>
          <template v-for="(e, j) in entriesByDay.get(cell.date) ?? []" :key="j">
            <button
              v-if="e.scheduleIndex !== null && cards[e.scheduleIndex]"
              type="button"
              class="entry"
              data-test="calendar-entry"
              @click="emit('show-schedule', e.scheduleIndex)"
            >{{ clockTo12Hour(e.start.slice(11, 16)) }} {{ e.user }}</button>
            <div
              v-else
              class="entry fallback"
              data-test="calendar-entry-fallback"
            >{{ clockTo12Hour(e.start.slice(11, 16)) }} {{ e.user }} <small v-if="e.scheduleIndex === null">supervisor</small></div>
          </template>
        </template>
      </div>
    </div>
    <OnCallDayEntryDialog
      v-model:visible="dialogVisible"
      :day="dialogDay"
      :members="members"
      @ok="entry => emit('add-entry', entry)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'

import { OnmsIconButton } from '@opennms/onms-ui'

import ChevronLeft from '@opennms/onms-ui/icons/navigation/ChevronLeft.vue'
import ChevronRight from '@opennms/onms-ui/icons/navigation/ChevronRight.vue'
import OnCallDayEntryDialog from '@/components/OnCallRoles/OnCallDayEntryDialog.vue'
import { ScheduleCard } from '@/lib/onCallSchedule'
import { clockTo12Hour } from '@/lib/pickerTime'
import API from '@/services'
import { ManagedOnCallRoleCreate, OnCallCalendarEntry } from '@/types/onCallRoleAdmin'

// `draft` is null while the draft cannot be built (a schedule has problems).
const props = defineProps<{
  draft: ManagedOnCallRoleCreate | null
  cards: ScheduleCard[]
  members: string[]
  timeZone: string
  settingsMissing?: boolean
  // the shown month (yyyy-MM), kept by the parent so that it survives the tab being unmounted
  month?: string | null
}>()

const emit = defineEmits<{
  'add-entry': [entry: { user: string, start: string, end: string, endOfDay: boolean }]
  'show-schedule': [index: number]
  'update:month': [month: string]
}>()

const DEBOUNCE_MS = 400
const WEEKDAY_HEADERS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
const SETTINGS_MISSING_MESSAGE = 'Schedule settings could not be loaded; reload the page.'
const NOT_READY_MESSAGE = 'Fix the problems on the Schedules tab to see the calendar.'

const pad = (n: number) => String(n).padStart(2, '0')

// the current month in the server's time zone; the browser's month if the zone is unknown to the browser
const serverMonth = (): string => {
  try {
    const parts = new Intl.DateTimeFormat('en-CA', { timeZone: props.timeZone, year: 'numeric', month: '2-digit' }).formatToParts(new Date())
    const year = parts.find(p => p.type === 'year')?.value
    const month = parts.find(p => p.type === 'month')?.value
    if (year && month) {
      return `${year}-${month}`
    }
  } catch {
    // fall through
  }
  const now = new Date()
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}`
}

const month = ref(props.month ?? serverMonth())
watch(month, m => emit('update:month', m), { immediate: true })
const entries = ref<OnCallCalendarEntry[]>([])
const errorText = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogDay = ref('')

let sequence = 0
let timer: ReturnType<typeof setTimeout> | undefined

const clearTimer = () => {
  if (timer) {
    clearTimeout(timer)
    timer = undefined
  }
}

const load = async () => {
  clearTimer()
  const seq = ++sequence
  if (!props.draft) {
    entries.value = []
    loading.value = false
    errorText.value = NOT_READY_MESSAGE
    return
  }
  loading.value = true
  const result = await API.previewOnCallCalendar(props.draft, month.value)
  if (seq !== sequence) {
    return
  }
  loading.value = false
  if (result.success && result.payload) {
    entries.value = result.payload.entries
    errorText.value = ''
  } else {
    entries.value = []
    errorText.value = result.message
  }
}

load()

watch(() => JSON.stringify(props.draft), () => {
  clearTimer()
  timer = setTimeout(load, DEBOUNCE_MS)
})

onBeforeUnmount(() => {
  clearTimer()
  sequence++
})

const shiftMonth = (delta: number) => {
  const [y, m] = month.value.split('-').map(Number)
  const d = new Date(Date.UTC(y, m - 1 + delta, 1))
  month.value = `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}`
  // the old month's entries must not show under the new month's days while it loads
  entries.value = []
  load()
}

const monthLabel = computed(() => {
  const [y, m] = month.value.split('-').map(Number)
  return new Date(Date.UTC(y, m - 1, 1)).toLocaleString('en-US', { timeZone: 'UTC', month: 'long', year: 'numeric' })
})

// Sunday-first weeks; null for the days that belong to the neighbouring months
const cells = computed(() => {
  const [y, m] = month.value.split('-').map(Number)
  const lead = new Date(Date.UTC(y, m - 1, 1)).getUTCDay()
  const days = new Date(Date.UTC(y, m, 0)).getUTCDate()
  const result: ({ date: string, day: number } | null)[] = Array(lead).fill(null)
  for (let day = 1; day <= days; day++) {
    result.push({ date: `${month.value}-${pad(day)}`, day })
  }
  while (result.length % 7 !== 0) {
    result.push(null)
  }
  return result
})

const entriesByDay = computed(() => {
  const byDay = new Map<string, OnCallCalendarEntry[]>()
  for (const e of entries.value) {
    const day = e.start.slice(0, 10)
    byDay.set(day, [...(byDay.get(day) ?? []), e])
  }
  return byDay
})

const openDay = (date: string) => {
  dialogDay.value = date
  dialogVisible.value = true
}
</script>

<style lang="scss" scoped>
.calendar-tab {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.zone {
  margin: 0;
  color: var(--p-text-muted-color);
}

.month-nav {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.month-label {
  min-width: 10rem;
  text-align: center;
  font-weight: 600;
}

.calendar-error {
  padding: 0.6rem 0.85rem;
  border: 1px solid var(--p-red-400, #e57373);
  border-radius: 4px;
  color: var(--p-red-400, #e57373);
}

.grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 1px;
  border: 1px solid var(--p-content-border-color);
  background: var(--p-content-border-color);

  &.loading {
    opacity: 0.6;
  }
}

.weekday {
  padding: 0.35rem;
  text-align: center;
  font-weight: 600;
  background: var(--p-content-background);
}

.cell {
  min-height: 6rem;
  padding: 0.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  background: var(--p-content-background);

  &.outside {
    background: color-mix(in srgb, var(--p-content-border-color) 40%, var(--p-content-background));
  }
}

.day-number {
  align-self: flex-start;
  padding: 0 0.25rem;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  font-size: 0.85rem;
  cursor: pointer;

  span {
    color: var(--p-text-muted-color);
  }

  &:hover span {
    color: var(--p-primary-color);
  }
}

.entry {
  padding: 0.1rem 0.3rem;
  border: 0;
  border-radius: 3px;
  text-align: left;
  font: inherit;
  font-size: 0.8rem;
  overflow-wrap: anywhere;
  background: color-mix(in srgb, var(--p-primary-color) 16%, transparent);
  color: inherit;

  &:is(button) {
    cursor: pointer;
  }

  &.fallback {
    color: var(--p-text-muted-color);
    background: transparent;
    font-style: italic;
  }
}
</style>
