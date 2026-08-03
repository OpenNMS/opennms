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
  Dashboard panel replicating the legacy Status Overview, on Chart.js: two
  doughnuts — nodes grouped by unacknowledged alarms, and nodes grouped by
  current outages — from /api/v2/status/summary/nodes/{alarms,outages}.
-->
<template>
  <div
    ref="rootRef"
    class="status-overview"
  >
    <div class="status-overview__donuts">
      <div class="status-overview__donut">
        <canvas ref="alarmsCanvas" />
      </div>
      <div class="status-overview__donut">
        <canvas ref="outagesCanvas" />
      </div>
    </div>
    <p
      v-if="loading"
      class="status-overview__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="alarmsTotal === 0 && outagesTotal === 0"
      class="status-overview__muted"
    >
      No alarms or outages.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import Chart from 'chart.js/auto'
import type { PanelComponentProps } from '@/types/dashboard'
import { getNodesByAlarms, getNodesByOutages, type StatusSummaryEntry } from '@/services/statusService'

const props = defineProps<PanelComponentProps>()

// Legacy status-box palette (title-case labels from the summary endpoints).
const STATUS_COLORS: Record<string, string> = {
  Normal: '#336600',
  Warning: '#ffcc00',
  Minor: '#ff9900',
  Major: '#ff3300',
  Critical: '#cc0000',
  Indeterminate: '#808080',
  Cleared: '#9e9e9e'
}
const colorFor = (label: string) => STATUS_COLORS[label] ?? '#999999'

const CENTER_FONT = 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif'

// Read a Feather theme color off the panel root (follows light/dark mode).
const themeColor = (varName: string, fallback: string): string => {
  const el = rootRef.value
  const c = el ? getComputedStyle(el).getPropertyValue(varName).trim() : ''
  return c || fallback
}

// Draws the total + caption at the doughnut's exact centre, sized proportionally
// to the hole (innerRadius). Centre-on-the-donut (not the canvas) keeps it aligned
// even though the legend sits at the bottom, and it scales as the panel resizes.
const drawCenterText = (caption: string) => ({
  id: `status-center-${caption}`,
  afterDatasetsDraw(chart: Chart) {
    const arc = chart.getDatasetMeta(0)?.data?.[0] as unknown as { x: number; y: number; innerRadius: number } | undefined
    if (!arc || !Number.isFinite(arc.innerRadius)) {
      return
    }
    const total = (chart.data.datasets[0].data as number[]).reduce((a, b) => a + (Number(b) || 0), 0)
    const numSize = Math.max(13, arc.innerRadius * 0.52)
    const capSize = Math.max(9, arc.innerRadius * 0.22)
    const ctx = chart.ctx
    ctx.save()
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillStyle = themeColor('--p-text-color', '#1f1f1f')
    ctx.font = `700 ${numSize}px ${CENTER_FONT}`
    ctx.fillText(String(total), arc.x, arc.y - capSize * 0.7)
    ctx.fillStyle = themeColor('--p-text-muted-color', '#666')
    ctx.font = `600 ${capSize}px ${CENTER_FONT}`
    ctx.fillText(caption, arc.x, arc.y + numSize * 0.45)
    ctx.restore()
  }
})

// Legend label text must follow the theme — Chart.js defaults to a dark gray that
// is unreadable on the dark surface. Read the Feather text color off the panel.
const legendColor = (): string => {
  const el = rootRef.value
  if (el) {
    const c = getComputedStyle(el).getPropertyValue('--p-text-color').trim()
    if (c) {
      return c
    }
  }
  return '#333333'
}

// Slice clicks deep-link to the legacy severity-filtered node list, matching
// the original homepage donuts (status-box onclick).
const statusUrl = (strategy: 'alarms' | 'outages', severity: string) =>
  `/opennms/status/index.jsp?title=Node%20List&type=nodes&strategy=${strategy}&severityFilter=${encodeURIComponent(severity)}`

const rootRef = ref<HTMLElement | null>(null)
const alarmsCanvas = ref<HTMLCanvasElement | null>(null)
const outagesCanvas = ref<HTMLCanvasElement | null>(null)
let resizeObserver: ResizeObserver | null = null
const alarmsEntries = ref<StatusSummaryEntry[]>([])
const outagesEntries = ref<StatusSummaryEntry[]>([])
const loading = ref(true)

let alarmsChart: Chart<'doughnut', number[], string> | null = null
let outagesChart: Chart<'doughnut', number[], string> | null = null

const sum = (entries: StatusSummaryEntry[]) => entries.reduce((a, e) => a + e.count, 0)
const alarmsTotal = computed(() => sum(alarmsEntries.value))
const outagesTotal = computed(() => sum(outagesEntries.value))

const renderDonut = (
  canvas: HTMLCanvasElement | null,
  entries: StatusSummaryEntry[],
  existing: Chart<'doughnut', number[], string> | null,
  strategy: 'alarms' | 'outages'
): Chart<'doughnut', number[], string> | null => {
  if (!canvas) {
    return existing
  }
  const filtered = entries.filter(e => e.count > 0)
  const labels = filtered.map(e => e.label)
  const data = filtered.map(e => e.count)
  const colors = filtered.map(e => colorFor(e.label))

  if (existing) {
    existing.data.labels = labels
    existing.data.datasets[0].data = data
    existing.data.datasets[0].backgroundColor = colors
    const legend = existing.options.plugins?.legend
    if (legend) {
      legend.labels = { ...(legend.labels ?? {}), color: legendColor() }
    }
    existing.update()
    return existing
  }

  return new Chart(canvas, {
    type: 'doughnut',
    data: { labels, datasets: [{ data, backgroundColor: colors, borderWidth: 1 }] },
    plugins: [drawCenterText(strategy === 'alarms' ? 'Alarms' : 'Outages')],
    options: {
      responsive: true,
      maintainAspectRatio: false,
      // ring thickness is (1 − cutout); 59.5% hole ⇒ 0.405R thick ≈ 90% of the
      // prior 0.45R (55% hole)
      cutout: '59.5%',
      plugins: { legend: { position: 'bottom', labels: { color: legendColor() }}},
      onClick: (_event, elements, chart) => {
        const idx = elements[0]?.index
        const severity = idx === undefined ? undefined : chart.data.labels?.[idx]
        if (severity) {
          window.location.assign(statusUrl(strategy, String(severity)))
        }
      },
      onHover: (_event, elements, chart) => {
        chart.canvas.style.cursor = elements.length ? 'pointer' : 'default'
      }
    }
  })
}

const load = async () => {
  loading.value = true
  const [alarms, outages] = await Promise.all([getNodesByAlarms(), getNodesByOutages()])
  alarmsEntries.value = alarms
  outagesEntries.value = outages
  loading.value = false
  await nextTick()
  alarmsChart = renderDonut(alarmsCanvas.value, alarms, alarmsChart, 'alarms')
  outagesChart = renderDonut(outagesCanvas.value, outages, outagesChart, 'outages')
}

let themeObserver: MutationObserver | null = null

const recolorLegends = () => {
  alarmsChart = renderDonut(alarmsCanvas.value, alarmsEntries.value, alarmsChart, 'alarms')
  outagesChart = renderDonut(outagesCanvas.value, outagesEntries.value, outagesChart, 'outages')
}

onMounted(() => {
  load()
  // resize charts when the panel (grid item) is resized
  if (rootRef.value) {
    resizeObserver = new ResizeObserver(() => {
      alarmsChart?.resize()
      outagesChart?.resize()
    })
    resizeObserver.observe(rootRef.value)
  }
  // recolor the legend when the .open-dark theme is toggled (no refresh needed).
  // The theme class is set on both <html> and <body>, so watching documentElement
  // alone catches every toggle without firing on unrelated <body> class churn.
  themeObserver = new MutationObserver(recolorLegends)
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
})
watch(() => props.refreshTick, load)
onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
  themeObserver?.disconnect()
  themeObserver = null
  alarmsChart?.destroy()
  outagesChart?.destroy()
  alarmsChart = null
  outagesChart = null
})
</script>

<style scoped lang="scss">
.status-overview {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  &__donuts {
    flex: 1 1 auto;
    min-height: 0;
    display: flex;
    gap: 0.5rem;
  }

  &__donut {
    position: relative;
    flex: 1 1 0;
    min-width: 0;
    min-height: 0;
  }

  &__muted {
    text-align: center;
    color: var(--p-text-muted-color, #666);
  }
}
</style>
