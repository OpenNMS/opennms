<template>
  <div
    class="adhoc-builder"
    :class="{ 'is-expanded': chartIsExpanded }"
  >
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>

    <div class="header">
      <div class="heading page-heading">
        <!--
          A fixed page title, not the graph's own title: the graph title is already
          shown on the plot itself and is editable in the toolbar, and a heading
          that changes with it stops naming the page you are on.
        -->
        <h2>Custom Performance Graphs</h2>
        <OnmsIconButton
          v-if="!viewOnly"
          title="Custom Performance Graphs Help"
          data-test="adhoc-info-icon"
          :icon="InfoIcon"
          @click="isHelpMessageDialogVisible = true"
        />
        <router-link
          v-else
          to="/adhoc-graphs"
          class="builder-link"
          data-test="adhoc-open-builder"
        >Open in the builder</router-link>
      </div>
    </div>

    <div
      v-if="showBuilderChrome"
      class="onms-row"
    >
      <div class="onms-col-4">
        <SelectionColumn
          title="Nodes"
          dataTest="nodes"
          serverFilter
          dataKey="id"
          optionLabel="label"
          filterPlaceholder="Search nodes"
          emptyMessage="No nodes match that search."
          :options="store.nodeOptions"
          :modelValue="store.selectedNodes"
          :loading="store.nodesLoading"
          :keyOf="option => (option as AdhocNodeOption).id"
          :labelOf="option => (option as AdhocNodeOption).label"
          :descriptionOf="describeNode"
          @filter="searchNodes"
          @update:modelValue="value => store.setSelectedNodes(value as AdhocNodeOption[])"
        />
      </div>
      <div class="onms-col-4">
        <SelectionColumn
          title="Resources"
          dataTest="resources"
          dataKey="id"
          optionLabel="label"
          filterPlaceholder="Filter resources"
          emptyMessage="Select a node to see its resources."
          :options="store.resourceOptions"
          :modelValue="store.selectedResources"
          :loading="store.resourcesLoading"
          :keyOf="option => (option as AdhocResourceOption).id"
          :labelOf="option => (option as AdhocResourceOption).label"
          :descriptionOf="describeResource"
          @update:modelValue="value => store.setSelectedResources(value as AdhocResourceOption[])"
        />
      </div>
      <div class="onms-col-4">
        <SelectionColumn
          title="Datasources"
          dataTest="datasources"
          dataKey="key"
          optionLabel="attribute"
          filterPlaceholder="Filter datasources"
          emptyMessage="Select a resource to see its datasources."
          :options="store.datasourceOptions"
          :modelValue="store.selectedDatasources"
          :loading="store.datasourcesLoading"
          :keyOf="option => (option as AdhocDatasourceOption).key"
          :labelOf="option => (option as AdhocDatasourceOption).attribute"
          :descriptionOf="describeDatasource"
          @update:modelValue="value => store.setSelectedDatasources(value as AdhocDatasourceOption[])"
        />
      </div>
    </div>

    <div
      v-if="showBuilderChrome"
      class="onms-row"
    >
      <div class="onms-col-12">
        <OnmsPanel
          :header="`Series (${config.series.length})`"
          toggleable
          :collapsed="seriesCollapsed"
          class="builder-panel"
          data-test="series-panel"
          @update:collapsed="value => seriesCollapsed = value"
        >
          <SeriesTable
            :series="config.series"
            :expressions="config.expressions"
            @update="updateSeries"
            @remove="removeSeries"
          />
        </OnmsPanel>
      </div>
    </div>

    <div
      v-if="showBuilderChrome"
      class="onms-row"
    >
      <div class="onms-col-12">
        <OnmsPanel
          :header="`Expressions (${config.expressions.length})`"
          toggleable
          :collapsed="expressionsCollapsed"
          class="builder-panel"
          data-test="expressions-panel"
          @update:collapsed="value => expressionsCollapsed = value"
        >
          <ExpressionEditor
            :series="config.series"
            :expressions="config.expressions"
            @add="addExpression"
            @update="updateExpression"
            @remove="removeExpression"
          />
        </OnmsPanel>
      </div>
    </div>

    <div class="onms-row chart-row">
      <div class="onms-col-12 chart-cell">
        <AdhocChartToolbar
          :config="config"
          :canQuery="canQuery"
          :hasData="Boolean(store.measurements)"
          :loading="store.queryLoading"
          :expanded="expanded"
          :viewOnly="viewOnly"
          @update="patch => Object.assign(config, patch)"
          @updateTime="updateTime"
          @refresh="runQuery"
          @clear="clearAll"
          @share="shareLink"
          @toggleExpand="expanded = !expanded"
          @popOut="popOut"
          @showDefinition="isDefinitionDialogVisible = true"
          @exportCsv="exportCsv"
          @exportPdf="exportPdf"
        />
        <AdhocChart
          ref="chartRef"
          :config="config"
          :measurements="store.measurements"
          :time="time"
          :loading="store.queryLoading"
          :error="store.queryError"
          :expanded="chartIsExpanded"
        />
      </div>
    </div>

    <RrdDefinitionDialog
      :visible="isDefinitionDialogVisible"
      :config="config"
      @close="isDefinitionDialogVisible = false"
    />

    <OnmsMessageDialog
      :visible="isHelpMessageDialogVisible"
      :relative="true"
      maxHeight="26em"
      maxWidth="50em"
      title="Custom Performance Graphs"
      @close="isHelpMessageDialogVisible = false"
    >
      <template #content>
        <div class="adhoc-help">
          <p>Build a graph from any combination of datasources, across any number of nodes, without a pre-defined graph definition.</p>
          <p>Pick nodes, then their resources, then the datasources you want to plot. Each selected datasource becomes a series you can label, recolor and restyle.</p>
          <h3>Expressions</h3>
          <p>Expressions are evaluated server-side with JEXL. Reference a source series by its label &mdash; for example <code>ifHCInOctets_eth0 * 8</code> to convert octets to bits &mdash; and the result is plotted as a series of its own.</p>
          <p>Labels are JEXL identifiers, so they may contain only letters, digits and underscores, and must be unique across the graph.</p>
          <p>Turn on <strong>Hide raw</strong> for a source to keep it out of the graph while still feeding an expression. It only takes effect when some expression actually references that source.</p>
          <h3>Sharing</h3>
          <p>The full selection, expressions, time range and render options live in the page URL, so a graph can be bookmarked or pasted to a colleague. Use the link button to copy it.</p>
        </div>
      </template>
    </OnmsMessageDialog>
  </div>
</template>

<script setup lang="ts">
import { OnmsIconButton, OnmsMessageDialog, OnmsPanel } from '@opennms/onms-ui'
import { onKeyStroke, useDebounceFn } from '@vueuse/core'
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AdhocChart from './AdhocChart.vue'
import AdhocChartToolbar from './AdhocChartToolbar.vue'
import ExpressionEditor from './ExpressionEditor.vue'
import SelectionColumn from './SelectionColumn.vue'
import RrdDefinitionDialog from './RrdDefinitionDialog.vue'
import SeriesTable from './SeriesTable.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'
import { downloadGraphCsv, exportGraphsToPdf } from '@/components/Resources/utils/graphExport'
import { copyToClipboard } from '@/composables/useClipboard'
import useSnackbar from '@/composables/useSnackbar'
import { useAppStore } from '@/stores/appStore'
import { useAdhocGraphStore } from '@/stores/adhocGraphStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb, StartEndTime } from '@/types'
import { DEFAULT_RANGE, resolveRelativeRange } from '@/components/Common/utils/timeRangeOptions'
import {
  AdhocDatasourceOption,
  AdhocExpression,
  AdhocGraphConfig,
  AdhocNodeOption,
  AdhocResourceOption,
  AdhocSeries
} from '@/types/adhocGraph'
import { ConsolidationFunctionType } from '@/types/timeSeries'
import { restepColorForTheme, seriesColor } from './utils/adhocColors'
import {
  buildMeasurementsPayload,
  configIsQueryable,
  DEFAULT_RESOLUTION,
  labelForDatasource,
  querySignature,
  toConvertedGraphData
} from './utils/adhocQuery'
import {
  decodeAdhocState,
  encodeAdhocState,
  encodedQueryLength,
  MAX_QUERY_LENGTH
} from './utils/adhocUrlState'

/** This component's own routes; also the last-resort fragment for a share link. */
const ADHOC_ROUTE_PATH = '/adhoc-graphs'
const ADHOC_VIEW_ROUTE_PATH = '/adhoc-graphs/view'

const props = withDefaults(defineProps<{
  /**
   * Graph-only route (/adhoc-graphs/view). The page renders from the URL alone:
   * no pickers, no editors, just the time controls, the plot and the exports.
   */
  viewOnly?: boolean
}>(), {
  viewOnly: false
})

const appStore = useAppStore()
const menuStore = useMenuStore()
const route = useRoute()
const router = useRouter()
const store = useAdhocGraphStore()
const { showSnackBar } = useSnackbar()

const chartRef = ref<InstanceType<typeof AdhocChart> | null>(null)
// Series is the main event, so it opens; expressions are optional, so they stay
// out of the way. Both are only INITIAL states — once the user toggles a panel,
// nothing here reopens or recloses it behind their back.
const seriesCollapsed = ref(false)
const expressionsCollapsed = ref(true)
const isHelpMessageDialogVisible = ref(false)
const isDefinitionDialogVisible = ref(false)
const expanded = ref(false)

/** Pickers and editors are hidden while expanded, and absent entirely on the view route. */
const showBuilderChrome = computed<boolean>(() => !props.viewOnly && !expanded.value)

const chartIsExpanded = computed<boolean>(() => props.viewOnly || expanded.value)

/** Guards the URL writer while hydrating, so restoring a link doesn't rewrite it. */
let hydrating = false

/**
 * Whether the user has already been told this graph outgrew its link. Latched so
 * the debounced writer says it once per episode rather than on every keystroke,
 * and re-arms if the graph shrinks back under the cap.
 */
let warnedUnshareable = false
let expressionSeq = 0

/**
 * Set on teardown. The URL write and the query below are debounced, so both can
 * still be pending when the user navigates away — and a late `router.replace`
 * would stamp this page's query onto whatever route they moved to.
 */
let disposed = false

const config = reactive<AdhocGraphConfig>({
  series: [],
  expressions: [],
  title: '',
  verticalLabel: '',
  stacked: false,
  resolution: DEFAULT_RESOLUTION
})

// Relative by default, so an unbookmarked page and a bookmarked one behave alike.
const time = reactive<StartEndTime>(resolveRelativeRange(DEFAULT_RANGE))

const breadcrumbs = computed<BreadCrumb[]>(() => (props.viewOnly ?
  [
    { label: 'Home', to: menuStore.mainMenu.homeUrl, isAbsoluteLink: true },
    { label: 'Custom Performance Graphs', to: ADHOC_ROUTE_PATH },
    { label: 'Graph', to: '#', position: 'last' }
  ] :
  [
    { label: 'Home', to: menuStore.mainMenu.homeUrl, isAbsoluteLink: true },
    { label: 'Custom Performance Graphs', to: '#', position: 'last' }
  ]))

const canQuery = computed<boolean>(() => configIsQueryable(config))

const describeNode = (option: unknown): string => `Node #${(option as AdhocNodeOption).id}`

const describeResource = (option: unknown): string => {
  const resource = option as AdhocResourceOption
  return `${resource.nodeLabel} · ${resource.typeLabel}`
}

const describeDatasource = (option: unknown): string => {
  const datasource = option as AdhocDatasourceOption
  return `${datasource.nodeLabel} · ${datasource.resourceLabel}`
}

const searchNodes = useDebounceFn((term: string) => store.searchNodes(term), 350)

/**
 * Keep `config.series` in step with the datasource selection.
 *
 * Existing entries are carried over by key so a label, color, style or aggregation
 * the user set is never lost when an unrelated datasource is ticked or unticked —
 * that regression is the whole reason this is a reconcile rather than a rebuild.
 */
const reconcileSeries = (datasources: AdhocDatasourceOption[]) => {
  const existing = new Map(config.series.map(entry => [entry.key, entry]))
  const taken = new Set<string>()
  const next: AdhocSeries[] = []

  for (const datasource of datasources) {
    const previous = existing.get(datasource.key)

    if (previous) {
      taken.add(previous.label)
      next.push(previous)
      continue
    }

    const label = labelForDatasource(datasource, taken)
    taken.add(label)
    next.push({
      key: datasource.key,
      label,
      resourceId: datasource.resourceId,
      attribute: datasource.attribute,
      aggregation: ConsolidationFunctionType.AVERAGE,
      // Slot by final position, so an existing series keeps its color when a new
      // one is added — color identifies the series, not its place in the list.
      color: seriesColor(next.length + config.expressions.length, appStore.theme),
      style: 'line',
      hidden: false
    })
  }

  config.series = next
}

const updateSeries = (key: string, patch: Partial<AdhocSeries>) => {
  config.series = config.series.map(entry => (entry.key === key ? { ...entry, ...patch } : entry))
}

const removeSeries = (key: string) => {
  store.setSelectedDatasources(store.selectedDatasources.filter(datasource => datasource.key !== key))
}

const addExpression = () => {
  config.expressions = [...config.expressions, {
    id: `expr-${++expressionSeq}`,
    label: `expression_${config.expressions.length + 1}`,
    value: '',
    color: seriesColor(config.series.length + config.expressions.length, appStore.theme),
    style: 'line'
  }]
}

const updateExpression = (id: string, patch: Partial<AdhocExpression>) => {
  config.expressions = config.expressions.map(entry => (entry.id === id ? { ...entry, ...patch } : entry))
}

const removeExpression = (id: string) => {
  config.expressions = config.expressions.filter(entry => entry.id !== id)
}

const updateTime = (value: StartEndTime) => {
  time.startTime = value.startTime
  time.endTime = value.endTime
  time.format = value.format
  // Deleted rather than left stale: a custom range carries no `range`, and keeping
  // the previous one would make an absolute window silently start sliding.
  if (value.range) {
    time.range = value.range
  } else {
    delete time.range
  }
}

/**
 * Slide a relative window up to the present. A no-op for a custom range, which is
 * absolute by definition.
 */
const refreshRelativeWindow = () => {
  if (!time.range) {
    return
  }

  const resolved = resolveRelativeRange(time.range)
  time.startTime = resolved.startTime
  time.endTime = resolved.endTime
}

const runQuery = () => {
  if (disposed || !canQuery.value) {
    return
  }

  // "Last hour" must mean the hour ending now, not the hour that ended whenever the
  // range was chosen — which may have been a long time ago on a page left open.
  refreshRelativeWindow()
  store.runQuery(buildMeasurementsPayload(config, time))
}

const clearAll = () => {
  store.clearAll()
  config.series = []
  config.expressions = []
  config.title = ''
  config.verticalLabel = ''
  config.stacked = false
  config.resolution = DEFAULT_RESOLUTION
}

const exportCsv = () => {
  if (!store.measurements) {
    return
  }

  downloadGraphCsv(store.measurements, toConvertedGraphData(config), config.title || 'adhoc-graph')
}

const exportPdf = () => {
  const target = chartRef.value?.exportTarget()

  if (!target || !exportGraphsToPdf(target, config.title || 'Custom Performance Graph')) {
    showSnackBar({ msg: 'There is no rendered graph to export yet.' })
  }
}

/**
 * The shareable link for the graph as it stands right now.
 *
 * Deliberately NOT `window.location.href`: the address bar is written by the
 * debounced `syncUrl`, so for a few hundred milliseconds after any edit it still
 * describes the previous state — long enough for "tweak something, hit copy" to
 * hand out a link to the wrong graph. This rebuilds the query synchronously from
 * the live config instead.
 *
 * Returns null when the selection is past the shareable size, which is the same
 * point at which syncUrl gives up on the address bar.
 */
const buildShareUrl = (routePath?: string): string | null => {
  const query = encodeAdhocState(config, time)

  if (encodedQueryLength(query) > MAX_QUERY_LENGTH) {
    return null
  }

  const params = new URLSearchParams()

  for (const [key, value] of Object.entries(query)) {
    for (const entry of Array.isArray(value) ? value : [value]) {
      if (typeof entry === 'string') {
        params.append(key, entry)
      }
    }
  }

  // Hash-history router: the route and its query both live inside the fragment,
  // so take the real origin/pathname (which differ between a deployed instance
  // and the dev server) and replace only the query part of the hash.
  const { origin, pathname, hash } = window.location
  // Prefer the live hash; fall back to the router's view, and finally to this
  // component's own route — a link is never worth emitting with an empty or
  // undefined fragment, which is what the first two produce before the first
  // navigation settles.
  const hashPath = routePath ?
    `#${routePath}` :
    hash.split('?')[0] || (route.path ? `#${route.path}` : `#${ADHOC_ROUTE_PATH}`)

  return `${origin}${pathname}${hashPath}?${params.toString()}`
}

/**
 * Open the current graph on its own, in a new tab.
 *
 * A real window rather than an in-app route change, so the builder stays put with
 * its selection intact — the point of popping out is to park the graph on another
 * screen while carrying on here. `noopener` because the new tab is untrusted with
 * a handle back to this one.
 */
const popOut = () => {
  const url = buildShareUrl(ADHOC_VIEW_ROUTE_PATH)

  if (!url) {
    showSnackBar({ msg: 'This graph has too many series to open in its own tab.', error: true })
    return
  }

  const opened = window.open(url, '_blank', 'noopener')

  if (!opened) {
    showSnackBar({ msg: 'The browser blocked the new tab. Allow pop-ups for this site, or copy the link instead.', error: true })
  }
}

const shareLink = async () => {
  const url = buildShareUrl()

  if (!url) {
    showSnackBar({ msg: 'This graph has too many series to share as a link.', error: true })
    return
  }

  // Called before any await so the click's transient user activation still stands.
  const copied = copyToClipboard(url)

  try {
    await copied
    showSnackBar({ msg: 'Link copied to the clipboard.' })
  } catch (_err) {
    // The browser can refuse outright (permissions policy, no activation). The
    // address bar still holds a working link, so point at it rather than failing
    // silently.
    showSnackBar({ msg: 'Could not copy automatically — the link is in the address bar.', error: true })
  }
}

/**
 * Mirror the graph into the address bar so it can be bookmarked or pasted to a
 * colleague. `replace` rather than `push`: every tweak would otherwise add a
 * history entry and make Back unusable.
 */
const syncUrl = useDebounceFn(() => {
  if (hydrating || disposed) {
    return
  }

  const query = encodeAdhocState(config, time)

  if (encodedQueryLength(query) > MAX_QUERY_LENGTH) {
    // Past this size the link stops being pasteable and some proxies truncate it.
    // The graph keeps working from in-memory state; only sharing is lost — so say
    // so. Blanking the address bar silently meant a large graph quietly stopped
    // being bookmarkable, and nothing revealed it until someone tried to copy.
    if (Object.keys(route.query).length) {
      router.replace({ query: {}})
    }

    if (!warnedUnshareable) {
      warnedUnshareable = true
      showSnackBar({
        msg: 'This graph now has too many series to keep in the page address — it still works, but the link no longer captures it.',
        error: true
      })
    }

    return
  }

  warnedUnshareable = false
  router.replace({ query: query as Record<string, string | string[]> })
}, 400)

const debouncedQuery = useDebounceFn(runQuery, 600)

watch(() => store.selectedDatasources, value => reconcileSeries([...value]), { deep: true })

// Only a change to what the server would return re-queries; style, color and
// title changes re-render from the data already in hand.
watch(() => querySignature(config, time), () => {
  syncUrl()
  debouncedQuery()
})

// Non-query config still belongs in the URL.
watch(() => [config.title, config.verticalLabel, config.stacked, config.series, config.expressions], syncUrl, { deep: true })

// A shared link carries the colors of the theme it was built in; move any color
// still sitting on a palette slot to that slot's step for the current theme.
watch(() => appStore.theme, (theme) => {
  config.series = config.series.map(entry => ({ ...entry, color: restepColorForTheme(entry.color, theme) }))
  config.expressions = config.expressions.map(entry => ({ ...entry, color: restepColorForTheme(entry.color, theme) }))
})

onKeyStroke('Escape', () => {
  if (expanded.value) {
    expanded.value = false
  }
})

onBeforeUnmount(() => {
  disposed = true
})

onMounted(async () => {
  const restored = decodeAdhocState(route.query)

  if (!restored?.config.series.length) {
    store.searchNodes('')
    return
  }

  hydrating = true

  Object.assign(config, restored.config)

  if (restored.time.range) {
    // Resolved against the clock now, so the link shows current data however old
    // the bookmark is.
    updateTime(resolveRelativeRange(restored.time.range))
  } else if (restored.time.startTime && restored.time.endTime) {
    updateTime(restored.time)
  }

  // Fill in anything the link left out: colors are optional in the encoding, and
  // an expression id is regenerated rather than carried.
  config.series = config.series.map((entry, index) => ({
    ...entry,
    color: entry.color || seriesColor(index, appStore.theme)
  }))
  config.expressions = config.expressions.map((entry, index) => ({
    ...entry,
    id: `expr-${++expressionSeq}`,
    color: entry.color || seriesColor(config.series.length + index, appStore.theme)
  }))

  // Walk the cascade backwards from the link's resource ids so all three panes
  // show what is actually being plotted, then populate the node picker. The search
  // runs after the restore, not before, so it can pin the restored nodes to the top
  // rather than racing them.
  // A link that carries expressions should show them; hiding them would make the
  // graph look like it came from its sources alone.
  expressionsCollapsed.value = config.expressions.length === 0

  await store.restoreSelection(config.series.map(entry => ({
    resourceId: entry.resourceId,
    attribute: entry.attribute
  })))

  store.searchNodes('')

  hydrating = false
  runQuery()
})
</script>

<style scoped lang="scss">
@import '@/styles/onms-typography';

/**
 * Normal mode scrolls as a whole. Expanded mode does not: the card is a fixed
 * height, so the chart row takes whatever is left over after the breadcrumbs,
 * heading and toolbar, and nothing spills past the bottom of the screen.
 */
.adhoc-builder {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow-y: auto;

  &.is-expanded {
    overflow: hidden;

    .chart-row,
    .chart-cell {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-height: 0;
    }
  }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-heading {
  display: flex;
  align-items: center;
  gap: 0.5rem;

  h2 {
    margin: 0;
    overflow-wrap: anywhere;
  }

  .builder-link {
    color: var(--onms-clickable-normal);
    white-space: nowrap;
  }
}

.builder-panel {
  width: 100%;
}
</style>
