<template>
  <div class="feather-row">
    <div class="feather-col-12 wrapper">
      <FeatherMegaMenu name="Management" close-text="Close" class="graph-controls">
        <template v-slot:button>{{ selectedTime }}</template>

        <FeatherList>
          <FeatherListItem
            v-for="option in options"
            :key="option.label"
            @click="selectOption($event, option)"
          >{{ option.label }}</FeatherListItem>
        </FeatherList>
      </FeatherMegaMenu>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { FeatherList, FeatherListItem } from '@featherds/list'
import { FeatherMegaMenu } from '@featherds/megamenu'
import { sub, getUnixTime } from 'date-fns'

interface TimeOption {
  label: string
  time: Record<string, unknown>
}

const emit = defineEmits(['updateTime'])

const selectedTime = ref('Last 24 hours')
const options = [
  { label: 'Last 5 minutes', time: { minutes: '5' } },
  { label: 'Last 15 minutes', time: { minutes: '15' } },
  { label: 'Last 30 minutes', time: { minutes: '30' } },
  { label: 'Last 1 hour', time: { hours: '1' } },
  { label: 'Last 3 hours', time: { hours: '3' } },
  { label: 'Last 12 hours', time: { hours: '12' } },
  { label: 'Last 24 hours', time: { hours: '24' } },
  { label: 'Last 2 days', time: { days: '2' } },
  { label: 'Last 7 days', time: { days: '7' } }
]

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
}
</script>

<style lang="scss" scoped>
.wrapper {
  height: 70px;
  .graph-controls {
    padding: 8px;
    max-height: 35px;
  }
}
</style>

<style lang="scss">
.graph-controls {
  .menu {
    max-width: 370px;
  }
  .menu-name {
    display: none !important;
  }
}
</style>
