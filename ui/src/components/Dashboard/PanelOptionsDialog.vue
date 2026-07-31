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
  <PDialog
    v-model:visible="visibleModel"
    modal
    :header="`Panel options — ${title}`"
    :style="{ width: '32rem' }"
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
        <PTextarea
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
        <PInputText
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
          <PSelect
            v-model="chartEntity"
            :options="entityOptions"
            :loading="entitiesLoading"
            filter
            class="opts__control"
          />
          <small class="opts__hint">Entities that have data for the selected metric.</small>
        </div>
        <div class="opts__field">
          <label class="opts__label">Metric</label>
          <PSelect
            v-model="chartMetric"
            :options="kpiOptions"
            option-label="label"
            option-value="value"
            class="opts__control"
          />
        </div>
      </template>

      <template v-if="panel.type === 'topn'">
        <div class="opts__field">
          <label class="opts__label">Rank by (KPI)</label>
          <PSelect
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
            max="50"
            class="opts__control opts__number"
          >
        </div>
      </template>
    </div>

    <template #footer>
      <PButton
        text
        label="Cancel"
        @click="visibleModel = false"
      />
      <PButton
        label="Apply"
        @click="apply"
      />
    </template>
  </PDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import Dialog from 'primevue/dialog'
import Textarea from 'primevue/textarea'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Button from 'primevue/button'
import type { DashboardPanel, PanelHeightMode } from '@/types/dashboard'
import { getPanelDefinition } from './registry'
import { useDashboardStore } from '@/stores/dashboardStore'
import { DEFAULT_TOPN_KPI, DEFAULT_TOPN_N, TOPN_KPIS } from '@/services/topnService'
import { DEFAULT_CHART_ENTITY, DEFAULT_CHART_METRIC, listMetricEntities } from '@/services/metricChartService'

const PDialog = Dialog
const PTextarea = Textarea
const PInputText = InputText
const PSelect = Select
const PButton = Button

const kpiOptions = TOPN_KPIS.map((k) => ({ label: k.label, value: k.id }))

const props = defineProps<{ panel: DashboardPanel; visible: boolean }>()
const emit = defineEmits<{ (e: 'update:visible', value: boolean): void }>()

const store = useDashboardStore()

const visibleModel = computed({
  get: () => props.visible,
  set: (v) => emit('update:visible', v)
})

const title = computed(
  () => props.panel.titleOverride || getPanelDefinition(props.panel.type)?.title || props.panel.type
)

const heightMode = ref<PanelHeightMode>('auto')
const shade = ref(false)
const notesText = ref('')
const htmlUrl = ref('')
const topnKpi = ref(DEFAULT_TOPN_KPI)

// panels that support optional severity row shading (legacy-style)
const SHADEABLE = ['pending-situations', 'nodes-with-alarms', 'availability']
const supportsShade = computed(() => SHADEABLE.includes(props.panel.type))
const topnN = ref(DEFAULT_TOPN_N)
const topnDirection = ref<'asc' | 'desc'>('desc')

// metric-chart: one entity x one metric (single-select each)
const chartEntity = ref(DEFAULT_CHART_ENTITY)
const chartMetric = ref(DEFAULT_CHART_METRIC)
const entityOptions = ref<string[]>([])
const entitiesLoading = ref(false)

const loadEntities = async () => {
  entitiesLoading.value = true
  const entities = await listMetricEntities(chartMetric.value)
  // keep the current selection listed even if it has no data right now
  if (chartEntity.value && !entities.includes(chartEntity.value)) {
    entities.unshift(chartEntity.value)
  }
  entityOptions.value = entities
  entitiesLoading.value = false
}

watch(chartMetric, () => {
  if (props.panel.type === 'metric-chart' && props.visible) loadEntities()
})

// External URLs are blocked by the dashboard CSP (frame-src 'self'); validate
// up front so the user gets an explanation instead of a silent broken iframe.
const urlError = computed(() => {
  const t = htmlUrl.value.trim()
  if (!t) return ''
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
  topnN.value = Number(props.panel.options?.n ?? DEFAULT_TOPN_N)
  topnDirection.value = props.panel.options?.direction === 'asc' ? 'asc' : 'desc'
  chartEntity.value = String(props.panel.options?.entity ?? DEFAULT_CHART_ENTITY)
  chartMetric.value = String(props.panel.options?.metric ?? DEFAULT_CHART_METRIC)
  if (props.panel.type === 'metric-chart') loadEntities()
}

watch(
  () => props.visible,
  (v) => {
    if (v) syncFromPanel()
  }
)

const apply = () => {
  if (props.panel.type === 'html-content' && urlError.value) {
    return // keep the dialog open; the error is shown inline
  }
  store.setPanelHeightMode(props.panel.id, heightMode.value)
  const opts: Record<string, unknown> = { ...props.panel.options }
  if (supportsShade.value) opts.shade = shade.value
  if (props.panel.type === 'notes') opts.text = notesText.value
  if (props.panel.type === 'html-content') opts.url = htmlUrl.value.trim()
  if (props.panel.type === 'topn') {
    opts.kpi = topnKpi.value
    opts.n = Math.min(50, Math.max(1, Number(topnN.value) || DEFAULT_TOPN_N))
    opts.direction = topnDirection.value
  }
  if (props.panel.type === 'metric-chart') {
    opts.entity = chartEntity.value
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
