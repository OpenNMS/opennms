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

/**
 * A node placed on the topology canvas. May reference a real OpenNMS node
 * by id; if it doesn't, it is a free-standing canvas node (label-only or
 * decorative). x/y are canvas coordinates owned by the view document.
 */
export interface CanvasNode {
  id: string
  nodeId?: number
  label: string
  x: number
  y: number
  icon?: string
  color?: string
  /**
   * User-chosen icon, persisted with the view. Either a built-in glyph key
   * (see deviceIcons DEVICE_ICON_SVG) or `asset:<id>` for an uploaded image
   * asset. Absent = automatic (sysObjectId-derived glyph, else a circle).
   */
  iconOverride?: string
  /**
   * The provider's own id and namespace for a discovered vertex, kept because
   * the canvas id is ours (`placed-7`, `disc-Service:1`) and says nothing about
   * what the provider called it. Set only for discovered graphs.
   */
  vertexId?: string
  namespace?: string
  /**
   * Whatever else the provider said about a discovered vertex, in its own
   * vocabulary (an application's id, a service's type and address, a GraphML
   * author's custom attributes). Set only for discovered graphs, which are not
   * persisted, and shown verbatim by the inspector: for a vertex that is not an
   * OnmsNode this is the only detail there is.
   */
  properties?: Record<string, string>
}

/**
 * How a link maps onto the real network: the discovery protocol it was
 * learned from and the interface (port) on each end, as reported by
 * enlinkd. Set when a discovered adjacency is adopted into a custom view
 * (Phase 2 assisted composition); absent on hand-drawn links. This is the
 * identity the (future) link-metrics work resolves to interface counters.
 * Ports are enlinkd's display strings (e.g. "eth1(interfaceName:...)"),
 * not ifIndexes -- resolution to resources happens server-side later.
 */
export interface CanvasLinkBinding {
  protocol: DiscoveredLinkType
  sourcePort?: string
  targetPort?: string
}

/**
 * An edge between two CanvasNodes. `origin` distinguishes hand-drawn links
 * from adopted discovered adjacencies (Phase 2 assisted composition); the
 * latter carry a `binding` with their network identity.
 */
export interface CanvasLink {
  id: string
  sourceId: string
  targetId: string
  label?: string
  style?: Record<string, unknown>
  origin: 'user' | 'discovered'
  binding?: CanvasLinkBinding
}

/**
 * A free-standing text annotation placed directly on the canvas (not
 * attached to any node). Lives on the DOM overlay layer.
 */
export interface CanvasLabel {
  id: string
  text: string
  x: number
  y: number
  fontSize?: number
  color?: string
}

/**
 * A decorative annotation shape (NMS-7504 "frames/blocks"): a labeled box or
 * ellipse drawn around nodes to visually group them. Purely visual in v1 --
 * moving a shape does not move the nodes it frames. Same graph-coordinate
 * rect convention as the background (x/y = top-left; sigma's y axis points
 * up, so the shape spans [y - height, y]). The optional `label` renders as a
 * title anchored at the shape's top edge, Visio-style.
 */
export interface CanvasShape {
  id: string
  type: 'rect' | 'ellipse'
  x: number
  y: number
  width: number
  height: number
  label?: string
  stroke?: string
  fill?: string
  opacity?: number
}

/**
 * A background image behind the canvas (NMS-7504: floor plans, rack
 * diagrams). `ref` is `asset:<id>` referencing an uploaded image asset. The
 * rect lives in graph coordinates (x/y = top-left corner; sigma's y axis
 * points up, so the image spans [y - height, y]) so it pans and zooms with
 * the nodes. Opacity is capped in the UI so status colors stay legible.
 */
export interface TopologyViewBackground {
  type: 'none' | 'image'
  ref?: string
  x?: number
  y?: number
  width?: number
  height?: number
  opacity?: number
}

/**
 * Per-view rendering defaults, persisted with the view (they shape how the
 * view looks for everyone, unlike UI preferences). Absent fields fall back
 * to the renderer's defaults: black node labels, link labels that follow
 * the link's color.
 */
export interface TopologyViewStyle {
  nodeLabelColor?: string
  linkLabelColor?: string
}

/**
 * A complete custom topology view. This is the unit that the views
 * catalog REST resource will persist.
 */
export interface TopologyView {
  id?: string
  name: string
  nodes: CanvasNode[]
  links: CanvasLink[]
  labels: CanvasLabel[]
  /** Decorative annotation shapes (frames/boxes); absent in older views. */
  shapes?: CanvasShape[]
  /** Per-view rendering defaults (label colors); absent in older views. */
  style?: TopologyViewStyle
  viewport: {
    zoom: number
    panX: number
    panY: number
  }
  background?: TopologyViewBackground
}

/**
 * Lightweight catalog entry used by ViewManager (list/rename/delete).
 */
export interface TopologyViewSummary {
  id: string
  name: string
}

/**
 * The discovery protocol a link was learned from. Phase 2 (assisted
 * composition) treats all of these uniformly; the value is kept so the UI
 * can label/tooltip a discovered link by how it was found.
 */
export type DiscoveredLinkType = 'lldp' | 'cdp' | 'ospf' | 'isis' | 'bridge'

/**
 * A discovered neighbor of a node, normalized from the per-protocol link
 * lists returned by /api/v2/enlinkd/{nodeId}. The neighbor's node id is the
 * key the canvas needs to place it or match a ghost link; `label` and the
 * optional port fields are for display. Phase 2 uses these for the neighbor
 * tray and ghost-edge link hints.
 */
export interface DiscoveredNeighbor {
  neighborNodeId: number
  neighborLabel: string
  linkType: DiscoveredLinkType
  localPort?: string
  remotePort?: string
}

/**
 * Identifies a discovered (auto-generated) topology to load from the Graph
 * REST API: a container plus a namespace. E.g. enlinkd L2 is
 * { container: 'enlinkd', namespace: 'nodes:Layer2' }. This is the "view
 * source" dimension that sits above the custom Edit/View modes; new providers
 * (BSM, VMware, GraphML) are just other container/namespace pairs.
 */
export interface DiscoveredGraphSource {
  container: string
  namespace: string
  /**
   * Auto-layout suited to the data's shape: 'force' (default) for mesh-like
   * graphs, 'hierarchy' for rooted parent-child trees (e.g. path outage).
   */
  layout?: 'force' | 'hierarchy'
}

/**
 * A discovered topology graph, normalized into the canvas model. Unlike a
 * custom view, the structure is read-only (it comes from discovery) and the
 * Graph API returns no meaningful positions (x/y = 0), so the front-end
 * auto-lays-out the nodes. Node `nodeId` carries the real OnmsNode id (for
 * status coloring + inspector); edges are all origin:'discovered'.
 */
export interface DiscoveredGraph {
  source: DiscoveredGraphSource
  label: string
  nodes: CanvasNode[]
  links: CanvasLink[]
}
