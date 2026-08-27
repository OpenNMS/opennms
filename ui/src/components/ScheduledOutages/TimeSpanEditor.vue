<template>
  <div class="time-spans" data-test="time-spans">
    <div class="section-title">Time spans</div>

    <div class="new-span">
      <!-- specific: full start/end dates; others: time-of-day (+ optional day) -->
      <template v-if="type === 'specific'">
        <div class="span-row">
          <span class="span-label">Start</span>
          <OnmsSelect v-model="fields.startDay" :options="DAYS_OF_MONTH_PADDED" optionLabel="label" optionValue="value" data-test="specific-start-day" />
          <OnmsSelect v-model="fields.startMonth" :options="MONTHS" optionLabel="label" optionValue="value" data-test="specific-start-month" />
          <OnmsSelect v-model="fields.startYear" :options="years" optionLabel="label" optionValue="value" data-test="specific-start-year" />
          <OnmsSelect v-model="fields.startHour" :options="HOURS" optionLabel="label" optionValue="value" />
          <OnmsSelect v-model="fields.startMinute" :options="MINUTES" optionLabel="label" optionValue="value" />
          <OnmsSelect v-model="fields.startSecond" :options="SECONDS" optionLabel="label" optionValue="value" />
        </div>
        <div class="span-row">
          <span class="span-label">End</span>
          <OnmsSelect v-model="fields.endDay" :options="DAYS_OF_MONTH_PADDED" optionLabel="label" optionValue="value" data-test="specific-end-day" />
          <OnmsSelect v-model="fields.endMonth" :options="MONTHS" optionLabel="label" optionValue="value" />
          <OnmsSelect v-model="fields.endYear" :options="years" optionLabel="label" optionValue="value" />
          <OnmsSelect v-model="fields.endHour" :options="HOURS" optionLabel="label" optionValue="value" />
          <OnmsSelect v-model="fields.endMinute" :options="MINUTES" optionLabel="label" optionValue="value" />
          <OnmsSelect v-model="fields.endSecond" :options="SECONDS" optionLabel="label" optionValue="value" />
        </div>
      </template>

      <template v-else>
        <div v-if="type === 'weekly'" class="span-row">
          <span class="span-label">Day</span>
          <OnmsSelect v-model="fields.day" :options="DAYS_OF_WEEK" optionLabel="label" optionValue="value" data-test="weekly-day" />
        </div>
        <div v-if="type === 'monthly'" class="span-row">
          <span class="span-label">Day</span>
          <OnmsSelect v-model="fields.day" :options="DAYS_OF_MONTH" optionLabel="label" optionValue="value" data-test="monthly-day" />
        </div>
        <div class="span-row">
          <span class="span-label">Start</span>
          <OnmsSelect v-model="fields.startHour" :options="HOURS" optionLabel="label" optionValue="value" data-test="time-start-hour" />
          <OnmsSelect v-model="fields.startMinute" :options="MINUTES" optionLabel="label" optionValue="value" />
          <OnmsSelect v-model="fields.startSecond" :options="SECONDS" optionLabel="label" optionValue="value" />
        </div>
        <div class="span-row">
          <span class="span-label">End</span>
          <OnmsSelect v-model="fields.endHour" :options="HOURS" optionLabel="label" optionValue="value" data-test="time-end-hour" />
          <OnmsSelect v-model="fields.endMinute" :options="MINUTES" optionLabel="label" optionValue="value" />
          <OnmsSelect v-model="fields.endSecond" :options="SECONDS" optionLabel="label" optionValue="value" />
        </div>
      </template>

      <OnmsButton label="Add Outage" icon="pi pi-plus" data-test="add-time" @click="addSpan" />
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
import { reactive } from 'vue'
import { OnmsButton, OnmsIconButton, OnmsSelect } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import { OutageTime, OutageType } from '@/types/scheduledOutage'
import {
  DAYS_OF_MONTH,
  DAYS_OF_MONTH_PADDED,
  DAYS_OF_WEEK,
  HOURS,
  MINUTES,
  MONTHS,
  SECONDS,
  TimeSpanFields,
  buildOutageTime,
  defaultTimeSpanFields,
  describeOutageTime,
  yearOptions
} from '@/components/ScheduledOutages/outageTime'

const props = defineProps<{
  type: OutageType
  times: OutageTime[]
}>()

const emit = defineEmits<{
  add: [value: OutageTime]
  remove: [index: number]
}>()

const currentYear = new Date().getFullYear()
const years = yearOptions(currentYear)
const fields = reactive<TimeSpanFields>(defaultTimeSpanFields(currentYear))

const addSpan = () => {
  emit('add', buildOutageTime(props.type, fields))
}
</script>

<style scoped lang="scss">
.time-spans {
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
    width: 3rem;
    font-weight: 600;
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
