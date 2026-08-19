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

import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'
import type {
  CanvasLabel,
  CanvasShape,
  DiscoveredNeighbor,
  DiscoveredGraph,
  DiscoveredGraphSource,
  TopologyView,
  TopologyViewBackground,
  TopologyViewStyle,
  TopologyViewSummary
} from '@/types/topology'
import {
  listViews,
  getView,
  saveView,
  deleteView,
  getNodeSeverities,
  getNodeIconIds,
  getNodeCategories,
  getNodeNeighbors,
  loadDiscoveredGraph,
  listGraphContainers
} from '@/services/topologyService'
import type { GraphContainerMeta } from '@/services/topologyService'
import { buildSources, type TopologySourceOption } from '@/components/Topology/sources'
import { focusSubgraph } from '@/components/Topology/focus'
import type { DeviceIconId } from '@/components/Topology/deviceIcons'

/**
 * The live canvas geometry the canvas component hands back on save:
 * nodes/edges from the graphology graph plus the sigma camera viewport.
 * Labels are not included here -- they already live in the store.
 */
type CanvasSnapshot = Pick<TopologyView, 'nodes' | 'links' | 'viewport'>

const emptyView = (): TopologyView => ({
  name: 'Untitled view',
  nodes: [],
  links: [],
  labels: [],
  viewport: { zoom: 1, panX: 0, panY: 0 }
})

/**
 * State for the custom topology canvas. Holds the catalog of saved
 * views plus the currently-open view, selection state, and the
 * Edit/View mode flag. Service-backed actions (list/load/save/delete)
 * are wired in subsequent steps once the persistence REST resource
 * is in place.
 */
export const useTopologyStore = defineStore('topologyStore', () => {
  const catalog = ref<TopologyViewSummary[]>([])
  const currentView = ref<TopologyView | null>(null)
  // Launch in View mode; the user explicitly switches to Edit to compose.
  const isEditMode = ref<boolean>(false)
  /**
   * Edge-draw mode: when true, clicks on nodes capture source/target
   * instead of selecting. Driven by the toolbar Draw Edge toggle.
   */
  const isLinkDrawMode = ref<boolean>(false)
  const selectedIds = ref<string[]>([])
  /**
   * Palette node ids currently placed on the canvas. The palette uses
   * this to hide already-placed entries; the canvas writes it on
   * drop/delete. Reactive Set: any mutation reassigns the ref.
   */
  const placedNodeIds = ref<Set<string>>(new Set())
  /**
   * Free-standing text annotations on the canvas. Lives outside the
   * graphology graph so it doesn't interact with sigma node/edge
   * concepts. Persisted as part of TopologyView when save lands.
   */
  const labels = ref<CanvasLabel[]>([])

  /**
   * Decorative annotation shapes (frames/boxes), store-owned like labels --
   * they live outside the graphology graph and are merged into the view at
   * save time. `isShapeDrawMode` is the toolbar Draw Box gesture: drag on
   * the stage to create a shape.
   */
  const shapes = ref<CanvasShape[]>([])
  const isShapeDrawMode = ref<boolean>(false)

  /**
   * Discovered adjacencies per real node id, fetched from /api/v2/enlinkd
   * for the placed nodes (Phase 2 assisted composition: ghost links + the
   * neighbor tray). Adjacency is a property of the network, not of a view,
   * so the cache survives view switches; ids fetch once per session.
   */
  const neighborsByNode = ref<Record<number, DiscoveredNeighbor[]>>({})
  const isLinkHintsEnabled = ref<boolean>(true)

  const setLinkHintsEnabled = (on: boolean) => {
    isLinkHintsEnabled.value = on
  }

  /**
   * Discovered adjacency for one node, cached. Used by the Inspector to
   * resolve protocol/port detail for a selected link that carries no
   * binding (links in discovered views, hand-drawn links).
   */
  const getNeighborsFor = async (nodeId: number): Promise<DiscoveredNeighbor[]> => {
    if (nodeId in neighborsByNode.value) {
      return neighborsByNode.value[nodeId]
    }
    const fetched = await getNodeNeighbors(nodeId)
    // An empty result is indistinguishable from a failed request, and caching it
    // hid a node's links for the rest of the session after one transient error.
    // Nodes genuinely without neighbours cost a repeat request; that is cheaper
    // than being wrong until reload.
    if (fetched.length > 0) {
      neighborsByNode.value = { ...neighborsByNode.value, [nodeId]: fetched }
    }
    return fetched
  }

  const refreshNeighbors = async (): Promise<void> => {
    const wanted = Array.from(placedNodeIds.value)
      .map(id => Number(id))
      .filter(n => Number.isInteger(n) && !(n in neighborsByNode.value))
    if (wanted.length === 0) {
      return
    }
    const fetched = await Promise.all(wanted.map(id => getNodeNeighbors(id)))
    const next = { ...neighborsByNode.value }
    wanted.forEach((id, i) => {
      next[id] = fetched[i]
    })
    neighborsByNode.value = next
  }

  const setShapeDrawMode = (value: boolean) => {
    isShapeDrawMode.value = value
  }

  /**
   * The open view's background image, if any. Lives directly on currentView
   * (saveCurrentView spreads currentView, so it persists with no extra
   * plumbing); this computed + setter pair is the canvas/Inspector surface.
   */
  const background = computed<TopologyViewBackground | undefined>(
    () => currentView.value?.background
  )

  const setBackground = (bg: TopologyViewBackground | undefined) => {
    if (!currentView.value) {
      return
    }
    currentView.value.background = bg
    if (!bg) {
      isBackgroundAdjustMode.value = false
    }
  }

  /**
   * While true (Edit mode only), the background image accepts mouse input on
   * the canvas: drag to move, corner handle to resize. Off by default so the
   * image never swallows node/stage clicks during normal composing.
   */
  const isBackgroundAdjustMode = ref<boolean>(false)

  const setBackgroundAdjustMode = (on: boolean) => {
    isBackgroundAdjustMode.value = on
  }

  /**
   * Whether the canvas stats overlay (Nodes/Links/Labels/Selected) shows.
   * A user preference, not view content -- kept in localStorage, never saved
   * with a view.
   */
  const STATS_PREF_KEY = 'opennms.topology.showCanvasStats'
  const readStatsPref = (): boolean => {
    try {
      return localStorage.getItem(STATS_PREF_KEY) !== 'false'
    } catch {
      return true
    }
  }
  const showCanvasStats = ref<boolean>(readStatsPref())

  const setShowCanvasStats = (on: boolean) => {
    showCanvasStats.value = on
    try {
      localStorage.setItem(STATS_PREF_KEY, String(on))
    } catch {
      // Preference simply won't survive a reload.
    }
  }

  /**
   * Per-view rendering defaults (node/link label colors). Lives on
   * currentView like the background, so Save persists it with the view.
   */
  const viewStyle = computed<TopologyViewStyle | undefined>(() => currentView.value?.style)

  const setViewStyle = (patch: Partial<TopologyViewStyle>) => {
    if (!currentView.value) {
      return
    }
    currentView.value.style = { ...currentView.value.style, ...patch }
  }

  /** True while a save request is in flight; drives toolbar disabled state. */
  const isSaving = ref<boolean>(false)

  /**
   * Discovered (auto-generated) topology state -- the "view source" dimension
   * beside the custom catalog. Holds the raw graph from the Graph REST API
   * (the canvas auto-lays-out and renders it read-only). Separate from
   * currentView, which is custom-only.
   */
  /**
   * Containers the Graph REST API reports, and the source menu derived from
   * them. Empty until loadGraphContainers() runs, which buildSources() treats
   * as "fall back to the curated groups" rather than an empty menu.
   */
  const graphContainers = ref<GraphContainerMeta[]>([])
  const topologySources = computed<TopologySourceOption[]>(() => buildSources(graphContainers.value))

  const loadGraphContainers = async (): Promise<void> => {
    graphContainers.value = await listGraphContainers()
  }

  const discoveredGraph = ref<DiscoveredGraph | null>(null)
  const isDiscoveredLoading = ref<boolean>(false)
  const discoveredError = ref<boolean>(false)
  /**
   * Focus + Semantic Zoom Level for discovered topologies: when a focus node
   * is set, the canvas renders only that node plus everything within
   * `semanticZoomLevel` hops, keeping large graphs legible. Null focus = show
   * the whole graph. Both reset whenever a new source loads.
   */
  const focusNodeId = ref<string | null>(null)
  const semanticZoomLevel = ref<number>(2)

  /**
   * Laying out and drawing a big discovered graph is the expensive part (the
   * fetch is cheap), so past this count the page holds the render until the user
   * focuses or opts in. Lives here rather than in the page because what is drawn
   * decides what gets polled -- see visibleNodeIds.
   */
  const LARGE_GRAPH_THRESHOLD = 3000
  const renderAllAccepted = ref(false)
  const discoveredNodeCount = computed<number>(() => discoveredGraph.value?.nodes.length ?? 0)

  /**
   * What the canvas would draw for the current focus and zoom level, before the
   * gate is consulted. Separate from visibleNodeIds so the gate can be decided
   * from it without the two becoming circular.
   */
  const renderedGraph = computed<DiscoveredGraph | null>(() =>
    discoveredGraph.value
      ? focusSubgraph(discoveredGraph.value, focusNodeId.value, semanticZoomLevel.value)
      : null
  )

  /**
   * Gate on the size of what would actually be drawn, not on whether a focus
   * exists. Any focus used to clear the gate outright, but stepping the zoom
   * level twice from the gate's own suggested anchor reaches ~1700 nodes and
   * then the whole graph, which is the several-second layout freeze the gate is
   * there to prevent.
   */
  const isLargeGraphGated = computed<boolean>(() =>
    !!discoveredGraph.value &&
    (renderedGraph.value?.nodes.length ?? 0) > LARGE_GRAPH_THRESHOLD &&
    !renderAllAccepted.value
  )
  const acceptRenderAll = () => {
    renderAllAccepted.value = true
  }

  /**
   * The node ids actually on screen: the focus subgraph of a discovered graph,
   * everything placed in a custom view, and nothing at all while the large-graph
   * gate is up.
   *
   * Status polling and the Explore panel both work from this rather than from
   * every node the graph happens to contain. On a 3400-vertex topology the
   * difference is a handful of ids against all of them, and the whole-graph
   * query was long enough that the server rejected it outright.
   */
  const visibleNodeIds = computed<number[]>(() => {
    if (!discoveredGraph.value) {
      return Array.from(placedNodeIds.value).map(Number).filter(Number.isInteger)
    }
    if (isLargeGraphGated.value) {
      return []
    }
    return (renderedGraph.value?.nodes ?? [])
      .map(n => n.nodeId)
      .filter((id): id is number => id != null)
  })

  /**
   * Latest alarm status for placed nodes, keyed by real OnmsNode id. The
   * canvas colors nodes from this map. Empty until a status refresh runs;
   * refreshes are driven by the page (interval in View mode, manual in
   * Edit mode -- see the Edit/View semantics in the plan).
   */
  const severities = ref<Record<number, string>>({})

  const setSeverities = (next: Record<number, string>) => {
    severities.value = next
  }

  /**
   * Bumped every time refreshStatus completes. Anything else built on the same
   * alarm data watches this instead of running a timer of its own, so the
   * canvas and the tables cannot drift apart or disagree about what is current.
   */
  const statusRevision = ref<number>(0)

  /**
   * Fetch current severities for the placed nodes that map to real
   * OnmsNode ids. Placed-node palette ids are the node ids; non-numeric
   * ids (mock/decorative nodes) are skipped.
   */
  const refreshStatus = async (): Promise<void> => {
    const ids = visibleNodeIds.value
    severities.value = ids.length === 0 ? {} : await getNodeSeverities(ids)
    statusRevision.value++
  }

  /**
   * Device-icon id per placed node (by real OnmsNode id), resolved from each
   * node's sysObjectId the legacy way. The canvas renders a glyph for nodes in
   * this map; others stay plain circles. Unlike severity, icons are static, so
   * this is refreshed when the visible set changes (below) rather than on the
   * status poll -- so icons show in Edit mode too.
   */
  const nodeIconIds = ref<Record<number, DeviceIconId>>({})

  const refreshDeviceIcons = async (): Promise<void> => {
    // Scoped to what is drawn, like severity: asking for every node in a
    // 3400-vertex graph is a request per ~110 ids for glyphs the canvas is not
    // rendering, and it ran even while the gate held the render.
    const ids = visibleNodeIds.value
    nodeIconIds.value = ids.length === 0 ? {} : await getNodeIconIds(ids)
  }

  // Whenever the set of nodes on screen changes -- a view load, a discovered
  // load, a palette drop, or a change of focus or zoom level -- refresh the icon
  // map AND severities. This followed placedNodeIds, which does not change when
  // focus does, so focusing into a subgraph left it painted with the previous
  // set's severities: default blue in View mode, and indefinitely in Edit mode
  // where the poll is stopped. Fetching here rather than only on the poll is
  // also what colors nodes on the initial load.
  watch(visibleNodeIds, () => {
    void refreshDeviceIcons()
    void refreshStatus()
    // Ghost links / neighbor tray only matter while composing a custom view.
    if (isEditMode.value && discoveredGraph.value === null) {
      void refreshNeighbors()
    }
  })

  /**
   * Rendered node disc size (sigma size units). The canvas renders every node
   * at this size; the toolbar slider sets it. It defaults as a function of
   * graph density -- small/hand-composed views get large nodes, dense
   * discovered graphs get smaller ones so they don't overlap -- and the user
   * can override per session.
   */
  const NODE_SIZE_MIN = 6
  const NODE_SIZE_MAX = 28
  const nodeSize = ref<number>(20)

  // Density curve: <=10 nodes -> 20, >=100 -> 9, linear between.
  const autoNodeSizeForCount = (count: number): number =>
    Math.round(Math.max(9, Math.min(20, 20 - (count - 10) * (11 / 90))))

  const setNodeSize = (n: number) => {
    nodeSize.value = Math.max(NODE_SIZE_MIN, Math.min(NODE_SIZE_MAX, Math.round(n)))
  }
  const setNodeSizeForCount = (count: number) => {
    nodeSize.value = autoNodeSizeForCount(count)
  }

  const newView = () => {
    currentView.value = emptyView()
    selectedIds.value = []
    labels.value = []
    shapes.value = []
    placedNodeIds.value = new Set()
    isBackgroundAdjustMode.value = false
    isShapeDrawMode.value = false
  }

  /**
   * Load a discovered topology from the Graph REST API into discoveredGraph.
   * The canvas watches this and renders it (auto-layout + read-only). Also
   * seeds placedNodeIds with the graph's real node ids so refreshStatus colors
   * them by severity. Returns the graph (raw, unpositioned) or false.
   */
  const loadDiscoveredSource = async (
    source: DiscoveredGraphSource
  ): Promise<DiscoveredGraph | false> => {
    isDiscoveredLoading.value = true
    discoveredError.value = false
    focusNodeId.value = null
    renderAllAccepted.value = false // each load re-arms the large-graph gate
    try {
      const graph = await loadDiscoveredGraph(source)
      if (graph === false) {
        discoveredError.value = true
        discoveredGraph.value = null
        return false
      }
      discoveredGraph.value = graph
      // refreshStatus keys off placedNodeIds (bare node-id strings).
      const nodeIds = graph.nodes
        .map(n => n.nodeId)
        .filter((id): id is number => id != null)
        .map(String)
      placedNodeIds.value = new Set(nodeIds)
      selectedIds.value = []
      return graph
    } finally {
      isDiscoveredLoading.value = false
    }
  }

  /** Clear discovered state when switching back to a custom source. */
  const clearDiscovered = () => {
    discoveredGraph.value = null
    discoveredError.value = false
    focusNodeId.value = null
  }

  const setFocusNode = (id: string | null) => {
    focusNodeId.value = id
  }

  // Clamp to a sane range so the stepper can't go negative or absurdly high.
  const setSemanticZoomLevel = (level: number) => {
    semanticZoomLevel.value = Math.max(0, Math.min(10, Math.round(level)))
  }

  /** Reload the catalog list (id + name) from the server. */
  const refreshCatalog = async (): Promise<boolean> => {
    const res = await listViews()
    catalog.value = res === false ? [] : res
    return res !== false
  }

  /** Rename the open view (persisted on the next save). */
  const renameCurrent = (name: string) => {
    if (currentView.value) {
      currentView.value = { ...currentView.value, name }
    }
  }

  /**
   * Persist the open view. The canvas component supplies the live
   * nodes/edges/viewport; labels are merged from the store. On success the
   * server's canonical record (id, owner, timestamps) becomes the current
   * view and the catalog is refreshed.
   */
  const saveCurrentView = async (snapshot: CanvasSnapshot): Promise<boolean> => {
    if (!currentView.value) {
      return false
    }
    isSaving.value = true
    try {
      const view: TopologyView = {
        ...currentView.value,
        nodes: snapshot.nodes,
        links: snapshot.links,
        labels: labels.value.map(l => ({ ...l })),
        shapes: shapes.value.map(s => ({ ...s })),
        viewport: snapshot.viewport
      }
      const saved = await saveView(view)
      if (saved === false) {
        return false
      }
      currentView.value = saved
      await refreshCatalog()
      return true
    } finally {
      isSaving.value = false
    }
  }

  /**
   * Save the current canvas as a NEW catalog entry under `name` (Save As).
   * Builds a fresh candidate (no id) from the open view + snapshot and only
   * adopts it as the current view when the save succeeds. This is the key
   * difference from the old inline Save As: a failed Save As -- most commonly
   * a duplicate name (the server replies 409) -- leaves the currently-open
   * view untouched, rather than detaching it (losing its id) and renaming it
   * to the conflicting name.
   */
  const saveCurrentViewAs = async (name: string, snapshot: CanvasSnapshot): Promise<boolean> => {
    if (!currentView.value) {
      return false
    }
    isSaving.value = true
    try {
      const candidate: TopologyView = {
        ...currentView.value,
        id: undefined,
        name,
        nodes: snapshot.nodes,
        links: snapshot.links,
        labels: labels.value.map(l => ({ ...l })),
        shapes: shapes.value.map(s => ({ ...s })),
        viewport: snapshot.viewport
      }
      const saved = await saveView(candidate)
      if (saved === false) {
        return false
      }
      currentView.value = saved
      await refreshCatalog()
      return true
    } finally {
      isSaving.value = false
    }
  }

  /**
   * Load a saved view by id and make it current. Returns the view so the
   * caller (the page) can hand it to the canvas to render; the canvas, in
   * turn, repopulates labels and placed-node ids in this store.
   */
  const openView = async (id: string): Promise<TopologyView | false> => {
    const view = await getView(id)
    if (view === false) {
      return false
    }
    currentView.value = view
    isBackgroundAdjustMode.value = false
    return view
  }

  /** Delete a view; if it was the open one, reset to a blank canvas. */
  const removeView = async (id: string): Promise<boolean> => {
    const ok = await deleteView(id)
    if (!ok) {
      return false
    }
    if (currentView.value?.id === id) {
      newView()
    }
    await refreshCatalog()
    return true
  }

  /**
   * Rename a saved view by id (fetch → set name → save), without disturbing
   * the open view unless it's the one being renamed. Used by the ViewManager.
   */
  const renameView = async (id: string, name: string): Promise<boolean> => {
    const view = await getView(id)
    if (view === false) {
      return false
    }
    const renamed = await saveView({ ...view, name })
    if (renamed === false) {
      return false
    }
    if (currentView.value?.id === id) {
      currentView.value = renamed
    }
    await refreshCatalog()
    return true
  }

  const setEditMode = (value: boolean) => {
    isEditMode.value = value
    // Background adjustment / shape drawing are Edit-mode gestures.
    if (!value) {
      isBackgroundAdjustMode.value = false
      isShapeDrawMode.value = false
    } else if (discoveredGraph.value === null) {
      void refreshNeighbors()
    }
  }

  const setLinkDrawMode = (value: boolean) => {
    isLinkDrawMode.value = value
  }

  const selectOnly = (id: string) => {
    selectedIds.value = [id]
  }

  /**
   * nodeId -> category names, for searching by category. Loaded on demand: most
   * sessions never search, and it costs a request per 500 nodes.
   */
  const nodeCategories = ref<Record<number, string[]>>({})

  const loadNodeCategories = async (nodeIds: number[]): Promise<void> => {
    nodeCategories.value = nodeIds.length === 0 ? {} : await getNodeCategories(nodeIds)
  }

  /** Replace the selection wholesale, e.g. with every member of a category. */
  const selectMany = (ids: string[]) => {
    selectedIds.value = [...ids]
  }

  const toggleSelection = (id: string) => {
    const idx = selectedIds.value.indexOf(id)
    if (idx >= 0) {
      selectedIds.value.splice(idx, 1)
    } else {
      selectedIds.value.push(id)
    }
  }

  const clearSelection = () => {
    selectedIds.value = []
  }

  const setSelection = (ids: string[]) => {
    selectedIds.value = [...ids]
  }

  const addToSelection = (ids: string[]) => {
    const merged = new Set(selectedIds.value)
    ids.forEach(id => merged.add(id))
    selectedIds.value = Array.from(merged)
  }

  const isPlaced = (paletteId: string): boolean => placedNodeIds.value.has(paletteId)

  const markPlaced = (paletteId: string) => {
    if (placedNodeIds.value.has(paletteId)) {
      return
    }
    placedNodeIds.value = new Set(placedNodeIds.value).add(paletteId)
  }

  const markUnplaced = (paletteId: string) => {
    if (!placedNodeIds.value.has(paletteId)) {
      return
    }
    const next = new Set(placedNodeIds.value)
    next.delete(paletteId)
    placedNodeIds.value = next
  }

  const setPlacedNodeIds = (ids: Iterable<string>) => {
    placedNodeIds.value = new Set(ids)
  }

  const setLabels = (next: CanvasLabel[]) => {
    labels.value = next.map(l => ({ ...l }))
  }

  const addLabel = (label: CanvasLabel) => {
    labels.value = [...labels.value, label]
  }

  const updateLabel = (id: string, patch: Partial<CanvasLabel>) => {
    labels.value = labels.value.map(l => (l.id === id ? { ...l, ...patch } : l))
  }

  const removeLabel = (id: string) => {
    labels.value = labels.value.filter(l => l.id !== id)
  }

  const getLabel = (id: string): CanvasLabel | undefined =>
    labels.value.find(l => l.id === id)

  const setShapes = (next: CanvasShape[]) => {
    shapes.value = next.map(s => ({ ...s }))
  }

  const addShape = (shape: CanvasShape) => {
    shapes.value = [...shapes.value, shape]
  }

  const updateShape = (id: string, patch: Partial<CanvasShape>) => {
    shapes.value = shapes.value.map(s => (s.id === id ? { ...s, ...patch } : s))
  }

  const removeShape = (id: string) => {
    shapes.value = shapes.value.filter(s => s.id !== id)
  }

  const getShape = (id: string): CanvasShape | undefined =>
    shapes.value.find(s => s.id === id)

  return {
    catalog,
    currentView,
    isEditMode,
    isLinkDrawMode,
    isSaving,
    statusRevision,
    graphContainers,
    topologySources,
    loadGraphContainers,
    discoveredGraph,
    isDiscoveredLoading,
    discoveredError,
    focusNodeId,
    semanticZoomLevel,
    loadDiscoveredSource,
    clearDiscovered,
    setFocusNode,
    setSemanticZoomLevel,
    severities,
    selectedIds,
    placedNodeIds,
    labels,
    background,
    setBackground,
    showCanvasStats,
    setShowCanvasStats,
    viewStyle,
    setViewStyle,
    isBackgroundAdjustMode,
    setBackgroundAdjustMode,
    newView,
    refreshCatalog,
    renameCurrent,
    saveCurrentView,
    saveCurrentViewAs,
    openView,
    removeView,
    renameView,
    setPlacedNodeIds,
    setLabels,
    shapes,
    neighborsByNode,
    getNeighborsFor,
    isLinkHintsEnabled,
    setLinkHintsEnabled,
    refreshNeighbors,
    isShapeDrawMode,
    setShapeDrawMode,
    setShapes,
    addShape,
    updateShape,
    removeShape,
    getShape,
    setSeverities,
    refreshStatus,
    nodeIconIds,
    refreshDeviceIcons,
    nodeSize,
    setNodeSize,
    setNodeSizeForCount,
    NODE_SIZE_MIN,
    NODE_SIZE_MAX,
    setEditMode,
    setLinkDrawMode,
    selectOnly,
    selectMany,
    nodeCategories,
    loadNodeCategories,
    visibleNodeIds,
    discoveredNodeCount,
    isLargeGraphGated,
    acceptRenderAll,
    toggleSelection,
    clearSelection,
    setSelection,
    addToSelection,
    isPlaced,
    markPlaced,
    markUnplaced,
    addLabel,
    updateLabel,
    removeLabel,
    getLabel
  }
})
