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

<template>
  <div class="topology-page" :class="store.isEditMode ? 'is-edit' : 'is-view'">
    <div class="topology-toolbar">
      <div class="toolbar-start">
          <span class="topology-title">Topology (Preview)</span>
          <!-- View-source dimension (above Edit/View): Custom vs discovered.
               A compact menu button so the toolbar stays uncluttered and new
               providers slot in as submenus. Each choice navigates the route
               (/topology/:source), so every source stays bookmarkable. -->
          <OnmsButton
            variant="outlined"
            aria-haspopup="true"
            class="source-button"
            @click="sourceMenuRef?.toggle($event)"
          >
            <span>Source: {{ currentSourceShort }}</span>
            <OnmsIcon :icon="ExpandMore" />
          </OnmsButton>
          <OnmsTieredMenu ref="sourceMenuRef" :items="sourceMenuModel" />
          <!-- Custom-view management (hidden for read-only discovered sources). -->
          <template v-if="!isDiscovered">
            <OnmsSelect
              v-model="currentViewId"
              :options="store.catalog"
              option-label="name"
              option-value="id"
              placeholder="Select a view"
              class="view-chooser"
              aria-label="Choose a topology view"
            />
            <OnmsButton label="New" variant="outlined" @click="onNew" />
            <OnmsButton label="Save" :loading="store.isSaving" :disabled="!canSave" @click="onSave" />
            <OnmsButton
              label="Save As"
              variant="outlined"
              :disabled="store.isSaving"
              @click="onSaveAs"
            />
            <OnmsButton
              label="Rename"
              variant="outlined"
              :disabled="!store.currentView"
              @click="onRename"
            />
            <OnmsButton
              label="Delete"
              severity="danger"
              variant="outlined"
              :disabled="!canDelete"
              @click="onDelete"
            />
          </template>
          <template v-else>
            <!-- Variant picker: which representation of this discovered source
                 (Combined / a single protocol / OSPF-by-area). Bookmarkable
                 via ?variant=. -->
            <OnmsSelect
              v-if="variantOptions.length > 1"
              v-model="selectedVariant"
              :options="variantOptions"
              option-label="label"
              option-value="key"
              class="variant-chooser"
              aria-label="Topology representation"
            />
            <span class="discovered-hint">{{ discoveredHint }}</span>
          </template>
      </div>
      <div class="toolbar-controls">
          <!-- Discovered sources are read-only: no Edit mode. -->
          <OnmsSelectButton
            v-if="!isDiscovered"
            v-model="mode"
            :options="modeOptions"
            option-label="label"
            option-value="value"
            :allow-empty="false"
            aria-label="View or Edit mode"
            class="mode-select"
          />
          <!-- Discovered-view search, focus + Semantic Zoom Level. -->
          <template v-if="isDiscovered">
            <OnmsAutoComplete
              v-model="searchModel"
              :suggestions="searchSuggestions"
              option-label="label"
              :complete-on-focus="true"
              placeholder="Search nodes"
              class="topology-search"
              aria-label="Search nodes to focus"
              @complete="onSearchComplete"
              @option-select="onSearchSelect"
            />
            <OnmsButton
              v-if="!store.focusNodeId"
              label="Focus"
              variant="outlined"
              :disabled="!selectedNodeId"
              @click="focusOnSelection"
            />
            <span v-else class="szl-control">
              <OnmsButton
                label="−"
                variant="outlined"
                :disabled="store.semanticZoomLevel <= 0"
                aria-label="Decrease zoom level"
                @click="stepSzl(-1)"
              />
              <span class="szl-value">{{ store.semanticZoomLevel }} hop{{ store.semanticZoomLevel === 1 ? '' : 's' }}</span>
              <OnmsButton
                label="+"
                variant="outlined"
                aria-label="Increase zoom level"
                @click="stepSzl(1)"
              />
              <OnmsButton label="Show all" variant="outlined" @click="showAll" />
            </span>
          </template>
          <OnmsButton
            label="Refresh status"
            variant="outlined"
            @click="store.refreshStatus()"
          />
          <OnmsButton
            v-if="store.isEditMode"
            :label="store.isLinkDrawMode ? 'Link: ON' : 'Draw Link'"
            :variant="store.isLinkDrawMode ? 'filled' : 'outlined'"
            @click="store.setLinkDrawMode(!store.isLinkDrawMode)"
          />
          <OnmsButton
            v-if="store.isEditMode"
            :label="store.isShapeDrawMode ? 'Box: drag to draw' : 'Draw Box'"
            :variant="store.isShapeDrawMode ? 'filled' : 'outlined'"
            @click="store.setShapeDrawMode(!store.isShapeDrawMode)"
          />
          <OnmsButton
            v-if="store.isEditMode"
            :label="store.isLinkHintsEnabled ? 'Link Hints: ON' : 'Link Hints'"
            :variant="store.isLinkHintsEnabled ? 'filled' : 'outlined'"
            title="Show discovered adjacencies between placed nodes as ghost links"
            @click="store.setLinkHintsEnabled(!store.isLinkHintsEnabled)"
          />
          <span class="node-size-control" title="Node size">
            <span class="node-size-dot node-size-dot-sm" />
            <OnmsSlider
              v-model="nodeSizeModel"
              :min="store.NODE_SIZE_MIN"
              :max="store.NODE_SIZE_MAX"
              class="node-size-slider"
              aria-label="Node size"
            />
            <span class="node-size-dot node-size-dot-lg" />
          </span>
          <OnmsButton label="Fit" variant="outlined" @click="canvasRef?.fit()" />
          <OnmsButton label="Export PNG" variant="outlined" @click="onExport" />
      </div>
    </div>

    <div class="topology-body">
      <!-- Palette is an Edit-mode tool (compose); hidden in View and for
           read-only discovered sources. -->
      <TopologyPalette v-if="store.isEditMode && !isDiscovered" class="topology-palette-pane" />
      <div class="topology-canvas-wrap">
        <TopologyCanvas
          ref="canvasRef"
          class="topology-canvas-pane"
          @node-contextmenu="onNodeContextMenu"
        />
        <!-- Many discovered sources (OSPF, IS-IS, Bridge, …) have no links
             unless that protocol was discovered; explain the empty canvas. -->
        <div v-if="discoveredEmpty" class="discovered-empty">
          <p>No discovered topology for <strong>{{ store.discoveredGraph?.label }}</strong>.</p>
          <p class="discovered-empty-hint">Nothing was found from current discovery data for this source.</p>
        </div>
        <!-- Large graphs are gated instead of rendered: the layout + first
             render are client-side and grow with node count, so past the
             threshold the user picks a focus (small, fast) or opts in. -->
        <div v-if="isLargeGraphGated" class="large-graph-gate">
          <p>
            <strong>{{ store.discoveredGraph?.label }}</strong> has
            {{ discoveredNodeCount.toLocaleString() }} nodes and
            {{ (store.discoveredGraph?.links.length ?? 0).toLocaleString() }} links.
          </p>
          <p class="discovered-empty-hint">
            Laying out a graph this large can take a long time in the browser.
            Use <em>Search nodes</em> above to focus on a node's neighborhood instead.
          </p>
          <OnmsButton
            :label="`Render all ${discoveredNodeCount.toLocaleString()} nodes`"
            variant="outlined"
            @click="renderAllAnyway"
          />
        </div>
      </div>
      <!-- View: full read-only Inspector on the left (order -1).
           Edit: slim Properties panel on the right, only when a label/edge
           is selected (nodes have no editable props here). -->
      <!-- Always rendered (in both modes) so selecting an edge/label doesn't
           reflow the canvas -- a reflow shifts the view and staled sigma's
           hit-detection, which broke selecting a second edge. -->
      <TopologyInspector
        :canvas="canvasRef"
        :variant="store.isEditMode ? 'props' : 'full'"
        class="topology-inspector-pane"
        :style="{ order: store.isEditMode ? 0 : -1 }"
      />
    </div>

    <!-- Bottom Explore panel: tables for the view, tied to selection. -->
    <TopologyExplorePanel @select="onExploreSelect" />

    <OnmsContextMenu ref="nodeMenuRef" :items="nodeMenuItems" />
    <OnmsConfirmationDialog
      :visible="deleteDialogVisible"
      title="Delete view"
      :action-button-text="'Delete'"
      cancel-button-text="Cancel"
      @ok="confirmDelete"
      @cancel="cancelDelete"
    >
      <template #content>
        Delete view "{{ pendingDelete?.name }}"? This cannot be undone.
      </template>
    </OnmsConfirmationDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {
  OnmsAutoComplete,
  OnmsButton,
  OnmsConfirmationDialog,
  OnmsContextMenu,
  OnmsIcon,
  OnmsSelect,
  OnmsSelectButton,
  OnmsSlider,
  OnmsTieredMenu,
  useOnmsToast
} from '@opennms/onms-ui'
import type { OnmsMenuItem } from '@opennms/onms-ui'
import type { SourceGroup } from '@/components/Topology/sources'
import ExpandMore from '@opennms/onms-ui/icons/navigation/ExpandMore.vue'
import { useRoute, useRouter } from 'vue-router'
import TopologyCanvas from '@/components/Topology/TopologyCanvas.vue'
import TopologyPalette from '@/components/Topology/TopologyPalette.vue'
import TopologyInspector from '@/components/Topology/TopologyInspector.vue'
import TopologyExplorePanel from '@/components/Topology/TopologyExplorePanel.vue'
import { useTopologyStore } from '@/stores/topologyStore'
import {
  CUSTOM_SOURCE_SLUG,
  isDiscoveredSlug,
  sourceForSlug,
  variantForKey,
  graphSourceFor
} from '@/components/Topology/sources'
import { focusSubgraph } from '@/components/Topology/focus'
import { nodeActionLinks } from '@/components/Topology/nodeActions'
import type { CanvasNode } from '@/types/topology'

const store = useTopologyStore()
const { showToast } = useOnmsToast()
const route = useRoute()
const router = useRouter()

const canvasRef = ref<InstanceType<typeof TopologyCanvas> | null>(null)

// View-source dimension (the route's :source param). 'custom' is the
// hand-composed catalog; the rest are discovered (read-only) topologies.
const sourceSlug = computed<string>(() => (route.params.source as string) || CUSTOM_SOURCE_SLUG)
const isDiscovered = computed<boolean>(() => isDiscoveredSlug(store.topologySources, sourceSlug.value))

const currentSource = computed(() => sourceForSlug(store.topologySources, sourceSlug.value))

// Navigate to a source via the route so every source stays bookmarkable.
// Dropping the query resets the variant to the group's default.
const goToSource = (slug: string) => {
  if (slug !== sourceSlug.value) {
    router.push({ name: 'Topology', params: { source: slug }})
  }
}

// Compact label for the source button.
const currentSourceShort = computed<string>(() => currentSource.value?.label ?? 'Custom')

// --- Discovered-source variant (representation) ----------------------------
// The variant (Combined / a single protocol / OSPF-by-area …) is a bookmarkable
// `?variant=<key>` query; absent => the group's default (variants[0]).
const variantKey = computed<string | undefined>(() => {
  const v = route.query.variant
  return typeof v === 'string' ? v : undefined
})
const variantOptions = computed(() => currentSource.value?.variants ?? [])
const selectedVariant = computed<string>({
  get: () => variantForKey(currentSource.value, variantKey.value)?.key ?? '',
  set: (key) => {
    const variants = currentSource.value?.variants
    if (!variants) {
      return
    }
    // Clean URL for the default variant; explicit ?variant otherwise.
    const query = key === variants[0].key ? {} : { variant: key }
    router.push({ name: 'Topology', params: { source: sourceSlug.value }, query })
  }
})

// Grouped source menu: Custom as a leaf, discovered sources under a submenu
// (new providers slot in as further submenus). Each command navigates the
// route. The active source is marked.
const sourceMenuRef = ref<{ toggle: (event: Event) => void } | null>(null)
const sourceMenuModel = computed<OnmsMenuItem[]>(() => {
  const item = (slug: string, label: string): OnmsMenuItem => ({
    label,
    class: slug === sourceSlug.value ? 'source-item-active' : undefined,
    command: () => goToSource(slug)
  })
  // Headings split on how the topology came to exist. A heading with nothing
  // under it is dropped rather than rendered empty, since the container list is
  // whatever the server happens to serve.
  const group = (g: SourceGroup) => store.topologySources
    .filter(s => s.group === g)
    .map(s => item(s.slug, s.label))
  const heading = (label: string, items: OnmsMenuItem[]) =>
    items.length > 0 ? [{ label, items }] : []
  return [
    item(CUSTOM_SOURCE_SLUG, 'Custom Topologies'),
    ...heading('Discovered Topologies', group('discovered')),
    ...heading('Derived Topologies', group('derived'))
  ]
})

const discoveredHint = computed<string>(() => {
  if (store.isDiscoveredLoading) {
    return 'Loading…'
  }
  if (store.discoveredError) {
    return 'Load failed'
  }
  return 'read-only'
})

// A discovered source that loaded successfully but has no vertices.
const discoveredEmpty = computed<boolean>(
  () =>
    isDiscovered.value &&
    !store.isDiscoveredLoading &&
    !store.discoveredError &&
    !!store.discoveredGraph &&
    store.discoveredGraph.nodes.length === 0
)

// --- Large-graph gate -------------------------------------------------------
// Fetching a big discovered graph is cheap (the server serves a cached,
// pre-built graph; ~0.8 MB per 2000 nodes); LAYING IT OUT and rendering it is
// the expensive, client-side part. Above this node count we hold the render
// and let the user either focus on a neighborhood (rendering just the focus
// subgraph, which is fast at any inventory size) or explicitly opt in.
const LARGE_GRAPH_THRESHOLD = 3000
// Per-load opt-in ("Render all N nodes"); reset whenever a source/variant loads.
const renderAllAccepted = ref(false)
const discoveredNodeCount = computed<number>(() => store.discoveredGraph?.nodes.length ?? 0)
const isLargeGraphGated = computed<boolean>(
  () =>
    !!store.discoveredGraph &&
    discoveredNodeCount.value > LARGE_GRAPH_THRESHOLD &&
    !store.focusNodeId &&
    !renderAllAccepted.value
)
const renderAllAnyway = () => {
  renderAllAccepted.value = true
  renderDiscovered()
}

// Right-click a node -> context menu of node-data cross-links (Node Details,
// Resource Graphs, Events, Alarms), plus "Set as focus point" in discovered
// views. Built per-click for the targeted node.
const nodeMenuRef = ref<{ show: (event: Event) => void } | null>(null)
const nodeMenuItems = ref<OnmsMenuItem[]>([])

const onNodeContextMenu = (payload: { event: MouseEvent; nodeId: number | null; nodeKey: string }) => {
  const { event, nodeId, nodeKey } = payload
  const items: OnmsMenuItem[] = []
  if (nodeId != null) {
    for (const link of nodeActionLinks(nodeId)) {
      items.push({ label: link.label, command: () => window.open(link.url, '_blank', 'noopener') })
    }
  }
  if (isDiscovered.value) {
    if (items.length) {
      items.push({ separator: true })
    }
    items.push({ label: 'Set as focus point', command: () => store.setFocusNode(nodeKey) })
  }
  if (items.length === 0) {
    return
  }
  nodeMenuItems.value = items
  nodeMenuRef.value?.show(event)
}

// Segmented View/Edit control (clear, always-visible mode indicator).
const modeOptions = [
  { label: 'View', value: false },
  { label: 'Edit', value: true }
]
const mode = computed<boolean>({
  get: () => store.isEditMode,
  set: value => store.setEditMode(value)
})

// Load whatever the route's :source points at -- the custom catalog or a
// discovered topology. Re-runs whenever the source changes.
const loadSource = async (): Promise<void> => {
  const option = sourceForSlug(store.topologySources, sourceSlug.value)
  if (!option) {
    // Unknown source -> fall back to custom.
    router.replace({ name: 'Topology', params: { source: CUSTOM_SOURCE_SLUG }})
    return
  }
  if (option.kind === 'discovered') {
    // Discovered topologies are read-only; force View mode and load the graph
    // for the selected variant (or the group's default).
    store.setEditMode(false)
    const gs = graphSourceFor(option, variantKey.value)
    const graph = gs ? await store.loadDiscoveredSource(gs) : false
    if (graph && store.discoveredGraph) {
      renderAllAccepted.value = false // each load re-arms the large-graph gate
      applyRouteFocus() // restore focus/SZL from the URL before the first render
      renderDiscovered()
      store.refreshStatus()
    } else {
      showToast({
        message: `Could not load ${option.label}.`,
        severity: 'error',
        timeout: 5000
      })
    }
    return
  }
  // Custom source: clear any discovered graph, load the catalog + the ?view=.
  // force=true so the custom view re-renders even when currentView already names
  // it (the canvas was showing a discovered graph until now).
  store.clearDiscovered()
  await store.refreshCatalog()
  await loadFromRoute(true)
}

onMounted(async () => {
  await store.loadGraphContainers()
  await loadSource()
})

// React to source or variant changes (selector, deep link, back/forward).
// One watcher over both so a group switch (which changes the slug and clears
// the variant in the same tick) reloads only once. loadSource handles the
// custom vs discovered branch; ?view= changes on custom are handled separately.
watch([sourceSlug, variantKey], () => loadSource())

// --- Discovered-view focus + Semantic Zoom Level ---------------------------

// The single selected node (the Focus action's target), or null.
const selectedNodeId = computed<string | null>(() =>
  store.selectedIds.length === 1 ? store.selectedIds[0] : null
)

// Render the discovered graph, reduced to the focus node + SZL hops when a
// focus is set (else the whole graph). Re-runs the auto-layout each time.
const renderDiscovered = () => {
  if (!store.discoveredGraph) {
    return
  }
  // Gated: clear the canvas (a previous variant may still be mounted) and let
  // the overlay take over. Focusing or "Render all" re-enters below.
  if (isLargeGraphGated.value) {
    canvasRef.value?.loadDiscoveredGraph({ ...store.discoveredGraph, nodes: [], links: [] })
    return
  }
  const graph = focusSubgraph(store.discoveredGraph, store.focusNodeId, store.semanticZoomLevel)
  canvasRef.value?.loadDiscoveredGraph(graph)
}

// Focus + SZL live in the URL (?focus=<nodeId>&szl=<hops>) so a focused
// discovered view is shareable/bookmarkable, like ?view= and ?variant=. The
// URL is the source of truth: the controls navigate, a watcher mirrors the
// query into the store, and the store drives the render below.
const SZL_DEFAULT = 2 // matches the store's initial semanticZoomLevel
const routeFocus = computed<string | null>(() => {
  const f = route.query.focus
  return typeof f === 'string' && f.length ? f : null
})
const routeSzl = computed<number | null>(() => {
  const s = route.query.szl
  if (typeof s !== 'string') {
    return null
  }
  const n = Number(s)
  return Number.isFinite(n) ? n : null
})

// Mirror the URL focus/SZL into the store. Idempotent: the store setters no-op
// on an unchanged value, so this can't loop with the navigation below.
const applyRouteFocus = () => {
  store.setFocusNode(routeFocus.value)
  store.setSemanticZoomLevel(routeSzl.value ?? SZL_DEFAULT)
}

// Navigate focus/SZL into the URL. szl only travels alongside a focus (it has
// no effect without one). replace (not push) keeps back/forward uncluttered.
const navFocus = (focus: string | null, szl: number) => {
  const query: Record<string, string> = { ...(route.query as Record<string, string>) }
  if (focus) {
    query.focus = focus
    query.szl = String(szl)
  } else {
    delete query.focus
    delete query.szl
  }
  router.replace({ name: 'Topology', params: { source: sourceSlug.value }, query })
}

const focusOnSelection = () => {
  if (selectedNodeId.value) {
    navFocus(selectedNodeId.value, store.semanticZoomLevel)
  }
}
const showAll = () => navFocus(null, store.semanticZoomLevel)

// Export the current map as a PNG. The file name reflects the open view
// (custom) or the source/variant (discovered); the canvas appends ".png".
// Node-size slider <-> store (clamped in the store setter).
const nodeSizeModel = computed<number>({
  get: () => store.nodeSize,
  set: n => store.setNodeSize(n)
})

// Explore-panel row -> select that node on the canvas (or clear to "show all").
const onExploreSelect = (placedId: string | null) => {
  if (placedId) {
    store.selectOnly(placedId)
  } else {
    store.clearSelection()
  }
}

const onExport = () => {
  const base = isDiscovered.value
    ? `topology-${sourceSlug.value}${variantKey.value ? '-' + variantKey.value : ''}`
    : `topology-${store.currentView?.name ?? 'view'}`
  void canvasRef.value?.exportImage(base.replace(/[^\w.-]+/g, '-'))
}
const stepSzl = (delta: number) =>
  navFocus(store.focusNodeId, Math.max(0, Math.min(10, store.semanticZoomLevel + delta)))

// --- Search -> focus -------------------------------------------------------
// Find a node by label or node id in a large discovered graph and focus it.
// Purely client-side over the already-loaded full graph (store.discoveredGraph
// is the whole graph; the focus reduction happens only at render). Selecting a
// result focuses it via the same URL navigation, so the focused view stays
// shareable.
const SEARCH_LIMIT = 12
const searchModel = ref<CanvasNode | string>('')
const searchSuggestions = ref<CanvasNode[]>([])

const onSearchComplete = (query: string) => {
  const nodes = store.discoveredGraph?.nodes ?? []
  const q = query.trim().toLowerCase()
  const matches = q
    ? nodes.filter(
      n => n.label.toLowerCase().includes(q) || String(n.nodeId ?? '').includes(q)
    )
    : nodes
  searchSuggestions.value = matches.slice(0, SEARCH_LIMIT)
}

// The seam types the selected option as unknown (it has no view of the
// suggestion shape); these come straight from searchSuggestions.
const onSearchSelect = (selected: unknown) => {
  const node = selected as CanvasNode | undefined
  if (node?.id) {
    navFocus(node.id, store.semanticZoomLevel)
  }
  searchModel.value = '' // clear the field; the focus chip/SZL control reflects the state
}

// URL focus/SZL changed (a control click, a deep link, or back/forward) -> store.
watch([routeFocus, routeSzl], () => {
  if (isDiscovered.value) {
    applyRouteFocus()
  }
})

// Re-render the focused subgraph when focus or the zoom level changes.
watch(
  () => [store.focusNodeId, store.semanticZoomLevel],
  () => {
    if (isDiscovered.value) {
      renderDiscovered()
    }
  }
)

// React to ?view= changes -- custom source only (discovered has no views).
// The loadFromRoute guard makes our own syncRouteToView writes no-ops here.
watch(
  () => route.query.view,
  () => {
    if (!isDiscovered.value) {
      loadFromRoute()
    }
  }
)

// Status auto-refresh: poll in View mode, frozen in Edit mode (so the
// canvas doesn't repaint while arranging). The manual "Refresh status"
// button works in either mode.
const STATUS_INTERVAL_MS = 30000
let statusTimer: ReturnType<typeof setInterval> | null = null

const stopPolling = () => {
  if (statusTimer !== null) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}

watch(
  () => store.isEditMode,
  (editMode) => {
    stopPolling()
    if (!editMode) {
      // Entering View mode: drop any in-flight edge-draw (an Edit-only tool),
      // then refresh status now and on an interval.
      store.setLinkDrawMode(false)
      store.refreshStatus()
      statusTimer = setInterval(() => store.refreshStatus(), STATUS_INTERVAL_MS)
    }
  },
  { immediate: true }
)

onBeforeUnmount(stopPolling)

const canSave = computed<boolean>(
  () => !!store.currentView && !store.isSaving && (store.currentView?.name?.trim().length ?? 0) > 0
)

// Delete acts on a saved view, but never the seeded 'Default' baseline.
const canDelete = computed<boolean>(
  () => !!store.currentView?.id && store.currentView?.name !== 'Default'
)

// The chooser's selection mirrors the open view; picking another loads it.
const currentViewId = computed<string | null>({
  get: () => store.currentView?.id ?? null,
  set: (id) => {
    if (id && id !== store.currentView?.id) {
      openIntoCanvas(id)
    }
  }
})

const saveCurrent = async (): Promise<boolean> => {
  const snapshot = canvasRef.value?.serialize()
  if (!snapshot) {
    return false
  }
  const ok = await store.saveCurrentView(snapshot)
  showToast(
    ok
      ? { message: `View "${store.currentView?.name}" saved`, severity: 'success', timeout: 3000 }
      : { message: 'Could not save the view.', severity: 'error', timeout: 5000 }
  )
  return ok
}

// Reflect the open view into the URL as ?view=<name> (bookmarkable). Guarded
// so it only writes when the value actually changes.
const syncRouteToView = () => {
  const name = store.currentView?.name
  const source = (route.params.source as string) || 'custom'
  if (name && route.query.view !== name) {
    router.replace({ name: 'Topology', params: { source }, query: { ...route.query, view: name }})
  }
}

// Load whatever ?view= names (or Default). The "already showing it" short-circuit
// avoids reloading on our own syncRouteToView writes -- but it must be bypassed
// when arriving from a discovered source, where currentView still names the last
// custom view even though the canvas is showing the discovered graph. Callers on
// a source switch pass force=true so the custom view actually re-renders.
const loadFromRoute = async (force = false): Promise<void> => {
  const wanted = (route.query.view as string) || 'Default'
  if (!force && store.currentView?.id && store.currentView.name === wanted) {
    return
  }
  const match = store.catalog.find(v => v.name === wanted)
  if (match) {
    await openIntoCanvas(match.id)
  } else {
    if (wanted !== 'Default') {
      showToast({ message: `View "${wanted}" not found`, severity: 'warn', timeout: 4000 })
    }
    await loadDefault()
  }
}

// Load a saved view by id into the canvas. No toast (used by the chooser,
// the initial route load, and after a delete).
const openIntoCanvas = async (id: string): Promise<boolean> => {
  const view = await store.openView(id)
  if (!view) {
    showToast({ message: 'Could not load the view.', severity: 'error', timeout: 5000 })
    return false
  }
  canvasRef.value?.loadView(view)
  syncRouteToView()
  return true
}

// Land on the seeded 'Default' view if present, else a blank canvas.
const loadDefault = async (): Promise<void> => {
  const def = store.catalog.find(v => v.name === 'Default')
  if (def) {
    await openIntoCanvas(def.id)
  } else {
    store.newView()
    if (store.currentView) {
      canvasRef.value?.loadView(store.currentView)
    }
    syncRouteToView()
  }
}

const onSave = () => saveCurrent()

// View names are unique in the catalog. Catch a collision up front so New /
// Save As give a clear message instead of a doomed request -- and, for Save
// As, so we never mutate the open view before a save that will fail.
const nameInUse = (name: string): boolean => store.catalog.some(v => v.name === name)

const warnNameInUse = (name: string) =>
  showToast({
    message: `A view named "${name}" already exists. Choose a different name.`,
    severity: 'warn',
    timeout: 5000
  })

const onNew = async () => {
  const name = window.prompt('Name the new view:', '')
  if (!name || !name.trim()) {
    return
  }
  const trimmed = name.trim()
  if (nameInUse(trimmed)) {
    warnNameInUse(trimmed)
    return
  }
  store.newView()
  store.renameCurrent(trimmed)
  store.setEditMode(true)
  if (store.currentView) {
    canvasRef.value?.loadView(store.currentView)
  }
  await saveCurrent()
  syncRouteToView()
}

const onSaveAs = async () => {
  if (!store.currentView) {
    return
  }
  const name = window.prompt('Save view as:', store.currentView.name)
  if (!name || !name.trim()) {
    return
  }
  const trimmed = name.trim()
  // Up-front collision check: Save As must create a new entry, so an existing
  // name (including the current view's own) is always a conflict.
  if (nameInUse(trimmed)) {
    warnNameInUse(trimmed)
    return
  }
  const snapshot = canvasRef.value?.serialize()
  if (!snapshot) {
    return
  }
  // Non-destructive: the open view is replaced only if the save succeeds.
  const ok = await store.saveCurrentViewAs(trimmed, snapshot)
  showToast(
    ok
      ? { message: `View "${trimmed}" saved`, severity: 'success', timeout: 3000 }
      : { message: 'Could not save the view; the name may already be in use.', severity: 'error', timeout: 5000 }
  )
  if (ok) {
    syncRouteToView()
  }
}

const onRename = async () => {
  const cur = store.currentView
  if (!cur) {
    return
  }
  const name = window.prompt('Rename view:', cur.name)
  if (!name || !name.trim() || name.trim() === cur.name) {
    return
  }
  if (cur.id) {
    const ok = await store.renameView(cur.id, name.trim())
    showToast(
      ok
        ? { message: `View renamed to "${name.trim()}"`, severity: 'success', timeout: 3000 }
        : { message: `Could not rename view "${cur.name}"`, severity: 'error', timeout: 5000 }
    )
  } else {
    // Unsaved view: just set the name locally (persisted on the next save).
    store.renameCurrent(name.trim())
  }
  syncRouteToView()
}

// Delete confirmation is a rendered dialog rather than an imperative service
// call, so the view being deleted is held here between opening the dialog and
// the user answering it.
const deleteDialogVisible = ref(false)
const pendingDelete = ref<{ id: string, name: string } | null>(null)

const onDelete = () => {
  const cur = store.currentView
  if (!cur?.id) {
    return
  }
  pendingDelete.value = { id: cur.id, name: cur.name }
  deleteDialogVisible.value = true
}

const cancelDelete = () => {
  deleteDialogVisible.value = false
  pendingDelete.value = null
}

const confirmDelete = async () => {
  const target = pendingDelete.value
  deleteDialogVisible.value = false
  pendingDelete.value = null
  if (!target) {
    return
  }
  const ok = await store.removeView(target.id)
  showToast(
    ok
      ? { message: `View "${target.name}" deleted`, severity: 'success', timeout: 3000 }
      : { message: `Could not delete view "${target.name}"`, severity: 'error', timeout: 5000 }
  )
  if (ok) {
    await loadDefault()
  }
}
</script>

<style scoped>
.topology-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem;
  /* Fill the layout's main area (header + footer overhead ~104px). A residual
     ~16px page scrollbar comes from the app shell (side-menu rail / footer
     spacer), independent of this page -- tracked separately. */
  height: calc(100vh - 104px);
}

.topology-toolbar {
  flex: 0 0 auto;
  /* Layout the PrimeVue Toolbar used to supply: its start and end groups sit
     at either end of a wrapping row. */
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: var(--p-content-background);
  border: 1px solid var(--p-content-border-color);
  border-radius: var(--p-content-border-radius);
  /* Ambient mode cue: a colored top accent reinforces the segmented
     View/Edit control so the current context is obvious at a glance. */
  border-top: 3px solid transparent;
}

.topology-page.is-edit .topology-toolbar {
  border-top-color: #f59e0b; /* amber = editing */
}

.topology-page.is-view .topology-toolbar {
  border-top-color: #00bfcb; /* teal accent = viewing */
}

.topology-title {
  font-size: 1.25rem;
  font-weight: 600;
}

.toolbar-start {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
}

.source-button {
  white-space: nowrap;
}

.source-button :deep(.p-button-label) {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.variant-chooser {
  min-width: 12rem;
}

/* Mark the active source in the menu. */
:deep(.source-item-active) > .p-tieredmenu-item-link,
:deep(.source-item-active) > .p-menuitem-link {
  font-weight: 700;
}

.view-chooser {
  min-width: 12rem;
}

.topology-search :deep(.p-autocomplete-input) {
  min-width: 11rem;
}

.node-size-control {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: var(--onms-secondary-text-on-surface);
}
.node-size-slider {
  width: 6rem;
}
.node-size-dot {
  border-radius: 50%;
  background: currentColor;
  flex: 0 0 auto;
}
.node-size-dot-sm {
  width: 0.4rem;
  height: 0.4rem;
}
.node-size-dot-lg {
  width: 0.7rem;
  height: 0.7rem;
}

.discovered-hint {
  font-size: 0.85rem;
  font-style: italic;
  color: var(--onms-secondary-text-on-surface);
}

.toolbar-controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.75rem;
}

.szl-control {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.szl-value {
  min-width: 3.5rem;
  text-align: center;
  font-variant-numeric: tabular-nums;
}

.topology-body {
  flex: 1 1 auto;
  display: flex;
  gap: 0.75rem;
  min-height: 400px;
  min-height: 0;
}

.topology-palette-pane {
  flex: 0 0 auto;
}

/* Wraps the canvas so the discovered empty-state can overlay it. */
.topology-canvas-wrap {
  flex: 1 1 auto;
  min-width: 0;
  position: relative;
  display: flex;
}

.topology-canvas-pane {
  flex: 1 1 auto;
  min-width: 0;
}

.discovered-empty {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  pointer-events: none;
  text-align: center;
  color: var(--onms-secondary-text-on-surface);
}

.discovered-empty-hint {
  font-size: 0.85rem;
}

.large-graph-gate {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  text-align: center;
  color: var(--onms-secondary-text-on-surface);
  background: var(--onms-surface);
}

.topology-inspector-pane {
  flex: 0 0 auto;
}
</style>
