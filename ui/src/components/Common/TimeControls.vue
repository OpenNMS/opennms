<template>
  <div class="onms-row">
    <div class="onms-col-12 wrapper">
      <span
        v-if="label"
        :id="labelId"
        class="time-range-label"
      >{{ label }}</span>

      <OnmsButton
        variant="text"
        class="graph-controls"
        aria-haspopup="true"
        :aria-labelledby="label ? labelId : undefined"
        @click="toggleMenu"
      >
        {{ selectedTime }} &nbsp;
        <OnmsIcon :icon="ArrowDropDown" />
      </OnmsButton>

      <OnmsPopover
        ref="menu"
        class="graph-controls-panel"
      >
        <div class="menu-content">
          <ul
            class="onms-list options-col"
            @mousedown="latchPickerDismissal"
          >
            <li
              class="list-item"
              v-for="option in TIME_RANGE_OPTIONS"
              :key="option.label"
              @click="selectOption(option)"
            >{{ option.label }}</li>
          </ul>

          <div class="custom-col">
            <FormField label="Start" class="date-input">
              <OnmsDatePicker
                v-model="startDateRef"
                showTime
                hourFormat="12"
                placeholder="Start date and time"
                :maxDate="endDateRef"
                @show="openPickerCount++"
                @hide="openPickerCount--"
              />
            </FormField>
            <FormField label="End" class="date-input">
              <OnmsDatePicker
                v-model="endDateRef"
                showTime
                hourFormat="12"
                placeholder="End date and time"
                :minDate="startDateRef"
                @show="openPickerCount++"
                @hide="openPickerCount--"
              />
            </FormField>
            <OnmsButton
              :disabled="disableCustomTimeBtn"
              variant="ghost"
              @click="applyCustomTime"
            >Apply custom time</OnmsButton>
          </div>
        </div>
      </OnmsPopover>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useId } from 'vue'

import { OnmsButton, OnmsDatePicker, OnmsIcon, OnmsPopover } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { sub, getUnixTime, differenceInHours, fromUnixTime } from 'date-fns'
import ArrowDropDown from '@opennms/onms-ui/icons/navigation/ArrowDropDown.vue'
import {
  relativeRangeOf,
  resolveRelativeRange,
  TIME_RANGE_OPTIONS,
  TimeOption
} from './utils/timeRangeOptions'

// The label lives here rather than in each consumer because aria-labelledby
// applied from outside would fall through to this component's plain root div,
// where it is inert; inside, it can name the actual trigger button. Optional so
// a consumer that supplies its own heading can omit it.
defineProps<{
  label?: string
}>()

const emit = defineEmits(['updateTime'])

const labelId = useId()

const menu = ref()

// One instant per range end (NMS-20280). These used to be a date ref plus a
// whole-hour OnmsSelect whose value was added to the date; OnmsDatePicker's
// showTime carries the time of day directly, to the minute.
const startDateRef = ref<Date | null>(null)
const endDateRef = ref<Date | null>(null)

const selectedTime = ref('Last day')

// How many picker overlays are on screen (there are two, and both can be open in
// sequence). Fed by OnmsDatePicker's show/hide.
const openPickerCount = ref(0)

/**
 * Latched on mousedown over the preset list, NOT read at click time.
 *
 * PrimeVue's DatePicker dismisses its overlay from a document-level *mousedown*
 * listener, so by the time the preset's click handler runs the panel is already
 * gone and openPickerCount is back to 0. Sampling here -- the <ul> sees mousedown
 * before it bubbles to document -- is what makes "a click that merely dismissed a
 * picker" distinguishable from "a click that chose a preset".
 */
const dismissedPickerOnPress = ref(false)

const latchPickerDismissal = () => {
  dismissedPickerOnPress.value = openPickerCount.value > 0
}

const disableCustomTimeBtn = computed(() => !startDateRef.value || !endDateRef.value)

const toggleMenu = (event: Event) => menu.value.toggle(event)

/**
 * A preset range is relative: it is emitted as a unit/amount alongside the resolved
 * window so the consumer can re-resolve it later. Only "Apply custom time" below
 * produces a genuinely absolute window, matching the legacy graph pages.
 */
const selectOption = (option: TimeOption) => {
  // The press that opened this click dismissed a picker overlay instead; accept
  // whatever that picker had selected and leave the popover open.
  if (dismissedPickerOnPress.value) {
    dismissedPickerOnPress.value = false
    return
  }

  selectedTime.value = option.label

  const range = relativeRangeOf(option)

  if (range) {
    emit('updateTime', resolveRelativeRange(range))
    menu.value.hide()
    return
  }

  // Fallback for an option that is not a single unit/amount; treated as absolute.
  const now = new Date()

  emit('updateTime', {
    startTime: getUnixTime(sub(now, option.time)),
    endTime: getUnixTime(now),
    format: Object.keys(option.time)[0]
  })

  menu.value.hide()
}

const applyCustomTime = () => {
  // The button is disabled until both are set; this narrows the refs without a cast.
  if (!startDateRef.value || !endDateRef.value) {
    return
  }

  let format = 'hours'
  const startTime = getUnixTime(startDateRef.value)
  const endTime = getUnixTime(endDateRef.value)

  // end - start, as Dates. This previously passed unix SECONDS in the wrong order,
  // so the difference was always negative and every custom range was labeled as
  // minutes.
  const difference = differenceInHours(fromUnixTime(endTime), fromUnixTime(startTime))

  if (difference < 1) {
    format = 'minutes'
  }
  if (difference > 24) {
    format = 'days'
  }
  if (difference > 8766) {
    format = 'years'
  }

  // No `range`: an explicit start and end is absolute by definition, and must not
  // slide when the graph is refreshed or the link is reopened.
  emit('updateTime', {
    startTime,
    endTime,
    format
  })

  selectedTime.value = 'Custom Time'
  menu.value.hide()
}
</script>

<style lang="scss" scoped>
@import '@/styles/onms-typography';
// No fixed height: the row is as tall as its tallest child, so the label text and
// the button text share a baseline. A 70px box around a 35px button made that
// impossible.
.wrapper {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;

  .time-range-label {
    font-weight: 700;
    white-space: nowrap;
  }

  .graph-controls {
    padding: 8px;
    max-height: 35px;
  }
}
</style>

<style lang="scss">
@import '@/styles/onms-typography';

.graph-controls-panel {
  .menu-content {
    display: flex;
    gap: 2rem;
    min-width: 40em;
    max-width: 550px;
  }

  .options-col {
    flex: 0 0 40%;
    list-style: none;
    padding: 0;
    margin: 0;

    .list-item {
      padding: 0.5rem 0.75rem;
      cursor: pointer;

      &:hover {
        background: var(--p-highlight-background);
      }
    }
  }

  .custom-col {
    flex: 1;

    // 1em between the Start, End and Apply blocks.
    > * + * {
      margin-top: 1em;
    }

    .date-input {
      @include onms-body-small;
    }
  }
}
</style>
