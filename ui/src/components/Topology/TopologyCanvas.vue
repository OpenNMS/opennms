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
  Sigma.js-based canvas. Owns the WebGL node/edge layer (sigma) and a
  graphology Graph instance as its data model. Mock-graph generation is
  Step 2 spike scaffolding -- the real palette / view document drive the
  graph from Step 3 onward.
-->

<template>
  <div
    class="topology-canvas-root"
    :class="{ 'is-drop-target': isDropHover, 'is-link-draw-mode': store.isLinkDrawMode, 'is-hovering-link': hoveredLinkId !== null }"
    @dragenter.prevent="onDragEnter"
    @dragover.prevent="onDragOver"
    @dragleave="onDragLeave"
    @drop.prevent="onDrop"
    @mousemove="onCanvasMouseMove"
    @contextmenu.prevent
  >
    <div v-if="store.showCanvasStats" class="topology-canvas-stats">
      <span>Nodes: {{ placedCount }}</span>
      <span>Links: {{ linkCount }}</span>
      <span>Labels: {{ store.labels.length }}</span>
      <span>Selected: {{ store.selectedIds.length }}</span>
    </div>
    <!-- Background image (floor plan / rack diagram), positioned in graph
         coordinates and re-projected each frame so it pans/zooms with the
         nodes. Sits below the (transparent) sigma canvases; only intercepts
         the mouse in background-adjust mode, where the layer is raised above
         the canvas so the image can be dragged/resized. -->
    <div
      v-if="backgroundVisible"
      class="topology-background-layer"
      :class="{ 'is-adjusting': store.isBackgroundAdjustMode && store.isEditMode }"
    >
      <img
        class="topology-background-image"
        :src="backgroundSrc"
        :style="backgroundStyle(cameraVersion)"
        alt=""
        draggable="false"
        @mousedown.prevent="onBackgroundMouseDown"
      />
      <div
        v-if="store.isBackgroundAdjustMode && store.isEditMode"
        class="topology-background-handle"
        :style="backgroundHandleStyle(cameraVersion)"
        @mousedown.prevent.stop="onBackgroundHandleMouseDown"
      />
    </div>
    <!-- Annotation shapes (visible layer): frames/boxes drawn below the
         nodes, re-projected each frame like the background. Mouse-inert --
         the interaction skeleton lives in a separate layer above the canvas
         so a frame's interior never blocks the nodes inside it. -->
    <svg v-if="visibleShapes.length > 0" class="topology-shapes-layer">
      <template v-for="shape in visibleShapes" :key="shape.id">
        <rect
          v-if="shape.type === 'rect'"
          v-bind="shapeRectAttrs(shape, cameraVersion)"
          :class="{ 'is-selected': store.selectedIds.includes(shape.id) }"
          class="topology-shape"
        />
        <ellipse
          v-else
          v-bind="shapeEllipseAttrs(shape, cameraVersion)"
          :class="{ 'is-selected': store.selectedIds.includes(shape.id) }"
          class="topology-shape"
        />
        <text
          v-if="shape.label"
          v-bind="shapeLabelAttrs(shape, cameraVersion)"
          class="topology-shape-title"
        >
          {{ shape.label }}
        </text>
      </template>
    </svg>
    <div ref="canvasEl" class="topology-canvas" />
    <div class="topology-labels-layer">
      <!-- Labels are reactively positioned in viewport space via
           cameraVersion (bumped on sigma's afterRender); references it
           in the style binding so Vue re-evaluates each render. -->
      <div
        v-for="label in store.labels"
        :key="label.id"
        class="topology-label"
        :class="{ 'is-selected': store.selectedIds.includes(label.id), 'is-editing': editingLabelId === label.id }"
        :style="labelStyle(label, cameraVersion)"
        @mousedown.stop="onLabelMouseDown($event, label)"
        @click.stop="onLabelClick($event, label)"
        @dblclick.stop="onLabelDoubleClick(label)"
      >
        <input
          v-if="editingLabelId === label.id"
          ref="editingInputRef"
          v-model="editingText"
          class="topology-label-input"
          :style="{ color: label.color || undefined }"
          @keydown.enter.prevent="commitEdit"
          @keydown.escape.prevent="cancelEdit"
          @blur="commitEdit"
        />
        <span v-else class="topology-label-text" :style="{ color: label.color || undefined }">
          {{ label.text }}
        </span>
      </div>
    </div>
    <!-- Ghost links (Phase 2 assisted composition): real discovered
         adjacencies between placed nodes, shown faint and dashed while
         composing; clicking one adopts it as a persisted link carrying its
         interface binding. -->
    <svg v-if="ghostHints.length > 0" class="topology-ghost-layer">
      <!-- Per hint: a fat invisible hit line (the click/hover target -- a
           3px dashed stroke is too easy to miss, and misses land on the
           stage) over the visible dashed line, which stays mouse-inert. -->
      <g
        v-for="hint in ghostHints"
        :key="hint.sourceId + '|' + hint.targetId"
        class="topology-ghost"
      >
        <line v-bind="ghostLineAttrs(hint, cameraVersion)" class="topology-ghost-link" />
        <line
          v-bind="ghostLineAttrs(hint, cameraVersion)"
          class="topology-ghost-hit"
          @click.stop="adoptHint(hint)"
          @mousedown.stop.prevent
        >
          <title>Discovered ({{ hint.binding.protocol.toUpperCase() }}){{ hint.binding.sourcePort ? `: ${hint.binding.sourcePort} — ${hint.binding.targetPort ?? '?'}` : '' }} — click to add this link</title>
        </line>
      </g>
    </svg>
    <!-- Annotation shapes (interaction skeleton, Edit mode): an invisible
         fat border per shape that takes clicks/drags via pointer-events:
         stroke -- the interior stays click-through to the nodes a frame
         surrounds -- plus a corner resize handle when selected. -->
    <svg v-if="store.isEditMode && visibleShapes.length > 0" class="topology-shapes-hit-layer">
      <template v-for="shape in visibleShapes" :key="shape.id">
        <rect
          v-if="shape.type === 'rect'"
          v-bind="shapeHitRectAttrs(shape, cameraVersion)"
          class="topology-shape-hit"
          @mousedown.stop.prevent="onShapeBorderMouseDown(shape, $event)"
        />
        <ellipse
          v-else
          v-bind="shapeHitEllipseAttrs(shape, cameraVersion)"
          class="topology-shape-hit"
          @mousedown.stop.prevent="onShapeBorderMouseDown(shape, $event)"
        />
        <rect
          v-if="store.selectedIds.includes(shape.id)"
          v-bind="shapeHandleAttrs(shape, cameraVersion)"
          class="topology-shape-resize-handle"
          @mousedown.stop.prevent="onShapeHandleMouseDown(shape, $event)"
        />
      </template>
    </svg>
    <!-- Draw Box mode: a stage-covering overlay captures the drag so sigma
         never sees it; releasing creates the shape and exits the mode. -->
    <div
      v-if="store.isShapeDrawMode && store.isEditMode"
      class="topology-shape-draw-overlay"
      @mousedown.prevent="onShapeDrawStart"
    >
      <div v-if="shapeDraft" class="topology-shape-draft" :style="shapeDraftStyle" />
    </div>
    <div
      v-if="rubberBand && rubberBandWidth > 1 && rubberBandHeight > 1"
      class="topology-rubber-band"
      :style="rubberBandStyle"
    />
    <svg v-if="linkPreview" class="topology-link-preview" xmlns="http://www.w3.org/2000/svg">
      <line
        :x1="linkPreview.x1"
        :y1="linkPreview.y1"
        :x2="linkPreview.x2"
        :y2="linkPreview.y2"
        class="topology-link-preview"
        stroke-width="2"
        stroke-dasharray="6 4"
      />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import Graph from 'graphology'
import Sigma from 'sigma'
import EdgeCurveProgram from '@sigma/edge-curve'
import { createNodeImageProgram } from '@sigma/node-image'
import { drawDiscNodeLabel } from 'sigma/rendering'
import { downloadAsImage } from '@sigma/export-image'
import { PALETTE_DRAG_MIME, type PaletteDragPayload } from '@/components/Topology/dragTypes'
import { useTopologyStore } from '@/stores/topologyStore'
import { useAppStore } from '@/stores/appStore'
import { DEFAULT_NODE_COLOR, severityColor } from '@/components/Topology/severity'
import {
  DEVICE_ICON_SVG,
  deviceIconImage,
  powerStateForIconKey,
  resolveDeviceIcon
} from '@/components/Topology/deviceIcons'
import {
  LABEL_PREFIX,
  SHAPE_PREFIX,
  isLabelId,
  isShapeId,
  placedIdFor,
  paletteIdFromPlacedId,
  nodeIdFromPlacedId
} from '@/components/Topology/nodeIds'
import { computeEdgeCurvatures, layoutDiscoveredGraph, layoutHierarchyGraph } from '@/components/Topology/layout'
import { computeGhostLinks, type LinkHint } from '@/components/Topology/linkHints'
import { assetUrl } from '@/services/topologyService'
import type {
  CanvasLink,
  CanvasLinkBinding,
  CanvasLabel,
  CanvasNode,
  CanvasShape,
  DiscoveredGraph,
  TopologyView,
  TopologyViewBackground
} from '@/types/topology'

const store = useTopologyStore()
const appStore = useAppStore()

/**
 * Resolve a node's persisted icon override to an image URL the sigma image
 * program can load: `asset:<id>` -> the asset bytes endpoint, a built-in
 * glyph key -> its data URL. Unknown values (e.g. a stale glyph key) resolve
 * to undefined so the automatic icon takes over rather than a broken image.
 */
const iconOverrideUrl = (override: string | undefined): string | undefined => {
  if (!override) {
    return undefined
  }
  if (override.startsWith('asset:')) {
    return assetUrl(override.slice('asset:'.length))
  }
  return DEVICE_ICON_SVG[override as keyof typeof DEVICE_ICON_SVG]
}

/**
 * Right-click on a node bubbles up to the page, which hosts the context menu
 * (it owns the router/source context for the actions). Payload carries the
 * native event (for positioning), the real OnmsNode id (null for decorative
 * nodes), and the canvas node key (for focus).
 */
const emit = defineEmits<{
  (e: 'node-contextmenu', payload: { event: MouseEvent; nodeId: number | null; nodeKey: string }): void
}>()

const canvasEl = ref<HTMLDivElement>()
const linkCount = ref(0)
const placedCount = ref(0)
const isDropHover = ref(false)
interface RubberBandState {
  startX: number
  startY: number
  currentX: number
  currentY: number
}
const rubberBand = ref<RubberBandState | null>(null)
const rubberBandWidth = computed(() =>
  rubberBand.value ? Math.abs(rubberBand.value.currentX - rubberBand.value.startX) : 0
)
const rubberBandHeight = computed(() =>
  rubberBand.value ? Math.abs(rubberBand.value.currentY - rubberBand.value.startY) : 0
)
const rubberBandStyle = computed(() => {
  if (!rubberBand.value) {
    return {}
  }
  const { startX, startY, currentX, currentY } = rubberBand.value
  return {
    left: Math.min(startX, currentX) + 'px',
    top: Math.min(startY, currentY) + 'px',
    width: rubberBandWidth.value + 'px',
    height: rubberBandHeight.value + 'px'
  }
})
let sigma: Sigma | null = null
let graph: Graph | null = null
// Keeps sigma's cached dimensions in sync when the canvas container resizes
// (mode toggle, inspector/browse panels, window). Without this sigma's
// mouse->graph hit-detection drifts after a resize -- enough that thin edges
// can't be clicked (big nodes still hit), which broke edge selection.
let resizeObserver: ResizeObserver | null = null
// Half-extent of the fixed coordinate frame used when there's no content to
// frame yet (empty canvas). Only its stability matters for placement -- drops
// land under the cursor because the frame doesn't move between viewportToGraph
// and render; fitCamera/setContentBBox narrow it to the content once present.
const DEFAULT_BBOX = 500
// Link thickness, in sigma's edge-size units. Sigma derives an edge's
// clickable zone from its *rendered* thickness, so these widths double as
// hit-target sizes. A roomy base makes links easy to hover; hover then
// fattens further so the click target is generous right when you're aiming
// at it (the affordance pattern Cytoscape/Grafana use). The edgeReducer is
// the single place these are applied, so per-link creation sizes don't matter.
const LINK_SIZE = 3
const LINK_HOVER_SIZE = 6
const LINK_SELECTED_SIZE = 4
// Transient hovered link id (cleared on leave). Drives the reducer + cursor.
const hoveredLinkId = ref<string | null>(null)

// Hovered node id: with it (or a single selected node), incident links are
// emphasized and the rest dimmed, so dense graphs stay legible.
const hoveredNodeId = ref<string | null>(null)
let draggedNode: string | null = null
let dragStartPos: { x: number; y: number } | null = null

/**
 * Bumped on sigma's afterRender event so label DOM positions reactively
 * re-project. The labels reference cameraVersion in their style binding,
 * which forces Vue to re-evaluate labelStyle each render.
 */
const cameraVersion = ref(0)

/**
 * Label edit state. Only one label edits at a time. `editingOriginalText`
 * captures the text at edit start so commit can compare and push an
 * undoable change only when the text actually changed. `editingIsNew`
 * marks labels that were just created (so Esc removes them rather than
 * reverting).
 */
const editingLabelId = ref<string | null>(null)
const editingText = ref('')
const editingInputRef = ref<HTMLInputElement[] | HTMLInputElement | null>(null)
let editingOriginalText = ''
let editingIsNew = false

/**
 * Label drag state. Separate from node drag so the two don't interfere.
 */
let draggingLabel: { id: string; startLabelX: number; startLabelY: number; startMouseGraphX: number; startMouseGraphY: number } | null = null

/**
 * Edge-draw state. `linkDrawSource` is the node id captured on first
 * click while in edge-draw mode; the next clickNode commits an edge
 * source -> target. Click on empty stage cancels the in-flight; Esc
 * exits edge-draw mode entirely.
 */
const linkDrawSource = ref<string | null>(null)
const cursorViewport = ref<{ x: number; y: number } | null>(null)

const linkPreview = computed<{ x1: number; y1: number; x2: number; y2: number } | null>(() => {
  if (!linkDrawSource.value || !cursorViewport.value || !sigma || !graph) {
    return null
  }
  if (!graph.hasNode(linkDrawSource.value)) {
    return null
  }
  void cameraVersion.value
  const sx = graph.getNodeAttribute(linkDrawSource.value, 'x') as number
  const sy = graph.getNodeAttribute(linkDrawSource.value, 'y') as number
  const src = sigma.graphToViewport({ x: sx, y: sy })
  return { x1: src.x, y1: src.y, x2: cursorViewport.value.x, y2: cursorViewport.value.y }
})

const onCanvasMouseMove = (event: MouseEvent) => {
  if (!store.isLinkDrawMode || !linkDrawSource.value || !canvasEl.value) {
    return
  }
  const rect = canvasEl.value.getBoundingClientRect()
  cursorViewport.value = {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  }
}

let linkIdSequence = 0
const newLinkId = () => `link-${Date.now()}-${linkIdSequence++}`

/**
 * Undo/redo. Each user action that mutates the canvas (add from
 * palette, move, delete) pushes a Command onto undoStack. Ctrl+Z pops
 * undoStack, runs cmd.undo(), pushes to redoStack. Ctrl+Shift+Z
 * (or Ctrl+Y) reverses. The do/undo closures capture the graph and
 * store references at command-creation time but check them defensively
 * at execution time -- the graph reference can change across rebuild
 * (e.g., when the mock-node-count slider changes), and history is
 * cleared at that point.
 */
interface Command {
  label: string
  do: () => void
  undo: () => void
}
const MAX_HISTORY = 100
const undoStack: Command[] = []
const redoStack: Command[] = []

const pushCommand = (cmd: Command) => {
  undoStack.push(cmd)
  if (undoStack.length > MAX_HISTORY) {
    undoStack.shift()
  }
  redoStack.length = 0
}

const undo = () => {
  const cmd = undoStack.pop()
  if (!cmd) {
    return
  }
  cmd.undo()
  redoStack.push(cmd)
}

const redo = () => {
  const cmd = redoStack.pop()
  if (!cmd) {
    return
  }
  cmd.do()
  undoStack.push(cmd)
}

const clearHistory = () => {
  undoStack.length = 0
  redoStack.length = 0
}

/**
 * (Re)create the sigma instance over a graph, killing any prior one and
 * re-wiring interaction handlers. Shared by the mock rebuild and by
 * loadView so the renderer options stay in one place.
 */
const mountSigma = (g: Graph) => {
  if (sigma) {
    sigma.kill()
    sigma = null
  }
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (!canvasEl.value) {
    return
  }
  sigma = new Sigma(g, canvasEl.value, {
    renderEdgeLabels: true,
    // Sigma v3 disables edge mouse events by default; enable them so an edge
    // can be clicked to select it (and then have its label edited in the
    // Inspector). Without this the 'clickEdge' handler below never fires.
    enableEdgeEvents: true,
    // Gentler zoom: sigma's defaults (1.7 per wheel notch, 2.2 per
    // double-click) jump roughly twice as far as feels right here. Using
    // ~the square root halves each step, so two steps cover what one did.
    zoomingRatio: 1.3,
    doubleClickZoomingRatio: 1.5,
    // This is a positioning editor: node x/y are absolute graph coordinates we
    // persist and expect to render consistently. Disable sigma's auto-rescale
    // (which re-normalizes coordinates to fit the node extent on every change)
    // so a node stays exactly where it's dropped/saved regardless of how many
    // other nodes are present. Framing is handled explicitly by fitCamera().
    autoRescale: false,
    // Recognized device types render as a glyph (drawn over the node's color
    // disc) via the image node program; everything else stays a plain circle.
    nodeProgramClasses: {
      image: createNodeImageProgram({ drawingMode: 'background', padding: 0.15 })
    },
    // Obstructed discovered links (straight run would pass under another
    // node) render as arcs so each stays visible and clickable; the curve
    // program participates in edge picking like the default line program.
    edgeProgramClasses: {
      curved: EdgeCurveProgram
    },
    // Theme-aware hover/selection halo (see drawThemedNodeHover).
    defaultDrawNodeHover: drawThemedNodeHover as never,
    // Color placed nodes by their node's current alarm severity (held in
    // the store, refreshed on an interval in View mode). Nodes without a
    // known severity -- decorative/mock nodes, or before a status fetch --
    // keep their own color. A known device type additionally renders an icon
    // (sysObjectId-derived; store.nodeIconIds). The severities/nodeIconIds
    // watchers below trigger a sigma.refresh() so changes repaint.
    nodeReducer: (node, attrs) => {
      // Prefer the node's own id attribute over parsing the canvas id: a
      // discovered graph can put several vertices on one node, in which case the
      // canvas id cannot encode it (see discoveredNodeCanvasId).
      const paletteId = paletteIdFromPlacedId(node)
      const nodeIdAttr = typeof attrs.nodeId === 'number' ? attrs.nodeId : null
      const resolvedNodeId = nodeIdAttr
        ?? (paletteId !== null && /^\d+$/.test(paletteId) ? Number(paletteId) : null)
      // All nodes render at the store's (density-defaulted, slider-adjustable) size.
      let res: typeof attrs = { ...attrs, size: store.nodeSize }
      if (resolvedNodeId !== null) {
        const severity = store.severities[resolvedNodeId]
        if (severity) {
          res = { ...res, color: severityColor(severity) }
        }
      }
      // Icon resolution, most specific first: a user-chosen override, then the
      // provider's own icon key, then the node's sysObjectId. The provider key
      // is checked outside the node branch because the vertices that most need
      // an icon -- a datacenter, a network, a datastore -- have no node at all.
      const overrideUrl = iconOverrideUrl(attrs.iconOverride as string | undefined)
      if (overrideUrl) {
        return { ...res, type: 'image', image: overrideUrl }
      }
      const providerKey = attrs.icon as string | undefined
      const providerIcon = resolveDeviceIcon(providerKey)
      if (providerIcon) {
        return {
          ...res,
          type: 'image',
          image: deviceIconImage(providerIcon, powerStateForIconKey(providerKey))
        }
      }
      const iconId = resolvedNodeId !== null ? store.nodeIconIds[resolvedNodeId] : undefined
      if (iconId) {
        return { ...res, type: 'image', image: DEVICE_ICON_SVG[iconId] }
      }
      return res
    },
    // The reducer is the single source of truth for link thickness and the
    // selected/hover visuals -- it never mutates the stored attributes. Hover
    // wins over selection so the element under the cursor is always the
    // fattest/clearest target. _selected is set by the selection watcher;
    // hover is tracked in hoveredLinkId via enter/leaveEdge.
    edgeReducer: (edge, attrs) => {
      // Emphasis is carried by color against a neutral base, not by fading the
      // rest: every link stays legible, and the emphasized one is a different
      // color rather than the same color slightly thicker.
      const base = { ...attrs, color: linkBaseColor(attrs.color) }
      if (edge === hoveredLinkId.value) {
        return { ...base, color: accentColor(), size: LINK_HOVER_SIZE }
      }
      if ((attrs as { _selected?: boolean })._selected) {
        return { ...base, color: accentColor(), size: LINK_SELECTED_SIZE }
      }
      // A hovered or selected node emphasizes its own links: with many straight
      // lines crossing under nodes (a dual-homed fabric, say) it is otherwise
      // hard to tell which belong to it.
      const emphasisNode =
        hoveredNodeId.value ??
        (store.selectedIds.length === 1 && g.hasNode(store.selectedIds[0]) ? store.selectedIds[0] : null)
      if (emphasisNode && (g.source(edge) === emphasisNode || g.target(edge) === emphasisNode)) {
        return { ...base, color: accentColor(), size: LINK_SELECTED_SIZE }
      }
      return { ...base, size: LINK_SIZE }
    }
  })
  // Pin a fixed coordinate frame. With autoRescale:false sigma still
  // re-derives its normalization from a viewport-sized box around the node
  // *centroid* on every render, so coordinates shift as nodes are added --
  // that's why the first few palette drops land in the wrong spot. Setting an
  // explicit customBBox makes sigma normalize from it instead, so the frame is
  // stable no matter how many nodes are present. fitCamera() narrows this box
  // to the actual content (and the camera back to 0.5/0.5) when framing.
  sigma.setCustomBBox({ x: [-DEFAULT_BBOX, DEFAULT_BBOX], y: [-DEFAULT_BBOX, DEFAULT_BBOX] })
  // Re-sync sigma's dimensions whenever its container changes size, so
  // hit-detection (especially for thin edges) stays accurate.
  resizeObserver = new ResizeObserver(() => {
    if (!sigma) {
      return
    }
    // sigma's resize() resyncs the canvas/WebGL dimensions but does NOT
    // re-render (it only emits "resize"), so the scene stays blank until the
    // next interaction -- the "nodes don't show until I zoom" symptom. A
    // follow-up refresh() repaints at the *current* camera, so the view
    // reappears at the size it had before, without changing the user's
    // zoom/pan (which is why we deliberately don't fitCamera here).
    sigma.resize()
    sigma.refresh()
  })
  resizeObserver.observe(canvasEl.value)
  attachInteractionHandlers(sigma, g)
  applyViewStyle()
}

/**
 * Start from an empty canvas. The user composes by dragging nodes from the
 * palette; loadView replaces this when a saved view is opened.
 */
const initGraph = () => {
  graph = new Graph()
  linkCount.value = 0
  placedCount.value = 0
  draggedNode = null
  dragStartPos = null
  clearHistory()
  store.clearSelection()
  mountSigma(graph)
}

/**
 * Serialize the current canvas into the flat shape persisted in a
 * TopologyView: graph nodes/edges plus the sigma camera viewport. Labels
 * are not included here -- they live in the store and are merged at save
 * time. The viewport stores the sigma camera state directly (ratio as
 * `zoom`, x/y as pan) so it round-trips exactly on load.
 */
const serialize = (): Pick<TopologyView, 'nodes' | 'links' | 'viewport'> => {
  const nodes: CanvasNode[] = []
  const links: CanvasLink[] = []
  if (graph) {
    graph.forEachNode((id, attrs) => {
      const paletteId = paletteIdFromPlacedId(id)
      const nodeId = paletteId !== null && /^\d+$/.test(paletteId) ? Number(paletteId) : undefined
      nodes.push({
        id,
        nodeId,
        label: (attrs.label as string) ?? '',
        x: attrs.x as number,
        y: attrs.y as number,
        color: attrs.color as string | undefined,
        iconOverride: (attrs.iconOverride as string | undefined) || undefined
      })
    })
    graph.forEachEdge((id, attrs, source, target) => {
      links.push({
        id,
        sourceId: source,
        targetId: target,
        label: (attrs.label as string | undefined) || undefined,
        origin: (attrs.origin as string) === 'discovered' ? 'discovered' : 'user',
        binding: (attrs.binding as CanvasLinkBinding | undefined) || undefined
      })
    })
  }
  const cam = sigma?.getCamera().getState()
  const viewport = cam
    ? { zoom: cam.ratio, panX: cam.x, panY: cam.y }
    : { zoom: 1, panX: 0, panY: 0 }
  return { nodes, links, viewport }
}

/**
 * Replace the canvas with a saved view: rebuild the graph from its
 * nodes/edges, restore the placed-node set and labels in the store, and
 * set the camera to the saved viewport. Clears undo/redo history (the old
 * commands reference a graph that no longer exists).
 */
const loadView = (view: TopologyView) => {
  const g = new Graph()
  for (const n of view.nodes) {
    if (g.hasNode(n.id)) {
      continue
    }
    g.addNode(n.id, {
      label: n.label,
      x: n.x,
      y: n.y,
      size: 20,
      color: n.color ?? DEFAULT_NODE_COLOR,
      nodeId: n.nodeId,
      iconOverride: n.iconOverride
    })
  }
  for (const e of view.links) {
    if (
      g.hasNode(e.sourceId) &&
      g.hasNode(e.targetId) &&
      !g.hasEdge(e.id) &&
      !g.hasEdge(e.sourceId, e.targetId)
    ) {
      g.addEdgeWithKey(e.id, e.sourceId, e.targetId, {
        size: 2,
        color: DEFAULT_NODE_COLOR,
        origin: e.origin,
        label: e.label,
        binding: e.binding
      })
    }
  }
  graph = g
  linkCount.value = g.size
  draggedNode = null
  dragStartPos = null
  clearHistory()
  store.clearSelection()

  // Rebuild the placed-node set so the palette hides what's on the canvas.
  const placed: string[] = []
  for (const n of view.nodes) {
    const pid = paletteIdFromPlacedId(n.id)
    if (pid !== null) {
      placed.push(pid)
    }
  }
  store.setPlacedNodeIds(placed)
  placedCount.value = placed.length
  store.setLabels(view.labels)
  store.setShapes(view.shapes ?? [])
  store.setNodeSizeForCount(g.order) // density-based default node size

  mountSigma(g)
  if (sigma) {
    const vp = view.viewport
    // Restore the saved zoom/pan when the view actually has one; the default
    // sentinel (zoom 1, pan 0/0) means no meaningful camera was saved -- e.g.
    // views created via the REST API -- so frame the content instead.
    const hasSavedCamera = !(vp.zoom === 1 && vp.panX === 0 && vp.panY === 0)
    if (hasSavedCamera) {
      // The saved camera (pan/ratio) is relative to the content-fitted frame,
      // so point the customBBox at the content first, then restore it exactly.
      setContentBBox()
      sigma.getCamera().setState({ x: vp.panX, y: vp.panY, ratio: vp.zoom, angle: 0 })
    } else {
      fitCamera(false)
      // Re-fit once after the resize observer's first post-load resize, which
      // would otherwise re-frame and push tall content off-screen.
    }
  }
}

/**
 * Point the sigma customBBox at the current content -- the bounding box over
 * all placed nodes plus free-standing labels, padded so nothing sits hard
 * against the edge. Because sigma normalizes coordinates from this box, making
 * it equal to the content means the content maps to the [0,1] frame and the
 * camera frames it with a plain centered state (see fitCamera). With nothing
 * to frame we fall back to the default square so an empty canvas still has a
 * stable frame for the first drops.
 *
 * Note this is only re-pointed on explicit framing (fit/load), never while
 * dragging or dropping -- keeping it fixed between frames is exactly what makes
 * node placement land where the cursor is.
 */
const setContentBBox = () => {
  if (!sigma || !graph) {
    return
  }
  let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity
  let count = 0
  graph.forEachNode((_id, a) => {
    const x = a.x as number, y = a.y as number
    if (x < minX) {
      minX = x
    }
    if (x > maxX) {
      maxX = x
    }
    if (y < minY) {
      minY = y
    }
    if (y > maxY) {
      maxY = y
    }
    count++
  })
  for (const l of store.labels) {
    if (l.x < minX) {
      minX = l.x
    }
    if (l.x > maxX) {
      maxX = l.x
    }
    if (l.y < minY) {
      minY = l.y
    }
    if (l.y > maxY) {
      maxY = l.y
    }
    count++
  }
  if (count === 0) {
    sigma.setCustomBBox({ x: [-DEFAULT_BBOX, DEFAULT_BBOX], y: [-DEFAULT_BBOX, DEFAULT_BBOX] })
    return
  }
  // Pad ~15% (floored) so edge nodes and their labels aren't clipped.
  const padX = Math.max((maxX - minX) * 0.15, 120)
  const padY = Math.max((maxY - minY) * 0.15, 120)
  sigma.setCustomBBox({ x: [minX - padX, maxX + padX], y: [minY - padY, maxY + padY] })
}

/**
 * Pan to a node without changing the zoom, for a search hit in a custom view.
 * getNodeDisplayData is already in the camera's framed coordinates, so this
 * avoids duplicating the customBBox mapping fitCamera relies on.
 */
const centerOnNode = (id: string) => {
  if (!sigma || !graph || !graph.hasNode(id)) {
    return
  }
  const pos = sigma.getNodeDisplayData(id)
  if (!pos) {
    return
  }
  const camera = sigma.getCamera()
  camera.animate({ x: pos.x, y: pos.y, ratio: camera.getState().ratio, angle: 0 }, { duration: 300 })
}

/**
 * Frame all placed nodes: narrow the coordinate frame to the content (via
 * setContentBBox) and center the camera on it. Since the customBBox now equals
 * the padded content box, a plain centered camera (0.5/0.5, ratio 1) frames it
 * exactly -- no scale calibration needed. `animate` is true for the Fit button,
 * false for the instant framing done on load.
 */
const fitCamera = (animate = true) => {
  if (!sigma || !graph || graph.order === 0) {
    return
  }
  setContentBBox()
  const target = { x: 0.5, y: 0.5, ratio: 1, angle: 0 }
  if (animate) {
    sigma.getCamera().animate(target, { duration: 300 })
  } else {
    sigma.getCamera().setState(target)
  }
}

/**
 * Render a discovered (auto-generated) topology read-only. The Graph REST API
 * gives no positions, so we auto-lay-out with d3-force, then build the
 * graphology graph and mount sigma. Editing stays disabled because the page
 * forces View mode for discovered sources (the interaction handlers are all
 * gated on store.isEditMode); pan/zoom/select still work for exploration.
 * Discovered edges render muted to read as "from discovery, not drawn."
 */
const loadDiscoveredGraph = (dg: DiscoveredGraph) => {
  // Density-based default node size, then lay out with spacing scaled to it.
  // Tree-shaped sources (path outage) use the tiered hierarchy layout; the
  // mesh-like ones (enlinkd) stay force-directed. A graph that declares its own
  // layout (GraphML's preferred-layout) overrides what the source implies.
  store.setNodeSizeForCount(dg.nodes.length)
  const positioned =
    (dg.layout ?? dg.source.layout) === 'hierarchy'
      ? layoutHierarchyGraph(dg.nodes, dg.links, {
        levelSpacing: Math.max(80, store.nodeSize * 6),
        // Wide enough that a node's right-hand label clears its next sibling.
        siblingSpacing: Math.max(70, store.nodeSize * 6)
      })
      : layoutDiscoveredGraph(dg.nodes, dg.links, {
        collideRadius: Math.max(24, store.nodeSize * 3)
      })
  const g = new Graph()
  for (const n of positioned) {
    if (g.hasNode(n.id)) {
      continue
    }
    g.addNode(n.id, {
      label: n.label,
      x: n.x,
      y: n.y,
      // Discovered graphs can be large (100+ nodes); a smaller node keeps them
      // legible without overlap. Hand-composed views use the larger size 20.
      size: 12,
      color: n.color ?? DEFAULT_NODE_COLOR,
      nodeId: n.nodeId,
      // The provider's own icon key (vmware.*, linkd.*), resolved in the node
      // reducer. Kept as the key rather than a glyph id so the power state
      // encoded in its suffix survives to the reducer too.
      icon: n.icon
    })
  }
  // Links whose straight run would pass under a third node render curved so
  // they stay individually visible and clickable (clearance ~ the rendered
  // node radius, store.nodeSize, plus a small margin).
  const curvatures = computeEdgeCurvatures(positioned, dg.links, store.nodeSize + 6)
  for (const e of dg.links) {
    if (
      g.hasNode(e.sourceId) &&
      g.hasNode(e.targetId) &&
      !g.hasEdge(e.id) &&
      !g.hasEdge(e.sourceId, e.targetId)
    ) {
      const curvature = curvatures.get(e.id)
      g.addEdgeWithKey(e.id, e.sourceId, e.targetId, {
        size: 2,
        color: '#9aa7b8',
        origin: e.origin,
        ...(curvature !== undefined ? { type: 'curved', curvature } : {})
      })
    }
  }
  graph = g
  linkCount.value = g.size
  placedCount.value = positioned.length
  draggedNode = null
  dragStartPos = null
  clearHistory()
  store.clearSelection()
  store.setLabels([]) // discovered topologies have no free-standing labels
  store.setShapes([]) // ...nor annotation shapes
  mountSigma(g)
  fitCamera()
}

/**
 * Wires drag-to-move and click-to-select behavior onto a freshly-built
 * sigma instance + graphology graph.
 *
 * Drag pattern follows the canonical sigma example:
 *   - mousedown on a node captures it as the dragged node
 *   - mousemove on the captor (body-level) updates the node's x/y
 *   - mouseup releases the node
 *   - preventSigmaDefault() prevents sigma's camera pan during the drag
 *
 * Selection writes into the topology store; the store's selectedIds is
 * watched separately to reflect highlight state on the graph.
 */
const attachInteractionHandlers = (s: Sigma, g: Graph) => {
  // Window-level mouseup listener installed only while a node drag is in
  // progress, so that releasing the mouse outside the canvas (over the
  // palette, the toolbar, or off the page entirely) still ends the drag.
  // Sigma only exposes a `mousemovebody` event, not a `mouseupbody`.
  const windowMouseUp = () => finishDrag()
  const finishDrag = () => {
    if (draggedNode && dragStartPos && graph && graph.hasNode(draggedNode)) {
      const id = draggedNode
      const start = dragStartPos
      const end = {
        x: graph.getNodeAttribute(id, 'x') as number,
        y: graph.getNodeAttribute(id, 'y') as number
      }
      // Only push a Move command if the node actually moved beyond a
      // sub-pixel threshold -- a plain click on a node should not
      // pollute the undo stack.
      if (Math.abs(end.x - start.x) > 0.001 || Math.abs(end.y - start.y) > 0.001) {
        pushCommand({
          label: `Move ${id}`,
          do: () => {
            if (!graph || !graph.hasNode(id)) {
              return
            }
            graph.setNodeAttribute(id, 'x', end.x)
            graph.setNodeAttribute(id, 'y', end.y)
          },
          undo: () => {
            if (!graph || !graph.hasNode(id)) {
              return
            }
            graph.setNodeAttribute(id, 'x', start.x)
            graph.setNodeAttribute(id, 'y', start.y)
          }
        })
      }
    }
    draggedNode = null
    dragStartPos = null
    window.removeEventListener('mouseup', windowMouseUp)
  }

  s.on('downNode', (e) => {
    // No node dragging in View mode (read-only canvas).
    if (!store.isEditMode) {
      return
    }
    draggedNode = e.node
    if (g.hasNode(e.node)) {
      dragStartPos = {
        x: g.getNodeAttribute(e.node, 'x') as number,
        y: g.getNodeAttribute(e.node, 'y') as number
      }
    }
    // Freeze sigma's auto-rescale by locking the bounding box at drag
    // start. Without this, dragging a node beyond the current natural
    // bbox grows the bbox, and sigma compensates by zooming the camera
    // out -- producing a "canvas zooms further out as I drag" effect.
    if (!s.getCustomBBox()) {
      s.setCustomBBox(s.getBBox())
    }
    window.addEventListener('mouseup', windowMouseUp)
  })

  // Rubber-band selection: shift+drag on empty stage. Plain drag still
  // pans the camera (sigma's default). The window-level mouseup handler
  // finishes the rubber band even if the user releases outside the
  // canvas.
  const windowRubberMouseUp = () => finishRubberBand()
  const finishRubberBand = () => {
    if (rubberBand.value && sigma && graph && canvasEl.value) {
      const { startX, startY, currentX, currentY } = rubberBand.value
      const x0 = Math.min(startX, currentX)
      const x1 = Math.max(startX, currentX)
      const y0 = Math.min(startY, currentY)
      const y1 = Math.max(startY, currentY)
      // Below a minimum drag distance, treat the gesture as a click and
      // make no selection change. Without this, a quick shift+click on
      // empty stage would clear selection unexpectedly.
      if (Math.abs(x1 - x0) > 3 || Math.abs(y1 - y0) > 3) {
        const inside: string[] = []
        graph.forEachNode((nodeId) => {
          const gx = graph!.getNodeAttribute(nodeId, 'x') as number
          const gy = graph!.getNodeAttribute(nodeId, 'y') as number
          const v = sigma!.graphToViewport({ x: gx, y: gy })
          if (v.x >= x0 && v.x <= x1 && v.y >= y0 && v.y <= y1) {
            inside.push(nodeId)
          }
        })
        // Labels live outside the graphology graph but share the same
        // graph coordinate system; project each and test against the
        // rubber band rectangle the same way.
        for (const label of store.labels) {
          const v = sigma!.graphToViewport({ x: label.x, y: label.y })
          if (v.x >= x0 && v.x <= x1 && v.y >= y0 && v.y <= y1) {
            inside.push(label.id)
          }
        }
        // Shift modifier was held to start the rubber band; treat it as
        // additive (matches shift+click behavior).
        store.addToSelection(inside)
      }
    }
    rubberBand.value = null
    window.removeEventListener('mouseup', windowRubberMouseUp)
  }

  s.on('downStage', (e) => {
    const original = e.event.original as MouseEvent | undefined
    if (!original?.shiftKey || !canvasEl.value) {
      return
    }
    const rect = canvasEl.value.getBoundingClientRect()
    const x = original.clientX - rect.left
    const y = original.clientY - rect.top
    rubberBand.value = { startX: x, startY: y, currentX: x, currentY: y }
    window.addEventListener('mouseup', windowRubberMouseUp)
  })

  const captor = s.getMouseCaptor()
  captor.on('mousemovebody', (e) => {
    // Rubber band takes priority over camera pan when active.
    if (rubberBand.value && canvasEl.value) {
      e.preventSigmaDefault()
      e.original.preventDefault()
      const rect = canvasEl.value.getBoundingClientRect()
      const original = e.original as MouseEvent
      rubberBand.value.currentX = original.clientX - rect.left
      rubberBand.value.currentY = original.clientY - rect.top
      return
    }
    if (!draggedNode) {
      return
    }
    // Prevent sigma's camera pan while dragging a node.
    e.preventSigmaDefault()
    e.original.preventDefault()
    e.original.stopPropagation()
    const pos = s.viewportToGraph(e)
    g.setNodeAttribute(draggedNode, 'x', pos.x)
    g.setNodeAttribute(draggedNode, 'y', pos.y)
  })

  // In-canvas mouseup still finishes the drag (the window listeners are
  // belt-and-suspenders for mouseups outside the canvas).
  captor.on('mouseup', () => {
    finishDrag()
    finishRubberBand()
  })

  s.on('clickNode', (e) => {
    const original = e.event.original as MouseEvent | undefined
    if (store.isLinkDrawMode) {
      // Seed cursorViewport from the click position so the preview line
      // is rendered immediately (rather than waiting for the next
      // mousemove to set it).
      if (original && canvasEl.value) {
        const rect = canvasEl.value.getBoundingClientRect()
        cursorViewport.value = {
          x: original.clientX - rect.left,
          y: original.clientY - rect.top
        }
      }
      handleLinkDrawClick(e.node)
      return
    }
    if (original?.shiftKey) {
      store.toggleSelection(e.node)
    } else {
      store.selectOnly(e.node)
    }
  })

  s.on('rightClickNode', (e) => {
    const original = e.event.original as MouseEvent | undefined
    if (!original) {
      return
    }
    original.preventDefault()
    emit('node-contextmenu', {
      event: original,
      nodeId: nodeIdFromPlacedId(e.node),
      nodeKey: e.node
    })
  })

  s.on('clickEdge', (e) => {
    if (store.isLinkDrawMode) {
      return
    }
    const original = e.event.original as MouseEvent | undefined
    if (original?.shiftKey) {
      store.toggleSelection(e.edge)
    } else {
      store.selectOnly(e.edge)
    }
  })

  // Hover affordance: fatten + recolor the link under the cursor (via the
  // reducer) and switch to a pointer cursor, so links read as clickable. In
  // link-draw mode the cursor stays a crosshair and clicks won't select, so
  // we skip the affordance there to avoid implying the link is clickable.
  s.on('enterEdge', (e) => {
    if (store.isLinkDrawMode) {
      return
    }
    hoveredLinkId.value = e.edge
    s.refresh()
  })
  s.on('leaveEdge', (e) => {
    if (hoveredLinkId.value !== e.edge) {
      return
    }
    hoveredLinkId.value = null
    s.refresh()
  })

  // Node hover drives the incident-link emphasis in the edgeReducer.
  s.on('enterNode', (e) => {
    hoveredNodeId.value = e.node
    s.refresh()
  })
  s.on('leaveNode', (e) => {
    if (hoveredNodeId.value !== e.node) {
      return
    }
    hoveredNodeId.value = null
    s.refresh()
  })

  s.on('clickStage', (e) => {
    const original = e.event.original as MouseEvent | undefined
    if (store.isLinkDrawMode) {
      // Click on empty stage cancels the in-flight edge but stays in
      // edge-draw mode (so the user can keep chaining edges).
      linkDrawSource.value = null
      return
    }
    // Shift+click on empty stage is reserved for rubber band; never
    // clear selection on it.
    if (original?.shiftKey) {
      return
    }
    store.clearSelection()
  })

  s.on('doubleClickStage', (e) => {
    // Double-click on empty stage creates a new free-standing label at
    // the cursor's graph coordinates and enters edit mode. Edit mode only;
    // in View mode let sigma's default double-click zoom happen.
    if (!store.isEditMode) {
      return
    }
    const original = e.event.original as MouseEvent | undefined
    if (!original || !canvasEl.value) {
      return
    }
    // sigma also fires its zoom-on-doubleClick by default; suppress it.
    e.preventSigmaDefault()
    const rect = canvasEl.value.getBoundingClientRect()
    const pos = s.viewportToGraph({
      x: original.clientX - rect.left,
      y: original.clientY - rect.top
    })
    createLabelAt(pos.x, pos.y)
  })

  // Bump cameraVersion on each rendered frame so the label DOM overlay
  // re-projects in lock-step with sigma's WebGL render.
  s.on('afterRender', () => {
    cameraVersion.value++
  })
}

/* ---------- Edges (user-drawn connections between nodes) ---------- */

/**
 * In edge-draw mode, the first node click captures the source; the
 * next clickNode (a different node) commits the edge. graphology
 * assigns the edge key via addEdgeWithKey; we use a deterministic id
 * (newLinkId) so undo/redo can re-create the exact same edge object.
 */
const handleLinkDrawClick = (nodeId: string) => {
  if (!graph || !store.isEditMode) {
    return
  }
  if (linkDrawSource.value === null) {
    linkDrawSource.value = nodeId
    return
  }
  const source = linkDrawSource.value
  linkDrawSource.value = null
  if (source === nodeId) {
    return
  } // ignore clicks on the same node (no self-loops)
  if (graph.hasEdge(source, nodeId) || graph.hasEdge(nodeId, source)) {
    // Don't create duplicate edges between the same endpoints.
    return
  }
  const edgeId = newLinkId()
  const attrs = { size: 2, color: DEFAULT_NODE_COLOR, origin: 'user' }
  graph.addEdgeWithKey(edgeId, source, nodeId, attrs)
  linkCount.value = graph.size
  pushCommand({
    label: 'Add edge',
    do: () => {
      if (!graph || graph.hasEdge(edgeId)) {
        return
      }
      graph.addEdgeWithKey(edgeId, source, nodeId, attrs)
      linkCount.value = graph.size
    },
    undo: () => {
      if (!graph || !graph.hasEdge(edgeId)) {
        return
      }
      graph.dropEdge(edgeId)
      linkCount.value = graph.size
    }
  })
}

// When the user toggles edge-draw mode off mid-flight, drop any
// captured source so re-entering the mode starts fresh.
watch(
  () => store.isLinkDrawMode,
  (on) => {
    if (!on) {
      linkDrawSource.value = null
    }
  }
)

/* ---------- Labels (free-standing DOM overlay annotations) ---------- */

let labelSequence = 0
const newLabelId = () => `${LABEL_PREFIX}${Date.now()}-${labelSequence++}`

/**
 * Projects a label's graph coordinates into viewport space and returns a
 * CSS style object positioning it inside .topology-labels-layer. The
 * cameraVersion argument is unused in computation but referenced so Vue's
 * reactivity re-evaluates this function on every render frame.
 */
const labelStyle = (label: CanvasLabel, _cameraVersion: number) => {
  if (!sigma) {
    return { display: 'none' }
  }
  void _cameraVersion
  const v = sigma.graphToViewport({ x: label.x, y: label.y })
  return {
    left: v.x + 'px',
    top: v.y + 'px',
    fontSize: label.fontSize ? `${label.fontSize}px` : undefined
  }
}

const createLabelAt = (graphX: number, graphY: number) => {
  const id = newLabelId()
  const label: CanvasLabel = { id, text: '', x: graphX, y: graphY }
  store.addLabel(label)
  startEditLabel(id, '', true)
}

const startEditLabel = (id: string, originalText: string, isNew: boolean) => {
  editingLabelId.value = id
  editingText.value = originalText
  editingOriginalText = originalText
  editingIsNew = isNew
  nextTick(() => {
    const ref = editingInputRef.value
    const input = Array.isArray(ref) ? ref[0] : ref
    input?.focus()
    input?.select()
  })
}

const commitEdit = () => {
  const id = editingLabelId.value
  if (id === null) {
    return
  }
  const text = editingText.value.trim()
  editingLabelId.value = null
  if (text.length === 0) {
    // Empty text on commit removes the label entirely.
    store.removeLabel(id)
    return
  }
  if (editingIsNew) {
    store.updateLabel(id, { text })
    const final = store.getLabel(id)
    if (!final) {
      return
    }
    const snapshot: CanvasLabel = { ...final }
    pushCommand({
      label: `Add label "${text}"`,
      do: () => {
        if (!store.getLabel(snapshot.id)) {
          store.addLabel(snapshot)
        }
      },
      undo: () => {
        store.removeLabel(snapshot.id)
      }
    })
    return
  }
  if (text === editingOriginalText) {
    return
  }
  const original = editingOriginalText
  store.updateLabel(id, { text })
  pushCommand({
    label: 'Edit label',
    do: () => store.updateLabel(id, { text }),
    undo: () => store.updateLabel(id, { text: original })
  })
}

const cancelEdit = () => {
  const id = editingLabelId.value
  if (id === null) {
    return
  }
  editingLabelId.value = null
  if (editingIsNew) {
    // Cancel of a freshly-created label drops it -- never lands in
    // history (no add command was pushed yet).
    store.removeLabel(id)
  }
}

const onLabelClick = (event: MouseEvent, label: CanvasLabel) => {
  if (editingLabelId.value === label.id) {
    return
  }
  if (event.shiftKey) {
    store.toggleSelection(label.id)
  } else {
    store.selectOnly(label.id)
  }
}

const onLabelDoubleClick = (label: CanvasLabel) => {
  if (!store.isEditMode) {
    return
  }
  startEditLabel(label.id, label.text, false)
}

const onLabelMouseDown = (event: MouseEvent, label: CanvasLabel) => {
  // Only left button; do not interfere with edit-mode input field.
  if (event.button !== 0) {
    return
  }
  // No label dragging in View mode (selection-to-inspect still works via click).
  if (!store.isEditMode) {
    return
  }
  if (editingLabelId.value === label.id) {
    return
  }
  if (!sigma || !canvasEl.value) {
    return
  }
  const rect = canvasEl.value.getBoundingClientRect()
  const mouseGraph = sigma.viewportToGraph({
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  })
  draggingLabel = {
    id: label.id,
    startLabelX: label.x,
    startLabelY: label.y,
    startMouseGraphX: mouseGraph.x,
    startMouseGraphY: mouseGraph.y
  }
  window.addEventListener('mousemove', onLabelMouseMove)
  window.addEventListener('mouseup', onLabelMouseUp)
}

const onLabelMouseMove = (event: MouseEvent) => {
  if (!draggingLabel || !sigma || !canvasEl.value) {
    return
  }
  const rect = canvasEl.value.getBoundingClientRect()
  const cur = sigma.viewportToGraph({
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  })
  const newX = draggingLabel.startLabelX + (cur.x - draggingLabel.startMouseGraphX)
  const newY = draggingLabel.startLabelY + (cur.y - draggingLabel.startMouseGraphY)
  store.updateLabel(draggingLabel.id, { x: newX, y: newY })
}

const onLabelMouseUp = () => {
  window.removeEventListener('mousemove', onLabelMouseMove)
  window.removeEventListener('mouseup', onLabelMouseUp)
  if (!draggingLabel) {
    return
  }
  const id = draggingLabel.id
  const start = { x: draggingLabel.startLabelX, y: draggingLabel.startLabelY }
  const current = store.getLabel(id)
  draggingLabel = null
  if (!current) {
    return
  }
  if (Math.abs(current.x - start.x) < 0.001 && Math.abs(current.y - start.y) < 0.001) {
    return
  }
  const end = { x: current.x, y: current.y }
  pushCommand({
    label: 'Move label',
    do: () => store.updateLabel(id, { x: end.x, y: end.y }),
    undo: () => store.updateLabel(id, { x: start.x, y: start.y })
  })
}

/**
 * Reflects the store's selectedIds into the graph. Nodes get the
 * `highlighted` attribute (sigma's built-in selection visual); edges
 * get a transient `_selected` flag that the edgeReducer maps to blue.
 * Label selection is rendered via a CSS class in the template -- no
 * graph mutation needed. Rebuild may have changed the graph reference,
 * so each id is guarded by hasNode/hasEdge.
 */
watch(
  () => store.selectedIds.slice(),
  (newIds, oldIds) => {
    if (!graph) {
      return
    }(oldIds ?? []).forEach((id) => {
      if (!graph) {
        return
      }
      if (graph.hasNode(id)) {
        graph.removeNodeAttribute(id, 'highlighted')
      } else if (graph.hasEdge(id)) {
        graph.removeEdgeAttribute(id, '_selected')
      }
    })
    newIds.forEach((id) => {
      if (!graph) {
        return
      }
      if (graph.hasNode(id)) {
        graph.setNodeAttribute(id, 'highlighted', true)
      } else if (graph.hasEdge(id)) {
        graph.setEdgeAttribute(id, '_selected', true)
      }
    })
    sigma?.refresh()
  }
)

/**
 * Repaint when node severities change so the nodeReducer recolors. Deep
 * watch because the store replaces the severities object on each refresh
 * but we also want to catch in-place updates defensively.
 */
watch(
  () => store.severities,
  () => sigma?.refresh(),
  { deep: true }
)

/**
 * Switching between View and Edit mounts/unmounts the palette pane and
 * reorders the inspector, which resizes the canvas. The ResizeObserver
 * usually catches that, but its callback can land a frame after Vue flushes
 * the DOM, leaving a blank canvas until the next paint. React to the mode
 * change directly as well: once the new layout settles (nextTick + a frame),
 * resync sigma's dimensions and repaint. We refresh() rather than fit so the
 * user's current zoom/pan is preserved across the switch.
 */
watch(
  () => store.isEditMode,
  () => {
    if (!sigma) {
      return
    }
    nextTick(() => {
      requestAnimationFrame(() => {
        if (!sigma) {
          return
        }
        sigma.resize()
        sigma.refresh()
      })
    })
  }
)

// Repaint when device icons resolve (fetched when the placed-node set changes).
watch(
  () => store.nodeIconIds,
  () => sigma?.refresh(),
  { deep: true }
)

// Repaint when the node size changes (slider or density default).
watch(
  () => store.nodeSize,
  () => sigma?.refresh()
)

/**
 * Translate a DragEvent's viewport (clientX/clientY) coordinates into the
 * graph's coordinate space, accounting for the canvas container's position
 * and sigma's current camera state.
 */
const eventToGraphCoords = (event: DragEvent): { x: number; y: number } | null => {
  if (!sigma || !canvasEl.value) {
    return null
  }
  const rect = canvasEl.value.getBoundingClientRect()
  const localX = event.clientX - rect.left
  const localY = event.clientY - rect.top
  return sigma.viewportToGraph({ x: localX, y: localY })
}

const isPaletteDrag = (event: DragEvent): boolean => {
  if (!event.dataTransfer) {
    return false
  }
  // Some browsers expose types via dataTransfer.types (lowercased).
  return Array.from(event.dataTransfer.types).includes(PALETTE_DRAG_MIME)
}

const onDragEnter = (event: DragEvent) => {
  if (isPaletteDrag(event)) {
    isDropHover.value = true
  }
}

const onDragOver = (event: DragEvent) => {
  if (isPaletteDrag(event) && event.dataTransfer) {
    event.dataTransfer.dropEffect = 'copy'
    isDropHover.value = true
  }
}

const onDragLeave = (event: DragEvent) => {
  // Only clear the hover state when the drag leaves the root, not when it
  // crosses internal element boundaries.
  if (event.currentTarget === event.target) {
    isDropHover.value = false
  }
}

const onDrop = (event: DragEvent) => {
  isDropHover.value = false
  if (!store.isEditMode) {
    return
  }
  if (!event.dataTransfer || !graph) {
    return
  }
  const raw = event.dataTransfer.getData(PALETTE_DRAG_MIME)
  if (!raw) {
    return
  }
  let payload: PaletteDragPayload
  try {
    payload = JSON.parse(raw)
  } catch {
    return
  }
  const coords = eventToGraphCoords(event)
  if (!coords) {
    return
  }

  if (store.isPlaced(payload.nodeId)) {
    // Defensive: the palette filters out already-placed nodes, but if
    // the user finds a way to drag one anyway, select the existing
    // canvas node instead of creating a duplicate.
    const existingId = placedIdFor(payload.nodeId)
    if (graph.hasNode(existingId)) {
      store.selectOnly(existingId)
    }
    return
  }

  const placedId = placedIdFor(payload.nodeId)
  if (graph.hasNode(placedId)) {
    return
  }
  const attrs = {
    label: payload.label,
    x: coords.x,
    y: coords.y,
    size: 20,
    color: DEFAULT_NODE_COLOR
  }
  const paletteId = payload.nodeId
  // Execute the addition, then record the inverse for undo.
  graph.addNode(placedId, attrs)
  store.markPlaced(paletteId)
  placedCount.value++
  pushCommand({
    label: `Add ${payload.label}`,
    do: () => {
      if (!graph || graph.hasNode(placedId)) {
        return
      }
      graph.addNode(placedId, attrs)
      store.markPlaced(paletteId)
      placedCount.value++
    },
    undo: () => {
      if (!graph || !graph.hasNode(placedId)) {
        return
      }
      graph.dropNode(placedId)
      store.markUnplaced(paletteId)
      placedCount.value = Math.max(0, placedCount.value - 1)
    }
  })
}

/**
 * Snapshot of a node + its incident edges, captured before deletion
 * so that an undo can restore the full graph topology around it.
 */
interface DeletedNodeSnapshot {
  id: string
  attrs: Record<string, unknown>
  paletteId: string | null
  edges: Array<{ source: string; target: string; attrs: Record<string, unknown> }>
}

/**
 * Delete the currently-selected canvas nodes. For palette-placed nodes,
 * the placed-id ↔ palette-id mapping is reversed so the palette entry
 * is restored. Mock-graph nodes (n0, n1, ...) just disappear -- they
 * have no palette counterpart. graphology.dropNode removes incident
 * edges automatically; we capture them first so undo can rebuild them.
 */
const deleteSelected = () => {
  if (!graph || !store.isEditMode) {
    return
  }
  const ids = store.selectedIds.slice()
  if (ids.length === 0) {
    return
  }

  // Partition into label ids, shape ids, edge ids, and node ids. Labels and
  // shapes live in the store; edges and nodes live in the graphology graph.
  const labelIds = ids.filter(isLabelId)
  const shapeIds = ids.filter(isShapeId)
  const edgeIds = ids.filter(id => !isLabelId(id) && !isShapeId(id) && graph!.hasEdge(id))
  const nodeIds = ids.filter(id => !isLabelId(id) && !isShapeId(id) && graph!.hasNode(id))
  const labelSnapshots: CanvasLabel[] = labelIds
    .map(id => store.getLabel(id))
    .filter((l): l is CanvasLabel => l !== undefined)
    .map(l => ({ ...l }))
  const shapeSnapshots: CanvasShape[] = shapeIds
    .map(id => store.getShape(id))
    .filter((s): s is CanvasShape => s !== undefined)
    .map(s => ({ ...s }))
  const edgeSnapshots: Array<{ id: string; source: string; target: string; attrs: Record<string, unknown> }> = []
  for (const eid of edgeIds) {
    if (!graph.hasEdge(eid)) {
      continue
    }
    edgeSnapshots.push({
      id: eid,
      source: graph.source(eid),
      target: graph.target(eid),
      attrs: { ...graph.getEdgeAttributes(eid) }
    })
  }

  const snapshots: DeletedNodeSnapshot[] = []
  for (const id of nodeIds) {
    if (!graph.hasNode(id)) {
      continue
    }
    const attrs = { ...graph.getNodeAttributes(id) }
    // `highlighted` is transient visual state owned by the selection
    // watcher, not user-meaningful node data. Preserving it across a
    // delete/undo cycle leaves the restored node visually selected
    // without selectedIds containing it -- the watcher then has no
    // diff to apply and the stale highlight sticks.
    delete attrs.highlighted
    const paletteId = paletteIdFromPlacedId(id)
    const edges: DeletedNodeSnapshot['edges'] = []
    graph.forEachEdge(id, (_key, edgeAttrs, source, target) => {
      // Capture each incident edge once; for an edge whose endpoints
      // are both in the deletion set, this still records it from each
      // side, but the undo step de-dupes via hasEdge.
      edges.push({ source, target, attrs: { ...edgeAttrs }})
    })
    snapshots.push({ id, attrs, paletteId, edges })
  }

  const applyDelete = () => {
    if (!graph) {
      return
    }
    // Edges first so node-deletion cascade doesn't trip the explicit
    // edge-drop step (dropNode also drops incident edges).
    for (const e of edgeSnapshots) {
      if (graph.hasEdge(e.id)) {
        graph.dropEdge(e.id)
      }
    }
    for (const s of snapshots) {
      if (!graph.hasNode(s.id)) {
        continue
      }
      if (s.paletteId !== null) {
        store.markUnplaced(s.paletteId)
        placedCount.value = Math.max(0, placedCount.value - 1)
      }
      graph.dropNode(s.id)
    }
    for (const l of labelSnapshots) {
      store.removeLabel(l.id)
    }
    for (const s of shapeSnapshots) {
      store.removeShape(s.id)
    }
    linkCount.value = graph.size
  }

  const applyUndo = () => {
    if (!graph) {
      return
    }
    for (const s of snapshots) {
      if (!graph.hasNode(s.id)) {
        graph.addNode(s.id, s.attrs)
        if (s.paletteId !== null) {
          store.markPlaced(s.paletteId)
          placedCount.value++
        }
      }
    }
    for (const s of snapshots) {
      for (const e of s.edges) {
        if (
          graph.hasNode(e.source) &&
          graph.hasNode(e.target) &&
          !graph.hasEdge(e.source, e.target) &&
          !graph.hasEdge(e.target, e.source)
        ) {
          graph.addEdge(e.source, e.target, e.attrs)
        }
      }
    }
    for (const e of edgeSnapshots) {
      if (
        graph.hasNode(e.source) &&
        graph.hasNode(e.target) &&
        !graph.hasEdge(e.id)
      ) {
        graph.addEdgeWithKey(e.id, e.source, e.target, e.attrs)
      }
    }
    for (const l of labelSnapshots) {
      if (!store.getLabel(l.id)) {
        store.addLabel(l)
      }
    }
    for (const s of shapeSnapshots) {
      if (!store.getShape(s.id)) {
        store.addShape(s)
      }
    }
    linkCount.value = graph.size
  }

  applyDelete()
  store.clearSelection()
  const totalDeleted = snapshots.length + labelSnapshots.length + shapeSnapshots.length + edgeSnapshots.length
  if (totalDeleted === 0) {
    return
  }
  pushCommand({
    label: `Delete ${totalDeleted} item(s)`,
    do: applyDelete,
    undo: applyUndo
  })
}

/**
 * Window keyboard handler. Handles Delete/Backspace (delete selected),
 * Ctrl+Z (undo), and Ctrl+Shift+Z or Ctrl+Y (redo). Skips when the user
 * is typing in a form field so it doesn't hijack the palette search box.
 */
const onKeyDown = (e: KeyboardEvent) => {
  const target = e.target as HTMLElement | null
  if (target) {
    const tag = target.tagName
    if (tag === 'INPUT' || tag === 'TEXTAREA' || target.isContentEditable) {
      return
    }
  }
  // All keyboard editing (undo/redo, delete, edit-mode escapes) is Edit-only.
  if (!store.isEditMode) {
    return
  }
  const ctrlOrMeta = e.ctrlKey || e.metaKey
  if (ctrlOrMeta && (e.key === 'z' || e.key === 'Z')) {
    e.preventDefault()
    if (e.shiftKey) {
      redo()
    } else {
      undo()
    }
    return
  }
  if (ctrlOrMeta && (e.key === 'y' || e.key === 'Y')) {
    e.preventDefault()
    redo()
    return
  }
  if (e.key === 'Escape') {
    if (store.isLinkDrawMode) {
      e.preventDefault()
      store.setLinkDrawMode(false)
      return
    }
    if (store.isShapeDrawMode) {
      e.preventDefault()
      store.setShapeDrawMode(false)
      return
    }
    if (editingLabelId.value !== null) {
      e.preventDefault()
      cancelEdit()
      return
    }
  }
  if (e.key === 'Delete' || e.key === 'Backspace') {
    if (store.selectedIds.length === 0) {
      return
    }
    e.preventDefault()
    deleteSelected()
  }
}

onMounted(() => {
  initGraph()
  window.addEventListener('keydown', onKeyDown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeyDown)
  endBackgroundDrag()
  endShapeDrag()
  window.removeEventListener('mousemove', onShapeDrawMove)
  window.removeEventListener('mouseup', onShapeDrawEnd)
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (sigma) {
    sigma.kill()
    sigma = null
  }
})

/**
 * Read a link's label and its endpoint labels, for the inspector. Returns
 * null if the id isn't a current link (graphology edge).
 */
const getLink = (
  id: string
): {
  label: string
  sourceLabel: string
  targetLabel: string
  sourceId: string
  targetId: string
  origin: 'user' | 'discovered'
  binding?: CanvasLinkBinding
} | null => {
  if (!graph || !graph.hasEdge(id)) {
    return null
  }
  const source = graph.source(id)
  const target = graph.target(id)
  return {
    label: (graph.getEdgeAttribute(id, 'label') as string) ?? '',
    sourceLabel: (graph.getNodeAttribute(source, 'label') as string) ?? source,
    targetLabel: (graph.getNodeAttribute(target, 'label') as string) ?? target,
    sourceId: source,
    targetId: target,
    origin: (graph.getEdgeAttribute(id, 'origin') as string) === 'discovered' ? 'discovered' : 'user',
    binding: (graph.getEdgeAttribute(id, 'binding') as CanvasLinkBinding | undefined) || undefined
  }
}

/**
 * Place a discovered neighbor next to a placed node and link them in one
 * step (the Inspector's neighbor tray). The neighbor lands on a ring around
 * the source node, angled by how many links the source already has so
 * successive placements fan out. One undo command covers node + link.
 */
const placeNeighbor = (fromId: string, neighbor: import('@/types/topology').DiscoveredNeighbor) => {
  if (!graph || !store.isEditMode || !graph.hasNode(fromId)) {
    return
  }
  const paletteId = String(neighbor.neighborNodeId)
  const placedId = placedIdFor(paletteId)
  const binding: CanvasLinkBinding = {
    protocol: neighbor.linkType,
    sourcePort: neighbor.localPort,
    targetPort: neighbor.remotePort
  }

  // Already on the canvas: just add the missing link.
  if (graph.hasNode(placedId)) {
    if (!graph.hasEdge(fromId, placedId) && !graph.hasEdge(placedId, fromId)) {
      adoptHint({ sourceId: fromId, targetId: placedId, binding })
    }
    return
  }

  const angle = (graph.degree(fromId) * 60 * Math.PI) / 180
  // Place the neighbor a constant *screen* distance away, whatever the zoom:
  // measure how many graph units ~150px currently spans. A fixed graph-unit
  // radius lands neighbors way off-screen when zoomed in.
  const probe = sigma
    ? (() => {
      const a = sigma.viewportToGraph({ x: 0, y: 0 })
      const b = sigma.viewportToGraph({ x: 150, y: 0 })
      return Math.hypot(b.x - a.x, b.y - a.y)
    })()
    : 150
  const radius = Math.max(probe, store.nodeSize * 2)
  const nodeAttrs = {
    label: neighbor.neighborLabel,
    x: (graph.getNodeAttribute(fromId, 'x') as number) + radius * Math.cos(angle),
    y: (graph.getNodeAttribute(fromId, 'y') as number) + radius * Math.sin(angle),
    size: 20,
    color: DEFAULT_NODE_COLOR
  }
  const linkId = newLinkId()
  const linkAttrs = { size: 2, color: DEFAULT_NODE_COLOR, origin: 'discovered', binding }

  const apply = () => {
    if (!graph) {
      return
    }
    if (!graph.hasNode(placedId)) {
      graph.addNode(placedId, nodeAttrs)
      store.markPlaced(paletteId)
      placedCount.value++
    }
    if (!graph.hasEdge(linkId) && graph.hasNode(fromId)) {
      graph.addEdgeWithKey(linkId, fromId, placedId, linkAttrs)
      linkCount.value = graph.size
    }
  }
  apply()
  // Selection stays on the source node: the tray remains open for adding
  // several neighbors in a row.
  pushCommand({
    label: `Add ${neighbor.neighborLabel} + link`,
    do: apply,
    undo: () => {
      if (!graph) {
        return
      }
      if (graph.hasEdge(linkId)) {
        graph.dropEdge(linkId)
      }
      if (graph.hasNode(placedId)) {
        graph.dropNode(placedId)
        store.markUnplaced(paletteId)
        placedCount.value = Math.max(0, placedCount.value - 1)
      }
      linkCount.value = graph.size
    }
  })
}

/**
 * Set an edge's label (rendered on the canvas and persisted via serialize).
 * Called per keystroke from the inspector, so it does not push an undo
 * command -- edge-label edits aren't individually undoable.
 */
const setLinkLabel = (id: string, label: string) => {
  if (!graph || !graph.hasEdge(id)) {
    return
  }
  graph.setEdgeAttribute(id, 'label', label)
  sigma?.refresh()
}

/* ---- Background image layer ------------------------------------------- */

const BG_MIN_SIZE = 50

/** Custom views only (discovered graphs have no background concept). */
const backgroundVisible = computed<boolean>(() => {
  const bg = store.background
  return (
    store.discoveredGraph === null &&
    !!bg &&
    bg.type === 'image' &&
    !!bg.ref?.startsWith('asset:')
  )
})

const backgroundSrc = computed<string>(() => {
  const ref = store.background?.ref
  return ref?.startsWith('asset:') ? assetUrl(ref.slice('asset:'.length)) : ''
})

/**
 * Project the background's graph-space rect to viewport CSS. Referencing
 * cameraVersion (bumped on each sigma render) keeps it locked to pan/zoom,
 * exactly like the free-standing labels overlay.
 */
const backgroundStyle = (_cameraVersion: number) => {
  void _cameraVersion
  const bg = store.background
  if (!sigma || !bg || bg.x === undefined || bg.y === undefined || !bg.width || !bg.height) {
    return { display: 'none' }
  }
  // Graph y points up: the rect spans [y - height, y].
  const topLeft = sigma.graphToViewport({ x: bg.x, y: bg.y })
  const bottomRight = sigma.graphToViewport({ x: bg.x + bg.width, y: bg.y - bg.height })
  return {
    left: topLeft.x + 'px',
    top: topLeft.y + 'px',
    width: Math.max(1, bottomRight.x - topLeft.x) + 'px',
    height: Math.max(1, bottomRight.y - topLeft.y) + 'px',
    opacity: bg.opacity ?? 0.5
  }
}

const backgroundHandleStyle = (_cameraVersion: number) => {
  void _cameraVersion
  const bg = store.background
  if (!sigma || !bg || bg.x === undefined || bg.y === undefined || !bg.width || !bg.height) {
    return { display: 'none' }
  }
  const bottomRight = sigma.graphToViewport({ x: bg.x + bg.width, y: bg.y - bg.height })
  return { left: bottomRight.x + 'px', top: bottomRight.y + 'px' }
}

let backgroundDrag: {
  mode: 'move' | 'resize'
  startGraphX: number
  startGraphY: number
  orig: { x: number; y: number; width: number; height: number }
} | null = null

const backgroundMouseGraph = (event: MouseEvent) => {
  const rect = canvasEl.value!.getBoundingClientRect()
  return sigma!.viewportToGraph({ x: event.clientX - rect.left, y: event.clientY - rect.top })
}

const beginBackgroundDrag = (event: MouseEvent, mode: 'move' | 'resize') => {
  const bg = store.background
  if (!sigma || !canvasEl.value || !bg || !store.isEditMode || !store.isBackgroundAdjustMode) {
    return
  }
  const g = backgroundMouseGraph(event)
  backgroundDrag = {
    mode,
    startGraphX: g.x,
    startGraphY: g.y,
    orig: {
      x: bg.x ?? 0,
      y: bg.y ?? 0,
      width: bg.width ?? BG_MIN_SIZE,
      height: bg.height ?? BG_MIN_SIZE
    }
  }
  window.addEventListener('mousemove', onBackgroundDragMove)
  window.addEventListener('mouseup', endBackgroundDrag)
}

const onBackgroundMouseDown = (event: MouseEvent) => beginBackgroundDrag(event, 'move')
const onBackgroundHandleMouseDown = (event: MouseEvent) => beginBackgroundDrag(event, 'resize')

const onBackgroundDragMove = (event: MouseEvent) => {
  if (!backgroundDrag || !sigma || !canvasEl.value || !store.background) {
    return
  }
  const g = backgroundMouseGraph(event)
  const dx = g.x - backgroundDrag.startGraphX
  const dy = g.y - backgroundDrag.startGraphY
  const { orig } = backgroundDrag
  const next: TopologyViewBackground =
    backgroundDrag.mode === 'move'
      ? { ...store.background, x: orig.x + dx, y: orig.y + dy }
      : {
        ...store.background,
        width: Math.max(BG_MIN_SIZE, orig.width + dx),
        // Dragging the handle downward is negative dy in graph coords.
        height: Math.max(BG_MIN_SIZE, orig.height - dy)
      }
  store.setBackground(next)
}

const endBackgroundDrag = () => {
  backgroundDrag = null
  window.removeEventListener('mousemove', onBackgroundDragMove)
  window.removeEventListener('mouseup', endBackgroundDrag)
}

/* ---- Annotation shapes (frames/boxes) --------------------------------- */

const SHAPE_DEFAULT_STROKE = '#64748b'
const SHAPE_DEFAULT_FILL = '#cbd5e1'
const SHAPE_DEFAULT_OPACITY = 0.35
const SHAPE_MIN_DRAW_PX = 8

let shapeIdSequence = 0
const newShapeId = () => `${SHAPE_PREFIX}${Date.now()}-${shapeIdSequence++}`

/** Shapes render for custom views only (discovered graphs are read-only). */
const visibleShapes = computed<CanvasShape[]>(() =>
  store.discoveredGraph === null ? store.shapes : []
)

/** The shape's graph rect projected to viewport px (same math as the background). */
const shapeViewportRect = (shape: CanvasShape) => {
  if (!sigma) {
    return null
  }
  const topLeft = sigma.graphToViewport({ x: shape.x, y: shape.y })
  const bottomRight = sigma.graphToViewport({ x: shape.x + shape.width, y: shape.y - shape.height })
  return {
    left: topLeft.x,
    top: topLeft.y,
    width: Math.max(1, bottomRight.x - topLeft.x),
    height: Math.max(1, bottomRight.y - topLeft.y)
  }
}

const shapeRectAttrs = (shape: CanvasShape, _cameraVersion: number) => {
  void _cameraVersion
  const r = shapeViewportRect(shape)
  if (!r) {
    return { display: 'none' }
  }
  return {
    x: r.left,
    y: r.top,
    width: r.width,
    height: r.height,
    rx: 6,
    stroke: shape.stroke ?? SHAPE_DEFAULT_STROKE,
    fill: shape.fill ?? SHAPE_DEFAULT_FILL,
    'fill-opacity': shape.opacity ?? SHAPE_DEFAULT_OPACITY
  }
}

const shapeEllipseAttrs = (shape: CanvasShape, _cameraVersion: number) => {
  void _cameraVersion
  const r = shapeViewportRect(shape)
  if (!r) {
    return { display: 'none' }
  }
  return {
    cx: r.left + r.width / 2,
    cy: r.top + r.height / 2,
    rx: r.width / 2,
    ry: r.height / 2,
    stroke: shape.stroke ?? SHAPE_DEFAULT_STROKE,
    fill: shape.fill ?? SHAPE_DEFAULT_FILL,
    'fill-opacity': shape.opacity ?? SHAPE_DEFAULT_OPACITY
  }
}

/** Title anchored inside the shape's top edge, Visio-style. */
const shapeLabelAttrs = (shape: CanvasShape, _cameraVersion: number) => {
  void _cameraVersion
  const r = shapeViewportRect(shape)
  if (!r) {
    return { display: 'none' }
  }
  return {
    x: r.left + r.width / 2,
    y: r.top + 16,
    fill: shape.stroke ?? SHAPE_DEFAULT_STROKE
  }
}

// Hit geometry mirrors the visible geometry; only the stroke is clickable
// (pointer-events: stroke in CSS), so the interior stays click-through.
const shapeHitRectAttrs = (shape: CanvasShape, cameraVersion: number) => shapeRectAttrs(shape, cameraVersion)
const shapeHitEllipseAttrs = (shape: CanvasShape, cameraVersion: number) => shapeEllipseAttrs(shape, cameraVersion)

const shapeHandleAttrs = (shape: CanvasShape, _cameraVersion: number) => {
  void _cameraVersion
  const r = shapeViewportRect(shape)
  if (!r) {
    return { display: 'none' }
  }
  return { x: r.left + r.width - 6, y: r.top + r.height - 6, width: 12, height: 12 }
}

/* Border drag = move (or, without movement, a selection click); corner
   handle = resize. Mirrors the background-adjust drag pattern. */
let shapeDrag: {
  id: string
  mode: 'move' | 'resize'
  startGraphX: number
  startGraphY: number
  orig: { x: number; y: number; width: number; height: number }
  moved: boolean
  shiftKey: boolean
} | null = null

const onShapeBorderMouseDown = (shape: CanvasShape, event: MouseEvent) =>
  beginShapeDrag(shape, event, 'move')
const onShapeHandleMouseDown = (shape: CanvasShape, event: MouseEvent) =>
  beginShapeDrag(shape, event, 'resize')

const beginShapeDrag = (shape: CanvasShape, event: MouseEvent, mode: 'move' | 'resize') => {
  if (!sigma || !canvasEl.value || !store.isEditMode) {
    return
  }
  const g = backgroundMouseGraph(event)
  shapeDrag = {
    id: shape.id,
    mode,
    startGraphX: g.x,
    startGraphY: g.y,
    orig: { x: shape.x, y: shape.y, width: shape.width, height: shape.height },
    moved: false,
    shiftKey: event.shiftKey
  }
  window.addEventListener('mousemove', onShapeDragMove)
  window.addEventListener('mouseup', endShapeDrag)
}

const onShapeDragMove = (event: MouseEvent) => {
  if (!shapeDrag || !sigma || !canvasEl.value) {
    return
  }
  const g = backgroundMouseGraph(event)
  const dx = g.x - shapeDrag.startGraphX
  const dy = g.y - shapeDrag.startGraphY
  if (!shapeDrag.moved && Math.abs(dx) < 1 && Math.abs(dy) < 1) {
    return
  }
  shapeDrag.moved = true
  const { orig } = shapeDrag
  if (shapeDrag.mode === 'move') {
    store.updateShape(shapeDrag.id, { x: orig.x + dx, y: orig.y + dy })
  } else {
    store.updateShape(shapeDrag.id, {
      width: Math.max(20, orig.width + dx),
      // Dragging the handle downward is negative dy in graph coords.
      height: Math.max(20, orig.height - dy)
    })
  }
}

const endShapeDrag = () => {
  if (shapeDrag && !shapeDrag.moved && shapeDrag.mode === 'move') {
    // No movement: it was a selection click on the border.
    if (shapeDrag.shiftKey) {
      store.toggleSelection(shapeDrag.id)
    } else {
      store.selectOnly(shapeDrag.id)
    }
  }
  shapeDrag = null
  window.removeEventListener('mousemove', onShapeDragMove)
  window.removeEventListener('mouseup', endShapeDrag)
}

/* Draw Box mode: drag out a rect on the stage-covering overlay. The draft is
   tracked in *client* coordinates -- the preview converts them to
   overlay-relative px for CSS, and the release converts them to canvas-
   relative px for viewportToGraph (the overlay and the sigma container do
   not share an origin). */
const shapeDraft = ref<{ x1: number; y1: number; x2: number; y2: number } | null>(null)
let shapeDrawOverlayRect: DOMRect | null = null

const shapeDraftStyle = computed(() => {
  const d = shapeDraft.value
  const o = shapeDrawOverlayRect
  if (!d || !o) {
    return {}
  }
  return {
    left: Math.min(d.x1, d.x2) - o.left + 'px',
    top: Math.min(d.y1, d.y2) - o.top + 'px',
    width: Math.abs(d.x2 - d.x1) + 'px',
    height: Math.abs(d.y2 - d.y1) + 'px'
  }
})

const onShapeDrawStart = (event: MouseEvent) => {
  shapeDrawOverlayRect = (event.currentTarget as HTMLElement).getBoundingClientRect()
  shapeDraft.value = { x1: event.clientX, y1: event.clientY, x2: event.clientX, y2: event.clientY }
  window.addEventListener('mousemove', onShapeDrawMove)
  window.addEventListener('mouseup', onShapeDrawEnd)
}

const onShapeDrawMove = (event: MouseEvent) => {
  if (!shapeDraft.value) {
    return
  }
  shapeDraft.value = { ...shapeDraft.value, x2: event.clientX, y2: event.clientY }
}

const onShapeDrawEnd = () => {
  window.removeEventListener('mousemove', onShapeDrawMove)
  window.removeEventListener('mouseup', onShapeDrawEnd)
  const d = shapeDraft.value
  shapeDraft.value = null
  shapeDrawOverlayRect = null
  if (!d || !sigma || !canvasEl.value) {
    return
  }
  if (Math.abs(d.x2 - d.x1) < SHAPE_MIN_DRAW_PX || Math.abs(d.y2 - d.y1) < SHAPE_MIN_DRAW_PX) {
    // Too small to be a deliberate shape; treat as a cancel.
    store.setShapeDrawMode(false)
    return
  }
  const canvasRect = canvasEl.value.getBoundingClientRect()
  const toGraph = (clientX: number, clientY: number) =>
    sigma!.viewportToGraph({ x: clientX - canvasRect.left, y: clientY - canvasRect.top })
  const g1 = toGraph(d.x1, d.y1)
  const g2 = toGraph(d.x2, d.y2)
  const shape: CanvasShape = {
    id: newShapeId(),
    type: 'rect',
    x: Math.min(g1.x, g2.x),
    y: Math.max(g1.y, g2.y),
    width: Math.abs(g2.x - g1.x),
    height: Math.abs(g2.y - g1.y),
    label: '',
    stroke: SHAPE_DEFAULT_STROKE,
    fill: SHAPE_DEFAULT_FILL,
    opacity: SHAPE_DEFAULT_OPACITY
  }
  store.addShape(shape)
  store.selectOnly(shape.id)
  store.setShapeDrawMode(false)
  pushCommand({
    label: 'Add shape',
    do: () => store.getShape(shape.id) || store.addShape({ ...shape }),
    undo: () => store.removeShape(shape.id)
  })
}

/**
 * Hover/selection halo fill. Slightly translucent so the halo reads as sitting
 * over the map rather than punching a hole in it: links and nodes it overlaps
 * stay faintly visible.
 */
/**
 * Theme colors resolved to concrete strings, because sigma draws to a canvas and
 * cannot use a CSS variable. Presentation only; persisted colors stay
 * theme-independent (see DEFAULT_NODE_COLOR).
 */
const themeColor = (token: string, fallback: string): string => {
  const value = getComputedStyle(document.documentElement).getPropertyValue(token).trim()
  return value || fallback
}

const accentColor = (): string => themeColor('--onms-topology-accent', DEFAULT_NODE_COLOR)

/**
 * How an unemphasized link is drawn. Only for links still on the default color,
 * since the stored value is persisted and cannot follow the theme.
 */
const linkBaseColor = (stored: unknown): string =>
  stored === DEFAULT_NODE_COLOR || stored == null
    ? themeColor('--onms-topology-link', '#7d8ca3')
    : String(stored)

const HOVER_HALO_LIGHT = 'rgba(255, 255, 255, 0.85)'
const HOVER_HALO_DARK = 'rgba(38, 44, 69, 0.85)'

/**
 * Themed replacement for sigma's default node hover/selection renderer.
 * The stock drawDiscNodeHover hardcodes a WHITE halo box behind the label;
 * in dark mode our label default is near-white, so hovered and selected
 * node labels rendered white-on-white. Same geometry as the original, with
 * a theme-aware halo, then sigma's own label routine on top (which already
 * follows the theme-aware labelColor set in applyViewStyle).
 */
const drawThemedNodeHover = (
  context: CanvasRenderingContext2D,
  data: { x: number; y: number; size: number; label?: string | null },
  settings: { labelSize: number; labelFont: string; labelWeight: string }
) => {
  const { labelSize: size, labelFont: font, labelWeight: weight } = settings
  context.font = `${weight} ${size}px ${font}`

  const dark = appStore.theme === 'open-dark'
  context.fillStyle = dark ? HOVER_HALO_DARK : HOVER_HALO_LIGHT
  context.shadowOffsetX = 0
  context.shadowOffsetY = 0
  // Softer than sigma's default 8: the halo is translucent now, and a heavy
  // blur reads through it as a grey smudge rather than a shadow.
  context.shadowBlur = 5
  context.shadowColor = 'rgba(0, 0, 0, 0.35)'

  const PADDING = 2
  if (typeof data.label === 'string') {
    const textWidth = context.measureText(data.label).width
    const boxWidth = Math.round(textWidth + 5)
    const boxHeight = Math.round(size + 2 * PADDING)
    const radius = Math.max(data.size, size / 2) + PADDING
    const angleRadian = Math.asin(boxHeight / 2 / radius)
    const xDeltaCoord = Math.sqrt(Math.abs(radius ** 2 - (boxHeight / 2) ** 2))
    context.beginPath()
    context.moveTo(data.x + xDeltaCoord, data.y + boxHeight / 2)
    context.lineTo(data.x + radius + boxWidth, data.y + boxHeight / 2)
    context.lineTo(data.x + radius + boxWidth, data.y - boxHeight / 2)
    context.lineTo(data.x + xDeltaCoord, data.y - boxHeight / 2)
    context.arc(data.x, data.y, radius, angleRadian, -angleRadian)
    context.closePath()
    context.fill()
  } else {
    context.beginPath()
    context.arc(data.x, data.y, data.size + PADDING, 0, Math.PI * 2)
    context.closePath()
    context.fill()
  }
  context.shadowOffsetX = 0
  context.shadowOffsetY = 0
  context.shadowBlur = 0

  drawDiscNodeLabel(
    context,
    data as Parameters<typeof drawDiscNodeLabel>[1],
    settings as Parameters<typeof drawDiscNodeLabel>[2]
  )
}

/**
 * Apply the view's label-color defaults to the renderer. Unset fields fall
 * back to a theme-appropriate default -- sigma draws labels onto a canvas,
 * which can't pick up the app's CSS theme variables -- while link labels
 * keep sigma's own default of taking the link's color attribute.
 */
const applyViewStyle = () => {
  if (!sigma) {
    return
  }
  // Per-view label colors belong to the open CUSTOM view. A discovered
  // source renders beside currentView without clearing it, so without this
  // gate the last custom view's colors would bleed into discovered renders.
  const style = store.discoveredGraph === null ? store.viewStyle : undefined
  const themeDefault = appStore.theme === 'open-dark' ? '#dfe3e8' : '#000'
  sigma.setSetting(
    'labelColor',
    (style?.nodeLabelColor ? { color: style.nodeLabelColor } : { color: themeDefault }) as never
  )
  sigma.setSetting(
    'edgeLabelColor',
    (style?.linkLabelColor ? { color: style.linkLabelColor } : { attribute: 'color' }) as never
  )
  sigma.refresh()
}

watch(
  [() => store.viewStyle, () => store.discoveredGraph],
  () => applyViewStyle(),
  { deep: true }
)

// Re-derive the canvas-drawn label default when the app theme flips.
watch(
  () => appStore.theme,
  () => applyViewStyle()
)

/* ---- Ghost links (assisted composition) -------------------------------- */

/**
 * Discovered adjacencies between placed nodes with no link yet. linkCount /
 * placedCount are the reactivity bridge to the (non-reactive) graphology
 * graph: both change whenever links or nodes are added or removed.
 */
const ghostHints = computed<LinkHint[]>(() => {
  if (!store.isEditMode || store.discoveredGraph !== null || !store.isLinkHintsEnabled) {
    return []
  }
  void linkCount.value
  void placedCount.value
  return computeGhostLinks(store.neighborsByNode, store.placedNodeIds, (a, b) => {
    if (!graph || !graph.hasNode(a) || !graph.hasNode(b)) {
      return true
    }
    return graph.hasEdge(a, b) || graph.hasEdge(b, a)
  })
})

const ghostLineAttrs = (hint: LinkHint, _cameraVersion: number) => {
  void _cameraVersion
  if (!sigma || !graph || !graph.hasNode(hint.sourceId) || !graph.hasNode(hint.targetId)) {
    return { display: 'none' }
  }
  const s = sigma.graphToViewport({
    x: graph.getNodeAttribute(hint.sourceId, 'x') as number,
    y: graph.getNodeAttribute(hint.sourceId, 'y') as number
  })
  const t = sigma.graphToViewport({
    x: graph.getNodeAttribute(hint.targetId, 'x') as number,
    y: graph.getNodeAttribute(hint.targetId, 'y') as number
  })
  return { x1: s.x, y1: s.y, x2: t.x, y2: t.y }
}

/** Adopt a ghost: it becomes a real, persisted link carrying its binding. */
const adoptHint = (hint: LinkHint) => {
  if (!graph || !store.isEditMode) {
    return
  }
  if (graph.hasEdge(hint.sourceId, hint.targetId) || graph.hasEdge(hint.targetId, hint.sourceId)) {
    return
  }
  const id = newLinkId()
  const attrs = {
    size: 2,
    color: DEFAULT_NODE_COLOR,
    origin: 'discovered',
    binding: { ...hint.binding }
  }
  graph.addEdgeWithKey(id, hint.sourceId, hint.targetId, attrs)
  linkCount.value = graph.size
  store.selectOnly(id)
  pushCommand({
    label: 'Add discovered link',
    do: () => {
      if (!graph || graph.hasEdge(id)) {
        return
      }
      if (!graph.hasNode(hint.sourceId) || !graph.hasNode(hint.targetId)) {
        return
      }
      graph.addEdgeWithKey(id, hint.sourceId, hint.targetId, attrs)
      linkCount.value = graph.size
    },
    undo: () => {
      if (!graph || !graph.hasEdge(id)) {
        return
      }
      graph.dropEdge(id)
      linkCount.value = graph.size
    }
  })
}

/** A node's persisted icon override (built-in glyph key or `asset:<id>`). */
const getNodeIconOverride = (id: string): string | undefined => {
  if (!graph || !graph.hasNode(id)) {
    return undefined
  }
  return (graph.getNodeAttribute(id, 'iconOverride') as string | undefined) || undefined
}

/**
 * Set or clear (undefined) a node's icon override. The nodeReducer resolves
 * it on the next repaint; persisted by serialize() with the view.
 */
const setNodeIconOverride = (id: string, override: string | undefined) => {
  if (!graph || !graph.hasNode(id)) {
    return
  }
  if (override) {
    graph.setNodeAttribute(id, 'iconOverride', override)
  } else {
    graph.removeNodeAttribute(id, 'iconOverride')
  }
  sigma?.refresh()
}

/**
 * Export the current map as a raster image. Uses @sigma/export-image, which
 * re-renders the scene into a temporary renderer (so the WebGL layers capture
 * correctly) and downloads it. `fileName` is the base name; the format
 * extension is appended by the library. Note: free-standing text labels are
 * DOM overlays and are not yet included in the export.
 */
const exportImage = async (fileName: string, format: 'png' | 'jpeg' = 'png'): Promise<void> => {
  if (!sigma) {
    return
  }
  // Match the canvas: a hardcoded white flattened a dark view while its labels
  // kept their dark-theme color. Opaque either way, since JPEG has no alpha and
  // an exported map is usually pasted into a document.
  const background = getComputedStyle(document.documentElement)
    .getPropertyValue('--onms-background')
    .trim() || '#ffffff'
  await downloadAsImage(sigma, { format, fileName, backgroundColor: background })
}

defineExpose({
  // Reset to the default centered view, but zoomed out slightly so the
  // edge nodes' labels aren't clipped: sigma's auto-fit bounds the node
  // x/y positions only, not the rendered label width that extends past
  // each node. The default reset state is { x: 0.5, y: 0.5, ratio: 1 };
  // a ratio above 1 zooms out, leaving margin on all sides.
  fit: fitCamera,
  centerOnNode,
  serialize,
  loadView,
  loadDiscoveredGraph,
  getLink,
  setLinkLabel,
  placeNeighbor,
  getNodeIconOverride,
  setNodeIconOverride,
  exportImage
})
</script>

<style scoped>
/* An SVG presentation attribute cannot take a CSS variable, so the link-draw
   preview is styled here instead. */
.topology-link-preview {
  stroke: var(--onms-topology-accent);
}

.topology-canvas-root {
  position: relative;
  width: 100%;
  height: 100%;
  /* Shrinkable. A 500px floor here and on .topology-canvas below meant the page
     could not absorb a short viewport: the flex column had a ~740px fixed cost
     plus the Explore panel, so anything under roughly 1190px tall overflowed
     into the footer instead of the canvas getting smaller. */
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: var(--onms-background);
  border: 1px solid var(--onms-border-on-surface);
  transition: box-shadow 100ms ease-in;
}

.topology-canvas-root.is-drop-target {
  box-shadow: inset 0 0 0 2px var(--onms-topology-accent);
}

.topology-canvas-stats {
  position: absolute;
  bottom: 0.5rem;
  right: 0.5rem;
  z-index: 1;
  background: var(--onms-surface);
  color: var(--onms-primary-text-on-surface);
  border: 1px solid var(--onms-border-on-surface);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  display: flex;
  gap: 1rem;
  pointer-events: none;
  font-family: monospace;
}

.topology-canvas {
  flex: 1 1 auto;
  width: 100%;
  /* Enough to stay usable, small enough to shrink out of the way. */
  min-height: 180px;
}

/* Background image layer: below the (transparent) sigma canvases, mouse-inert
   unless adjusting -- then raised above the canvas so the image/handle take
   the drag, while the rest of the layer stays click-through. */
.topology-background-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.topology-background-image {
  position: absolute;
  pointer-events: none;
  user-select: none;
}

.topology-background-layer.is-adjusting {
  z-index: 4;
}

.topology-background-layer.is-adjusting .topology-background-image {
  pointer-events: auto;
  cursor: move;
  outline: 2px dashed var(--onms-topology-accent);
}

.topology-background-handle {
  position: absolute;
  width: 14px;
  height: 14px;
  transform: translate(-50%, -50%);
  background: var(--onms-topology-accent);
  border: 2px solid #fff;
  border-radius: 3px;
  pointer-events: auto;
  cursor: nwse-resize;
}

/* Ghost links: above the canvas so they're clickable, but only the dashed
   stroke takes the mouse -- everything else passes through to sigma. */
.topology-ghost-layer {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
  z-index: 3;
}

.topology-ghost-link {
  stroke: var(--onms-topology-accent);
  stroke-width: 3;
  stroke-dasharray: 4 6;
  opacity: 0.45;
  pointer-events: none;
}

.topology-ghost-hit {
  stroke: transparent;
  stroke-width: 16;
  pointer-events: stroke;
  cursor: copy;
}

.topology-ghost:hover .topology-ghost-link {
  opacity: 0.9;
  stroke-width: 4;
}

/* Annotation shapes: the visible layer sits below the sigma canvases and is
   mouse-inert; the hit layer sits above them, but only shape *borders* (and
   the resize handle) accept the mouse, so frame interiors stay click-through
   to the nodes they surround. */
.topology-shapes-layer {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.topology-shape {
  stroke-width: 2;
}

.topology-shape.is-selected {
  stroke: var(--onms-topology-accent);
  stroke-width: 3;
  stroke-dasharray: 8 4;
}

.topology-shape-title {
  font-size: 13px;
  font-weight: 600;
  text-anchor: middle;
  user-select: none;
}

.topology-shapes-hit-layer {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
  z-index: 4;
}

.topology-shapes-hit-layer .topology-shape-hit {
  fill: none;
  stroke: transparent;
  stroke-width: 14;
  pointer-events: stroke;
  cursor: move;
}

.topology-shapes-hit-layer .topology-shape-resize-handle {
  fill: var(--onms-topology-accent);
  stroke: #fff;
  stroke-width: 2;
  pointer-events: all;
  cursor: nwse-resize;
}

.topology-shape-draw-overlay {
  position: absolute;
  inset: 0;
  z-index: 5;
  cursor: crosshair;
}

.topology-shape-draft {
  position: absolute;
  border: 2px dashed var(--onms-topology-accent);
  background: color-mix(in srgb, var(--onms-topology-accent) 8%, transparent);
  border-radius: 6px;
  pointer-events: none;
}

.topology-rubber-band {
  position: absolute;
  pointer-events: none;
  border: 1px dashed var(--onms-topology-accent);
  background: color-mix(in srgb, var(--onms-topology-accent) 8%, transparent);
  z-index: 2;
}

.topology-link-preview {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 2;
}

.topology-canvas-root.is-link-draw-mode {
  cursor: crosshair;
}

.topology-canvas-root.is-link-draw-mode .topology-canvas canvas {
  cursor: crosshair;
}

/* Pointer over a hovered link signals it's clickable. Draw mode keeps its
   crosshair (the rule above wins by being listed where draw mode is active,
   but be explicit so a hovered link mid-draw doesn't flip to a pointer). */
.topology-canvas-root.is-hovering-link:not(.is-link-draw-mode) .topology-canvas canvas {
  cursor: pointer;
}

.topology-labels-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 3;
}

.topology-label {
  position: absolute;
  transform: translate(-50%, -50%);
  pointer-events: auto;
  cursor: grab;
  user-select: none;
  padding: 2px 6px;
  border-radius: 3px;
  background: var(--onms-surface);
  font-size: 12px;
  font-weight: 500;
  color: var(--onms-primary-text-on-surface);
  white-space: nowrap;
  /* Tokenized: a black shadow this faint is invisible on a dark surface, where
     the token resolves to a light translucent edge instead. */
  box-shadow: 0 1px 2px var(--onms-border-on-surface);
  border: 1px solid transparent;
}

.topology-label:hover {
  border-color: var(--onms-border-on-surface);
}

.topology-label.is-selected {
  border-color: var(--onms-topology-accent);
  background: var(--onms-surface);
}

.topology-label.is-editing {
  background: var(--onms-surface);
  border-color: var(--onms-topology-accent);
  padding: 0;
  cursor: text;
}

.topology-label:active {
  cursor: grabbing;
}

.topology-label-input {
  border: none;
  outline: none;
  padding: 2px 6px;
  font: inherit;
  background: transparent;
  width: 12ch;
  min-width: 4ch;
  font-family: inherit;
}

.topology-label-text {
  display: inline-block;
}
</style>
