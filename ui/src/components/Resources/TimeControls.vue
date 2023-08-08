<template>
  <div class="feather-row">
    <div class="feather-col-12 wrapper">
      <ShimFeatherMegaMenu ref="mega" name="Management" close-text="Close" class="graph-controls">
        <template v-slot:button>
          {{ selectedTime }} &nbsp;
          <FeatherIcon :icon="ArrowDropDown" />
        </template>

        <div class="feather-row">
          <div class="feather-col-5">
            <FeatherList>
              <FeatherListItem
                v-for="option in options"
                :key="option.label"
                @click="selectOption($event, option)"
              >{{ option.label }}</FeatherListItem>
            </FeatherList>
          </div>

          <div class="feather-col-5">
            <FeatherDateInput v-model="startDateRef" label="Start Date" class="date-input" />
            <FeatherSelect
              :options="times"
              v-model="startTimeRef"
              label="Start Time"
              text-prop="label"
            />
            <FeatherDateInput v-model="endDateRef" label="End Date" class="date-input" />
            <FeatherSelect
              :options="times"
              v-model="endTimeRef"
              label="End Time"
              text-prop="label"
            />
            <FeatherButton
              :disabled="disableCustomTimeBtn"
              text
              @click="applyCustomTime"
            >Apply custom time</FeatherButton>
          </div>
        </div>
      </ShimFeatherMegaMenu>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherList, FeatherListItem } from '@featherds/list'
import { ShimFeatherMegaMenu } from '../Common/ShimFeatherMegaMenu'
// add this back when exports are fixed
//import { FeatherMegaMenu } from '@featherds/megamenu'
import { add, sub, getUnixTime, differenceInHours } from 'date-fns'
import { FeatherDateInput } from '@featherds/date-input'
import { FeatherButton } from '@featherds/button'
import { FeatherSelect } from '@featherds/select'
import { FeatherIcon } from '@featherds/icon'
import ArrowDropDown from '@featherds/icon/navigation/ArrowDropDown'

interface TimeOption {
  label: string
  time: Record<string, unknown>
}

const emit = defineEmits(['updateTime'])

const mega = ref()
const startDateRef = ref()
const startTimeRef = ref<TimeOption>({ label: '1 PM', time: { hours: '1' } })
const endDateRef = ref()
const endTimeRef = ref<TimeOption>({ label: '1 PM', time: { hours: '1' } })

const selectedTime = ref('Last Day')
const options = [
  { label: 'Last hour', time: { minutes: '60' } },
  { label: 'Last 2 hours', time: { hours: '2' } },
  { label: 'Last 4 hours', time: { hours: '4' } },
  { label: 'Last 8 hours', time: { hours: '5' } },
  { label: 'Last 12 hours', time: { hours: '12' } },
  { label: 'Last day', time: { hours: '24' } },
  { label: 'Last week', time: { days: '7' } },
  { label: 'Last month', time: { months: '1' } },
  { label: 'Last year', time: { years: '1' } }
]

const times = [
  { label: '12 AM', time: { hours: '0' } },
  { label: '1 AM', time: { hours: '1' } },
  { label: '2 AM', time: { hours: '2' } },
  { label: '3 AM', time: { hours: '3' } },
  { label: '4 AM', time: { hours: '4' } },
  { label: '5 AM', time: { hours: '5' } },
  { label: '6 AM', time: { hours: '6' } },
  { label: '7 AM', time: { hours: '7' } },
  { label: '8 AM', time: { hours: '8' } },
  { label: '9 AM', time: { hours: '9' } },
  { label: '10 AM', time: { hours: '10' } },
  { label: '11 AM', time: { hours: '11' } },
  { label: '12 PM', time: { hours: '12' } },
  { label: '1 PM', time: { hours: '13' } },
  { label: '2 PM', time: { hours: '14' } },
  { label: '3 PM', time: { hours: '15' } },
  { label: '4 PM', time: { hours: '16' } },
  { label: '5 PM', time: { hours: '17' } },
  { label: '6 PM', time: { hours: '18' } },
  { label: '7 PM', time: { hours: '19' } },
  { label: '8 PM', time: { hours: '20' } },
  { label: '9 PM', time: { hours: '21' } },
  { label: '10 PM', time: { hours: '22' } },
  { label: '11 PM', time: { hours: '23' } }
]

const disableCustomTimeBtn = computed(() => Boolean(!startDateRef.value || !startTimeRef.value || !endDateRef.value || !endTimeRef.value))

const selectOption = (event: Event, option: TimeOption) => {
  event.stopImmediatePropagation() // prevent @featherds issue
  selectedTime.value = option.label
  const now = new Date()
  const startTime = getUnixTime(sub(now, option.time))
  const endTime = getUnixTime(now)
  const format = Object.keys(option.time)[0]

  emit('updateTime', {
    startTime,
    endTime,
    format
  })

  mega.value.closeMenu()
}

const applyCustomTime = () => {
  let format = 'hours'
  const startTime = getUnixTime(add(startDateRef.value, startTimeRef.value.time))
  const endTime = getUnixTime(add(endDateRef.value, endTimeRef.value.time))

  const difference = differenceInHours(startTime, endTime)
  if (difference < 1) format = 'minutes'
  if (difference > 24) format = 'days'
  if (difference > 8766) format = 'years'

  emit('updateTime', {
    startTime,
    endTime,
    format
  })

  selectedTime.value = 'Custom Time'
  mega.value.closeMenu()
}
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
.wrapper {
  height: 70px;
  .graph-controls {
    padding: 8px;
    max-height: 35px;
  }

  .date-input {
    @include body-small;
  }
}
</style>

<style lang="scss">
.graph-controls {
  .menu {
    max-width: 550px;
  }
  .menu-name {
    display: none !important;
  }
}
</style>
../Common/ProxyFeatherMegaMenu