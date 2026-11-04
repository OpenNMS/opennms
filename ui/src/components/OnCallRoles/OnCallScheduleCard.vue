<template>
  <div
    class="schedule-card"
    :class="{ highlight: highlighted }"
    data-test="oncall-schedule-card"
  >
    <template v-if="card.legacy">
      <div
        class="card-head"
        data-test="oncall-schedule-legacy"
      >
        <strong>{{ card.user }}</strong>
        <OnmsTag :value="card.type" severity="secondary" />
        <OnmsButton
          variant="text"
          severity="danger"
          label="Remove"
          data-test="remove-schedule"
          @click="emit('remove')"
        />
      </div>
      <p class="note">
        This schedule uses a form the editor can't change. It is kept as stored; remove it to replace it.
      </p>
      <div
        v-for="(t, i) in card.times"
        :key="i"
        class="raw-line"
      >{{ t.day ? `${t.day} ` : '' }}{{ t.begin }} &rarr; {{ t.end }}</div>
    </template>
    <template v-else>
      <div class="card-head">
        <OnmsSelect
          :modelValue="card.user"
          :options="userOptions"
          optionLabel="label"
          optionValue="value"
          placeholder="User"
          aria-label="User"
          class="user-select"
          data-test="schedule-user"
          @update:modelValue="setUser"
        />
        <OnmsTag
          v-if="notMember"
          value="not in group"
          severity="warn"
          data-test="not-in-group"
        />
        <OnmsSelect
          :modelValue="card.type"
          :options="TYPE_OPTIONS"
          optionLabel="label"
          optionValue="value"
          aria-label="Schedule type"
          data-test="schedule-type"
          @update:modelValue="askType"
        />
        <OnmsButton
          variant="text"
          severity="danger"
          label="Remove"
          data-test="remove-schedule"
          @click="emit('remove')"
        />
      </div>
      <div
        v-for="(r, i) in card.times"
        :key="rowKey(r)"
        class="time-row"
      >
        <OnmsSelect
          v-if="card.type === 'weekly' || card.type === 'monthly'"
          :modelValue="r.day"
          :options="card.type === 'weekly' ? weekdayOptions : MONTH_DAYS"
          :optionLabel="card.type === 'weekly' ? 'label' : undefined"
          :optionValue="card.type === 'weekly' ? 'value' : undefined"
          :placeholder="card.type === 'weekly' ? 'Weekday' : 'Day'"
          :aria-label="card.type === 'weekly' ? 'Weekday' : 'Day of month'"
          :data-test="`time-day-${i}`"
          @update:modelValue="v => setTime(i, { day: v as string })"
        />
        <input
          :value="r.begin"
          :type="inputType"
          class="time-input"
          aria-label="Begin"
          :data-test="`time-begin-${i}`"
          @input="setTime(i, { begin: ($event.target as HTMLInputElement).value })"
        >
        <span class="to">to</span>
        <!-- a specific time still needs its end date when it runs to the end of the day -->
        <input
          v-if="card.type === 'specific' && r.endOfDay"
          :value="r.end.slice(0, 10)"
          type="date"
          class="time-input"
          aria-label="End date"
          :data-test="`time-end-date-${i}`"
          @input="setEndDate(i, ($event.target as HTMLInputElement).value)"
        >
        <input
          v-else
          :value="r.end"
          :type="inputType"
          class="time-input"
          aria-label="End"
          :disabled="r.endOfDay"
          :data-test="`time-end-${i}`"
          @input="setTime(i, { end: ($event.target as HTMLInputElement).value })"
        >
        <span class="end-of-day">
          <OnmsCheckbox
            :modelValue="r.endOfDay"
            :inputId="`${card.key}-eod-${i}`"
            :data-test="`time-end-of-day-${i}`"
            @update:modelValue="v => setEndOfDay(i, v)"
          />
          <label :for="`${card.key}-eod-${i}`">End of day</label>
        </span>
        <OnmsIconButton
          severity="danger"
          :icon="Cancel"
          aria-label="Remove time range"
          :data-test="`remove-time-${i}`"
          @click="removeTime(i)"
        />
      </div>
      <OnmsButton
        variant="outlined"
        size="small"
        icon="pi pi-plus"
        label="Add time"
        data-test="add-time"
        @click="addTime"
      />
      <div
        v-if="problems.length"
        class="problems"
        role="alert"
        data-test="schedule-problems"
      >
        <small v-for="p in problems" :key="p">{{ p }}</small>
      </div>
    </template>
    <OnmsConfirmationDialog
      :visible="pendingType !== null"
      title="Change Schedule Type"
      actionButtonText="Change"
      @ok="confirmType"
      @cancel="pendingType = null"
    >
      <template #content>
        <p>Changing the type clears this schedule's time ranges. Continue?</p>
      </template>
    </OnmsConfirmationDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, toRaw } from 'vue'

import { OnmsButton, OnmsCheckbox, OnmsConfirmationDialog, OnmsIconButton, OnmsSelect, OnmsTag } from '@opennms/onms-ui'

import Cancel from '@opennms/onms-ui/icons/navigation/Cancel.vue'
import { cardProblems, ScheduleCard, TimeRange, WEEKDAYS } from '@/lib/onCallSchedule'
import { OnCallScheduleType } from '@/types/onCallRoleAdmin'

const props = defineProps<{
  card: ScheduleCard
  members: string[]
  highlighted: boolean
}>()

const emit = defineEmits<{
  'update:card': [card: ScheduleCard]
  remove: []
}>()

const TYPE_OPTIONS: { label: string, value: OnCallScheduleType }[] = [
  { label: 'Specific', value: 'specific' },
  { label: 'Daily', value: 'daily' },
  { label: 'Weekly', value: 'weekly' },
  { label: 'Monthly', value: 'monthly' }
]
const MONTH_DAYS = Array.from({ length: 31 }, (_, i) => String(i + 1))
const weekdayOptions = WEEKDAYS.map(d => ({ label: d.charAt(0).toUpperCase() + d.slice(1), value: d }))

const pendingType = ref<OnCallScheduleType | null>(null)

const inputType = computed(() => props.card.type === 'specific' ? 'datetime-local' : 'time')
const problems = computed(() => cardProblems(props.card))
// a stored user who left the group stays selected
const notMember = computed(() => !!props.card.user && !props.members.includes(props.card.user))
const userOptions = computed(() => {
  const names = notMember.value ? [...props.members, props.card.user] : props.members
  return names.map(n => ({ label: n, value: n }))
})

const update = (changes: Partial<ScheduleCard>) => {
  emit('update:card', { ...props.card, ...changes, dirty: true })
}

const setUser = (user: unknown) => update({ user: user as string })

// Stable v-for keys for the time rows. Kept outside TimeRange so nothing leaks into serialization;
// an edited row is a new object that inherits its predecessor's key.
const rowKeys = new WeakMap<TimeRange, number>()
let rowCounter = 0
// Keyed on the raw object: the parent's reactive state hands this component proxies.
const rowKey = (r: TimeRange): number => {
  const raw = toRaw(r)
  let k = rowKeys.get(raw)
  if (k === undefined) {
    k = ++rowCounter
    rowKeys.set(raw, k)
  }
  return k
}

const setTime = (index: number, changes: Partial<TimeRange>) => {
  update({
    times: props.card.times.map((t, i) => {
      if (i !== index) {
        return t
      }
      const next = { ...t, ...changes }
      rowKeys.set(next, rowKey(t))
      return next
    })
  })
}

// a specific time keeps its end date, with the end clock fixed at the end of that day
const setEndOfDay = (index: number, endOfDay: boolean) => {
  const end = props.card.times[index].end
  if (endOfDay && props.card.type === 'specific') {
    setTime(index, { endOfDay, end: end.length >= 10 ? `${end.slice(0, 10)}T23:59` : '' })
  } else {
    setTime(index, { endOfDay })
  }
}

const setEndDate = (index: number, date: string) => setTime(index, { end: date ? `${date}T23:59` : '' })

const addTime = () => {
  update({ times: [...props.card.times, { id: null, day: null, begin: '', end: '', endOfDay: false, original: null }] })
}

const removeTime = (index: number) => update({ times: props.card.times.filter((_, i) => i !== index) })

const askType = (value: unknown) => {
  const type = value as OnCallScheduleType
  if (type === props.card.type) {
    return
  }
  if (props.card.times.length) {
    pendingType.value = type
  } else {
    update({ type })
  }
}

const confirmType = () => {
  if (pendingType.value) {
    update({ type: pendingType.value, times: [] })
  }
  pendingType.value = null
}
</script>

<style lang="scss" scoped>
.schedule-card {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  padding: 0.85rem 1rem;
  border: 1px solid var(--p-content-border-color);
  border-radius: 6px;
  transition: background-color 0.3s;

  &.highlight {
    background: color-mix(in srgb, var(--p-primary-color) 14%, transparent);
  }
}

.card-head,
.time-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.card-head :last-child:is(button) {
  margin-left: auto;
}

.time-input {
  padding: 0.35rem 0.5rem;
  border: 1px solid var(--p-inputtext-border-color, var(--p-content-border-color));
  border-radius: var(--p-inputtext-border-radius, 4px);
  background: var(--p-inputtext-background, transparent);
  color: var(--p-inputtext-color, inherit);
  color-scheme: light dark;
}

.to,
.note {
  color: var(--p-text-muted-color);
}

.note {
  margin: 0;
}

.end-of-day {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
}

.problems {
  display: flex;
  flex-direction: column;
  color: var(--p-red-500, #d32f2f);
}

.raw-line {
  font-family: monospace;
}
</style>
