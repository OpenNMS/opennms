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
        <label for="oncall-day-entry-begin">Begin</label>
        <OnmsDatePicker
          :modelValue="dayTimeToDate(begin)"
          inputId="oncall-day-entry-begin"
          showTime
          hourFormat="12"
          class="time-input"
          data-test="entry-begin"
          @update:modelValue="v => begin = dateToDayTime(v)"
        />
      </div>
      <div class="time-row">
        <label for="oncall-day-entry-end">End</label>
        <OnmsDatePicker
          v-if="endOfDay"
          :modelValue="dayToDate(endDate)"
          inputId="oncall-day-entry-end"
          class="time-input"
          data-test="entry-end-date"
          @update:modelValue="v => endDate = dateToDay(v)"
        />
        <OnmsDatePicker
          v-else
          :modelValue="dayTimeToDate(end)"
          inputId="oncall-day-entry-end"
          showTime
          hourFormat="12"
          class="time-input"
          data-test="entry-end"
          @update:modelValue="v => end = dateToDayTime(v)"
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
import { dateToDay, dateToDayTime, dayTimeToDate, dayToDate } from '@/lib/pickerTime'

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

const problem = computed(() => {
  if (!begin.value || !effectiveEnd.value || effectiveEnd.value.startsWith('T')) {
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

  label:first-child {
    min-width: 3.5rem;
  }
}

.time-input {
  width: 13rem;
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
