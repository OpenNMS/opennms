<template>
  <div class="onms-row">
    <div class="onms-col-12 forecast-container">
      <div v-if="loadError" class="fc-warn" data-test="forecast-load-error">{{ loadError }}</div>
      <div v-if="warning" class="fc-warn" data-test="forecast-warning">{{ warning }}</div>

      <div class="canvas-wrapper">
        <canvas ref="canvasRef" data-test="forecast-canvas"></canvas>
      </div>

      <div class="controls">
        <div class="field">
          <label for="fc-metric">Select the metric to forecast</label>
          <Select
            id="fc-metric"
            v-model="selectedMetric"
            :options="metricOptions"
            optionLabel="label"
            optionValue="value"
            data-test="forecast-metric"
            @change="reset"
          />
        </div>

        <div class="field">
          <label for="fc-template">Select a template</label>
          <Select
            id="fc-template"
            v-model="selectedTemplateId"
            :options="templateOptions"
            optionLabel="label"
            optionValue="value"
            data-test="forecast-template"
            @change="onTemplateChange"
          />
          <small class="hint">Choose one of the available forecasting templates, or configure your own options.</small>
        </div>

        <div v-if="selectedTemplateId === 'custom'" class="custom-options">
          <div class="field" v-for="opt in customFields" :key="opt.key">
            <label :for="`fc-${opt.key}`">{{ opt.label }}</label>
            <InputNumber
              :inputId="`fc-${opt.key}`"
              v-model="(options as any)[opt.key]"
              :minFractionDigits="0"
              :maxFractionDigits="opt.frac"
              :step="opt.step"
              :invalid="!!optionProblems[opt.key]"
              :useGrouping="false"
              :data-test="`fc-${opt.key}`"
            />
            <small v-if="optionProblems[opt.key]" class="field-error">{{ optionProblems[opt.key] }}</small>
          </div>
        </div>

        <div class="buttons">
          <OnmsButton variant="outlined" data-test="forecast-reset" @click="reset">Reset</OnmsButton>
          <OnmsButton :disabled="!canForecast || forecasting" data-test="forecast-run" @click="runForecast">
            {{ forecasting ? 'Forecasting…' : 'Forecast' }}
          </OnmsButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Chart, registerables } from 'chart.js'
import { format } from 'date-fns'

import { OnmsButton } from '@opennms/onms-ui'
import Select from 'primevue/select'
import InputNumber from 'primevue/inputnumber'

import API from '@/services'
import RrdGraphConverter from './utils/RrdGraphConverter.class'
import { computeForecast, ForecastOptions } from './utils/forecasting'
import { Metric, Series } from '@/types'

Chart.register(...registerables)

const props = defineProps<{
  label: string
  forecastDefinition: string
  forecastResourceId: string
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
let chart: Chart | null = null

const model = ref<{ title: string; verticalLabel: string; metrics: Metric[]; series: Series[] } | null>(null)
const series = ref<Series[]>([])
const selectedMetric = ref<string>('')
const loadError = ref<string | null>(null)
const warning = ref<string | null>(null)
const forecasting = ref(false)

const defaultOptions: ForecastOptions = {
  trainingStart: 14, graphStart: 7, season: 1, forecasts: 1,
  outlierThreshold: 0.975, confidenceLevel: 0.95, trendOrder: 3
}

const templates = [
  { id: '1day', name: '1 day forecast', options: { ...defaultOptions } },
  { id: '7day', name: '7 day forecast', options: { ...defaultOptions, trainingStart: 60, graphStart: 30, forecasts: 7 } },
  { id: '31day', name: '31 day forecast', options: { ...defaultOptions, trainingStart: 365, graphStart: 90, forecasts: 4, season: 7 } },
  { id: 'custom', name: 'Custom', options: { ...defaultOptions } }
]
const templateOptions = templates.map((t) => ({ label: t.name, value: t.id }))

const customFields = [
  { key: 'trainingStart', label: 'Training window (days)', step: 1, frac: 0 },
  { key: 'graphStart', label: 'Graph start (days back)', step: 1, frac: 0 },
  { key: 'season', label: 'Season length (days)', step: 0.5, frac: 2 },
  { key: 'forecasts', label: 'Periods to forecast', step: 1, frac: 0 },
  { key: 'outlierThreshold', label: 'Outlier threshold (0.5–1)', step: 0.005, frac: 3 },
  { key: 'confidenceLevel', label: 'Confidence level (0–1)', step: 0.01, frac: 2 },
  { key: 'trendOrder', label: 'Trend polynomial order', step: 1, frac: 0 }
]

const selectedTemplateId = ref('1day')
const options = ref<ForecastOptions>({ ...defaultOptions })

const metricOptions = computed(() => series.value.map((s) => ({ label: s.name, value: s.metric })))

const onTemplateChange = () => {
  const t = templates.find((x) => x.id === selectedTemplateId.value)
  if (t) options.value = { ...t.options }
}

// carry over (and complete) the legacy form validation the JSP enforced
const optionProblems = computed<Record<string, string>>(() => {
  const o = options.value
  const p: Record<string, string> = {}
  const intGe1 = (v: number) => Number.isInteger(v) && v >= 1
  if (!(o.trainingStart >= 1)) p.trainingStart = 'Must be at least 1 day.'
  if (!(o.graphStart >= 1)) p.graphStart = 'Must be at least 1 day.'
  if (!(o.season > 0)) p.season = 'Must be greater than 0.'
  else if (!(o.season * 2 < o.trainingStart)) p.season = 'Season × 2 must be less than the training window.'
  if (!intGe1(o.forecasts)) p.forecasts = 'Must be a whole number ≥ 1.'
  if (!(o.outlierThreshold > 0.5 && o.outlierThreshold < 1)) p.outlierThreshold = 'Must be between 0.5 and 1.'
  if (!(o.confidenceLevel > 0 && o.confidenceLevel < 1)) p.confidenceLevel = 'Must be between 0 and 1.'
  if (!intGe1(o.trendOrder)) p.trendOrder = 'Must be a whole number ≥ 1.'
  return p
})

const canForecast = computed(() => !!selectedMetric.value && Object.keys(optionProblems.value).length === 0)

// only DEF-backed metrics (with a real attribute + resource) can be fetched as a
// measurements source; CDEF/expression series are skipped as non-forecastable
const metricFor = (seriesMetricName: string): Metric | undefined =>
  model.value?.metrics.find((m) => m.name === seriesMetricName)
const isFetchable = (m?: Metric): boolean => !!m && !!m.attribute && !!m.resourceId && !m.expression

const fetchColumn = async (seriesMetricName: string, startMs: number, endMs: number) => {
  const metric = metricFor(seriesMetricName)
  if (!isFetchable(metric)) return null
  const step = Math.max(1, Math.floor((endMs - startMs) / 1000))
  const resp = await API.getGraphMetrics({
    start: startMs, end: endMs, step,
    source: [{
      aggregation: metric!.aggregation || 'AVERAGE',
      attribute: metric!.attribute,
      label: 'data',
      resourceId: metric!.resourceId,
      transient: false
    }]
  } as any)
  if (!resp) return null
  const timestamps = (resp.timestamps ?? []) as number[]
  const values = (resp.columns?.[0]?.values ?? []).map((v: unknown) => (typeof v === 'number' ? v : NaN))
  // the API's step and returned timestamp spacing are already in milliseconds
  const stepMs = timestamps.length > 1 ? timestamps[1] - timestamps[0] : step
  return { timestamps, values, stepMs }
}

const line = (label: string, color: string, points: { x: number; y: number }[], dash = false, fill = false) => ({
  label, data: points, borderColor: color, backgroundColor: color,
  borderDash: dash ? [4, 4] : [], fill: fill ? '-1' : false, radius: 0, hitRadius: 4, borderWidth: 1.5, tension: 0
})

const drawChart = (datasets: any[]) => {
  if (!canvasRef.value) return
  if (chart) chart.destroy()
  chart = new Chart(canvasRef.value, {
    type: 'line',
    data: { datasets },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      parsing: false as any,
      plugins: {
        title: { display: true, text: model.value?.title ?? '' },
        legend: { display: true, position: 'bottom' }
      },
      scales: {
        x: { type: 'linear', ticks: { maxTicksLimit: 10, callback: (v: any) => format(new Date(v), 'MMM d HH:mm') } },
        y: { title: { display: true, text: model.value?.verticalLabel ?? '' } }
      }
    }
  })
}

const toPoints = (ts: number[], vals: number[]) =>
  ts.map((x, i) => ({ x, y: vals[i] })).filter((p) => Number.isFinite(p.y))

const seriesName = () => series.value.find((s) => s.metric === selectedMetric.value)?.name ?? 'Actual'

const reset = async () => {
  warning.value = null
  const end = Date.now()
  const start = end - Math.max(1, options.value.graphStart) * 86400 * 1000
  const data = await fetchColumn(selectedMetric.value, start, end)
  if (!data) {
    warning.value = 'Could not load data for the selected metric.'
    drawChart([])
    return
  }
  drawChart([line(seriesName(), '#7EE600', toPoints(data.timestamps, data.values))])
}

const runForecast = async () => {
  warning.value = null
  forecasting.value = true
  try {
    const end = Date.now()
    const trainStart = end - options.value.trainingStart * 86400 * 1000
    const data = await fetchColumn(selectedMetric.value, trainStart, end)
    if (!data) { warning.value = 'Could not load data for the selected metric.'; drawChart([]); return }

    const result = computeForecast(data.timestamps, data.values, data.stepMs, options.value)
    warning.value = result.warning

    const datasets: any[] = [line(seriesName(), '#7EE600', toPoints(data.timestamps, data.values))]
    if (result.timestamps.length) {
      datasets.push(line('HW Bounds (low)', '#ff0000', toPoints(result.timestamps, result.lower), true))
      datasets.push({ ...line('HW Bounds (high)', 'rgba(255,0,0,0.12)', toPoints(result.timestamps, result.upper), true, true), borderColor: '#ff0000' })
      datasets.push(line('HW Fit', '#9d4edd', toPoints(result.timestamps, result.fit)))
      datasets.push(line('Trend', '#00b4d8', toPoints(result.timestamps, result.trend)))
    }
    drawChart(datasets)
  } finally {
    forecasting.value = false
  }
}

onMounted(async () => {
  try {
    const definitionData = await API.getDefinitionData(props.forecastDefinition)
    const converter = new RrdGraphConverter({ graphDef: definitionData, resourceId: props.forecastResourceId })
    model.value = converter.model as any
    series.value = (converter.model.series || []).filter((s: Series) => s.name && s.metric && isFetchable(metricFor(s.metric)))
    if (!series.value.length) {
      loadError.value = 'This graph has no forecastable metrics (it may be built entirely from computed expressions).'
      return
    }
    selectedMetric.value = series.value[0].metric as string
    await reset()
  } catch (e) {
    loadError.value = 'Failed to load the graph definition for forecasting.'
  }
})

onBeforeUnmount(() => {
  chart?.destroy()
  chart = null
})
</script>

<style scoped lang="scss">
.forecast-container {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.canvas-wrapper {
  position: relative;
  height: 420px;
}
.controls {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 560px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.custom-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}
.hint {
  color: var(--onms-body-text-color-muted, #6c757d);
  font-size: 0.8rem;
}
.field-error {
  color: var(--onms-error-color, #e24c4c);
  font-size: 0.8rem;
}
.buttons {
  display: flex;
  gap: 0.75rem;
}
.fc-warn {
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border: 1px solid var(--onms-warning-color, #fde68a);
  background: var(--onms-warning-background-color, #fffbeb);
  color: var(--onms-warning-text-color, #854d0e);
  font-size: 0.9rem;
}
</style>
