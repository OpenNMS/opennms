<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="`Add On-Call Time: ${day}`"
    class="oncall-day-entry-dialog"
    width="min(480px, 95vw)"
    data-test="oncall-day-entry-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <FormField label="User" for="oncall-day-entry-user" required>
        <OnmsSelect
          v-model="user"
          inputId="oncall-day-entry-user"
          :options="members"
          placeholder="User"
          fluid
          data-test="entry-user"
        />
      </FormField>
      <div class="time-row">
        <span class="row-label">Begin</span>
        <!-- a date and a separate time, so a time the browser's clocks skip still works -->
        <SplitDateTimePicker
          v-model="begin"
          idPrefix="oncall-day-entry-begin"
          label="Begin"
          dataTest="entry-begin"
        />
      </div>
      <div class="time-row">
        <span class="row-label">End</span>
        <template v-if="endOfDay">
          <label
            class="visually-hidden"
            for="oncall-day-entry-end-date"
          >End date</label>
          <OnmsDatePicker
            :modelValue="dayToDate(endDate)"
            inputId="oncall-day-entry-end-date"
            class="date-input"
            data-test="entry-end-date"
            @update:modelValue="v => endDate = dateToDay(v)"
          />
        </template>
        <SplitDateTimePicker
          v-else
          v-model="end"
          idPrefix="oncall-day-entry-end"
          label="End"
          dataTest="entry-end"
        />
        <span class="end-of-day">
          <OnmsCheckbox
            v-model="endOfDay"
            inputId="oncall-day-entry-eod"
            data-test="entry-end-of-day"
          />
          <label for="oncall-day-entry-eod">End of day</label>
        </span>
      </div>
      <small
        v-if="problem"
        class="problem"
        data-test="entry-problem"
      >{{ problem }}</small>
    </div>

    <template #footer>
      <OnmsButton
        variant="text"
        label="Cancel"
        data-test="cancel-button"
        @click="emit('update:visible', false)"
      />
      <OnmsButton
        label="OK"
        :disabled="!user || !!problem"
        data-test="ok-button"
        @click="ok"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsCheckbox, OnmsDatePicker, OnmsDialog, OnmsSelect } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import SplitDateTimePicker from '@/components/Common/SplitDateTimePicker.vue'
import { dateToDay, dayToDate } from '@/lib/pickerTime'

const props = defineProps<{
  visible: boolean
  // yyyy-MM-dd
  day: string
  members: string[]
}>()

const emit = defineEmits<{
  'update:visible': [visible: boolean]
  // minute-precision local times; with endOfDay, end is <date>T23:59
  ok: [entry: { user: string, start: string, end: string, endOfDay: boolean }]
}>()

const user = ref('')
const begin = ref('')
const end = ref('')
const endDate = ref('')
const endOfDay = ref(true)

watch(
  [() => props.visible, () => props.day],
  ([isVisible]) => {
    if (isVisible) {
      user.value = ''
      begin.value = `${props.day}T00:00`
      end.value = `${props.day}T23:59`
      endDate.value = props.day
      endOfDay.value = true
    }
  },
  { immediate: true }
)

// leaving end of day on keeps the date; turning it off starts from the end of that date
watch(endOfDay, (on) => {
  if (on) {
    endDate.value = end.value.slice(0, 10) || endDate.value
  } else {
    end.value = `${endDate.value}T23:59`
  }
})

const effectiveEnd = computed(() => endOfDay.value ? `${endDate.value}T23:59` : end.value)

const COMPLETE = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/

const problem = computed(() => {
  // either part of a split date and time may still be empty
  if (!COMPLETE.test(begin.value) || !COMPLETE.test(effectiveEnd.value)) {
    return 'Enter a begin and an end.'
  }
  // end of day runs through 23:59:59, so a begin at 23:59 still comes before it
  if (begin.value >= (endOfDay.value ? `${endDate.value}T23:59:59` : effectiveEnd.value)) {
    return 'The time must begin before it ends.'
  }
  return null
})

const ok = () => {
  emit('ok', { user: user.value, start: begin.value, end: effectiveEnd.value, endOfDay: endOfDay.value })
  emit('update:visible', false)
}
</script>

<style lang="scss" scoped>
.form-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;
}

.time-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;

  .row-label {
    min-width: 3.5rem;
  }
}

.date-input {
  width: 9rem;
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}

.end-of-day {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
}

.problem {
  color: var(--p-red-500, #d32f2f);
}
</style>
