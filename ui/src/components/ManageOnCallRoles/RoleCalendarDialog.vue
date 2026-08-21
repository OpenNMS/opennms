<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="`On-Call Schedule: ${roleName}`"
    class="role-calendar-dialog"
    width="min(1000px, 95vw)"
    data-test="role-calendar-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div
      v-if="errorText"
      class="dialog-error"
      role="alert"
      data-test="dialog-error"
    >{{ errorText }}</div>
    <div class="calendar-controls">
      <OnmsIconButton
        :icon="ChevronLeft"
        aria-label="Previous month"
        data-test="previous-month-button"
        @click="shiftMonth(-1)"
      />
      <span class="month-label" data-test="month-label">{{ monthLabel }}</span>
      <OnmsIconButton
        :icon="ChevronRight"
        aria-label="Next month"
        data-test="next-month-button"
        @click="shiftMonth(1)"
      />
    </div>
    <p
      v-if="serverZone"
      class="zone-hint"
      data-test="zone-hint"
    >All times are shown and entered in server time ({{ serverZone }}).</p>

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
          <OnmsTag
            :value="schedule.type"
            :severity="schedule.type === 'specific' ? 'info' : 'secondary'"
          />
          <span class="schedule-user">{{ schedule.user }}</span>
          <span class="schedule-times">{{ scheduleSummary(schedule) }}</span>
          <OnmsIconButton
            severity="danger"
            :icon="Cancel"
            :aria-label="`Remove coverage for ${schedule.user}`"
            data-test="remove-schedule-button"
            @click="removeSchedule(index)"
          />
        </li>
      </ul>
      <p v-else class="no-schedules">No coverage entries; the supervisor is always on call.</p>

      <div class="add-entry-row">
        <FormField label="User" for="calendar-entry-user">
          <OnmsSelect
            v-model="entryUser"
            inputId="calendar-entry-user"
            :options="memberOptions"
            filter
            data-test="entry-user-select"
          />
        </FormField>
        <FormField label="From" for="calendar-entry-start">
          <OnmsDatePicker
            v-model="entryStart"
            inputId="calendar-entry-start"
            showTime
            hourFormat="24"
            data-test="entry-start-input"
          />
        </FormField>
        <FormField label="To" for="calendar-entry-end">
          <OnmsDatePicker
            v-model="entryEnd"
            inputId="calendar-entry-end"
            showTime
            hourFormat="24"
            data-test="entry-end-input"
          />
        </FormField>
        <OnmsButton
          variant="outlined"
          label="Add Coverage"
          icon="pi pi-plus"
          data-test="add-entry-button"
          :disabled="!entryUser || !entryStart || !entryEnd || !!entryRangeProblem || saving"
          @click="addEntry"
        />
      </div>
      <small
        v-if="entryRangeProblem"
        class="field-error"
        data-test="entry-range-error"
      >{{ entryRangeProblem }}</small>
    </div>

    <OnmsConfirmationDialog
      :visible="showRemoveConfirmation"
      title="Remove Coverage Entry"
      actionButtonText="Remove"
      @ok="confirmRemove"
      @cancel="cancelRemove"
    >
      <template #content>
        <p data-test="remove-warning">
          This <strong>{{ scheduleToRemove?.schedule.type }}</strong> entry for
          <strong>{{ scheduleToRemove?.schedule.user }}</strong> comes from groups.xml.
          This editor can only add one-off coverage, so a recurring entry cannot be
          recreated here once removed. Remove it anyway?
        </p>
      </template>
    </OnmsConfirmationDialog>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsConfirmationDialog, OnmsDatePicker, OnmsDialog, OnmsIconButton, OnmsSelect, OnmsTag } from '@opennms/onms-ui'

import Cancel from '@/components/icons/navigation/Cancel.vue'
import FormField from '@/components/Common/FormField.vue'
import ChevronLeft from '@/components/icons/navigation/ChevronLeft.vue'
import ChevronRight from '@/components/icons/navigation/ChevronRight.vue'
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
const errorText = ref('')
const showRemoveConfirmation = ref(false)
const scheduleToRemove = ref<{ index: number, schedule: OnCallSchedule } | null>(null)

const serverZone = computed(() => calendar.value?.['time-zone'] ?? '')

const entryRangeProblem = computed(() => {
  if (entryStart.value && entryEnd.value && entryStart.value >= entryEnd.value) {
    return 'The start time must be before the end time.'
  }
  return null
})

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
      errorText.value = ''
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

const timeOf = (millis: number) => {
  const options: Intl.DateTimeFormatOptions = { hour: '2-digit', minute: '2-digit', hour12: false }
  if (serverZone.value) {
    try {
      return new Date(millis).toLocaleTimeString(undefined, { ...options, timeZone: serverZone.value })
    } catch (_err) {
      // unknown zone id in this browser; fall through to local time
    }
  }
  return new Date(millis).toLocaleTimeString(undefined, options)
}

const entryLabel = (entry: OnCallCalendarEntry) =>
  `${timeOf(entry.start)}–${timeOf(entry.end)} ${(entry.user ?? []).join(', ')}`

const scheduleSummary = (schedule: OnCallSchedule) =>
  schedule.time.map(t => (t.day ? `${t.day} ` : '') + `${t.begins} – ${t.ends}`).join('; ')

// re-read the stored role right before mutating so a concurrent edit from
// another session (or a hand-edit) is folded in instead of overwritten
const refreshRole = async (): Promise<boolean> => {
  const fresh = await store.getRole(props.roleName)
  if (fresh) {
    role.value = fresh
  }
  return fresh !== null
}

const saveSchedules = async (schedules: OnCallSchedule[]): Promise<boolean> => {
  if (!role.value) {
    return false
  }
  const error = await store.updateRole({ name: role.value.name, schedule: schedules })
  if (error === null) {
    errorText.value = ''
    await load()
    return true
  }
  errorText.value = error
  return false
}

const addEntry = async () => {
  if (!entryUser.value || !entryStart.value || !entryEnd.value || entryRangeProblem.value) {
    return
  }
  saving.value = true
  try {
    if (!(await refreshRole()) || !role.value) {
      return
    }
    const schedules = [...(role.value.schedule ?? []), {
      user: entryUser.value,
      type: 'specific' as const,
      time: [{ begins: formatScheduleTimestamp(entryStart.value), ends: formatScheduleTimestamp(entryEnd.value) }]
    }]
    if (await saveSchedules(schedules)) {
      entryUser.value = null
      entryStart.value = null
      entryEnd.value = null
    }
  } finally {
    saving.value = false
  }
}

const scheduleKey = (schedule: OnCallSchedule) =>
  [schedule.user, schedule.type, ...schedule.time.map(t => `${t.day ?? ''}|${t.begins}|${t.ends}`)].join('//')

const removeSchedule = (index: number) => {
  const schedule = role.value?.schedule?.[index]
  if (!schedule) {
    return
  }
  if (schedule.type !== 'specific') {
    scheduleToRemove.value = { index, schedule }
    showRemoveConfirmation.value = true
    return
  }
  doRemove(schedule)
}

const doRemove = async (schedule: OnCallSchedule) => {
  saving.value = true
  try {
    if (!(await refreshRole()) || !role.value) {
      return
    }
    // match by content, not index: the stored list may have changed since render
    const schedules = [...(role.value.schedule ?? [])]
    const target = scheduleKey(schedule)
    const index = schedules.findIndex(s => scheduleKey(s) === target)
    if (index < 0) {
      errorText.value = 'That coverage entry no longer exists; the list has been refreshed.'
      return
    }
    schedules.splice(index, 1)
    await saveSchedules(schedules)
  } finally {
    saving.value = false
  }
}

const confirmRemove = async () => {
  const target = scheduleToRemove.value
  showRemoveConfirmation.value = false
  scheduleToRemove.value = null
  if (target) {
    await doRemove(target.schedule)
  }
}

const cancelRemove = () => {
  showRemoveConfirmation.value = false
  scheduleToRemove.value = null
}
</script>

<style lang="scss" scoped>
.zone-hint {
  margin: -0.5rem 0 0.75rem 0;
  text-align: center;
  font-size: 0.85rem;
  color: var(--p-text-muted-color);
}

.dialog-error {
  margin-bottom: 0.75rem;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
  background: var(--p-red-50, #fef2f2);
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}

.field-error {
  display: block;
  margin-top: 0.5rem;
  color: var(--p-red-500, #e24c4c);
}

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
    align-items: flex-end;
    gap: 0.75rem;
    flex-wrap: wrap;

    :deep(.p-select) {
      min-width: 160px;
    }
  }
}
</style>
