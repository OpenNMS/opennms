<template>
  <div class="feather-row">
    <div class="feather-col-12 container">
      <router-link
        v-if="!isSingleGraph"
        :to="`/resource-graphs/graphs/${label}/${definition}/${resourceId}`"
        target="_blank"
      >
        <FeatherButton secondary class="single-graph-btn">Open</FeatherButton>
      </router-link>
      <FeatherTabContainer class="graph-data-tabs">
        <template v-slot:tabs>
          <FeatherTab>Graph</FeatherTab>
          <FeatherTab>Data</FeatherTab>
        </template>
        <FeatherTabPanel>
          <div class="canvas-wrapper">
            <canvas :id="definition"></canvas>
          </div>
        </FeatherTabPanel>
        <FeatherTabPanel>
          <div class="canvas-wrapper" v-if="graphData">
            <GraphDataTable :convertedGraphData="convertedGraphDataRef" :graphData="graphData" />
          </div>
        </FeatherTabPanel>
      </FeatherTabContainer>
    </div>
  </div>
</template>
  
<script setup lang=ts>
import GraphDataTable from './GraphDataTable.vue'
import { ConvertedGraphData, GraphMetricsPayload, GraphMetricsResponse, Metric, PreFabGraph, StartEndTime } from '@/types'
import { onMounted, ref, computed, PropType, watch } from 'vue'
import { useStore } from 'vuex'
import { RrdGraphConverter } from 'backshift'
import { ChartOptions, TitleOptions, ChartData } from 'chart.js'
import { formatTimestamps, getFormattedLegendStatements } from './utils'
import { Chart, registerables } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'
import { format } from 'd3'
import { FeatherButton } from '@featherds/button'
import {
  FeatherTab,
  FeatherTabContainer,
  FeatherTabPanel,
} from '@featherds/tabs'
Chart.register(...registerables)
Chart.register(zoomPlugin)

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
  },
  label: {
    required: true,
    type: String
  },
  isSingleGraph: {
    required: true,
    type: Boolean
  }
})

const store = useStore()
const graphData = ref<GraphMetricsResponse>({} as GraphMetricsResponse)
const series = ref<SeriesObject[]>([])
const convertedGraphDataRef = ref<any>({
  title: '',
  verticalLabel: '',
  metrics: []
})
let chart: any = {}
const yAxisFormatter = format('.3s')

const options = computed<ChartOptions>(() => ({
  responsive: true,
  maintainAspectRatio: false,
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
        mode: 'x',
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
      } as TitleOptions,
      ticks: {
        callback: function (value) {
          return yAxisFormatter(value as number)
        }
      }
    }
  }
}))

const getDatasetsForColumn = (index: number, columnValues: number[], datasetLabels: { name: string, statement: string }[]) => {
  const label = graphData.value.labels[index]
  const datasetLabelObj = datasetLabels.filter((datasetLabel) => datasetLabel.name === label)[0]
  const seriesObjs = []
  const datasets = []

  for (const item of series.value) {
    if (item.metric === label) {
      seriesObjs.push(item)
    }
  }

  let area: any = false
  for (const obj of seriesObjs) {
    if (obj.type === 'area') {
      area = obj.color
      break
    }
  }

  for (const obj of seriesObjs) {
    if (obj.name !== undefined) {
      const index = series.value.findIndex((series) => series.name === obj.name)
      datasets.push({
        hidden: Boolean(obj.type === 'hidden'),
        fill: area ? {
          target: 'origin',
          above: area
        } : false,
        label: datasetLabelObj.statement,
        data: columnValues,
        backgroundColor: obj.color,
        order: series.value.length - index
      })
    }
  }

  return datasets
}

const dataSets = computed(() => {
  let sets: any = []

  for (const [index, column] of graphData.value.columns.entries()) {
    const datasets = getDatasetsForColumn(index, column.values, graphData.value.formattedLabels)
    sets = [...sets, ...datasets]
  }

  return sets
})

const chartData = computed<ChartData<any>>(() => {
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

const render = async (update?: boolean) => {
  const definitionData: PreFabGraph = await store.dispatch('graphModule/getDefinitionData', props.definition)

  try {
    const convertedGraphData: ConvertedGraphData = RrdGraphConverter.getData({
      graphDef: definitionData,
      resourceId: props.resourceId
    })

    series.value = convertedGraphData.series
    convertedGraphDataRef.value = convertedGraphData

    const metrics: Metric[] = convertedGraphData.metrics.map((metric: Metric): Metric => ({
      aggregation: metric.aggregation,
      attribute: metric.attribute,
      label: metric.name,
      resourceId: metric.resourceId,
      transient: metric.transient
    }))

    const payload = getGraphMetricsPayload(metrics)
    const graphMetrics = await store.dispatch('graphModule/getGraphMetrics', payload)

    let formattedGraphData = formatTimestamps(graphMetrics, props.time.format)
    formattedGraphData = getFormattedLegendStatements(graphMetrics, convertedGraphData)
    graphData.value = formattedGraphData

    if (update) {
      chart.data = chartData.value
      chart.update()
    } else {
      const ctx: any = document.getElementById(props.definition)
      chart = new Chart(ctx, {
        type: 'line',
        data: chartData.value,
        options: options.value
      })
    }
  } catch (error) {
    console.log('Could not render graph for ', props.definition)
    emit('addGraphDefinition') // adds another to infinite scroll
  }
}

watch(props.time, () => render(true))
onMounted(() => render())
</script>
  
<style scoped lang="scss">
.container {
  position: relative;
}
.canvas-wrapper {
  display: block;
  height: 320px;
}
.graph-data-tabs {
  margin-top: 30px;
}
.single-graph-btn {
  position: absolute;
  top: 12px;
  right: 70px;
  z-index: 1;
}
</style>

<style lang="scss">
.graph-data-tabs {
  ul {
    margin-left: 37px !important;
  }
}
</style>
