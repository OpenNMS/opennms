<template>
  <Dialog
    :visible="visible"
    modal
    maximizable
    :header="`On-Call Schedule: ${roleName}`"
    class="role-calendar-dialog"
    :style="{ width: '1000px', maxWidth: '95vw' }"
    data-test="role-calendar-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="calendar-controls">
      <Button
        text
        icon="pi pi-chevron-left"
        aria-label="Previous month"
        data-test="previous-month-button"
        @click="shiftMonth(-1)"
      />
      <span class="month-label" data-test="month-label">{{ monthLabel }}</span>
      <Button
        text
        icon="pi pi-chevron-right"
        aria-label="Next month"
        data-test="next-month-button"
        @click="shiftMonth(1)"
      />
    </div>

    <div class="calendar-grid" data-test="calendar-grid">
      <div
        v-for="weekday in weekdayHeaders"
        :key="weekday"
        class="weekday-header"
      >{{ weekday }}</div>
      <div
        v-for="blank in leadingBlanks"
        :key="`blank-${blank}`"
        class="day-cell blank"
      />
      <div
        v-for="day in calendar?.day ?? []"
        :key="day.date"
        class="day-cell"
      >
        <div class="day-number">{{ Number(day.date.slice(-2)) }}</div>
        <div
          v-for="(entry, index) in day.entry ?? []"
          :key="index"
          class="entry"
          :class="{ supervisor: entry.supervisor }"
        >
          <template v-if="entry.supervisor">unscheduled</template>
          <template v-else>{{ entryLabel(entry) }}</template>
        </div>
      </div>
    </div>

    <div class="schedules-section">
      <div class="section-title">Coverage Entries</div>
      <p class="section-hint">
        Intervals with nobody scheduled fall back to the supervisor and show as
        <em>unscheduled</em>. Weekly, daily and monthly entries (from
        groups.xml) are shown and preserved; this editor adds one-off coverage.
      </p>
      <ul
        v-if="role?.schedule?.length"
        class="schedule-list"
        data-test="schedule-list"
      >
        <li
          v-for="(schedule, index) in role.schedule"
          :key="index"
          class="schedule-row"
        >
          <Tag
            :value="schedule.type"
            :severity="schedule.type === 'specific' ? 'info' : 'secondary'"
          />
          <span class="schedule-user">{{ schedule.user }}</span>
          <span class="schedule-times">{{ scheduleSummary(schedule) }}</span>
          <Button
            text
            rounded
            icon="pi pi-times"
            severity="danger"
            :aria-label="`Remove coverage for ${schedule.user}`"
            data-test="remove-schedule-button"
            @click="removeSchedule(index)"
          />
        </li>
      </ul>
      <p v-else class="no-schedules">No coverage entries; the supervisor is always on call.</p>

      <div class="add-entry-row">
        <IftaLabel>
          <Select
            v-model="entryUser"
            labelId="calendar-entry-user"
            :options="memberOptions"
            filter
            data-test="entry-user-select"
          />
          <label for="calendar-entry-user">User</label>
        </IftaLabel>
        <IftaLabel>
          <DatePicker
            v-model="entryStart"
            inputId="calendar-entry-start"
            showTime
            hourFormat="24"
            data-test="entry-start-input"
          />
          <label for="calendar-entry-start">From</label>
        </IftaLabel>
        <IftaLabel>
          <DatePicker
            v-model="entryEnd"
            inputId="calendar-entry-end"
            showTime
            hourFormat="24"
            data-test="entry-end-input"
          />
          <label for="calendar-entry-end">To</label>
        </IftaLabel>
        <Button
          outlined
          label="Add Coverage"
          icon="pi pi-plus"
          data-test="add-entry-button"
          :disabled="!entryUser || !entryStart || !entryEnd || saving"
          @click="addEntry"
        />
      </div>
    </div>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import Button from 'primevue/button'
import DatePicker from 'primevue/datepicker'
import Dialog from 'primevue/dialog'
import IftaLabel from 'primevue/iftalabel'
import Select from 'primevue/select'
import Tag from 'primevue/tag'

import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'
import { formatScheduleTimestamp, OnCallCalendar, OnCallCalendarEntry, OnCallRole, OnCallSchedule } from '@/types/onCallRoleAdmin'

const props = defineProps<{
  visible: boolean
  roleName: string
}>()

const emit = defineEmits(['update:visible'])

const store = useOnCallRoleAdminStore()

const weekdayHeaders = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

const today = new Date()
const year = ref(today.getFullYear())
const month = ref(today.getMonth() + 1)
const calendar = ref<OnCallCalendar | null>(null)
const role = ref<OnCallRole | null>(null)
const entryUser = ref<string | null>(null)
const entryStart = ref<Date | null>(null)
const entryEnd = ref<Date | null>(null)
const saving = ref(false)

const monthLabel = computed(() => new Date(year.value, month.value - 1, 1)
  .toLocaleDateString(undefined, { month: 'long', year: 'numeric' }))

const leadingBlanks = computed(() => new Date(year.value, month.value - 1, 1).getDay())

const memberOptions = computed(() => {
  const group = role.value?.['membership-group']
  return group ? store.groupMembers[group] ?? [] : []
})

const load = async () => {
  if (!props.roleName) {
    return
  }
  const [calendarResult, roleResult] = await Promise.all([
    store.getCalendar(props.roleName, year.value, month.value),
    store.getRole(props.roleName)
  ])
  if (calendarResult) {
    calendar.value = calendarResult
  }
  if (roleResult) {
    role.value = roleResult
  }
}

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      year.value = today.getFullYear()
      month.value = today.getMonth() + 1
      entryUser.value = null
      entryStart.value = null
      entryEnd.value = null
      load()
    }
  }
)

const shiftMonth = (delta: number) => {
  const next = new Date(year.value, month.value - 1 + delta, 1)
  year.value = next.getFullYear()
  month.value = next.getMonth() + 1
  load()
}

const timeOf = (millis: number) => new Date(millis).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', hour12: false })

const entryLabel = (entry: OnCallCalendarEntry) =>
  `${timeOf(entry.start)}–${timeOf(entry.end)} ${(entry.user ?? []).join(', ')}`

const scheduleSummary = (schedule: OnCallSchedule) =>
  schedule.time.map((t) => (t.day ? `${t.day} ` : '') + `${t.begins} – ${t.ends}`).join('; ')

const saveSchedules = async (schedules: OnCallSchedule[]) => {
  if (!role.value) {
    return
  }
  saving.value = true
  try {
    const ok = await store.updateRole({ name: role.value.name, schedule: schedules })
    if (ok) {
      await load()
    }
  } finally {
    saving.value = false
  }
}

const addEntry = async () => {
  if (!role.value || !entryUser.value || !entryStart.value || !entryEnd.value) {
    return
  }
  const schedules = [...(role.value.schedule ?? []), {
    user: entryUser.value,
    type: 'specific' as const,
    time: [{ begins: formatScheduleTimestamp(entryStart.value), ends: formatScheduleTimestamp(entryEnd.value) }]
  }]
  await saveSchedules(schedules)
  entryUser.value = null
  entryStart.value = null
  entryEnd.value = null
}

const removeSchedule = async (index: number) => {
  if (!role.value) {
    return
  }
  const schedules = [...(role.value.schedule ?? [])]
  schedules.splice(index, 1)
  await saveSchedules(schedules)
}
</script>

<style lang="scss" scoped>
.calendar-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;

  .month-label {
    font-weight: 600;
    min-width: 11rem;
    text-align: center;
  }
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;

  .weekday-header {
    text-align: center;
    font-weight: 600;
    font-size: 0.85rem;
    padding: 0.25rem;
  }

  .day-cell {
    border: 1px solid var(--p-content-border-color);
    border-radius: 4px;
    min-height: 4.5rem;
    padding: 0.25rem;
    font-size: 0.75rem;
    overflow: hidden;

    &.blank {
      border: none;
    }

    .day-number {
      font-weight: 600;
      margin-bottom: 0.15rem;
    }

    .entry {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;

      &.supervisor {
        color: var(--p-text-muted-color);
        font-style: italic;
      }
    }
  }
}

.schedules-section {
  margin-top: 1rem;

  .section-title {
    font-weight: 600;
    margin-bottom: 0.25rem;
  }

  .section-hint {
    margin: 0 0 0.75rem 0;
    font-size: 0.875rem;
    color: var(--p-text-muted-color);
  }

  .schedule-list {
    list-style: none;
    margin: 0 0 0.75rem 0;
    padding: 0;
    border: 1px solid var(--p-content-border-color);
    border-radius: 6px;

    .schedule-row {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.25rem 0.75rem;

      & + .schedule-row {
        border-top: 1px solid var(--p-content-border-color);
      }

      .schedule-user {
        font-weight: 600;
      }

      .schedule-times {
        flex: 1;
        font-size: 0.85rem;
        color: var(--p-text-muted-color);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  .no-schedules {
    margin: 0 0 0.75rem 0;
    color: var(--p-text-muted-color);
  }

  .add-entry-row {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    flex-wrap: wrap;

    :deep(.p-select) {
      min-width: 160px;
    }
  }
}
</style>
