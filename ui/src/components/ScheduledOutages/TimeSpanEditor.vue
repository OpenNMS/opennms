<template>
  <div class="time-spans" data-test="time-spans">
    <div class="section-title">Time spans</div>

    <ul class="span-list" data-test="time-list">
      <li v-if="!times.length" class="none" data-test="time-empty">No time spans yet. Add one below.</li>
      <li v-for="(t, index) in times" :key="index" class="span-item">
        <span data-test="time-row">{{ describeOutageTime(type, t) }}</span>
        <OnmsIconButton
          :icon="Delete"
          severity="danger"
          title="Remove time span"
          aria-label="Remove time span"
          data-test="remove-time"
          @click="emit('remove', index)"
        />
      </li>
    </ul>

    <div class="new-span" data-test="new-span">
      <div class="subsection-title">Add a time span</div>
      <!-- specific: full start/end dates; others: time-of-day (+ optional day) -->
      <div v-if="type === 'weekly'" class="span-row">
        <label class="span-label" for="span-day">Day of Week</label>
        <OnmsSelect v-model="fields.day" inputId="span-day" :options="DAYS_OF_WEEK" optionLabel="label" optionValue="value" data-test="weekly-day" />
      </div>
      <div v-if="type === 'monthly'" class="span-row">
        <label class="span-label" for="span-day">Day of Month</label>
        <OnmsSelect v-model="fields.day" inputId="span-day" :options="DAYS_OF_MONTH" optionLabel="label" optionValue="value" data-test="monthly-day" />
      </div>
      <div class="span-row">
        <label class="span-label" for="span-start">Start</label>
        <OnmsDatePicker
          v-model="fields.start"
          inputId="span-start"
          :showTime="type === 'specific'"
          :timeOnly="type !== 'specific'"
          hourFormat="24"
          showSeconds
          data-test="span-start"
        />
      </div>
      <div class="span-row">
        <label class="span-label" for="span-end">End</label>
        <OnmsDatePicker
          v-model="fields.end"
          inputId="span-end"
          :showTime="type === 'specific'"
          :timeOnly="type !== 'specific'"
          hourFormat="24"
          showSeconds
          data-test="span-end"
        />
      </div>
      <small v-if="problem" class="field-error" data-test="span-error">{{ problem }}</small>
      <OnmsButton label="Add Time Span" icon="pi pi-plus" class="add-button" data-test="add-time" :disabled="!!problem" @click="addSpan" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { OnmsButton, OnmsDatePicker, OnmsIconButton, OnmsSelect } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import { OutageTime, OutageType } from '@/types/scheduledOutage'
import {
  CompleteTimeSpanFields,
  DAYS_OF_MONTH,
  DAYS_OF_WEEK,
  TimeSpanFields,
  buildOutageTime,
  defaultTimeSpanFields,
  describeOutageTime,
  timeSpanProblem
} from '@/components/ScheduledOutages/outageTime'

const props = defineProps<{
  type: OutageType
  times: OutageTime[]
}>()

const emit = defineEmits<{
  add: [value: OutageTime]
  remove: [index: number]
}>()

const fields = reactive<TimeSpanFields>(defaultTimeSpanFields(new Date().getFullYear()))

// the day field is shared between weekly (names) and monthly (numbers), so a
// type switch must reset it to a value that exists in the new option list
watch(() => props.type, (type) => {
  if (type === 'monthly' && !DAYS_OF_MONTH.some(d => d.value === fields.day)) {
    fields.day = '1'
  } else if (type === 'weekly' && !DAYS_OF_WEEK.some(d => d.value === fields.day)) {
    fields.day = 'sunday'
  }
}, { immediate: true })

const problem = computed(() => timeSpanProblem(props.type, fields))

const addSpan = () => {
  if (problem.value) {
    return
  }
  emit('add', buildOutageTime(props.type, fields as CompleteTimeSpanFields))
}
</script>

<style scoped lang="scss">
.time-spans {
  margin-top: 1rem;

  .section-title {
    font-weight: 600;
    margin-bottom: 0.5rem;
  }

  .new-span {
    display: flex;
    flex-direction: column;
    gap: 0.4rem;
  }

  .subsection-title {
    font-weight: 600;
  }

  .span-row {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    flex-wrap: wrap;
  }

  .span-label {
    width: 6.5rem;
    font-weight: 600;
  }

  .add-button {
    align-self: flex-start;
  }

  .field-error {
    color: var(--p-red-500, #e24c4c);
  }

  .none {
    font-style: italic;
    color: var(--p-text-muted-color);
  }

  .span-list {
    list-style: none;
    margin: 0 0 1rem 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .span-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
    min-height: 2.25rem;
    padding: 0 0.25rem 0 0.5rem;
    border-radius: 4px;
    background: var(--p-content-hover-background, rgba(127, 127, 127, 0.08));
  }
}
</style>
