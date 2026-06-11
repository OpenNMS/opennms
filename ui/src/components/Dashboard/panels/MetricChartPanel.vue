<!--
Licensed to The OpenNMS Group, Inc (TOG) under one or more
contributor license agreements.  See the LICENSE.md file
distributed with this work for additional information
regarding copyright ownership.

TOG licenses this file to You under the GNU Affero General
Public License Version 3 (the "License") or (at your option)
any later version.  You may not use this file except in
compliance with the License.  You may obtain a copy of the
License at:

     https://www.gnu.org/licenses/agpl-3.0.txt

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.  See the License for the specific
language governing permissions and limitations under the
License.
-->

<!--
  Metric-chart panel: a line chart of one metric on one entity over the
  (resolved) timeframe. Entity and metric are picked via the panel options
  (gear), one of each. Defaults: localhost / Node Response Time (ICMP).
-->
<template>
  <div
    ref="rootRef"
    class="metric-chart"
  >
    <div class="metric-chart__plot">
      <canvas ref="canvasRef" />
    </div>
    <p
      v-if="loading"
      class="metric-chart__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="empty"
      class="metric-chart__muted"
    >
      No data for {{ entity }} — {{ metricLabel }} in this timeframe.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import Chart from 'chart.js/auto'
import { format } from 'date-fns'
import type { PanelComponentProps } from '@/types/dashboard'
import { TOPN_KPIS } from '@/services/topnService'
import {
  DEFAULT_CHART_ENTITY,
  DEFAULT_CHART_METRIC,
  queryMetricSeries,
  type MetricSeries
} from '@/services/metricChartService'

const props = defineProps<PanelComponentProps>()

const LINE_COLOR = '#3a87ad' // legacy OpenNMS chart blue
const FILL_COLOR = 'rgba(58, 135, 173, 0.15)'

const rootRef = ref<HTMLElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const loading = ref(true)
const empty = ref(false)

let chart: Chart<'line', (number | null)[], string> | null = null
let resizeObserver: ResizeObserver | null = null
let themeObserver: MutationObserver | null = null
let series: MetricSeries | null = null

const metricId = computed(() => String(props.options?.metric ?? DEFAULT_CHART_METRIC))
const entity = computed(() => String(props.options?.entity ?? DEFAULT_CHART_ENTITY))
const metricLabel = computed(() => TOPN_KPIS.find((k) => k.id === metricId.value)?.label ?? metricId.value)

// Axis/grid colors must follow the theme (same approach as Status Overview).
const textColor = (): string => {
  const el = rootRef.value
  if (el) {
    const c = getComputedStyle(el).getPropertyValue('--feather-primary-text-on-surface').trim()
    if (c) return c
  }
  return '#333333'
}
const gridColor = () => 'rgba(128, 128, 128, 0.2)'

// No chart.js date adapter in this app — use a category axis with labels
// formatted by range (time-only within ~2 days, date+time beyond).
const timeLabels = (s: MetricSeries): string[] => {
  const span = (s.timestamps[s.timestamps.length - 1] ?? 0) - (s.timestamps[0] ?? 0)
  const fmt = span <= 48 * 3_600_000 ? 'HH:mm' : 'MMM d HH:mm'
  return s.timestamps.map((t) => format(new Date(t), fmt))
}

const render = () => {
  const canvas = canvasRef.value
  if (!canvas || !series) return
  const labels = timeLabels(series)
  const data = series.values
  const datasetLabel = `${entity.value} — ${metricLabel.value} (${series.unit})`
  const color = textColor()

  if (chart) {
    chart.data.labels = labels
    chart.data.datasets[0].data = data
    chart.data.datasets[0].label = datasetLabel
    const o = chart.options
    if (o.plugins?.legend?.labels) o.plugins.legend.labels.color = color
    if (o.scales?.x?.ticks) o.scales.x.ticks.color = color
    if (o.scales?.y?.ticks) o.scales.y.ticks.color = color
    if (o.scales?.y && 'title' in o.scales.y && o.scales.y.title) o.scales.y.title.color = color
    chart.update()
    return
  }

  chart = new Chart(canvas, {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: datasetLabel,
          data,
          borderColor: LINE_COLOR,
          backgroundColor: FILL_COLOR,
          borderWidth: 2,
          pointRadius: 0,
          pointHitRadius: 8,
          tension: 0.2,
          fill: true,
          spanGaps: true
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: { legend: { position: 'bottom', labels: { color, boxWidth: 12 } } },
      scales: {
        x: {
          ticks: { color, maxTicksLimit: 8, maxRotation: 0, autoSkip: true },
          grid: { color: gridColor() }
        },
        y: {
          beginAtZero: true,
          title: { display: true, text: series.unit, color },
          ticks: { color },
          grid: { color: gridColor() }
        }
      }
    }
  })
}

const load = async () => {
  loading.value = true
  series = await queryMetricSeries(metricId.value, entity.value, props.timeframe)
  empty.value = !series
  loading.value = false
  await nextTick()
  if (series) {
    render()
  } else if (chart) {
    chart.destroy()
    chart = null
  }
}

onMounted(() => {
  load()
  if (rootRef.value) {
    resizeObserver = new ResizeObserver(() => chart?.resize())
    resizeObserver.observe(rootRef.value)
  }
  // recolor axes/legend when the .open-dark theme is toggled
  themeObserver = new MutationObserver(render)
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
  themeObserver.observe(document.body, { attributes: true, attributeFilter: ['class'] })
})

watch(
  [() => props.refreshTick, metricId, entity, () => props.timeframe.preset, () => props.timeframe.from, () => props.timeframe.to],
  load
)

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
  themeObserver?.disconnect()
  themeObserver = null
  chart?.destroy()
  chart = null
})
</script>

<style scoped lang="scss">
.metric-chart {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  &__plot {
    flex: 1 1 auto;
    min-height: 0;
    position: relative;
  }

  &__muted {
    text-align: center;
    color: var(--feather-secondary-text-on-surface, #666);
  }
}
</style>
