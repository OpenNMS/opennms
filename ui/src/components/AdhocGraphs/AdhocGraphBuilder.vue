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
          dataKey="id"
          optionLabel="label"
          filterPlaceholder="Search nodes, or a filter rule"
          emptyMessage="No nodes match."
          :filterTerm="store.nodeFilter"
          :filterActive="Boolean(store.nodeFilter.trim())"
          :errorMessage="store.nodeFilterError"
          :note="nodeNote"
          :options="store.nodeOptions"
          :modelValue="store.pickedNodes"
          :loading="store.nodesLoading"
          :keyOf="option => (option as AdhocNodeOption).id"
          :labelOf="option => (option as AdhocNodeOption).label"
          :descriptionOf="describeNode"
          @filter="onNodeFilter"
          @update:modelValue="value => store.setPickedNodes(value as AdhocNodeOption[])"
        />
      </div>
      <div class="onms-col-4">
        <SelectionColumn
          title="Resources"
          dataTest="resources"
          dataKey="id"
          optionLabel="label"
          filterPlaceholder="Resource, e.g. interfaceSnmp[eth*]"
          :emptyMessage="store.effectiveNodes.length ? 'No resources match.' : 'Type a node filter, or select nodes, to see resources.'"
          :filterTerm="store.resourceFilter"
          :filterActive="Boolean(store.resourceFilter.trim())"
          :options="store.resourceOptions"
          :modelValue="store.reachablePickedResources"
          :loading="store.resourcesLoading"
          :keyOf="option => (option as AdhocResourceOption).id"
          :labelOf="option => (option as AdhocResourceOption).label"
          :descriptionOf="describeResource"
          @filter="store.setResourceFilter"
          @update:modelValue="value => store.setPickedResources(value as AdhocResourceOption[])"
        />
      </div>
      <div class="onms-col-4">
        <SelectionColumn
          title="Datasources"
          dataTest="datasources"
          dataKey="key"
          optionLabel="attribute"
          filterPlaceholder="Datasource, e.g. ifHC*Octets"
          :emptyMessage="store.effectiveResources.length ? 'No datasources match.' : 'Type a resource filter, or select resources, to see datasources.'"
          :filterTerm="store.datasourceFilter"
          :filterActive="Boolean(store.datasourceFilter.trim())"
          :options="store.datasourceOptions"
          :modelValue="store.reachablePickedDatasources"
          :keyOf="option => (option as AdhocDatasourceOption).key"
          :labelOf="option => (option as AdhocDatasourceOption).attribute"
          :descriptionOf="describeDatasource"
          @filter="store.setDatasourceFilter"
          @update:modelValue="value => store.setPickedDatasources(value as AdhocDatasourceOption[])"
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
          :error="tooManySeriesMessage || store.queryError"
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
          <h3>Filters</h3>
          <p>Each column is a filter. Whatever all three match is in the graph, so a graph can be described rather than clicked together: nodes matching a rule, their interfaces matching a pattern, and one or two attributes of each.</p>
          <p><strong>Nodes</strong> takes a label fragment, or an OpenNMS filter rule such as <code>catincRouters &amp; location='Default'</code>, the same expression a Grafana <code>nodeFilter()</code> variable takes. <strong>Resources</strong> and <strong>Datasources</strong> take plain text or a wildcard pattern: <code>interfaceSnmp[eth*]</code>, <code>ifHC*Octets</code>.</p>
          <p>Selecting items in a column narrows it to those items; clear the selection to go back to everything the filter matches. A column with an empty filter and nothing selected contributes nothing, so nothing is graphed by accident.</p>
          <h3>Expressions</h3>
          <p>Expressions are evaluated server-side with JEXL. Reference a source series by its label &mdash; for example <code>ifHCInOctets_eth0 * 8</code> to convert octets to bits &mdash; and the result is plotted as a series of its own.</p>
          <p>Labels are JEXL identifiers, so they may contain only letters, digits and underscores, and must be unique across the graph.</p>
          <p>Turn on <strong>Hide raw</strong> for a source to keep it out of the graph while still feeding an expression. It only takes effect when some expression actually references that source.</p>
          <h3>Sharing</h3>
          <p>The filters, selections, expressions, time range and render options live in the page URL, so a graph can be bookmarked or pasted to a colleague. Use the link button to copy it. A link is re-evaluated when it is opened, so a graph of "all production routers" picks up routers added since.</p>
        </div>
      </template>
    </OnmsMessageDialog>
  </div>
</template>

<script setup lang="ts">
import { OnmsIconButton, OnmsMessageDialog, OnmsPanel } from '@opennms/onms-ui'
import { onKeyStroke, useDebounceFn } from '@vueuse/core'
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
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
import { MAX_GRAPH_NODES, useAdhocGraphStore } from '@/stores/adhocGraphStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb, StartEndTime } from '@/types'
import { DEFAULT_RANGE, resolveRelativeRange } from '@/components/Common/utils/timeRangeOptions'
import {
  AdhocDatasourceOption,
  AdhocExpression,
  AdhocGraphConfig,
  AdhocNodeOption,
  AdhocResourceOption,
  AdhocSeries,
  AdhocSeriesOverride
} from '@/types/adhocGraph'
import { ConsolidationFunctionType } from '@/types/timeSeries'
import { restepColorForTheme, seriesColor } from './utils/adhocColors'
import {
  buildMeasurementsPayload,
  configIsQueryable,
  DEFAULT_RESOLUTION,
  labelForDatasource,
  MAX_SERIES,
  querySignature,
  toConvertedGraphData
} from './utils/adhocQuery'
import {
  AdhocLinkState,
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

/**
 * Per-series edits, keyed by series. The series list is generated from the
 * filters every time they change, so an edited label or color has to live apart
 * from the generated row and be re-applied when the row is rebuilt.
 */
const overrides = reactive<Record<string, AdhocSeriesOverride>>({})

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

/**
 * Filters compose multiplicatively, so a loose combination can name far more
 * series than a plot can show or the measurements API should be asked for. Past
 * the cap the page explains rather than tries.
 */
const tooManySeriesMessage = computed<string>(() => {
  if (store.nodeLimitExceeded) {
    return `The node filter matches ${store.nodeLimitExceeded.toLocaleString()} nodes; a graph can be built from at most ${MAX_GRAPH_NODES}. Narrow the rule, or select nodes.`
  }

  const count = store.effectiveDatasources.length

  if (count <= MAX_SERIES) {
    return ''
  }

  return `These filters match ${count.toLocaleString()} series; the limit is ${MAX_SERIES}. Narrow a filter, or select items in a column.`
})

const canQuery = computed<boolean>(() => !tooManySeriesMessage.value && configIsQueryable(config))

/** A label search is paged; say so, or a rule that matches 2,000 nodes looks like it matched 100. */
const nodeNote = computed<string>(() => {
  if (store.nodeLimitExceeded) {
    return `${store.nodeLimitExceeded.toLocaleString()} nodes match; a graph can be built from at most ${MAX_GRAPH_NODES}.`
  }

  if (store.nodeMatchOverflow <= 0) {
    return ''
  }

  const shown = store.nodeMatches.length
  const total = (shown + store.nodeMatchOverflow).toLocaleString()
  return `Showing the first ${shown} of ${total} nodes. Narrow the search, or use a filter rule to match them all.`
})

/**
 * The node's identity the way the Grafana plugin presents it by default: the
 * foreign-source pair when the node is requisitioned, the database id otherwise,
 * plus the location when known.
 */
const describeNode = (option: unknown): string => {
  const node = option as AdhocNodeOption
  const identity = node.foreignSource && node.foreignId ? `${node.foreignSource}:${node.foreignId}` : `#${node.id}`
  return node.location ? `${identity} · ${node.location}` : identity
}

const describeResource = (option: unknown): string => {
  const resource = option as AdhocResourceOption
  return [resource.nodeLabel, resource.typeLabel].filter(Boolean).join(' · ') || resource.id
}

const describeDatasource = (option: unknown): string => {
  const datasource = option as AdhocDatasourceOption
  return [datasource.nodeLabel, datasource.resourceLabel].filter(Boolean).join(' · ')
}

// A label fragment can follow typing; a rule is a round trip to the filter
// engine, so wait a little longer for the typing to settle.
// The column echoes the text locally meanwhile, so nothing snaps back; the store
// only learns the new filter when it is evaluated, or the matches of the previous
// filter would count as "matching" the half-typed one for half a second.
const onNodeFilter = useDebounceFn((term: string) => store.setNodeFilter(term), 500)

/**
 * Rebuild `config.series` from what the filters currently match, re-applying the
 * user's edits. Colors are carried over from the previous build where the series
 * survived, so adding a series does not recolor its neighbors; a new series takes
 * the palette slot for its position. Labels are NOT carried over: a generated
 * label depends on node and resource labels that a link-restored series does not
 * have until its data loads, and freezing the first guess would leave it wrong,
 * and any expression that referenced the real label broken. Only an edited label
 * (an override) persists.
 */
const reconcileSeries = (datasources: AdhocDatasourceOption[]) => {
  if (datasources.length > MAX_SERIES) {
    config.series = []
    return
  }

  const existing = new Map(config.series.map(entry => [entry.key, entry]))
  const taken = new Set<string>()
  const next: AdhocSeries[] = []

  // Edited labels are claimed first so a generated one can never collide with them.
  for (const datasource of datasources) {
    const label = overrides[datasource.key]?.label

    if (label) {
      taken.add(label)
    }
  }

  for (const datasource of datasources) {
    const override = overrides[datasource.key] ?? {}
    const previous = existing.get(datasource.key)
    const label = override.label ?? labelForDatasource(datasource, taken)
    taken.add(label)

    next.push({
      key: datasource.key,
      label,
      resourceId: datasource.resourceId,
      attribute: datasource.attribute,
      aggregation: override.aggregation ?? ConsolidationFunctionType.AVERAGE,
      color: override.color ?? previous?.color ?? seriesColor(next.length + config.expressions.length, appStore.theme),
      style: override.style ?? 'line',
      hidden: override.hidden ?? false
    })
  }

  config.series = next
}

const updateSeries = (key: string, patch: Partial<AdhocSeries>) => {
  overrides[key] = { ...overrides[key], ...patch }
  config.series = config.series.map(entry => (entry.key === key ? { ...entry, ...patch } : entry))
}

/**
 * Taking one series out of a generated set means pinning the rest: the picks
 * become everything that was matched except this one. With picks already in
 * place it is simply one fewer pick. Removing the last one has to empty the
 * graph, not fall back to "everything matching", so the filter goes with it.
 */
const removeSeries = (key: string) => {
  const current = store.pickedDatasources.length ? store.reachablePickedDatasources : store.effectiveDatasources
  const remaining = current.filter(datasource => datasource.key !== key)

  if (!remaining.length) {
    store.setDatasourceFilter('')
  }

  store.setPickedDatasources(remaining)
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
  // Back to the browse page, not a blank column under an empty box.
  store.evaluateNodes()
  config.series = []
  config.expressions = []
  config.title = ''
  config.verticalLabel = ''
  config.stacked = false
  config.resolution = DEFAULT_RESOLUTION

  for (const key of Object.keys(overrides)) {
    delete overrides[key]
  }
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

/** What a link carries besides the config: the picker, the edits, and the series count for comparison. */
const linkState = (): AdhocLinkState => ({
  selection: store.selectionState,
  overrides: Object.fromEntries(
    Object.entries(overrides).filter(([key]) => config.series.some(entry => entry.key === key))
  ),
  seriesCount: config.series.length
})

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
  const query = encodeAdhocState(config, time, linkState())

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
    showSnackBar({ msg: 'This graph has too many selections to open in its own tab.', error: true })
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
    showSnackBar({ msg: 'This graph has too many selections to share as a link.', error: true })
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
    showSnackBar({ msg: 'Could not copy automatically; the link is in the address bar.', error: true })
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

  const query = encodeAdhocState(config, time, linkState())

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
        msg: 'This graph now has too many selections to keep in the page address; it still works, but the link no longer captures it.',
        error: true
      })
    }

    return
  }

  warnedUnshareable = false
  router.replace({ query: query as Record<string, string | string[]> })
}, 400)

const debouncedQuery = useDebounceFn(runQuery, 600)

// The series are whatever the filters and picks currently produce.
watch(() => store.effectiveDatasources, value => reconcileSeries([...value]), { deep: true })

// Only a change to what the server would return re-queries; style, color and
// title changes re-render from the data already in hand.
watch(() => querySignature(config, time), () => {
  syncUrl()
  debouncedQuery()
})

// Non-query config still belongs in the URL, and so does the picker itself.
watch(() => [config.title, config.verticalLabel, config.stacked, config.series, config.expressions], syncUrl, { deep: true })
watch(() => store.selectionState, syncUrl, { deep: true })

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

  if (!restored) {
    store.evaluateNodes()
    return
  }

  hydrating = true

  Object.assign(config, restored.config)
  Object.assign(overrides, restored.link.overrides)

  if (restored.time.range) {
    // Resolved against the clock now, so the link shows current data however old
    // the bookmark is.
    updateTime(resolveRelativeRange(restored.time.range))
  } else if (restored.time.startTime && restored.time.endTime) {
    updateTime(restored.time)
  }

  // An expression id is regenerated rather than carried; a color is optional.
  config.expressions = config.expressions.map((entry, index) => ({
    ...entry,
    id: `expr-${++expressionSeq}`,
    color: entry.color || seriesColor(index, appStore.theme)
  }))

  // A link that carries expressions should show them; hiding them would make the
  // graph look like it came from its sources alone.
  expressionsCollapsed.value = config.expressions.length === 0

  // Evaluate the picker as the server stands now. The series follow through the
  // watcher above, so wait a tick for them before deciding what to say and query.
  await store.restore(restored.link.selection)
  await nextTick()

  const shared = restored.link.seriesCount
  const now = store.effectiveDatasources.length

  if (shared > 0 && now !== shared) {
    showSnackBar({
      msg: `This link now matches ${now.toLocaleString()} series; it matched ${shared.toLocaleString()} when it was made.`
    })
  }

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
