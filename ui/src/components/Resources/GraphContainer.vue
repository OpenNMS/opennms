<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div v-for="definition in props.resource.definitions" :key="definition">
        <Graph
          v-if="definitionsToDisplay.includes(definition)"
          :definition="definition"
          :resourceId="props.resource.id"
          :time="time"
          @addGraphDefinition="addGraphDefinition"
        />
      </div>
    </div>
  </div>
</template>
  
<script setup lang=ts>
import { StartEndTime } from '@/types'
import { PropType, ref, watch, onMounted } from 'vue'
import Graph from './Graph.vue'
import { useStore } from 'vuex'
import { useScroll } from '@vueuse/core'

const el = document.getElementById('card')
const { arrivedState } = useScroll(el)
const definitionsToDisplay = ref<any>([])

const store = useStore()

const props = defineProps({
  resource: {
    required: true,
    type: Object as PropType<{ id: string, definitions: string[] }>
  },
  time: {
    required: true,
    type: Object as PropType<StartEndTime>
  }
})

const definitionsList: string[] = JSON.parse(JSON.stringify(store.state.graphModule.definitionsList))

const addGraphDefinition = () => {
  const next = definitionsList.shift()
  console.log(next)
  definitionsToDisplay.value = [...definitionsToDisplay.value, next]
}

watch(arrivedState, () => {
  if (arrivedState.bottom) {
    addGraphDefinition()
  }
})
onMounted(() => {
  console.log(definitionsList)
  addGraphDefinition()
  addGraphDefinition()
  addGraphDefinition()
  addGraphDefinition()
})
</script>
  
<style scoped lang="scss">
</style>
  