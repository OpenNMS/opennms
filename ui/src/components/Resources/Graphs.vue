<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <TimeControls @updateTime="updateTime" />
      <GraphContainer
        v-for="resource in resources"
        :resource="resource"
        :key="resource.id"
        :time="time"
      />
    </div>
  </div>
</template>
  
<script setup lang=ts>
import { computed, reactive } from 'vue'
import { useStore } from 'vuex'
import GraphContainer from './GraphContainer.vue'
import TimeControls from './TimeControls.vue'
import { Chart, registerables } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'
import { sub, getUnixTime } from 'date-fns'
import { StartEndTime } from '@/types'

Chart.register(...registerables)
Chart.register(zoomPlugin)

const store = useStore()
const now = new Date()

const resources = computed<{ id: string, definitions: string[] }[]>(() => store.state.graphModule.definitions)

const time = reactive<StartEndTime>({
  startTime: getUnixTime(sub(now, { hours: 24 })),
  endTime: getUnixTime(now),
  format: 'hours'
})

const updateTime = (newStartEndTime: StartEndTime) => {
  time.endTime = newStartEndTime.endTime
  time.startTime = newStartEndTime.startTime
  time.format = newStartEndTime.format
}
</script>
  
<style scoped lang="scss">
</style>
  