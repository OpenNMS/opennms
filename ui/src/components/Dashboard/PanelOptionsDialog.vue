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

<!-- Per-panel options: height mode (all panels) + panel-type-specific settings. -->
<template>
  <OnmsDialog
    v-model:visible="visibleModel"
    modal
    :header="`Panel options — ${title}`"
    width="32rem"
  >
    <div class="opts">
      <fieldset class="opts__group">
        <legend class="opts__legend">Panel height</legend>
        <label class="opts__radio">
          <input
            v-model="heightMode"
            type="radio"
            value="fixed"
          >
          Fixed height (scrollbar)
        </label>
        <label class="opts__radio">
          <input
            v-model="heightMode"
            type="radio"
            value="auto"
          >
          Auto-fit to content
        </label>
      </fieldset>

      <label
        v-if="supportsShade"
        class="opts__radio"
      >
        <input
          v-model="shade"
          type="checkbox"
        >
        Shade rows by severity
      </label>

      <div
        v-if="panel.type === 'notes'"
        class="opts__field"
      >
        <label class="opts__label">Notes</label>
        <OnmsTextarea
          v-model="notesText"
          rows="6"
          auto-resize
          class="opts__control"
        />
      </div>

      <div
        v-else-if="panel.type === 'html-content'"
        class="opts__field"
      >
        <label class="opts__label">Content URL</label>
        <OnmsInputText
          v-model="htmlUrl"
          placeholder="/opennms/… or https://this-server/…"
          class="opts__control"
          :class="{ 'opts__control--error': !!urlError }"
        />
        <small
          v-if="urlError"
          class="opts__error"
        >{{ urlError }}</small>
        <small
          v-else
          class="opts__hint"
        >
          Loaded in an iframe. Only same-origin URLs work (the dashboard CSP is <code>frame-src 'self'</code>);
          note many external sites also refuse to be framed.
        </small>
      </div>

      <template v-if="panel.type === 'metric-chart'">
        <div class="opts__field">
          <label class="opts__label">Entity</label>
          <OnmsSelect
            v-model="chartEntity"
            :options="entityOptions"
            option-label="label"
            option-value="value"
            :loading="entitiesLoading"
            filter
            class="opts__control"
          />
          <small class="opts__hint">Entities that have data for the selected metric.</small>
        </div>
        <div class="opts__field">
          <label class="opts__label">Metric</label>
          <OnmsSelect
            :model-value="chartMetric"
            :options="kpiOptions"
            option-label="label"
            option-value="value"
            class="opts__control"
            @update:model-value="onMetricChanged"
          />
        </div>
      </template>

      <template v-if="panel.type === 'topn'">
        <div class="opts__field">
          <label class="opts__label">Rank by (KPI)</label>
          <OnmsSelect
            v-model="topnKpi"
            :options="kpiOptions"
            option-label="label"
            option-value="value"
            class="opts__control"
          />
        </div>
        <fieldset class="opts__group">
          <legend class="opts__legend">Order</legend>
          <label class="opts__radio">
            <input
              v-model="topnDirection"
              type="radio"
              value="desc"
            >
            Descending (highest first)
          </label>
          <label class="opts__radio">
            <input
              v-model="topnDirection"
              type="radio"
              value="asc"
            >
            Ascending (lowest first)
          </label>
        </fieldset>
        <div class="opts__field">
          <label class="opts__label">How many (N)</label>
          <input
            v-model.number="topnN"
            type="number"
            min="1"
            :max="MAX_TOPN_N"
            class="opts__control opts__number"
          >
        </div>
      </template>
    </div>

    <template #footer>
      <OnmsButton
        variant="text"
        label="Cancel"
        @click="visibleModel = false"
      />
      <OnmsButton
        label="Apply"
        :disabled="entitiesLoading"
        @click="apply"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsDialog, OnmsTextarea, OnmsInputText, OnmsButton, OnmsSelect } from '@opennms/onms-ui'
import type { DashboardPanel, PanelHeightMode } from '@/types/dashboard'
import { getPanelDefinition } from './registry'
import { useDashboardStore } from '@/stores/dashboardStore'
import { DEFAULT_TOPN_KPI, DEFAULT_TOPN_N, MAX_TOPN_N, TOPN_KPIS, clampTopnN, listAvailableKpis, type TopnKpiDef } from '@/services/topnService'
import { DEFAULT_CHART_METRIC, listMetricEntities } from '@/services/metricChartService'

const props = defineProps<{ panel: DashboardPanel; visible: boolean }>()
const emit = defineEmits<{ (e: 'update:visible', value: boolean): void }>()

const store = useDashboardStore()

type SelectOption = { label: string; value: string }

const toKpiOptions = (kpis: TopnKpiDef[]): SelectOption[] => kpis.map(k => ({ label: k.label, value: k.id }))

// only the metrics this system actually has data for, plus whatever is configured
const kpiOptions = ref<SelectOption[]>(toKpiOptions(TOPN_KPIS))

const loadKpis = async () => {
  let kpis = TOPN_KPIS
  try {
    kpis = await listAvailableKpis()
  } catch (err) {
    console.warn('Panel options: available metrics could not be listed', err)
  }
  const options = toKpiOptions(kpis)
  for (const configured of [topnKpi.value, chartMetric.value]) {
    if (configured && !options.some(o => o.value === configured)) {
      const known = TOPN_KPIS.find(k => k.id === configured)
      options.push({ label: known?.label ?? configured, value: configured })
    }
  }
  kpiOptions.value = options
}

const visibleModel = computed({
  get: () => props.visible,
  set: v => emit('update:visible', v)
})

const title = computed(
  () => props.panel.titleOverride || getPanelDefinition(props.panel.type)?.title || props.panel.type
)

const heightMode = ref<PanelHeightMode>('auto')
const shade = ref(false)
const notesText = ref('')
const htmlUrl = ref('')

// panels that support optional severity row shading (legacy-style)
const SHADEABLE = ['pending-situations', 'nodes-with-alarms', 'availability']
const supportsShade = computed(() => SHADEABLE.includes(props.panel.type))

const topnKpi = ref(DEFAULT_TOPN_KPI)
const topnN = ref(DEFAULT_TOPN_N)
const topnDirection = ref<'asc' | 'desc'>('desc')

// metric-chart: one entity x one metric (single-select each)
const chartEntity = ref('') // resource id
const chartEntityLabel = ref('')
const chartMetric = ref(DEFAULT_CHART_METRIC)
const entityOptions = ref<SelectOption[]>([])
const entitiesLoading = ref(false)

const NO_DATA_SUFFIX = ' (no data for this metric)'
let entitySeq = 0

// On open, a saved entity without data for the metric stays selectable but is
// marked, so the panel's "No data" has a visible cause; after the user changes
// the metric such an entity is dropped and the selection cleared instead.
const loadEntities = async (metricChanged = false) => {
  const seq = ++entitySeq
  entitiesLoading.value = true
  let entities: SelectOption[] = []
  try {
    entities = (await listMetricEntities(chartMetric.value)).map(e => ({ label: e.label, value: e.id }))
  } catch (err) {
    console.warn('Metric chart options: entities could not be listed', err)
  }
  if (seq !== entitySeq) {
    return
  }
  // panels saved before ids were stored hold the label; move them onto the id
  const byLabel = chartEntity.value && !entities.some(e => e.value === chartEntity.value)
    ? entities.find(e => e.label === chartEntity.value) : undefined
  if (byLabel) {
    chartEntityLabel.value = byLabel.label
    chartEntity.value = byLabel.value
  }
  if (chartEntity.value && !entities.some(e => e.value === chartEntity.value)) {
    if (metricChanged) {
      chartEntity.value = ''
      chartEntityLabel.value = ''
    } else {
      entities.unshift({ label: (chartEntityLabel.value || chartEntity.value) + NO_DATA_SUFFIX, value: chartEntity.value })
    }
  }
  entityOptions.value = entities
  entitiesLoading.value = false
}

// Only a user's change reloads in drop mode; syncFromPanel sets the metric
// without going through here, so opening the dialog never discards the entity.
const onMetricChanged = (metric: unknown) => {
  chartMetric.value = String(metric ?? DEFAULT_CHART_METRIC)
  if (props.panel.type === 'metric-chart') {
    loadEntities(true)
  }
}

watch(chartEntity, (id) => {
  const picked = entityOptions.value.find(e => e.value === id)
  if (picked) {
    chartEntityLabel.value = picked.label.endsWith(NO_DATA_SUFFIX) ? picked.label.slice(0, -NO_DATA_SUFFIX.length) : picked.label
  }
})


// External URLs are blocked by the dashboard CSP (frame-src 'self'); validate
// up front so the user gets an explanation instead of a silent broken iframe.
const urlError = computed(() => {
  const t = htmlUrl.value.trim()
  if (!t) {
    return ''
  }
  let parsed: URL
  try {
    parsed = new URL(t, window.location.origin)
  } catch {
    return 'That is not a valid URL.'
  }
  if (parsed.origin !== window.location.origin) {
    return `External URLs (${parsed.origin}) are blocked by the dashboard security policy (frame-src 'self'). Use a URL on this server, or ask an admin to allow the host in the server CSP.`
  }
  return ''
})

const syncFromPanel = () => {
  heightMode.value = store.resolvedHeightMode(props.panel)
  shade.value = !!props.panel.options?.shade
  notesText.value = String(props.panel.options?.text ?? '')
  htmlUrl.value = String(props.panel.options?.url ?? '')
  topnKpi.value = String(props.panel.options?.kpi ?? DEFAULT_TOPN_KPI)
  topnN.value = clampTopnN(props.panel.options?.n)
  topnDirection.value = props.panel.options?.direction === 'asc' ? 'asc' : 'desc'
  chartEntity.value = String(props.panel.options?.entity ?? '')
  chartEntityLabel.value = String(props.panel.options?.entityLabel ?? '')
  chartMetric.value = String(props.panel.options?.metric ?? DEFAULT_CHART_METRIC)
  if (props.panel.type === 'metric-chart' || props.panel.type === 'topn') {
    loadKpis()
  }
  if (props.panel.type === 'metric-chart') {
    loadEntities()
  }
}

watch(
  () => props.visible,
  (v) => {
    if (v) {
      syncFromPanel()
    }
  }
)

const apply = () => {
  if (props.panel.type === 'html-content' && urlError.value) {
    return // keep the dialog open; the error is shown inline
  }
  store.setPanelHeightMode(props.panel.id, heightMode.value)
  const opts: Record<string, unknown> = { ...props.panel.options }
  if (supportsShade.value) {
    opts.shade = shade.value
  }
  if (props.panel.type === 'notes') {
    opts.text = notesText.value
  }
  if (props.panel.type === 'html-content') {
    opts.url = htmlUrl.value.trim()
  }
  if (props.panel.type === 'topn') {
    opts.kpi = topnKpi.value
    opts.n = clampTopnN(topnN.value)
    opts.direction = topnDirection.value
  }
  if (props.panel.type === 'metric-chart') {
    opts.entity = chartEntity.value
    opts.entityLabel = chartEntityLabel.value
    opts.metric = chartMetric.value
  }
  store.setPanelOptions(props.panel.id, opts)
  visibleModel.value = false
}
</script>

<style scoped lang="scss">
.opts {
  display: flex;
  flex-direction: column;
  gap: 1rem;

  &__group {
    border: 1px solid var(--p-content-border-color, #ddd);
    border-radius: 4px;
    padding: 0.5rem 0.75rem;
  }

  &__legend {
    font-weight: 600;
    padding: 0 0.25rem;
  }

  &__radio {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.2rem 0;
  }

  &__field {
    display: flex;
    flex-direction: column;
    gap: 0.35rem;
  }

  &__label {
    font-weight: 600;
  }

  &__control {
    width: 100%;
  }

  &__hint {
    color: var(--p-text-muted-color, #666);
  }

  &__error {
    color: var(--p-red-500, #b00020);
  }

  &__control--error {
    outline: 1px solid var(--p-red-500, #b00020);
    border-radius: 4px;
  }

  &__number {
    width: 6rem;
    padding: 0.4rem 0.5rem;
    border: 1px solid var(--p-content-border-color, #ccc);
    border-radius: 4px;
  }
}
</style>
