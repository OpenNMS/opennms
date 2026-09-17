<template>
  <div class="time-spans" data-test="time-spans">
    <div class="section-title">Time spans</div>

    <div class="new-span">
      <!-- specific: full start/end dates; others: time-of-day (+ optional day) -->
      <div v-if="type === 'weekly'" class="span-row">
        <span class="span-label">Day of Week</span>
        <OnmsSelect v-model="fields.day" :options="DAYS_OF_WEEK" optionLabel="label" optionValue="value" data-test="weekly-day" />
      </div>
      <div v-if="type === 'monthly'" class="span-row">
        <span class="span-label">Day of Month</span>
        <OnmsSelect v-model="fields.day" :options="DAYS_OF_MONTH" optionLabel="label" optionValue="value" data-test="monthly-day" />
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
      <OnmsButton label="Add Timespan" icon="pi pi-plus" class="add-button" data-test="add-time" :disabled="!!problem" @click="addSpan" />
    </div>

    <ul v-if="times.length" class="span-list">
      <li v-for="(t, index) in times" :key="index" class="span-item">
        <span data-test="time-row">{{ describeOutageTime(type, t) }}</span>
        <OnmsIconButton
          :icon="Delete"
          severity="danger"
          :title="`Remove time span`"
          aria-label="Remove time span"
          data-test="remove-time"
          @click="emit('remove', index)"
        />
      </li>
    </ul>
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
    margin-bottom: 0.75rem;
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

  .span-list {
    list-style: none;
    margin: 0;
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
    padding: 0.15rem 0.25rem;
    border-radius: 4px;
    background: var(--p-content-hover-background, rgba(127, 127, 127, 0.08));
  }
}
</style>
