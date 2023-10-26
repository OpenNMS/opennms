<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-11">
      <div class="controls">
        <TimeControls @updateTime="updateTime" />
        <FeatherInput
          v-if="!singleGraphDefinition"
          class="search-input"
          label="Search"
          v-model="searchVal"
          @update:modelValue="searchHandler"
        />
      </div>
      <GraphContainer
        v-for="resource in resources"
        :resource="resource"
        :key="resource.id"
        :time="time"
        :definitionsToDisplay="definitionsToDisplay"
        :isSingleGraph="Boolean(singleGraphDefinition)"
        @addGraphDefinition="addGraphDefinition"
      />
    </div>
  </div>
</template>
  
<script setup lang="ts">
import GraphContainer from './GraphContainer.vue'
import TimeControls from './TimeControls.vue'
import { sub, getUnixTime } from 'date-fns'
import { StartEndTime } from '@/types'
import { FeatherInput } from '@featherds/input'
import useSpinner from '@/composables/useSpinner'
import { UpdateModelFunction } from '@/types'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { GraphDefinition, useGraphStore } from '@/stores/graphStore'
import { useMenuStore } from '@/stores/menuStore'
import { useResourceStore } from '@/stores/resourceStore'
import { BreadCrumb } from '@/types'

const el = document.getElementById('card')
const { arrivedState } = useScroll(el, { offset: { bottom: 100 } })
const definitionsToDisplay = ref<string[]>([])

const graphStore = useGraphStore()
const menuStore = useMenuStore()
const resourceStore = useResourceStore()
const router = useRouter()
const { startSpinner, stopSpinner } = useSpinner()
const now = new Date()
const initNumOfGraphs = 4
const searchVal = ref<string>('')

const props = defineProps({
  singleGraphDefinition: {
    type: String
  },
  singleGraphResourceId: {
    type: String
  },
  label: {
    type: String
  }
})

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Resource Graphs', to: '/resource-graphs' },
    { label: 'Graphs', to: '#', position: 'last' }
  ]
})

const resources = props.singleGraphResourceId ?
  ref([{ id: props.singleGraphResourceId, definitions: [props.singleGraphDefinition as string], label: props.label as string } as GraphDefinition]) :
  computed<GraphDefinition[]>(() => graphStore.definitions)

const definitionsList = computed<string[]>(() => graphStore.definitionsList)
let definitionsListCopy: string[] = JSON.parse(JSON.stringify(graphStore.definitionsList))

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
  const next = definitionsListCopy.shift()
  if (next) {
    definitionsToDisplay.value = [...definitionsToDisplay.value, next]
  }
}

const searchHandler: UpdateModelFunction = (searchInputVal: string) => {
  startSpinner()
  searchVal.value = searchInputVal

  const search = useDebounceFn((val: string) => {
    if (val) {
      definitionsListCopy = definitionsList.value.filter((definition) =>
        definition.toLowerCase().includes(val.toLowerCase()))

      definitionsToDisplay.value = definitionsListCopy.splice(0, 4)
    }

    if (!val) {
      definitionsListCopy = JSON.parse(JSON.stringify(definitionsList.value))
      definitionsToDisplay.value = definitionsListCopy.splice(0, 4)
    }

    stopSpinner()
  }, 1000)

  search(searchInputVal)
}

watch(arrivedState, () => {
  // add a new graph when the scroll bar hits the bottom
  if (arrivedState.bottom && !props.singleGraphDefinition) {
    addGraphDefinition()
  }
})

onMounted(() => {
  // for displaying only selected graph
  if (props.singleGraphDefinition) {
    definitionsToDisplay.value = [props.singleGraphDefinition]
    return
  }

  [...Array(initNumOfGraphs)].forEach(() => {
    addGraphDefinition()
  })
})

onBeforeMount(() => {
  if (props.singleGraphDefinition) {
    return
  }

  // if no resources, route to resource selection
  if (!resourceStore.resources.length) {
    router.push('/resource-graphs')
  }
})
</script>

<style scoped lang="scss">
.controls {
  display: flex;
  justify-content: space-between;

  .search-input {
    width: 230px;
    margin-top: 5px;
    margin-bottom: -7px;
  }
}
</style>
