///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///
<!--
  Dashboard panel drawing one bar chart from etc/chart-configuration.xml, the
  data the legacy Charts page rendered through JFreeChart. Which chart is the
  `chart` panel option; the registry seeds it for the three shipped charts.
  Styled like the Resource Graphs: soft grid, HTML legend under the plot.
-->
<template>
  <div
    ref="rootRef"
    class="configured-chart"
  >
    <div class="configured-chart__canvas">
      <canvas ref="canvasRef" />
    </div>
    <div
      :id="legendId"
      class="configured-chart__legend"
    />
    <p
      v-if="loading"
      class="configured-chart__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="!chartName"
      class="configured-chart__muted"
    >
      No chart selected. Pick one in the panel options.
    </p>
    <p
      v-else-if="failed"
      class="configured-chart__muted"
    >
      Unable to load the chart.
    </p>
    <p
      v-else-if="isEmpty"
      class="configured-chart__muted"
    >
      No data for this chart.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import Chart from 'chart.js/auto'
import type { Plugin } from 'chart.js'
import type { PanelComponentProps } from '@/types/dashboard'
import type { ChartData } from '@/types/charts'
import { getChart } from '@/services/chartService'
import HtmlLegendPlugin from '@/components/Resources/plugins/HtmlLegendPlugin'

const props = defineProps<PanelComponentProps>()

// series without a configured color take these, in order
const FALLBACK_COLORS = ['#3b82f6', '#f97316', '#22c55e', '#a855f7', '#ef4444', '#14b8a6']

const rootRef = ref<HTMLElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const loading = ref(true)
const failed = ref(false)
const data = ref<ChartData | null>(null)
let chart: Chart<'bar', (number | null)[], string> | null = null
let resizeObserver: ResizeObserver | null = null
let themeObserver: MutationObserver | null = null

const chartName = computed(() => String(props.options?.chart ?? '').trim())
const isEmpty = computed(() => !data.value || data.value.categories.length === 0)
const legendId = computed(() => `configured-chart-legend-${props.panelId}`)

// Chart.js defaults its text to a dark gray that vanishes on the dark theme
const textColor = (): string => {
  const el = rootRef.value
  if (el) {
    const c = getComputedStyle(el).getPropertyValue('--p-text-color').trim()
    if (c) {
      return c
    }
  }
  return '#333333'
}

// the configured colors are the legacy chart's saturated primaries; a translucent
// fill under a solid edge keeps their meaning (severity yellow/red) without the glare
const withAlpha = (hex: string, alpha: number): string => {
  const m = /^#?([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$/i.exec(hex)
  if (!m) {
    return hex
  }
  return `rgba(${parseInt(m[1], 16)}, ${parseInt(m[2], 16)}, ${parseInt(m[3], 16)}, ${alpha})`
}

// the value above each bar, as the legacy chart printed it
const valueLabels = (color: string): Plugin<'bar'> => ({
  id: 'configuredChartValues',
  afterDatasetsDraw(c) {
    const ctx = c.ctx
    ctx.save()
    ctx.font = '600 11px OpenSans, Helvetica, Arial, sans-serif'
    ctx.fillStyle = color
    ctx.textAlign = 'center'
    ctx.textBaseline = 'bottom'
    c.data.datasets.forEach((dataset, i) => {
      if (!c.isDatasetVisible(i)) {
        return
      }
      c.getDatasetMeta(i).data.forEach((bar, j) => {
        const value = dataset.data[j]
        if (value === null || value === undefined) {
          return
        }
        ctx.fillText(String(value), bar.x, bar.y - 3)
      })
    })
    ctx.restore()
  }
})

// the Resource Graphs legend, pointed at this panel's own container
const legendFor = (containerID: string): Plugin<'bar'> => ({
  id: HtmlLegendPlugin.id,
  afterUpdate: (c, args) => HtmlLegendPlugin.afterUpdate(c, args, { containerID })
})

const render = () => {
  chart?.destroy()
  chart = null
  const legend = document.getElementById(legendId.value)
  if (legend) {
    legend.innerHTML = ''
  }
  const canvas = canvasRef.value
  if (!canvas || !data.value || isEmpty.value) {
    return
  }
  const color = textColor()
  const axisTitle = (text?: string | null) => ({ display: !!text, text: text ?? '', color, font: { size: 12 }})
  chart = new Chart(canvas, {
    type: 'bar',
    data: {
      labels: data.value.categories.map(c => c.label),
      datasets: data.value.series.map((s, i) => {
        const base = s.color ?? FALLBACK_COLORS[i % FALLBACK_COLORS.length]
        return {
          label: s.name,
          data: s.values,
          backgroundColor: withAlpha(base, 0.7),
          borderColor: base,
          borderWidth: 1.5,
          borderRadius: 4,
          maxBarThickness: 48,
          categoryPercentage: 0.7,
          barPercentage: 0.85
        }
      })
    },
    plugins: [legendFor(legendId.value), valueLabels(color)],
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: false,
      layout: { padding: { top: 16 }},
      plugins: {
        legend: { display: false }
      },
      scales: {
        x: { title: axisTitle(data.value.domainAxisLabel), ticks: { color }, grid: { display: false, drawBorder: false }},
        y: {
          beginAtZero: true,
          title: axisTitle(data.value.rangeAxisLabel),
          ticks: { color, precision: 0, maxTicksLimit: 6 },
          grid: { color: 'rgba(128, 128, 128, 0.18)', borderDash: [4, 4], drawBorder: false }
        }
      }
    }
  })
}

const load = async () => {
  if (!chartName.value) {
    data.value = null
    loading.value = false
    render()
    return
  }
  loading.value = true
  const result = await getChart(chartName.value)
  failed.value = result === null
  data.value = result
  loading.value = false
  await nextTick()
  render()
}

onMounted(() => {
  load()
  if (rootRef.value) {
    resizeObserver = new ResizeObserver(() => chart?.resize())
    resizeObserver.observe(rootRef.value)
  }
  themeObserver = new MutationObserver(render)
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
})

watch(() => props.refreshTick, load)
watch(chartName, load)

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
@import '@/styles/onms-typography';

.configured-chart {
  height: 100%;
  min-height: 12rem;
  display: flex;
  flex-direction: column;

  &__canvas {
    flex: 1 1 auto;
    min-height: 0;
    position: relative;
  }

  &__legend {
    @include onms-body-small;
    text-align: center;
    margin-top: 0.25rem;
  }

  &__muted {
    margin: 0.25rem 0 0;
    font-size: 0.85rem;
    color: var(--p-text-muted-color, #6b7280);
    text-align: center;
  }
}
</style>
