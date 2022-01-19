<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <TimeControls @updateTime="updateTime" />
      <GraphContainer
        v-for="resource in resources"
        :resource="resource"
        :key="resource.id"
        :time="time"
        :definitionsToDisplay="definitionsToDisplay"
        @addGraphDefinition="addGraphDefinition"
      />
    </div>
  </div>
</template>
  
<script setup lang=ts>
import { computed, reactive, watch, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import GraphContainer from './GraphContainer.vue'
import TimeControls from './TimeControls.vue'
import { sub, getUnixTime } from 'date-fns'
import { StartEndTime } from '@/types'
import { useScroll } from '@vueuse/core'

const el = document.getElementById('card')
const { arrivedState } = useScroll(el)
const definitionsToDisplay = ref<any>([])
const store = useStore()
const now = new Date()

const resources = computed<{ id: string, definitions: string[], label: string }[]>(() => store.state.graphModule.definitions)
const definitionsList: string[] = JSON.parse(JSON.stringify(store.state.graphModule.definitionsList))

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

const addGraphDefinition = () => {
  const next = definitionsList.shift()
  definitionsToDisplay.value = [...definitionsToDisplay.value, next]
}

watch(arrivedState, () => {
  if (arrivedState.bottom) {
    addGraphDefinition()
  }
})
onMounted(() => {
  addGraphDefinition()
  addGraphDefinition()
  addGraphDefinition()
  addGraphDefinition()
})
</script>
  
<style scoped lang="scss">
</style>
  