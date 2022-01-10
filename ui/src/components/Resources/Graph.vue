<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <LineChart :chartData="lineChartData" :options="options" v-if="graphData.columns"/>
    </div>
  </div>
</template>
  
<script setup lang=ts>
import { GraphMetricsPayload, GraphMetricsResponse, Metric, PreFabGraph } from '@/types'
import { onMounted, ref, computed } from 'vue'
import { useStore } from 'vuex'
import { RrdGraphConverter } from 'backshift'
import { LineChart } from 'vue-chart-3'

interface SeriesObject {
  color: string
  metric: string
  name: string
  type: string
}

const store = useStore()

const graphData = ref<GraphMetricsResponse>({} as GraphMetricsResponse)
const series = ref<SeriesObject[]>([])
const convertedGraphDataRef = ref({ title: '' })

const options = computed(() => ({
  responsive: true,
  plugins: {
    legend: {
      display: true,
      position: 'bottom'
    },
    title: {
      display: true,
      text: convertedGraphDataRef.value.title,
    },
    zoom: {
      zoom: {
        wheel: {
          enabled: true,
        },
        mode: 'xy',
      },
      pan: {
        enabled: true,
        mode: 'x'
      }
    }
  },
}))

const dataSets = computed(() => {
  const sets = []

  for (const [index, column] of graphData.value.columns.entries()) {
    sets.push({
      label: series.value[index].name,
      data: column.values,
      backgroundColor: series.value[index].color
    })
  }

  return sets
})

const lineChartData = computed(() => {
  return {
    labels: graphData.value.timestamps,
    datasets: dataSets.value
  }
})

const props = defineProps({
  definition: {
    required: true,
    type: String
  },
  resourceId: {
    required: true,
    type: String
  }
})

onMounted(async () => {
  const definitionData: PreFabGraph = await store.dispatch('graphModule/getDefinitionData', props.definition)

  const convertedGraphData = RrdGraphConverter.getData({
    graphDef: definitionData,
    resourceId: props.resourceId
  })

  console.log(convertedGraphData)
  series.value = convertedGraphData.series
  convertedGraphDataRef.value = convertedGraphData

  const metrics: Metric[] = convertedGraphData.metrics.map((metric: any): Metric => ({
    aggregation: metric.aggregation,
    attribute: metric.attribute,
    label: metric.name,
    resourceId: metric.resourceId,
    transient: metric.transient
  }))

  const payload: GraphMetricsPayload = {
    end: 1641831900314,
    start: 1641745500314,
    step: 130909,
    source: metrics
  }

  graphData.value = await store.dispatch('graphModule/getGraphMetrics', payload)
})
</script>
  
<style scoped lang="scss">
</style>
  