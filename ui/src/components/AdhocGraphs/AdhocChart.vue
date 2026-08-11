<template>
  <div
    class="adhoc-chart"
    :class="{ 'is-expanded': expanded }"
  >
    <OnmsTabs
      value="graph"
      class="chart-tabs"
    >
      <OnmsTabList>
        <OnmsTab value="graph">Graph</OnmsTab>
        <OnmsTab value="data">Data</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel value="graph">
          <div
            ref="chartAreaRef"
            class="chart-area"
          >
            <div
              v-if="loading"
              class="chart-status"
              data-test="chart-loading"
            >
              <OnmsSpinner />
            </div>
            <p
              v-else-if="error"
              class="chart-status chart-error"
              role="alert"
              data-test="chart-error"
            >{{ error }}</p>
            <p
              v-else-if="!plotted.length"
              class="chart-status chart-empty"
              data-test="chart-empty"
            >
              Select one or more datasources to graph.
            </p>
            <p
              v-else-if="!measurements"
              class="chart-status chart-empty"
              data-test="chart-unrun"
            >
              Choose a time range or select Refresh to draw the graph.
            </p>
            <p
              v-else-if="noDataInRange"
              class="chart-status chart-empty"
              data-test="chart-no-data"
            >
              No data was collected for this selection over the chosen range.
            </p>

            <!-- The canvas stays mounted across states so Chart.js keeps its context;
                 the status messages above simply cover it. -->
            <div
              class="canvas-wrapper"
              :class="{ 'is-hidden': !showCanvas }"
            >
              <canvas
                :id="canvasId"
                data-test="chart-canvas"
              ></canvas>
            </div>
          </div>
          <div
            class="chart-legend"
            :id="`${canvasId}-lc`"
          ></div>
        </OnmsTabPanel>

        <OnmsTabPanel value="data">
          <GraphDataTable
            v-if="measurements && plotted.length"
            :id="canvasId"
            :graphData="tableData"
            :convertedGraphData="convertedGraphData"
          />
          <p
            v-else
            class="chart-status chart-empty"
          >Nothing to tabulate yet.</p>
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>
  </div>
</template>

<script setup lang="ts">
import {
  OnmsSpinner,
  OnmsTab,
  OnmsTabList,
  OnmsTabPanel,
  OnmsTabPanels,
  OnmsTabs
} from '@opennms/onms-ui'
import { Chart, ChartDataset, ChartOptions, registerables } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'
import { format as d3Format } from 'd3'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

import GraphDataTable from '@/components/Resources/GraphDataTable.vue'
import HtmlLegendPlugin from '@/components/Resources/plugins/HtmlLegendPlugin'
import { formatTimestamps } from '@/components/Resources/utils/LegendFormatter'
import { GraphMetricsResponse, StartEndTime } from '@/types'
import { AdhocExpression, AdhocGraphConfig, AdhocSeries } from '@/types/adhocGraph'
import { useAppStore } from '@/stores/appStore'
import { chartInk, seriesColor, strokeWidthFor } from './utils/adhocColors'
import { plottedSeries, toConvertedGraphData } from './utils/adhocQuery'

Chart.register(...registerables)
Chart.register(zoomPlugin)

const props = defineProps<{
  config: AdhocGraphConfig
  measurements: GraphMetricsResponse | null
  time: StartEndTime
  loading: boolean
  error: string
  /** Let the plot fill the available height instead of its default band. */
  expanded?: boolean
}>()

/** Stable id — this page hosts exactly one ad-hoc chart. */
const canvasId = 'adhoc-graph'

const appStore = useAppStore()
const chartAreaRef = ref<HTMLElement | null>(null)
let chart: Chart | null = null

const yAxisFormatter = d3Format('.3s')

const plotted = computed<(AdhocSeries | AdhocExpression)[]>(() => plottedSeries(props.config))

const convertedGraphData = computed(() => toConvertedGraphData(props.config))

/** Column values for a label, or an empty array when the server returned no such column. */
const columnFor = (label: string): number[] => {
  const index = props.measurements?.labels.indexOf(label) ?? -1
  return index >= 0 ? (props.measurements?.columns[index]?.values ?? []) : []
}

const noDataInRange = computed<boolean>(() => {
  if (!props.measurements || !plotted.value.length) {
    return false
  }

  // `relaxed: true` means a missing or empty source comes back as a column of NaN
  // rather than as an error, so "nothing collected" has to be detected here.
  return plotted.value.every(item =>
    columnFor(item.label).every(value => value === null || value === undefined || Number.isNaN(value)))
})

const showCanvas = computed<boolean>(() =>
  !props.loading && !props.error && Boolean(props.measurements) && plotted.value.length > 0 && !noDataInRange.value)

const EMPTY_MEASUREMENTS = {
  columns: [] as unknown as GraphMetricsResponse['columns'],
  labels: [],
  timestamps: [],
  formattedTimestamps: [],
  formattedLabels: []
} as unknown as GraphMetricsResponse

/** A timestamp-formatted copy; formatTimestamps mutates, so never hand it the prop. */
const tableData = computed<GraphMetricsResponse>(() => {
  if (!props.measurements) {
    return EMPTY_MEASUREMENTS
  }

  return formatTimestamps({ ...props.measurements, formattedTimestamps: [] }, props.time.format)
})

const isStacked = computed<boolean>(() =>
  props.config.stacked || props.config.series.some(entry => entry.style === 'stack'))

/**
 * Translucent companion to a series colour, for area/stack fills. Overlapping
 * opaque fills hide each other; 22% alpha keeps every band readable while the
 * 2px stroke still carries the line itself.
 */
const fillColor = (color: string): string => `${color}38`

/**
 * Every configured series in a stable order, whether plotted or not. Both the
 * colour slot and the dash pattern are looked up here rather than by position in
 * the plotted list, so hiding one series never repaints the survivors.
 */
const slotIndexOf = computed<Map<string, number>>(() => new Map(
  [...props.config.series, ...props.config.expressions].map((item, index) => [item.label, index])
))

const buildDatasets = (): ChartDataset<'line'>[] =>
  plotted.value.map((item, index) => {
    const slot = slotIndexOf.value.get(item.label) ?? index
    const color = item.color || seriesColor(slot, appStore.theme)
    const filled = item.style === 'area' || item.style === 'stack'

    return {
      label: item.label,
      data: columnFor(item.label),
      borderColor: color,
      backgroundColor: filled ? fillColor(color) : color,
      // Weight comes from the chosen style (LINE1/2/3) and nothing else.
      borderWidth: strokeWidthFor(item.style),
      fill: filled ? 'origin' : false,
      // No smoothing: a curve through sampled counters invents values that were
      // never collected.
      tension: 0,
      spanGaps: false,
      pointRadius: 0,
      pointHoverRadius: 5,
      hitRadius: 8,
      order: plotted.value.length - index
    }
  })

const buildOptions = (): ChartOptions<'line'> => {
  const ink = chartInk()

  return {
    responsive: true,
    maintainAspectRatio: false,
    animation: false,
    // Index mode with intersect off gives the whole-column readout users expect
    // when comparing series, without having to land exactly on a point.
    interaction: {
      mode: 'index',
      intersect: false
    },
    plugins: {
      htmlLegend: { containerID: `${canvasId}-lc` },
      legend: { display: false },
      title: {
        display: Boolean(props.config.title),
        text: props.config.title,
        color: ink.text
      },
      tooltip: {
        callbacks: {
          label: (context) => {
            const value = context.parsed.y
            return `${context.dataset.label}: ${Number.isFinite(value) ? yAxisFormatter(value) : 'N/A'}`
          }
        }
      },
      zoom: {
        zoom: {
          wheel: { enabled: true },
          pinch: { enabled: true },
          mode: 'x'
        },
        pan: { enabled: true, mode: 'x' }
      }
    },
    scales: {
      x: {
        ticks: { maxTicksLimit: 12, color: ink.muted },
        grid: { color: ink.grid }
      },
      y: {
        stacked: isStacked.value,
        title: {
          display: Boolean(props.config.verticalLabel),
          text: props.config.verticalLabel,
          color: ink.muted
        },
        ticks: {
          maxTicksLimit: 8,
          color: ink.muted,
          callback: value => yAxisFormatter(value as number)
        },
        grid: { color: ink.grid }
      }
    }
  } as ChartOptions<'line'>
}

const render = () => {
  const canvas = document.getElementById(canvasId) as HTMLCanvasElement | null

  if (!canvas || !showCanvas.value) {
    return
  }

  const data = {
    labels: tableData.value.formattedTimestamps,
    datasets: buildDatasets()
  }

  if (chart) {
    chart.data = data
    chart.options = buildOptions()
    chart.update()
    return
  }

  // Chart.js refuses a canvas another chart still owns ("Canvas is already in
  // use"); a stale instance can survive a navigation away and back.
  Chart.getChart(canvas)?.destroy()
  chart = new Chart(canvas, {
    type: 'line',
    data,
    options: buildOptions(),
    plugins: [HtmlLegendPlugin]
  })
}

const destroyChart = () => {
  chart?.destroy()
  chart = null
}

watch(
  () => [props.measurements, props.config, appStore.theme, showCanvas.value],
  () => {
    if (showCanvas.value) {
      render()
    } else {
      // Drop the chart when there is nothing to show so the legend and canvas do
      // not keep displaying the previous selection's series.
      destroyChart()
    }
  },
  { deep: true, flush: 'post' }
)

/** Reset zoom/pan when the range changes — a stale zoom window hides the new data. */
watch(() => [props.time.startTime, props.time.endTime], () => chart?.resetZoom())

const exportTarget = (): HTMLElement | null => chartAreaRef.value

defineExpose({ exportTarget })

onMounted(render)
onBeforeUnmount(destroyChart)
</script>

<style scoped lang="scss">
@import '@/styles/onms-typography';

.adhoc-chart {
  width: 100%;
}

.chart-area {
  position: relative;
  height: 420px;
}

/**
 * Expanded: fill the height the parent hands down instead of guessing at it.
 *
 * A viewport-relative height cannot work here — the chart sits inside a card that
 * already starts below the masthead, under breadcrumbs, a heading and a toolbar —
 * so any calc(100vh - x) either overflows or wastes space. Flex measures the real
 * leftover instead. Chart.js is responsive with maintainAspectRatio off, so the
 * canvas simply follows its container.
 *
 * The chain has to be unbroken from this root down to .chart-area, which is why
 * PrimeVue's own tab panels are reached with :deep(). `min-height: 0` on each link
 * is what lets a flex child actually shrink below its content size.
 */
.adhoc-chart.is-expanded {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;

  .chart-tabs {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
  }

  :deep(.p-tabpanels) {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
    overflow: hidden;
  }

  :deep(.p-tabpanel) {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
  }

  .chart-area {
    flex: 1;
    height: auto;
    // Below this the plot stops being readable; the legend gives up space first.
    min-height: 10rem;
  }

  // A graph with a dozen series has a tall legend. Cap it and let it scroll so it
  // can never push the plot off the bottom of the card.
  .chart-legend {
    flex: 0 0 auto;
    max-height: 7rem;
    overflow-y: auto;
  }
}

.canvas-wrapper {
  height: 100%;

  &.is-hidden {
    visibility: hidden;
  }
}

.chart-status {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 1rem;
  text-align: center;
}

.chart-empty {
  color: var(--p-text-muted-color);
}

.chart-error {
  color: var(--p-red-500);
}

.chart-legend {
  @include onms-body-small;
  margin-top: 0.5rem;
}
</style>
