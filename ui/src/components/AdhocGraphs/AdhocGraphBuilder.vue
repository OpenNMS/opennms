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
          :filterTerm="nodeText"
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
          @clear="onPickNodes([])"
          @update:modelValue="value => onPickNodes(value as AdhocNodeOption[])"
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
          :filterTerm="resourceText"
          :filterActive="Boolean(store.resourceFilter.trim())"
          :note="resourceNote"
          :options="store.resourceOptions"
          :modelValue="store.reachablePickedResources"
          :setAsideCount="store.setAsideResources"
          :loading="store.resourcesLoading"
          :keyOf="option => (option as AdhocResourceOption).id"
          :labelOf="option => (option as AdhocResourceOption).label"
          :descriptionOf="describeResource"
          @filter="onResourceFilter"
          @clear="store.setPickedResources([])"
          @update:modelValue="value => onPickResources(value as AdhocResourceOption[])"
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
          :filterTerm="datasourceText"
          :filterActive="Boolean(store.datasourceFilter.trim())"
          :options="store.datasourceOptions"
          :modelValue="store.reachablePickedDatasources"
          :setAsideCount="store.setAsideDatasources"
          :excludedCount="store.excludedDatasourceKeys.length"
          :keyOf="option => (option as AdhocDatasourceOption).key"
          :labelOf="option => (option as AdhocDatasourceOption).attribute"
          :descriptionOf="describeDatasource"
          @filter="onDatasourceFilter"
          @clear="store.setPickedDatasources([])"
          @update:modelValue="value => onPickDatasources(value as AdhocDatasourceOption[])"
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
          :canRefresh="store.effectiveNodes.length > 0"
          :hasData="Boolean(store.measurements)"
          :loading="store.queryLoading"
          :expanded="expanded"
          :viewOnly="viewOnly"
          :canAddToCollection="canAddToCollection"
          @update="patch => Object.assign(config, patch)"
          @updateTime="updateTime"
          @refresh="refresh"
          @clear="clearAll"
          @share="shareLink"
          @addToCollection="openAddToCollection"
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
    <AddToGraphCollectionDialog
      v-if="collectionGraph"
      :visible="addToCollectionOpen"
      :graph="collectionGraph"
      :details="collectionDetails"
      @update:visible="(value: boolean) => addToCollectionOpen = value"
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
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AdhocChart from './AdhocChart.vue'
import AdhocChartToolbar from './AdhocChartToolbar.vue'
import ExpressionEditor from './ExpressionEditor.vue'
import SelectionColumn from './SelectionColumn.vue'
import RrdDefinitionDialog from './RrdDefinitionDialog.vue'
import SeriesTable from './SeriesTable.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import AddToGraphCollectionDialog from '@/components/GraphCollections/AddToGraphCollectionDialog.vue'
import { adhocStateFromQuery } from '@/components/GraphCollections/adhocCollectionState'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'
import { downloadGraphCsv, exportGraphsToPdf } from '@/components/Resources/utils/graphExport'
import { copyToClipboard } from '@/composables/useClipboard'
import useSnackbar from '@/composables/useSnackbar'
import useRole from '@/composables/useRole'
import { MAX_GRAPH_NODES, useAdhocGraphStore } from '@/stores/adhocGraphStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { GraphCollectionGraph } from '@/types/graphCollection'
import {
  AdhocDatasourceOption,
  AdhocNodeOption,
  AdhocResourceOption
} from '@/types/adhocGraph'
import { useAdhocGraphDefinition } from './useAdhocGraphDefinition'
import { querySignature, toConvertedGraphData } from './utils/adhocQuery'
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

const menuStore = useMenuStore()
const route = useRoute()
const router = useRouter()
const store = useAdhocGraphStore()
const { showSnackBar } = useSnackbar()
const { readOnlyRole, rolesAreLoaded } = useRole()

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

/**
 * Set on teardown. The URL write and the query below are debounced, so both can
 * still be pending when the user navigates away — and a late `router.replace`
 * would stamp this page's query onto whatever route they moved to.
 */
let disposed = false

// The graph itself: config, edits, window and the series pipeline, shared with the
// graph collection cells that draw a saved ad-hoc graph.
const {
  config,
  time,
  tooManySeriesMessage,
  canQuery,
  updateSeries,
  addExpression,
  updateExpression,
  removeExpression,
  updateTime,
  runQuery,
  resetDefinition,
  linkState,
  restore
} = useAdhocGraphDefinition(store)

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

/** Resources the server would not give up, so the user knows the list is short. */
const resourceNote = computed<string>(() => (store.failedNodeLoads > 0 ?
  `Resources could not be loaded for ${store.failedNodeLoads} ${store.failedNodeLoads === 1 ? 'node' : 'nodes'}.` :
  ''))

/** A label search is paged; say so, or a rule that matches 2,000 nodes looks like it matched 100. */
const nodeNote = computed<string>(() => {
  if (store.nodeLimitExceeded) {
    return `${store.nodeLimitExceeded} nodes match; a graph can be built from at most ${MAX_GRAPH_NODES}.`
  }

  if (store.nodeMatchOverflow <= 0) {
    return ''
  }

  const shown = store.nodeMatches.length
  const total = (shown + store.nodeMatchOverflow)
  return `Showing the first ${shown} of ${total} nodes, for browsing only. Narrow the search, or use a filter rule, to graph them.`
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
/**
 * The text in each box, as typed. The store only learns a filter once typing has
 * settled, or the matches of the previous filter would count as "matching" the
 * half-typed one, and the resource and datasource products would be rebuilt on
 * every keystroke. The boxes follow the store the other way, for a restored link.
 */
const nodeText = ref('')
const resourceText = ref('')
const datasourceText = ref('')

watch(() => store.nodeFilter, value => nodeText.value = value)
watch(() => store.resourceFilter, value => resourceText.value = value)
watch(() => store.datasourceFilter, value => datasourceText.value = value)

/**
 * Bumped by Clear all. A debounced filter still pending when the user clears
 * would otherwise land afterwards and refill the picker with what they had typed.
 */
let filterGeneration = 0

const settled = (apply: (term: string) => unknown, ms: number) => {
  const debounced = useDebounceFn((generation: number, term: string) => {
    if (generation === filterGeneration) {
      apply(term)
    }
  }, ms)

  return (term: string) => debounced(filterGeneration, term)
}

const applyNodeFilter = settled(term => store.setNodeFilter(term), 500)
const applyResourceFilter = settled(term => store.setResourceFilter(term), 250)
const applyDatasourceFilter = settled(term => store.setDatasourceFilter(term), 250)

const onNodeFilter = (term: string) => {
  nodeText.value = term
  applyNodeFilter(term)
}

const onResourceFilter = (term: string) => {
  resourceText.value = term
  applyResourceFilter(term)
}

const onDatasourceFilter = (term: string) => {
  datasourceText.value = term
  applyDatasourceFilter(term)
}

/**
 * Selecting in a column that was following its filter turns that column into a
 * fixed list: what the filter matches later is not added. Say so once per
 * switch, because nothing on the page otherwise distinguishes the two states.
 */
const notePinned = (what: string, count: number) => {
  showSnackBar({
    msg: `${what}: ${count} selected. ${what} the filter matches from now on are not added; clear the selection to follow the filter again.`
  })
}

/**
 * A column shows only the selections its filters currently reach; the rest are
 * set aside, to come back when the filters reach them again. A click in the list
 * therefore reports only the visible part of the selection, and the hidden part
 * has to be kept by hand, or one click would silently delete it. Unchecking the
 * last visible item is still a click, not a clear: the column's Clear button
 * is a separate event for the whole selection.
 */
const withHidden = <T>(visible: T[], all: T[], reachable: T[], keyOf: (item: T) => string): T[] => {
  const reachableKeys = new Set(reachable.map(keyOf))
  return [...visible, ...all.filter(item => !reachableKeys.has(keyOf(item)))]
}

const onPickNodes = (nodes: AdhocNodeOption[]) => {
  if (!store.pickedNodes.length && nodes.length && store.nodeFilter.trim()) {
    notePinned('Nodes', nodes.length)
  }

  store.setPickedNodes(nodes)
}

const onPickResources = (visible: AdhocResourceOption[]) => {
  const resources = withHidden(visible, store.pickedResources, store.reachablePickedResources, resource => resource.id)

  if (!store.pickedResources.length && resources.length && store.resourceFilter.trim()) {
    notePinned('Resources', resources.length)
  }

  store.setPickedResources(resources)
}

const onPickDatasources = (visible: AdhocDatasourceOption[]) => {
  const datasources = withHidden(visible, store.pickedDatasources, store.reachablePickedDatasources, datasource => datasource.key)

  if (!store.pickedDatasources.length && datasources.length && store.datasourceFilter.trim()) {
    notePinned('Datasources', datasources.length)
  }

  store.setPickedDatasources(datasources)
}

/**
 * Taking one series out of a generated set excludes that one series: the graph
 * keeps following the filter, minus it, and the link carries one key rather
 * than the other 199. With picks in place it is simply one fewer pick, hidden
 * picks included. Removing the last series has to empty the graph, not fall
 * back to "everything matching", so the filter goes with it.
 */
const removeSeries = (key: string) => {
  if (store.pickedDatasources.length) {
    store.setPickedDatasources(store.pickedDatasources.filter(datasource => datasource.key !== key))
    return
  }

  if (store.effectiveDatasources.length <= 1) {
    // Emptying the graph on purpose: a filter still settling must not refill it.
    filterGeneration++
    datasourceText.value = ''
    store.setDatasourceFilter('')
    store.setExcludedDatasourceKeys([])
    return
  }

  store.setExcludedDatasourceKeys([...new Set([...store.excludedDatasourceKeys, key])])
}

/** Refresh means "as the server stands now": the resource lists too, not only the data. */
const refresh = async () => {
  await store.refreshResources()
  runQuery()
}

const clearAll = () => {
  filterGeneration++
  nodeText.value = ''
  resourceText.value = ''
  datasourceText.value = ''
  resetDefinition()
  store.clearAll()
  // Back to the browse page, not a blank column under an empty box.
  store.evaluateNodes()
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

// Graph collections take any logged-in user who is not read-only, as the server enforces.
const canAddToCollection = computed<boolean>(() => rolesAreLoaded.value && !readOnlyRole.value)
const addToCollectionOpen = ref(false)
const collectionGraph = ref<GraphCollectionGraph | null>(null)
const collectionDetails = computed(() => [
  { label: 'Graph', value: config.title || 'Custom performance graph' },
  { label: 'Series', value: String(config.series.length) },
  ...(config.expressions.length ? [{ label: 'Expressions', value: String(config.expressions.length) }] : [])
])

/**
 * Hand the graph to a collection as the link would carry it, minus the window: the
 * collection's timespan draws it, like every other graph there.
 */
const openAddToCollection = () => {
  const state = adhocStateFromQuery(encodeAdhocState(config, time, linkState()))

  if (state.length > MAX_QUERY_LENGTH) {
    showSnackBar({ msg: 'This graph has too many selections to keep in a collection.', error: true })
    return
  }

  collectionGraph.value = {
    kind: 'adhoc',
    title: config.title,
    resourceId: null,
    graphtype: null,
    timespan: '1_day',
    adhocState: state
  }
  addToCollectionOpen.value = true
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

// Only a change to what the server would return re-queries; style, color and
// title changes re-render from the data already in hand.
watch(() => querySignature(config, time), () => {
  syncUrl()
  debouncedQuery()
})

// Non-query config still belongs in the URL, and so does the picker itself.
watch(() => [config.title, config.verticalLabel, config.stacked, config.series, config.expressions], syncUrl, { deep: true })
watch(() => store.selectionState, syncUrl)

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

  // A link that carries expressions should show them; hiding them would make the
  // graph look like it came from its sources alone.
  expressionsCollapsed.value = restored.config.expressions.length === 0

  // The picker is evaluated as the server stands now and the series follow; then
  // decide what to say and query.
  await restore(restored)

  const shared = restored.link.seriesCount
  const now = store.effectiveDatasources.length

  // Only a real difference is worth a notice; a lookup that failed just now is
  // reported by the columns themselves.
  if (shared > 0 && now !== shared && !store.nodeFilterError && !store.failedNodeLoads) {
    showSnackBar({
      msg: `This link now matches ${now} series; it matched ${shared} when it was made.`
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
