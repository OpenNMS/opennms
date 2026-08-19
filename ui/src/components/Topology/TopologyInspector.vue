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
  Selection-driven properties panel. Shows (and, for labels, edits) the
  currently-selected canvas element: a free-standing label's text/color/
  size, or a placed node's OpenNMS detail and current alarm severity.
  Node and link editing beyond this is a follow-up.
-->

<template>
  <div class="ti-resizable" :style="{ width: panelWidth + 'px' }">
    <!-- Drag strip on the canvas-facing edge: right of the View-mode (left)
         panel, left of the Edit-mode (right) panel. Width persists. -->
    <div
      class="ti-resize-handle"
      :class="variant === 'props' ? 'ti-resize-left' : 'ti-resize-right'"
      title="Drag to resize"
      @mousedown.prevent="startResize"
    />
  <OnmsCard class="topology-inspector">
    <template #title>
      <span class="ti-title">{{ variant === 'props' ? 'Properties' : 'Inspector' }}</span>
    </template>
    <template #content>
      <!-- Nothing selected (full/View only) -->
      <p v-if="kind === 'none' && variant === 'full'" class="ti-empty">
        Select a node, link, label, or box to see its properties.
      </p>

      <!-- Multiple items (full/View only) -->
      <p v-else-if="kind === 'multi' && variant === 'full'" class="ti-empty">
        {{ store.selectedIds.length }} items selected.
      </p>

      <!-- A free-standing label (editable in Edit mode, read-only in View) -->
      <div v-else-if="kind === 'label' && label" class="ti-section">
        <div class="ti-field">
          <label class="ti-label">Text</label>
          <OnmsInputText v-model="labelText" class="ti-input" :disabled="!editable" />
        </div>
        <div class="ti-field">
          <label class="ti-label">Color</label>
          <OnmsColorPicker
            :model-value="labelColor"
            :disabled="!editable"
            @update:model-value="onLabelColor"
          />
        </div>
        <div class="ti-field">
          <label class="ti-label">Font size</label>
          <OnmsInputNumber v-model="labelFontSize" :min="8" :max="48" show-buttons button-layout="horizontal" :disabled="!editable" />
        </div>
      </div>

      <!-- A placed OpenNMS node (detail is read-only; full/View only) -->
      <div v-else-if="kind === 'node' && variant === 'full'" class="ti-section">
        <div v-if="nodeLoading" class="ti-empty">Loading node…</div>
        <template v-else-if="nodeDetail">
          <div class="ti-node-header">
            <span class="ti-severity-dot" :style="{ background: severityColor(nodeSeverity) }" />
            <span class="ti-node-label">{{ nodeDetail.label }}</span>
          </div>
          <h4 class="ti-section-title">Node Details</h4>
          <dl class="ti-detail">
            <dt>Node ID</dt><dd>{{ nodeDetail.id }}</dd>
            <dt>Node label</dt><dd>{{ nodeDetail.label }}</dd>
            <!-- The legacy map shows the sysObjectId as "Enterprise OID", and
                 only when the node has one (it needs SNMP data). -->
            <template v-if="nodeDetail.sysObjectId">
              <dt>Enterprise OID</dt><dd>{{ nodeDetail.sysObjectId }}</dd>
            </template>
            <dt>Highest Alarm Severity</dt><dd>{{ nodeSeverity || 'Normal / none' }}</dd>
            <dt>Location</dt><dd>{{ nodeDetail.location || '—' }}</dd>
            <dt>Foreign source</dt><dd>{{ nodeDetail.foreignSource || '—' }}</dd>
            <dt>Categories</dt>
            <dd>{{ nodeDetail.categories?.length ? nodeDetail.categories.map((c) => c.name).join(', ') : '—' }}</dd>
          </dl>
          <!-- Operator-configured info-panel items (etc/infopanel templates),
               rendered server-side and sanitized before display. -->
          <section
            v-for="item in infoPanelItems"
            :key="item.title"
            class="ti-infopanel-item"
          >
            <h4 class="ti-infopanel-title">{{ item.title }}</h4>
            <div class="ti-infopanel-html" v-html="sanitizeHtml(item.html)" />
          </section>
        </template>
        <p v-else class="ti-empty">Node details unavailable.</p>
        <!-- What the provider calls this vertex, as opposed to what OpenNMS
             knows about the node behind it. Outside the node-detail branch, so a
             failed node lookup does not lose it too. -->
        <template v-if="technicalDetail.length > 0">
          <h4 class="ti-section-title">Technical Details</h4>
          <dl class="ti-detail">
            <template v-for="row in technicalDetail" :key="row.label">
              <dt>{{ row.label }}</dt>
              <dd>
                <span v-if="row.color" class="ti-detail-dot" :style="{ background: row.color }" />
                {{ row.value }}
              </dd>
            </template>
          </dl>
        </template>
      </div>

      <!-- A discovered vertex that is not an OnmsNode: an application, a
           business service, a GraphML group. The provider's own properties are
           all the detail there is. -->
      <div v-else-if="kind === 'vertex' && variant === 'full'" class="ti-section">
        <div class="ti-node-header">
          <span class="ti-node-label">{{ discoveredVertex?.label }}</span>
        </div>
        <h4 class="ti-section-title">Technical Details</h4>
        <dl class="ti-detail">
          <template v-for="row in technicalDetail" :key="row.label">
            <dt>{{ row.label }}</dt>
            <dd>
              <span v-if="row.color" class="ti-detail-dot" :style="{ background: row.color }" />
              {{ row.value }}
            </dd>
          </template>
        </dl>
      </div>

      <!-- A link between two nodes -->
      <div v-else-if="kind === 'link' && link" class="ti-section">
        <div class="ti-node-header">
          <span class="ti-link-endpoints">{{ link.sourceLabel }} → {{ link.targetLabel }}</span>
        </div>
        <h4 class="ti-section-title">Technical Details</h4>
        <dl class="ti-detail">
          <dt>Source</dt><dd>{{ link.sourceLabel }}</dd>
          <dt>Target</dt><dd>{{ link.targetLabel }}</dd>
        </dl>
        <!-- What discovery knows about this adjacency. The old map colored the
             line red and left the operator to work out which end and which
             interface; naming them is the useful part. -->
        <div v-for="b in linkBindings" :key="b.protocol + (b.sourcePort ?? '')" class="ti-binding">
          <p class="ti-binding-line">
            Discovered via {{ b.protocol.toUpperCase() }}<template v-if="b.sourcePort">
              — {{ b.sourcePort }} ↔ {{ b.targetPort ?? '?' }}</template>
          </p>
          <dl v-if="b.sourceIfIndex != null || b.lastPollTime" class="ti-detail ti-binding-detail">
            <template v-if="b.sourceIfIndex != null">
              <dt>Source ifIndex</dt><dd>{{ b.sourceIfIndex }}</dd>
            </template>
            <template v-if="b.lastPollTime">
              <dt>Last confirmed</dt><dd>{{ b.lastPollTime }}</dd>
            </template>
          </dl>
        </div>
        <div class="ti-field">
          <label class="ti-label">Link label</label>
          <OnmsInputText v-model="linkLabel" class="ti-input" placeholder="(none)" :disabled="!editable" />
          <p v-if="editable" class="ti-hint">Applies as you type — <strong>Save</strong> the view to keep it.</p>
        </div>
        <div v-for="item in edgeInfoPanelItems" :key="item.title + item.order" class="ti-infopanel-item">
          <h4 class="ti-infopanel-title">{{ item.title }}</h4>
          <div class="ti-infopanel-html" v-html="sanitizeHtml(item.html)" />
        </div>
      </div>

      <!-- An annotation shape (frame/box): title, rect/ellipse, colors,
           opacity. Editable in Edit mode, read-only in View. -->
      <div v-else-if="kind === 'shape' && shape" class="ti-section">
        <div class="ti-field">
          <label class="ti-label">Title</label>
          <OnmsInputText v-model="shapeLabel" class="ti-input" placeholder="(none)" :disabled="!editable" />
        </div>
        <div class="ti-field">
          <label class="ti-label">Shape</label>
          <div class="ti-row">
            <OnmsButton
              label="Box"
              size="small"
              :variant="shapeType === 'rect' ? 'filled' : 'outlined'"
              :disabled="!editable"
              @click="shapeType = 'rect'"
            />
            <OnmsButton
              label="Ellipse"
              size="small"
              :variant="shapeType === 'ellipse' ? 'filled' : 'outlined'"
              :disabled="!editable"
              @click="shapeType = 'ellipse'"
            />
          </div>
        </div>
        <div class="ti-field">
          <label class="ti-label">Border color</label>
          <OnmsColorPicker
            :model-value="shape.stroke ?? '#64748b'"
            :disabled="!editable"
            @update:model-value="onShapeStroke"
          />
        </div>
        <div class="ti-field">
          <label class="ti-label">Fill color</label>
          <OnmsColorPicker
            :model-value="shape.fill ?? '#cbd5e1'"
            :disabled="!editable"
            @update:model-value="onShapeFill"
          />
        </div>
        <div class="ti-field">
          <label class="ti-label">Fill opacity</label>
          <input
            type="range"
            min="0"
            max="0.9"
            step="0.05"
            :value="shape.opacity ?? 0.35"
            :disabled="!editable"
            @input="onShapeOpacity"
          />
        </div>
        <p v-if="editable" class="ti-hint">
          Drag the border to move, the corner handle to resize — <strong>Save</strong> the view to keep it.
        </p>
      </div>

      <p v-else-if="variant === 'full'" class="ti-empty">Link selected.</p>

      <!-- Edit-mode node: icon picker. Automatic (sysObjectId-derived) /
           built-in glyphs / uploaded icon assets, applied to the canvas
           immediately and persisted with the view on Save. -->
      <div v-else-if="kind === 'node' && variant === 'props'" class="ti-section">
        <div class="ti-field">
          <label class="ti-label">Icon</label>
          <div class="ti-icon-grid">
            <button
              type="button"
              class="ti-icon-option"
              :class="{ 'is-selected': !iconOverride }"
              title="Automatic (from SNMP)"
              @click="applyIconOverride(undefined)"
            >
              Auto
            </button>
            <button
              v-for="glyph in builtinIcons"
              :key="glyph.id"
              type="button"
              class="ti-icon-option ti-icon-builtin"
              :class="{ 'is-selected': iconOverride === glyph.id }"
              :title="glyph.label"
              @click="applyIconOverride(glyph.id)"
            >
              <img :src="glyph.url" :alt="glyph.label" />
            </button>
            <button
              v-for="asset in iconAssets"
              :key="asset.id"
              type="button"
              class="ti-icon-option"
              :class="{ 'is-selected': iconOverride === 'asset:' + asset.id }"
              :title="asset.name"
              @click="applyIconOverride('asset:' + asset.id)"
            >
              <img :src="assetUrl(asset.id)" :alt="asset.name" />
            </button>
          </div>
          <OnmsButton label="Upload icon…" size="small" variant="text" @click="iconFileInput?.click()" />
          <input
            ref="iconFileInput"
            type="file"
            accept="image/png,image/jpeg,image/gif,image/webp"
            class="ti-hidden-input"
            @change="onIconFileChosen"
          />
          <p class="ti-hint">Applies immediately — <strong>Save</strong> the view to keep it.</p>
        </div>
        <!-- Phase 2 assisted composition: this node's discovered neighbors
             that aren't on the canvas yet; one click places and links. -->
        <div v-if="unplacedNeighbors.length > 0" class="ti-field">
          <label class="ti-label">Discovered neighbors</label>
          <ul class="ti-neighbors">
            <li v-for="n in unplacedNeighbors" :key="n.neighborNodeId + n.linkType" class="ti-neighbor-row">
              <span class="ti-neighbor-label" :title="n.localPort ? `${n.localPort} ↔ ${n.remotePort ?? '?'}` : undefined">
                {{ n.neighborLabel }}
                <span class="ti-neighbor-proto">{{ n.linkType.toUpperCase() }}</span>
              </span>
              <OnmsButton
                label="Add"
                size="small"
                variant="text"
                title="Place this neighbor and link it"
                @click="addNeighbor(n)"
              />
            </li>
          </ul>
        </div>
      </div>

      <!-- Edit-mode Properties panel with nothing selected: the hint, plus
           the view background controls (pick/upload an image, opacity,
           adjust placement, remove). The panel is always present (reserves
           layout) so selecting a link/label never shifts the canvas. -->
      <div v-else-if="variant === 'props'" class="ti-section">
        <p class="ti-empty">Select a node, link, label, or box to edit its properties.</p>
        <div class="ti-field">
          <label class="ti-label">Canvas</label>
          <label class="ti-check">
            <input
              type="checkbox"
              :checked="store.showCanvasStats"
              @change="store.setShowCanvasStats(($event.target as HTMLInputElement).checked)"
            />
            Show stats overlay
          </label>
          <div class="ti-field">
            <label class="ti-label">Node label color</label>
            <div class="ti-row">
              <OnmsColorPicker
                :model-value="store.viewStyle?.nodeLabelColor ?? '#000000'"
                @update:model-value="onNodeLabelColor"
              />
              <OnmsButton
                v-if="store.viewStyle?.nodeLabelColor"
                label="Auto"
                size="small"
                variant="text"
                title="Follow the light/dark theme"
                @click="store.setViewStyle({ nodeLabelColor: undefined })"
              />
              <span v-else class="ti-inline-hint">automatic (theme)</span>
            </div>
          </div>
          <div class="ti-field">
            <label class="ti-label">Link label color</label>
            <div class="ti-row">
              <OnmsColorPicker
                :model-value="store.viewStyle?.linkLabelColor ?? '#9aa7b8'"
                @update:model-value="onLinkLabelColor"
              />
              <OnmsButton
                v-if="store.viewStyle?.linkLabelColor"
                label="Auto"
                size="small"
                variant="text"
                title="Follow the light/dark theme"
                @click="store.setViewStyle({ linkLabelColor: undefined })"
              />
              <span v-else class="ti-inline-hint">automatic (theme)</span>
            </div>
          </div>
          <p class="ti-hint">
            Custom label colors save with the view and don't follow the light/dark
            theme — <strong>Auto</strong> re-derives them from it. The stats overlay
            is a personal preference.
          </p>
        </div>
        <div class="ti-field">
          <label class="ti-label">Background</label>
          <template v-if="store.background?.ref">
            <div class="ti-field">
              <label class="ti-label">Opacity</label>
              <input
                type="range"
                min="0.1"
                max="1"
                step="0.05"
                :value="store.background?.opacity ?? 0.5"
                @input="onBackgroundOpacity"
              />
            </div>
            <div class="ti-row">
              <OnmsButton
                :label="store.isBackgroundAdjustMode ? 'Done adjusting' : 'Adjust position/size'"
                size="small"
                :variant="store.isBackgroundAdjustMode ? 'filled' : 'outlined'"
                @click="store.setBackgroundAdjustMode(!store.isBackgroundAdjustMode)"
              />
              <OnmsButton label="Remove" size="small" severity="danger" variant="text" @click="removeBackground" />
            </div>
          </template>
          <div class="ti-icon-grid">
            <button
              v-for="asset in backgroundAssets"
              :key="asset.id"
              type="button"
              class="ti-icon-option ti-bg-option"
              :class="{ 'is-selected': store.background?.ref === 'asset:' + asset.id }"
              :title="asset.name"
              @click="chooseBackground(asset)"
            >
              <img :src="assetUrl(asset.id)" :alt="asset.name" />
            </button>
          </div>
          <OnmsButton label="Upload background…" size="small" variant="text" @click="bgFileInput?.click()" />
          <input
            ref="bgFileInput"
            type="file"
            accept="image/png,image/jpeg,image/gif,image/webp"
            class="ti-hidden-input"
            @change="onBackgroundFileChosen"
          />
          <p class="ti-hint"><strong>Save</strong> the view to keep background changes.</p>
        </div>
      </div>
    </template>
  </OnmsCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { OnmsButton, OnmsCard, OnmsColorPicker, OnmsInputNumber, OnmsInputText } from '@opennms/onms-ui'
import { useTopologyStore } from '@/stores/topologyStore'
import { isLabelId, isShapeId, nodeIdFromPlacedId } from '@/components/Topology/nodeIds'
import { severityColor } from '@/components/Topology/severity'
import {
  DEVICE_ICON_SVG,
  powerStateColor,
  powerStateForIconKey,
  powerStateLabel
} from '@/components/Topology/deviceIcons'
import { getNodeById } from '@/services/nodeService'
import {
  getEdgeInfoPanel,
  getNodeInfoPanel,
  assetUrl,
  listAssets,
  uploadAsset,
  type NodeInfoPanelItem,
  type TopologyAssetMeta
} from '@/services/topologyService'
import DOMPurify from 'dompurify'
import type { Node } from '@/types'
import type { CanvasLinkBinding, DiscoveredNeighbor } from '@/types/topology'


/** Minimal read/write surface the canvas exposes (via defineExpose). */
interface CanvasLinkApi {
  getLink: (id: string) => {
    label: string
    sourceLabel: string
    targetLabel: string
    sourceId: string
    targetId: string
    origin: 'user' | 'discovered'
    binding?: CanvasLinkBinding
  } | null
  setLinkLabel: (id: string, label: string) => void
  placeNeighbor: (fromId: string, neighbor: DiscoveredNeighbor) => void
  getNodeIconOverride: (id: string) => string | undefined
  setNodeIconOverride: (id: string, override: string | undefined) => void
}

const props = defineProps<{
  canvas: CanvasLinkApi | null
  /**
   * 'full' (View): node detail + label/link + empty/multi states.
   * 'props' (Edit): only the editable label/link property fields; the page
   * mounts this variant solely when a label or link is selected.
   */
  variant?: 'full' | 'props'
}>()

const variant = computed<'full' | 'props'>(() => props.variant ?? 'full')

// --- Panel resize -----------------------------------------------------------
// Drag the canvas-facing edge to resize; the width persists across sessions.
// The canvas pane flexes to absorb the change and repaints via its own
// ResizeObserver (preserving zoom/pan), so no coordination is needed here.
const WIDTH_STORAGE_KEY = 'opennms.topology.inspectorWidth'
const WIDTH_MIN = 220
const WIDTH_MAX = 640
const WIDTH_DEFAULT = 288 // matches the previous fixed 18rem
const storedWidth = Number(localStorage.getItem(WIDTH_STORAGE_KEY))
const panelWidth = ref(
  Number.isFinite(storedWidth) && storedWidth >= WIDTH_MIN && storedWidth <= WIDTH_MAX
    ? storedWidth
    : WIDTH_DEFAULT
)
let resizeStart: { x: number; width: number } | null = null
const onResizeMove = (e: MouseEvent) => {
  if (!resizeStart) {
    return
  }
  const delta = e.clientX - resizeStart.x
  // View mode: panel sits left, its right edge drags (+x grows). Edit mode:
  // panel sits right, its left edge drags (+x shrinks).
  const next = variant.value === 'props' ? resizeStart.width - delta : resizeStart.width + delta
  panelWidth.value = Math.min(WIDTH_MAX, Math.max(WIDTH_MIN, next))
}
const endResize = () => {
  if (!resizeStart) {
    return
  }
  resizeStart = null
  window.removeEventListener('mousemove', onResizeMove)
  window.removeEventListener('mouseup', endResize)
  document.body.style.userSelect = ''
  localStorage.setItem(WIDTH_STORAGE_KEY, String(panelWidth.value))
}
const startResize = (e: MouseEvent) => {
  resizeStart = { x: e.clientX, width: panelWidth.value }
  document.body.style.userSelect = 'none' // no text selection mid-drag
  window.addEventListener('mousemove', onResizeMove)
  window.addEventListener('mouseup', endResize)
}
onBeforeUnmount(endResize)

const store = useTopologyStore()

// Properties are editable only in Edit mode; View mode is read-only.
const editable = computed<boolean>(() => store.isEditMode)

const selectedId = computed<string | null>(() =>
  store.selectedIds.length === 1 ? store.selectedIds[0] : null
)

/**
 * The selected canvas node, when it belongs to the discovered graph. This is
 * where a provider's own vocabulary lives for a vertex that is not an OnmsNode.
 */
const discoveredVertex = computed(() =>
  selectedId.value ? store.discoveredGraph?.nodes.find(n => n.id === selectedId.value) : undefined
)

/**
 * The OnmsNode behind the selection. The canvas id encodes it for custom views
 * and for discovered graphs with one vertex per node; where several vertices
 * share a node (an application watching two services on one host) the id
 * cannot, so the vertex carries it instead.
 */
const selectedNodeId = computed<number | null>(() => {
  const id = selectedId.value
  if (!id) {
    return null
  }
  return nodeIdFromPlacedId(id) ?? discoveredVertex.value?.nodeId ?? null
})

// Keys arrive in the provider's vocabulary and there is no fixed set to map, so
// they are split on camel case and sentence-cased. These two would otherwise
// read as "Application id" and "Ip address".
const KEY_ACRONYMS: Record<string, string> = { id: 'ID', ip: 'IP' }

const humanizeKey = (key: string): string =>
  key
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .split(' ')
    .map((word, i) => KEY_ACRONYMS[word.toLowerCase()]
      ?? (i === 0 ? word.charAt(0).toUpperCase() + word.slice(1) : word.toLowerCase()))
    .join(' ')

/** The selected vertex's provider properties, as display rows. */
const vertexDetail = computed<Array<{ label: string, value: string }>>(() =>
  Object.entries(discoveredVertex.value?.properties ?? {}).map(([key, value]) => ({
    label: humanizeKey(key),
    value
  }))
)

/**
 * The legacy map's "Technical Details": what the provider calls this vertex,
 * rather than what OpenNMS knows about the node behind it. Name, the
 * provider's own namespace-qualified id and icon key, then whatever else it
 * sent (IP address, vertex type, an application id).
 */
const technicalDetail = computed<Array<{ label: string, value: string, color?: string }>>(() => {
  const vertex = discoveredVertex.value
  if (!vertex) {
    return []
  }
  const rows: Array<{ label: string, value: string, color?: string }> =
    [{ label: 'Name', value: vertex.label }]
  if (vertex.namespace && vertex.vertexId) {
    rows.push({ label: 'ID', value: `${vertex.namespace}:${vertex.vertexId}` })
  }
  // Named next to its own color, so the canvas badge needs no legend.
  const powerState = powerStateForIconKey(vertex.icon)
  if (powerState) {
    rows.push({
      label: 'Power state',
      value: powerStateLabel(powerState),
      color: powerStateColor(powerState)
    })
  }
  if (vertex.icon) {
    rows.push({ label: 'Icon key', value: vertex.icon })
  }
  return [...rows, ...vertexDetail.value]
})

const kind = computed<'none' | 'multi' | 'label' | 'node' | 'shape' | 'vertex' | 'link'>(() => {
  if (store.selectedIds.length === 0) {
    return 'none'
  }
  if (store.selectedIds.length > 1) {
    return 'multi'
  }
  const id = selectedId.value as string
  if (isLabelId(id)) {
    return 'label'
  }
  if (isShapeId(id)) {
    return 'shape'
  }
  if (selectedNodeId.value !== null) {
    return 'node'
  }
  // A discovered vertex that is not an OnmsNode at all: an application, a
  // business service, a GraphML group. Its provider properties are the detail.
  if (discoveredVertex.value) {
    return 'vertex'
  }
  return 'link'
})

/* ---- Label editing (store-backed) ---- */
const label = computed(() => (selectedId.value && isLabelId(selectedId.value) ? store.getLabel(selectedId.value) : undefined))

const labelText = computed<string>({
  get: () => label.value?.text ?? '',
  set: text => label.value && store.updateLabel(label.value.id, { text })
})
const labelColor = computed<string>(() => label.value?.color ?? '#1d2939')
const onLabelColor = (color: string) => {
  if (label.value) {
    store.updateLabel(label.value.id, { color })
  }
}
const labelFontSize = computed<number>({
  get: () => label.value?.fontSize ?? 12,
  set: fontSize => label.value && store.updateLabel(label.value.id, { fontSize })
})

/* ---- Node detail (read-only, fetched on selection) ---- */
const nodeDetail = ref<Node | null>(null)

/** City and state if the asset names them, so the map has a caption. */
const geoPlace = computed<string>(() => {
  const asset = nodeDetail.value?.assetRecord as
    { city?: string, state?: string } | undefined
  return [asset?.city, asset?.state].filter(Boolean).join(', ')
})
const nodeLoading = ref(false)

/* ---- Operator-configured info-panel items (etc/infopanel templates) ---- */
const infoPanelItems = ref<NodeInfoPanelItem[]>([])

// Server HTML is sanitized before it ever reaches v-html. The templates are
// admin-authored, but they can interpolate node-derived data (e.g. a device's
// sysName), so we don't trust the output blindly.
const sanitizeHtml = (html: string): string => DOMPurify.sanitize(html)

watch(
  selectedId,
  async (id) => {
    infoPanelItems.value = []
    const nid = selectedNodeId.value
    if (id === null || nid === null) {
      return
    }
    infoPanelItems.value = await getNodeInfoPanel(nid)
  },
  { immediate: true }
)

const nodeSeverity = computed<string | undefined>(() => {
  const id = selectedId.value
  if (!id) {
    return undefined
  }
  const nid = selectedNodeId.value
  return nid !== null ? store.severities[nid] : undefined
})

watch(
  selectedId,
  async (id) => {
    nodeDetail.value = null
    const nid = selectedNodeId.value
    if (id === null || nid === null) {
      return
    }
    nodeLoading.value = true
    try {
      const res = await getNodeById(String(nid))
      nodeDetail.value = res === false ? null : res
    } finally {
      nodeLoading.value = false
    }
  },
  { immediate: true }
)

/* ---- Node icon picker (Edit mode) ---- */
const builtinIcons = (Object.keys(DEVICE_ICON_SVG) as Array<keyof typeof DEVICE_ICON_SVG>).map(id => ({
  id: id as string,
  label: id.charAt(0).toUpperCase() + id.slice(1),
  url: DEVICE_ICON_SVG[id]
}))

const iconOverride = ref<string | undefined>(undefined)
const iconAssets = ref<TopologyAssetMeta[]>([])
const iconFileInput = ref<HTMLInputElement | null>(null)
let iconAssetsLoaded = false

const applyIconOverride = (override: string | undefined) => {
  const id = selectedId.value
  if (!id || !props.canvas) {
    return
  }
  props.canvas.setNodeIconOverride(id, override)
  iconOverride.value = override
}

const onIconFileChosen = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) {
    return
  }
  const name = file.name.replace(/\.[^.]+$/, '') || file.name
  const created = await uploadAsset(name, 'icon', file)
  if (created === false) {
    return
  }
  iconAssets.value = await listAssets('icon')
  applyIconOverride('asset:' + created.id)
}

// Sync the current override on selection, and lazily load the uploaded-icon
// catalog the first time the picker is shown.
watch(
  [selectedId, kind],
  async ([id, k]) => {
    if (k !== 'node' || !id || !props.canvas) {
      iconOverride.value = undefined
      return
    }
    iconOverride.value = props.canvas.getNodeIconOverride(id)
    if (variant.value === 'props' && !iconAssetsLoaded) {
      iconAssetsLoaded = true
      iconAssets.value = await listAssets('icon')
    }
  },
  { immediate: true }
)

/* ---- Neighbor tray (Edit mode, node selected) ---- */
const unplacedNeighbors = computed<DiscoveredNeighbor[]>(() => {
  const id = selectedId.value
  if (!id || kind.value !== 'node' || variant.value !== 'props') {
    return []
  }
  const nid = nodeIdFromPlacedId(id)
  if (nid === null) {
    return []
  }
  const seen = new Set<number>()
  return (store.neighborsByNode[nid] ?? []).filter((n) => {
    if (store.placedNodeIds.has(String(n.neighborNodeId))) {
      return false
    }
    if (seen.has(n.neighborNodeId)) {
      return false
    }
    seen.add(n.neighborNodeId)
    return true
  })
})

const addNeighbor = (neighbor: DiscoveredNeighbor) => {
  const id = selectedId.value
  if (!id || !props.canvas) {
    return
  }
  props.canvas.placeNeighbor(id, neighbor)
}

/* ---- Canvas defaults (Edit mode, nothing selected) ---- */
const onNodeLabelColor = (nodeLabelColor: string) => {
  store.setViewStyle({ nodeLabelColor })
}

const onLinkLabelColor = (linkLabelColor: string) => {
  store.setViewStyle({ linkLabelColor })
}

/* ---- View background (Edit mode, nothing selected) ---- */
const backgroundAssets = ref<TopologyAssetMeta[]>([])
const bgFileInput = ref<HTMLInputElement | null>(null)
let backgroundAssetsLoaded = false

// Lazily load the background catalog the first time the empty Edit panel
// (which hosts the background controls) is shown.
watch(
  [kind, variant],
  async ([k, v]) => {
    const showsBackgroundControls = v === 'props' && (k === 'none' || k === 'multi')
    if (showsBackgroundControls && !backgroundAssetsLoaded) {
      backgroundAssetsLoaded = true
      backgroundAssets.value = await listAssets('background')
    }
  },
  { immediate: true }
)

/**
 * Use an asset as the view background. A first-time pick is placed centered
 * at the origin, 600 graph units wide at the image's own aspect ratio;
 * re-picking a different image keeps the current placement.
 */
const chooseBackground = (asset: TopologyAssetMeta) => {
  const current = store.background
  if (current?.x !== undefined && current.width) {
    store.setBackground({ ...current, type: 'image', ref: 'asset:' + asset.id })
    return
  }
  const img = new Image()
  img.onload = () => {
    const width = 600
    const aspect = img.naturalWidth > 0 ? img.naturalHeight / img.naturalWidth : 2 / 3
    const height = Math.max(50, Math.round(width * aspect))
    store.setBackground({
      type: 'image',
      ref: 'asset:' + asset.id,
      x: -width / 2,
      y: height / 2,
      width,
      height,
      opacity: 0.5
    })
  }
  img.src = assetUrl(asset.id)
}

const onBackgroundFileChosen = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) {
    return
  }
  const name = file.name.replace(/\.[^.]+$/, '') || file.name
  const created = await uploadAsset(name, 'background', file)
  if (created === false) {
    return
  }
  backgroundAssets.value = await listAssets('background')
  chooseBackground(created)
}

const onBackgroundOpacity = (event: Event) => {
  if (!store.background) {
    return
  }
  const opacity = Number((event.target as HTMLInputElement).value)
  store.setBackground({ ...store.background, opacity })
}

const removeBackground = () => store.setBackground(undefined)

/* ---- Shape (annotation frame) editing, store-backed ---- */
const shape = computed(() =>
  selectedId.value && isShapeId(selectedId.value) ? store.getShape(selectedId.value) : undefined
)

const shapeLabel = computed<string>({
  get: () => shape.value?.label ?? '',
  set: label => shape.value && store.updateShape(shape.value.id, { label })
})

const shapeType = computed<'rect' | 'ellipse'>({
  get: () => shape.value?.type ?? 'rect',
  set: type => shape.value && store.updateShape(shape.value.id, { type })
})

const onShapeStroke = (stroke: string) => {
  if (shape.value) {
    store.updateShape(shape.value.id, { stroke })
  }
}

const onShapeFill = (fill: string) => {
  if (shape.value) {
    store.updateShape(shape.value.id, { fill })
  }
}

const onShapeOpacity = (event: Event) => {
  if (shape.value) {
    store.updateShape(shape.value.id, { opacity: Number((event.target as HTMLInputElement).value) })
  }
}

/* ---- Link editing (reads/writes the canvas graph via the exposed API) ---- */
const link = ref<ReturnType<CanvasLinkApi['getLink']>>(null)

watch(
  selectedId,
  (id) => {
    link.value = id && kind.value === 'link' && props.canvas ? props.canvas.getLink(id) : null
  },
  { immediate: true }
)

/**
 * The discovery detail lines for the selected link. An adopted link carries
 * its own binding; otherwise (links in discovered views, hand-drawn links)
 * the per-node enlinkd adjacency is consulted: every protocol entry between
 * the two endpoints renders as its own line, so a pair discovered over both
 * LLDP and CDP shows both.
 */
const resolvedBindings = ref<CanvasLinkBinding[]>([])
// Edge-scoped etc/infopanel templates for the selected link (parity with the
// legacy map's edge context); same render/sanitize path as the node panels.
const edgeInfoPanelItems = ref<NodeInfoPanelItem[]>([])

const linkBindings = computed<CanvasLinkBinding[]>(() =>
  link.value?.binding ? [link.value.binding] : resolvedBindings.value
)

watch(
  link,
  async (l) => {
    resolvedBindings.value = []
    edgeInfoPanelItems.value = []
    if (!l) {
      return
    }
    const sourceNodeId = nodeIdFromPlacedId(l.sourceId)
    const targetNodeId = nodeIdFromPlacedId(l.targetId)
    if (sourceNodeId === null || targetNodeId === null) {
      return
    }
    getEdgeInfoPanel(sourceNodeId, targetNodeId, l.binding).then((items) => {
      if (link.value === l) {
        edgeInfoPanelItems.value = items
      }
    })
    if (l.binding) {
      return
    }
    const neighbors = await store.getNeighborsFor(sourceNodeId)
    // Still looking at the same link? (selection may have moved meanwhile)
    if (link.value !== l) {
      return
    }
    const seen = new Set<string>()
    resolvedBindings.value = neighbors
      .filter(n => n.neighborNodeId === targetNodeId)
      .filter((n) => {
        if (seen.has(n.linkType)) {
          return false
        }
        seen.add(n.linkType)
        return true
      })
      .map(n => ({
        protocol: n.linkType,
        sourcePort: n.localPort,
        targetPort: n.remotePort,
        sourceIfIndex: n.localIfIndex,
        lastPollTime: n.lastPollTime
      }))
  },
  { immediate: true }
)

const linkLabel = computed<string>({
  get: () => link.value?.label ?? '',
  set: (value) => {
    const id = selectedId.value
    if (!id || !props.canvas) {
      return
    }
    props.canvas.setLinkLabel(id, value)
    if (link.value) {
      link.value = { ...link.value, label: value }
    }
  }
})
</script>

<style scoped>
.ti-resizable {
  position: relative;
  /* Flex column rather than percentage heights: the panel must never grow past
     the space the body gives it, whatever detail arrives asynchronously. A
     percentage height only holds while every ancestor resolves one, and when it
     did not the panel pushed the layout instead of scrolling itself. */
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.topology-inspector {
  width: 100%;
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
}

/* The card puts its content in an inner wrapper; both have to be allowed to
   shrink or the scroll lands on the wrong element. */
.topology-inspector :deep(.p-card-body),
.topology-inspector :deep(.p-card-content) {
  min-height: 0;
}

.ti-resize-handle {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 6px;
  cursor: col-resize;
  z-index: 2;
}

.ti-resize-handle:hover {
  background: var(--onms-border-on-surface);
}

.ti-resize-right {
  right: -3px;
}

.ti-resize-left {
  left: -3px;
}

.ti-title {
  font-size: 1rem;
  font-weight: 600;
}

.ti-empty {
  color: var(--onms-secondary-text-on-surface);
  font-size: 0.875rem;
}

.ti-section {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.ti-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.ti-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--onms-secondary-text-on-surface);
}

.ti-input {
  width: 100%;
}

.ti-section-title {
  margin: 0.75rem 0 0.25rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--onms-primary-text-on-surface);
}

.ti-detail + .ti-node-header {
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--onms-border-on-surface);
}

.ti-node-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.ti-binding-detail {
  margin: 0.15rem 0 0.5rem;
}

.ti-binding-line {
  margin: 0;
}

.ti-detail-dot {
  display: inline-block;
  width: 0.6rem;
  height: 0.6rem;
  border-radius: 50%;
  margin-right: 0.35rem;
  border: 1px solid rgba(0, 0, 0, 0.15);
}

.ti-severity-dot {
  width: 0.75rem;
  height: 0.75rem;
  border-radius: 50%;
  flex: 0 0 auto;
  border: 1px solid rgba(0, 0, 0, 0.15);
}

.ti-node-label {
  font-weight: 600;
}

.ti-link-endpoints {
  font-weight: 600;
  word-break: break-word;
}

.ti-detail {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0.25rem 0.75rem;
  margin: 0;
  font-size: 0.8125rem;
}

.ti-detail dt {
  color: var(--onms-secondary-text-on-surface);
}

.ti-detail dd {
  margin: 0;
  word-break: break-word;
}

.ti-infopanel-item {
  margin-top: 0.85rem;
  padding-top: 0.6rem;
  border-top: 1px solid var(--onms-border-on-surface);
}

.ti-infopanel-title {
  margin: 0 0 0.35rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--onms-secondary-text-on-surface);
}

.ti-infopanel-html {
  font-size: 0.85rem;
  color: var(--onms-primary-text-on-surface);
  overflow-x: auto;
}

.ti-infopanel-html :deep(table) {
  width: 100%;
  border-collapse: collapse;
}

.ti-hint {
  margin: 0.35rem 0 0;
  font-size: 0.75rem;
  color: var(--onms-secondary-text-on-surface);
}

.ti-icon-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  margin-bottom: 0.35rem;
}

.ti-icon-option {
  width: 2.25rem;
  height: 2.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 1px solid var(--onms-border-on-surface);
  border-radius: 4px;
  background: var(--onms-surface);
  cursor: pointer;
  font-size: 0.65rem;
  color: var(--onms-secondary-text-on-surface);
}

.ti-icon-option img {
  width: 1.5rem;
  height: 1.5rem;
  object-fit: contain;
}

.ti-icon-option.is-selected {
  border-color: #1f5fb0;
  box-shadow: 0 0 0 1px #1f5fb0;
}

/* The built-in glyphs are white strokes (drawn over the node's colored disc
   on the canvas), so preview them on a dark chip or they'd be invisible. */
.ti-icon-option.ti-icon-builtin {
  background: #5b6b80;
}

.ti-hidden-input {
  display: none;
}

.ti-binding {
  margin: 0 0 0.5rem;
  font-size: 0.75rem;
  color: var(--onms-secondary-text-on-surface);
}

.ti-neighbors {
  list-style: none;
  margin: 0;
  padding: 0;
}

.ti-neighbor-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  padding: 0.15rem 0;
  font-size: 0.8125rem;
}

.ti-neighbor-label {
  color: var(--onms-primary-text-on-surface);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ti-neighbor-proto {
  font-size: 0.65rem;
  color: var(--onms-secondary-text-on-surface);
  border: 1px solid var(--onms-border-on-surface);
  border-radius: 3px;
  padding: 0 0.25rem;
  margin-left: 0.3rem;
}

.ti-inline-hint {
  font-size: 0.75rem;
  color: var(--onms-secondary-text-on-surface);
}

.ti-check {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.85rem;
  color: var(--onms-primary-text-on-surface);
  margin-bottom: 0.35rem;
  cursor: pointer;
}

.ti-row {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  margin-bottom: 0.35rem;
}

.ti-bg-option {
  width: 3.5rem;
  height: 2.5rem;
}

.ti-bg-option img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 3px;
}
</style>
