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
            <canvas :id="`${label}-${definition}`"></canvas>
            <div ref="legendRef" class="lc" :id="`${label}-${definition}-lc`"></div>
          </div>
        </FeatherTabPanel>
        <FeatherTabPanel>
          <div class="canvas-wrapper" v-if="graphData">
            <GraphDataTable
              :id="`${label}-${definition}`"
              :convertedGraphData="convertedGraphDataRef"
              :graphData="graphData"
            />
          </div>
        </FeatherTabPanel>
      </FeatherTabContainer>
    </div>
  </div>
</template>
  
<script setup lang="ts">
import RrdGraphConverter from './utils/RrdGraphConverter.class'
import { formatTimestamps, getFormattedLegendStatements } from './utils/LegendFormatter'
import GraphDataTable from './GraphDataTable.vue'
import { ConvertedGraphData, GraphMetricsPayload, GraphMetricsResponse, Metric, PreFabGraph, StartEndTime } from '@/types'
import { useGraphStore } from '@/stores/graphStore'
import { useElementSize } from '@vueuse/core'
import { ChartOptions, TitleOptions, ChartData } from 'chart.js'
import { Chart, registerables } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'
import HtmlLegendPlugin from './plugins/HtmlLegendPlugin'
import { format } from 'd3'
import { FeatherButton } from '@featherds/button'
import {
  FeatherTab,
  FeatherTabContainer,
  FeatherTabPanel
} from '@featherds/tabs'
import { PropType } from 'vue'
Chart.register(...registerables)
Chart.register(zoomPlugin)

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

const graphStore = useGraphStore()
const graphData = ref<GraphMetricsResponse | null>(null)
const convertedGraphDataRef = ref<ConvertedGraphData>({
  title: '',
  verticalLabel: '',
  series: [],
  values: [],
  metrics: [],
  printStatements: [],
  properties: {}
})
let chart: any = {}
const legendRef = ref()
const { height } = useElementSize(legendRef)
const yAxisFormatter = format('.3s')

const legendHeight = computed(() => height.value + 'px')

const options = computed<ChartOptions>(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    htmlLegend: {
      // ID of the container to put the legend in
      containerID: `${props.label}-${props.definition}-lc`
    },
    legend: {
      display: false
    },
    title: {
      display: true,
      text: convertedGraphDataRef.value.title
    } as TitleOptions,
    zoom: {
      zoom: {
        wheel: {
          enabled: true
        },
        mode: 'x'
      },
      pan: {
        enabled: true,
        mode: 'x'
      }
    }
  },
  scales: {
    y: {
      title: {
        display: true,
        text: convertedGraphDataRef.value.verticalLabel
      } as TitleOptions,
      ticks: {
        callback: (value) => yAxisFormatter(value as number),
        maxTicksLimit: 8
      },
      stacked: false
    },
    x: {
      ticks: {
        maxTicksLimit: 12
      }
    }
  }
}))

const getDatasetsForColumn = (index: number, columnValues: number[], datasetLabels: { name: string, statement: string }[]) => {
  const label = graphData.value?.labels[index] || ''
  const datasetLabelObj = datasetLabels.filter((datasetLabel) => datasetLabel.name === label)[0]
  const seriesObjs = []
  const datasets = []

  for (const item of convertedGraphDataRef.value.series) {
    if (item.metric === label) {
      seriesObjs.push(item)
    }
  }

  let areaOrStack = false
  let areaOrStackColor = ''
  for (const obj of seriesObjs) {
    if (obj.type === 'area' || obj.type === 'stack') {
      areaOrStack = true
      areaOrStackColor = obj.color

      if (obj.type === 'stack') {
        (options.value.scales as any).y.stacked = true
      }

      break
    }
  }

  for (const obj of seriesObjs) {
    if (obj.name !== undefined) {
      const index = convertedGraphDataRef.value.series.findIndex((series) => series.name === obj.name)
      datasets.push({
        hidden: Boolean(obj.type === 'hidden'),
        fill: areaOrStack ? {
          target: 'origin',
          above: areaOrStackColor
        } : false,
        label: datasetLabelObj.statement,
        data: columnValues,
        borderColor: obj.color,
        backgroundColor: obj.color,
        radius: 0,
        hitRadius: 5,
        hoverRadius: 6,
        order: convertedGraphDataRef.value.series.length - index
      })
    }
  }

  return datasets
}

const dataSets = computed(() => {
  let sets: any = []

  for (const [index, column] of graphData.value?.columns.entries() || []) {
    const datasets = getDatasetsForColumn(index, column.values, graphData.value?.formattedLabels || [])
    sets = [...sets, ...datasets]
  }

  return sets
})

const chartData = computed<ChartData<any>>(() => {
  return {
    labels: graphData.value?.formattedTimestamps,
    datasets: dataSets.value
  }
})

const getGraphMetricsPayload = (source: Metric[]): GraphMetricsPayload => {
  const start = props.time.startTime as number * 1000
  const end = props.time.endTime as number * 1000
  const step = Math.floor((end - start) / 1000)
  const expression = []

  const metricsWithExpressions = source.filter((metric) => Boolean(metric.expression))
  const metricsWithoutExpressions = source.filter((metric) => Boolean(!metric.expression))

  for (const metric of metricsWithExpressions) {
    expression.push({
      value: metric.expression as string,
      label: metric.label as string,
      transient: metric.transient as boolean
    })
  }

  const payload: GraphMetricsPayload = {
    start,
    end,
    step,
    source: metricsWithoutExpressions
  }

  if (metricsWithExpressions.length) {
    payload.expression = expression
  }

  return payload
}

const render = async (update?: boolean) => {
  const definitionData: PreFabGraph | null = await graphStore.getDefinitionData(props.definition)

  try {
    const rrdGraphConverter = new RrdGraphConverter({
      graphDef: definitionData,
      resourceId: props.resourceId
    })

    const rrdGraphConverterModel = rrdGraphConverter.model
    convertedGraphDataRef.value = rrdGraphConverterModel

    const metrics: Metric[] = rrdGraphConverterModel.metrics.map((metric: Metric): Metric => ({
      aggregation: metric.aggregation,
      attribute: metric.attribute,
      label: metric.name,
      resourceId: metric.resourceId,
      transient: metric.transient,
      expression: metric.expression
    }))

    const payload = getGraphMetricsPayload(metrics)
    const graphMetrics = await graphStore.getGraphMetrics(payload)

    if (graphMetrics === null) {
      graphData.value = null
      return
    }

    let formattedGraphData = formatTimestamps(graphMetrics, props.time.format)
    formattedGraphData = getFormattedLegendStatements(graphMetrics, rrdGraphConverterModel)
    graphData.value = formattedGraphData

    if (update) {
      chart.data = chartData.value
      chart.update()
    } else {
      const ctx: any = document.getElementById(`${props.label}-${props.definition}`)
      chart = new Chart(ctx, {
        type: 'line',
        data: chartData.value,
        options: options.value,
        plugins: [HtmlLegendPlugin]
      })
    }
  } catch (error) {
    console.log(error)
    console.log('Could not render graph for ', props.definition)
    emit('addGraphDefinition') // adds another to infinite scroll
  }
}

watch(props.time, () => render(true))

onMounted(() => render())
</script>
  
<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
.container {
  position: relative;
}
.canvas-wrapper {
  display: block;
  height: 370px;
}
.graph-data-tabs {
  margin-top: 50px;
  margin-bottom: v-bind(legendHeight);
}
.single-graph-btn {
  position: absolute;
  top: 12px;
  right: 70px;
  z-index: 1;
}
.lc {
  @include body-small;
}
</style>

<style lang="scss">
.graph-data-tabs {
  ul {
    margin-left: 37px !important;
  }
}
</style>
