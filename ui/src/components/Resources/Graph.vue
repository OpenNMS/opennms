<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <LineChart :chartData="lineChartData" :options="options" v-if="graphData.columns" />
    </div>
  </div>
</template>
  
<script setup lang=ts>
import { GraphMetricsPayload, GraphMetricsResponse, Metric, PreFabGraph, StartEndTime } from '@/types'
import { onMounted, ref, computed, PropType, watch } from 'vue'
import { useStore } from 'vuex'
import { RrdGraphConverter } from 'backshift'
import { LineChart } from 'vue-chart-3'
import { ChartOptions, TitleOptions } from 'chart.js'
import { formatXLabels } from './utils'

interface SeriesObject {
  color: string
  metric: string
  name: string
  type: string
}

const emit = defineEmits(['addGraphDefinition'])

const props = defineProps({
  definition: {
    required: true,
    type: String
  },
  resourceId: {
    required: true,
    type: String
  },
  time: {
    required: true,
    type: Object as PropType<StartEndTime>
  }
})

const store = useStore()
const graphData = ref<GraphMetricsResponse>({} as GraphMetricsResponse)
const series = ref<SeriesObject[]>([])
const convertedGraphDataRef = ref({
  title: '',
  verticalLabel: '',
  metrics: []
})

const options = computed<ChartOptions>(() => ({
  responsive: true,
  plugins: {
    legend: {
      display: true,
      position: 'bottom',
      align: 'start'
    },
    title: {
      display: true,
      text: convertedGraphDataRef.value.title,
    } as TitleOptions,
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
    },
  },
  scales: {
    y: {
      title: {
        display: true,
        text: convertedGraphDataRef.value.verticalLabel,
      } as TitleOptions
    }
  }
}))

const getLabels = (index: number) => {
  const label = graphData.value.labels[index]
  for (const item of series.value) {
    if (item.metric === label) {
      if (item.name) {
        return item.name
      }
    }
  }
}

const getColors = (index: number) => {
  const colors = []
  const label = graphData.value.labels[index]
  for (const item of series.value) {
    if (item.metric === label) {
      if (item.name) {
        colors.push(item.color)
      }
    }
  }

  return colors
}

const isHidden = (index: number) => {
  const label = graphData.value.labels[index]

  for (const item of series.value) {
    if (item.metric === label) {
      if (item.type === 'hidden') {
        return true
      }
    }
  }

  return false
}

const dataSets = computed(() => {
  const sets = []

  for (const [index, column] of graphData.value.columns.entries()) {
    if (isHidden(index)) continue

    sets.push({
      label: getLabels(index),
      data: column.values,
      backgroundColor: getColors(index)
    })
  }

  return sets
})

const lineChartData = computed(() => {
  return {
    labels: graphData.value.formattedTimestamps,
    datasets: dataSets.value
  }
})

const getGraphMetricsPayload = (source: Metric[]): GraphMetricsPayload => {
  const resolution = 1000
  const start = props.time.startTime as number * 1000
  const end = props.time.endTime as number * 1000
  const step = Math.floor((end - start) / resolution)

  return {
    start,
    end,
    step,
    source
  }
}

const render = async () => {
  const definitionData: PreFabGraph = await store.dispatch('graphModule/getDefinitionData', props.definition)

  try {
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

    const payload = getGraphMetricsPayload(metrics)
    const graphMetrics = await store.dispatch('graphModule/getGraphMetrics', payload)
    const formattedMetrics = formatXLabels(graphMetrics, props.time.format)
    graphData.value = formattedMetrics

  } catch (error) {
    console.log('Could not render graph for ', props.definition)
    emit('addGraphDefinition') // adds another to infinite scroll
  }
}

watch(props.time, () => render())
onMounted(() => render())
</script>
  
<style scoped lang="scss">
</style>
  