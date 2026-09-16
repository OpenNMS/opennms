<template>
  <div class="onms-row">
    <div class="onms-col-12 forecast-container">
      <div v-if="loadError" class="fc-warn" data-test="forecast-load-error">{{ loadError }}</div>
      <div v-if="warning" class="fc-warn" data-test="forecast-warning">{{ warning }}</div>

      <div class="canvas-wrapper">
        <canvas ref="canvasRef" data-test="forecast-canvas"></canvas>
      </div>

      <div class="controls">
        <FormField
          label="Select the metric to forecast"
          for="fc-metric"
        >
          <OnmsSelect
            inputId="fc-metric"
            v-model="selectedMetric"
            :options="metricOptions"
            optionLabel="label"
            optionValue="value"
            data-test="forecast-metric"
            @update:modelValue="reset"
          />
        </FormField>

        <FormField
          label="Select a template"
          for="fc-template"
          hint="Choose one of the available forecasting templates, or configure your own options."
        >
          <OnmsSelect
            inputId="fc-template"
            v-model="selectedTemplateId"
            :options="templateOptions"
            optionLabel="label"
            optionValue="value"
            data-test="forecast-template"
            @update:modelValue="onTemplateChange"
          />
        </FormField>

        <div v-if="selectedTemplateId === 'custom'" class="custom-options">
          <FormField
            v-for="opt in customFields"
            :key="opt.key"
            :label="opt.label"
            :for="`fc-${opt.key}`"
            :error="optionProblems[opt.key] || undefined"
          >
            <OnmsInputNumber
              :inputId="`fc-${opt.key}`"
              v-model="(options as any)[opt.key]"
              :maxFractionDigits="opt.frac"
              :step="opt.step"
              :invalid="!!optionProblems[opt.key]"
              :useGrouping="false"
              :data-test="`fc-${opt.key}`"
            />
          </FormField>
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

import { OnmsButton, OnmsInputNumber, OnmsSelect } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import API from '@/services'
import RrdGraphConverter from './utils/RrdGraphConverter.class'
import { Series } from '@/types'
import { ForecastLineDataset, ForecastModel, ForecastOptions, ForecastTemplate } from '@/components/Resources/types'
import { forecastOptionProblems } from '@/components/Resources/utils/validation'
import {
  DAY_MS,
  FORECAST_COLORS,
  buildForecastDatasets,
  buildModelPayload,
  fetchColumn,
  forecastFilters,
  forecastWarningFor,
  line,
  metricFor,
  toPoints
} from '@/components/Resources/utils/forecastUtils'

Chart.register(...registerables)

const props = defineProps<{
  label: string
  forecastDefinition: string
  forecastResourceId: string
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
let chart: Chart | null = null

const model = ref<ForecastModel | null>(null)
const series = ref<Series[]>([])
const selectedMetric = ref<string>('')
const loadError = ref<string | null>(null)
const warning = ref<string | null>(null)
const forecasting = ref(false)

const defaultOptions: ForecastOptions = {
  trainingStart: 14,
  graphStart: 7,
  season: 1,
  forecasts: 1,
  outlierThreshold: 0.975,
  confidenceLevel: 0.95,
  trendOrder: 3
}

const templates: ForecastTemplate[] = [
  { id: '1day', name: '1 day forecast', options: { ...defaultOptions }},
  { id: '7day', name: '7 day forecast', options: { ...defaultOptions, trainingStart: 60, graphStart: 30, forecasts: 7 }},
  { id: '31day', name: '31 day forecast', options: { ...defaultOptions, trainingStart: 365, graphStart: 90, forecasts: 4, season: 7 }},
  { id: 'custom', name: 'Custom', options: { ...defaultOptions }}
]
const templateOptions = templates.map(t => ({ label: t.name, value: t.id }))

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

const metricOptions = computed(() => {
  return series.value.map(s => ({ label: s.name, value: s.metric }))
})

const onTemplateChange = () => {
  const t = templates.find(x => x.id === selectedTemplateId.value)
  if (t) {
    options.value = { ...t.options }
  }
}

const optionProblems = computed<Record<string, string>>(() => {
  return forecastOptionProblems(options.value)
})

const canForecast = computed(() => {
  return !!selectedMetric.value && Object.keys(optionProblems.value).length === 0
})

const drawChart = (datasets: ForecastLineDataset[]) => {
  if (!canvasRef.value) {
    return
  }
  if (chart) {
    chart.destroy()
  }
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
        x: { type: 'linear', ticks: { maxTicksLimit: 10, callback: (v: any) => format(new Date(v), 'MMM d HH:mm') }},
        y: { title: { display: true, text: model.value?.verticalLabel ?? '' }}
      }
    }
  })
}

const seriesName = () => {
  return series.value.find(s => s.metric === selectedMetric.value)?.name ?? 'Actual'
}

const reset = async () => {
  warning.value = null
  const end = Date.now()
  const start = end - Math.max(1, options.value.graphStart) * DAY_MS
  const data = await fetchColumn(model.value, selectedMetric.value, start, end)
  if (!data) {
    warning.value = 'Could not load data for the selected metric.'
    drawChart([])
    return
  }
  drawChart([line(seriesName(), FORECAST_COLORS.data, toPoints(data.timestamps, data.values))])
}

const runForecast = async () => {
  warning.value = null
  forecasting.value = true
  try {
    if (!metricFor(model.value, selectedMetric.value)) {
      warning.value = 'Could not load data for the selected metric.'
      drawChart([])
      return
    }
    const end = Date.now()
    const start = end - options.value.trainingStart * DAY_MS
    const resp = await API.getGraphMetrics(
      buildModelPayload(model.value, selectedMetric.value, start, end, forecastFilters(selectedMetric.value, options.value))
    )
    if (!resp || !(resp.timestamps ?? []).length) {
      warning.value = 'Could not load data for the selected metric.'
      drawChart([])
      return
    }
    drawChart(buildForecastDatasets(resp, seriesName(), selectedMetric.value))
    // the data line still renders; explain why the forecast overlay is missing
    // or degenerate instead of leaving empty legend entries unexplained
    warning.value = forecastWarningFor(resp, selectedMetric.value)
  } finally {
    forecasting.value = false
  }
}

onMounted(async () => {
  try {
    const definitionData = await API.getDefinitionData(props.forecastDefinition)
    const converter = new RrdGraphConverter({ graphDef: definitionData, resourceId: props.forecastResourceId })
    const converted = converter.model as ForecastModel
    model.value = converted
    series.value = (converted.series || []).filter((s: Series) => s.name && s.metric && !!metricFor(converted, s.metric))
    if (!series.value.length) {
      loadError.value = 'This graph has no forecastable series.'
      return
    }
    selectedMetric.value = series.value[0].metric
    await reset()
  } catch {
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
.custom-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
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
