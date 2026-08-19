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

import { v2 } from './axiosInstances'
import { getNodes } from './nodeService'
import { placedIdFor } from '@/components/Topology/nodeIds'
import type {
  CanvasLink,
  CanvasNode,
  DiscoveredGraph,
  DiscoveredGraphSource,
  DiscoveredLinkType,
  DiscoveredNeighbor,
  TopologyView,
  TopologyViewSummary
} from '@/types/topology'
import type { NodeApiResponse, QueryParameters } from '@/types'
import { aggregateNodeSeverities } from '@/components/Topology/severity'
import { deviceIconForSysObjectId, type DeviceIconId } from '@/components/Topology/deviceIcons'

/**
 * Palette node source: the real OpenNMS node inventory from
 * /api/v2/nodes. Returns the same NodeApiResponse the palette already
 * consumes (or `false` on error). A default page size is applied when the
 * caller doesn't specify one.
 */
const fetchPaletteNodes = async (
  queryParameters?: QueryParameters
): Promise<NodeApiResponse | false> => {
  return getNodes({ limit: 200, ...queryParameters })
}

/**
 * Current alarm status for a set of nodes, as a map of node id -> highest
 * severity. Used to color placed canvas nodes. Returns an empty map (never
 * `false`) so a status refresh failure leaves the canvas uncolored rather
 * than tearing down the view. Node ids match the real OnmsNode ids carried
 * by placed palette nodes; a hand-composed view holds few, so a single
 * FIQL "node.id==a,node.id==b" query covers them.
 */
const alarmsEndpoint = '/alarms'

/**
 * Split node ids into chunks whose encoded query stays inside Jetty's request
 * budget: `org.opennms.netmgt.jetty.requestHeaderSize` defaults to 4000 bytes
 * and covers the whole request line plus headers. Measured against a live
 * instance: 150 `node.id==` clauses encode to 3011 bytes and pass, 200 encode to
 * 4011 and answer 414. The budget here leaves room for the cookies and headers a
 * browser sends and curl does not.
 *
 * Counting clauses cannot work, which is how the first attempt at this stayed
 * broken: `node.id==` is five bytes longer per clause than `id==`, and axios
 * percent-encodes `=` as %3D, so one count gives very different URL lengths.
 */
const QUERY_BUDGET_BYTES = 2000

export const chunkByQueryLength = (
  ids: number[],
  clause: (id: number) => string
): number[][] => {
  const chunks: number[][] = []
  let current: number[] = []
  let length = 0
  for (const id of ids) {
    const cost = encodeURIComponent(clause(id)).length + 1 // + the joining comma
    if (current.length > 0 && length + cost > QUERY_BUDGET_BYTES) {
      chunks.push(current)
      current = []
      length = 0
    }
    current.push(id)
    length += cost
  }
  if (current.length > 0) {
    chunks.push(current)
  }
  return chunks
}

const getNodeSeverities = async (nodeIds: number[]): Promise<Record<number, string>> => {
  if (nodeIds.length === 0) {
    return {}
  }
  // One clause per id: 3400 nodes built a 47 KB query and the server answered
  // 414, which this function's catch turned into an empty map -- so every poll
  // silently reset the canvas to no-alarm instead of failing visibly.
  const alarms: Array<{ nodeId?: number; severity?: string }> = []
  for (const chunk of chunkByQueryLength(nodeIds, id => `node.id==${id}`)) {
    try {
      const resp = await v2.get<{ alarm?: Array<{ nodeId?: number; severity?: string }> }>(
        alarmsEndpoint,
        { params: { _s: chunk.map(id => `node.id==${id}`).join(','), limit: 1000 }}
      )
      if (resp.status !== 204 && resp.data) {
        alarms.push(...(resp.data.alarm ?? []))
      }
    } catch {
      // A failed chunk leaves those nodes uncolored, not the whole canvas.
    }
  }
  return aggregateNodeSeverities(alarms)
}

/**
 * Topology views catalog, backed by the /api/v2/topology/views REST
 * resource. Calls return typed data or `false`, matching the convention
 * used by the other services in this directory.
 *
 * The server stores the canvas as an opaque JSON document under a
 * `definition` field, with the catalog metadata (name, owner, timestamps)
 * as siblings. The front-end model is flat -- nodes, edges, labels, and
 * viewport live at the top of TopologyView -- so this service maps between
 * the two shapes. Ids are integers on the wire and strings in the UI.
 *
 * Access control is the standard /api/v2 RBAC (any authenticated user can
 * read; ROLE_REST or ROLE_ADMIN can write); the catalog is shared, so there
 * is no per-view role field.
 */

const viewsEndpoint = 'topology/views'

/** The canvas document as stored under the server's `definition` field. */
interface TopologyViewDefinition {
  nodes: TopologyView['nodes']
  links: TopologyView['links']
  labels: TopologyView['labels']
  shapes?: TopologyView['shapes']
  style?: TopologyView['style']
  viewport: TopologyView['viewport']
  background?: TopologyView['background']
}

/** Wire shape of a view as returned by /api/v2/topology/views. */
interface TopologyViewDTO {
  id?: number
  name: string
  definition: TopologyViewDefinition
  owner?: string
  created?: number
  lastModified?: number
}

const toDto = (view: TopologyView): TopologyViewDTO => ({
  name: view.name,
  definition: {
    nodes: view.nodes,
    links: view.links,
    labels: view.labels,
    shapes: view.shapes,
    style: view.style,
    viewport: view.viewport,
    background: view.background
  }
})

const fromDto = (dto: TopologyViewDTO): TopologyView => ({
  id: dto.id != null ? String(dto.id) : undefined,
  name: dto.name,
  nodes: dto.definition?.nodes ?? [],
  links: dto.definition?.links ?? [],
  labels: dto.definition?.labels ?? [],
  shapes: dto.definition?.shapes ?? [],
  style: dto.definition?.style,
  viewport: dto.definition?.viewport ?? { zoom: 1, panX: 0, panY: 0 },
  background: dto.definition?.background
})

const listViews = async (): Promise<TopologyViewSummary[] | false> => {
  try {
    const resp = await v2.get<TopologyViewDTO[]>(viewsEndpoint)
    return (resp.data ?? []).map(dto => ({
      id: dto.id != null ? String(dto.id) : '',
      name: dto.name
    }))
  } catch {
    return false
  }
}

const getView = async (id: string): Promise<TopologyView | false> => {
  try {
    const resp = await v2.get<TopologyViewDTO>(`${viewsEndpoint}/${id}`)
    return fromDto(resp.data)
  } catch {
    return false
  }
}

/**
 * Create (POST) or update (PUT) a view. The server replies 201 with a
 * Location header on create and 204 with no body on update, so in both
 * cases the saved document is re-fetched to return the canonical record
 * (server-assigned id, owner, timestamps).
 */
const saveView = async (view: TopologyView): Promise<TopologyView | false> => {
  try {
    if (view.id) {
      await v2.put(`${viewsEndpoint}/${view.id}`, toDto(view))
      return await getView(view.id)
    }
    const resp = await v2.post(viewsEndpoint, toDto(view))
    const location: string | undefined = resp.headers?.location
    const newId = location ? location.substring(location.lastIndexOf('/') + 1) : undefined
    if (!newId) {
      return false
    }
    return await getView(newId)
  } catch {
    return false
  }
}

const deleteView = async (id: string): Promise<boolean> => {
  try {
    await v2.delete(`${viewsEndpoint}/${id}`)
    return true
  } catch {
    return false
  }
}

/**
 * Discovered-topology neighbors for a node, from /api/v2/enlinkd/{nodeId}.
 *
 * That endpoint returns one array per discovery protocol (LLDP, CDP, OSPF,
 * IS-IS, bridge), each with protocol-specific field names. Phase 2 (assisted
 * composition) only needs, per neighbor: which node it is, a display label,
 * and how the link was found -- so this flattens all the protocol lists into
 * one normalized DiscoveredNeighbor[].
 *
 * The remote node id is not a first-class field on the wire; it is embedded
 * in the `...Url` fields (e.g. "element/linkednode.jsp?node=123"), so it is
 * parsed out of whichever Url field carries it. Links whose remote node can't
 * be resolved to an id -- or that point back at the node itself -- are
 * dropped (they can't be placed or matched on the canvas), and a neighbor
 * reached over several ports collapses to a single entry per protocol.
 */
const enlinkdEndpoint = 'enlinkd'

const PROTOCOL_LINK_FIELDS: Array<{ field: string; type: DiscoveredLinkType }> = [
  { field: 'lldpLinkNodes', type: 'lldp' },
  { field: 'cdpLinkNodes', type: 'cdp' },
  { field: 'ospfLinkNodes', type: 'ospf' },
  { field: 'isisLinkNodes', type: 'isis' },
  { field: 'bridgeLinkNodes', type: 'bridge' }
]

const NODE_URL_RE = /node=(\d+)/
// Field-name fragments (lowercased) that tend to carry a human-readable
// remote node name across the various protocol DTOs.
const REMOTE_LABEL_HINTS = ['reminfo', 'remsysname', 'cachedeviceid', 'remrouterid', 'neighsysid']
const LOCAL_PORT_HINT = 'localport'
// enlinkd reports the ifIndex inside the port's display string
// ("GigabitEthernet0/2(ifindex:2)(interfaceName:Gi0/2)") and in the interface
// URL it builds ("...snmpinterface.jsp?node=1&ifindex=2"), never as its own
// field. Either form identifies the interface an operator would go and look at.
const IFINDEX_RE = /ifindex[:=](\d+)/i

const parseIfIndex = (link: Record<string, unknown>): number | undefined => {
  for (const [key, value] of Object.entries(link)) {
    if (typeof value !== 'string' || !key.toLowerCase().includes(LOCAL_PORT_HINT)) {
      continue
    }
    const match = IFINDEX_RE.exec(value)
    if (match) {
      return Number(match[1])
    }
  }
  return undefined
}
const REMOTE_PORT_HINT = 'remport'

const firstStringField = (
  link: Record<string, unknown>,
  predicate: (lowerKey: string, value: string) => boolean
): string | undefined => {
  for (const [key, value] of Object.entries(link)) {
    if (typeof value === 'string' && value && predicate(key.toLowerCase(), value)) {
      return value
    }
  }
  return undefined
}

/**
 * The node at the far end of a link, read from whichever URL enlinkd built for
 * it. Every candidate is checked rather than the first one found: a record
 * carries a URL for its *local* port too ("snmpinterface.jsp?node=1&ifindex=2"),
 * and that one often comes first, so taking the first match resolved every link
 * to the node it started from and discarded it as a self-link.
 */
const parseNeighborNodeId = (
  link: Record<string, unknown>,
  nodeId: number
): number | undefined => {
  for (const [key, value] of Object.entries(link)) {
    if (typeof value !== 'string' || !key.toLowerCase().includes('url')) {
      continue
    }
    const match = NODE_URL_RE.exec(value)
    const candidate = match ? Number(match[1]) : undefined
    if (candidate !== undefined && candidate !== nodeId) {
      return candidate
    }
  }
  return undefined
}

/**
 * Pure transform of an enlinkd response into normalized neighbors. Exported
 * so it can be unit-tested against captured payloads without HTTP.
 */
const parseEnlinkdNeighbors = (
  data: Record<string, unknown> | null | undefined,
  nodeId: number
): DiscoveredNeighbor[] => {
  if (!data) {
    return []
  }
  const neighbors: DiscoveredNeighbor[] = []
  // Keyed by protocol as well as node: the same pair is commonly discovered over
  // both LLDP and OSPF, and a set shared across protocols meant whichever came
  // first silently discarded the rest -- including the per-protocol ports and
  // ifIndex the inspector exists to show.
  const seen = new Set<string>()
  for (const { field, type } of PROTOCOL_LINK_FIELDS) {
    const links = data[field]
    if (!Array.isArray(links)) {
      continue
    }
    for (const raw of links) {
      if (!raw || typeof raw !== 'object') {
        continue
      }
      const link = raw as Record<string, unknown>
      const neighborNodeId = parseNeighborNodeId(link, nodeId)
      const key = `${type}|${neighborNodeId}`
      if (neighborNodeId == null || neighborNodeId === nodeId || seen.has(key)) {
        continue
      }
      seen.add(key)
      neighbors.push({
        neighborNodeId,
        neighborLabel:
          firstStringField(link, k => REMOTE_LABEL_HINTS.some(h => k.includes(h))) ??
          `Node ${neighborNodeId}`,
        linkType: type,
        localPort: firstStringField(link, k => k.includes(LOCAL_PORT_HINT)),
        remotePort: firstStringField(link, k => k.includes(REMOTE_PORT_HINT)),
        localIfIndex: parseIfIndex(link),
        lastPollTime: firstStringField(link, k => k.includes('lastpolltime'))
      })
    }
  }
  return neighbors
}

const getNodeNeighbors = async (nodeId: number): Promise<DiscoveredNeighbor[]> => {
  try {
    const resp = await v2.get<Record<string, unknown>>(`${enlinkdEndpoint}/${nodeId}`)
    return parseEnlinkdNeighbors(resp.data, nodeId)
  } catch {
    return []
  }
}

/**
 * Discovered (auto-generated) topology graph from the Graph REST API
 * /api/v2/graphs/{container}/{namespace} (e.g. enlinkd L2 =
 * enlinkd/nodes:Layer2). This is the source for the *discovered* view type;
 * it's provider-agnostic, so BSM/VMware/GraphML are just other
 * container/namespace pairs.
 *
 * The wire shape: a vertex carries `id` (vertex id, = node id for node
 * vertices), `label`, `nodeID` (the real OnmsNode id), `iconKey`, and x/y that
 * are always "0" (no stored layout -- the front-end auto-lays-out). An edge
 * carries `id` plus `source`/`target` refs whose `id` is a vertex id. We map
 * vertices -> CanvasNode and edges -> CanvasLink (origin:'discovered'),
 * dropping any edge whose endpoints aren't present as vertices.
 */
const graphsEndpoint = 'graphs'

interface GraphApiVertex {
  id: string
  namespace?: string
  label?: string
  /** Providers disagree: enlinkd sends `label`, the application graph `name`. */
  name?: string
  nodeID?: string
  /** The application graph's node reference; bare id or foreignSource:foreignId. */
  nodeCriteria?: string
  iconKey?: string
  tooltipText?: string
}

interface GraphApiEdgeRef {
  namespace?: string
  id: string
}

interface GraphApiEdge {
  id: string
  label?: string
  namespace?: string
  source?: GraphApiEdgeRef
  target?: GraphApiEdgeRef
}

interface GraphApiResponse {
  vertices?: GraphApiVertex[]
  edges?: GraphApiEdge[]
  label?: string
  namespace?: string
  /** GraphML's `preferred-layout`; absent from every other provider. */
  'preferred-layout'?: string
}

/**
 * A vertex's OnmsNode id, if it has one. Providers disagree on the field name:
 * enlinkd sends `nodeID`, the application graph sends `nodeCriteria`. Only a
 * bare number is usable, since nodeCriteria may instead be
 * `foreignSource:foreignId`.
 */
const vertexNodeId = (vertex: GraphApiVertex): number | undefined => {
  for (const raw of [vertex.nodeID, vertex.nodeCriteria]) {
    if (raw != null && /^\d+$/.test(raw)) {
      return Number(raw)
    }
  }
  return undefined
}

/**
 * Canvas id for a discovered vertex. Where the vertex is the only one on its
 * node we reuse the custom-view `placed-<nodeId>` convention, so it inherits
 * the inspector's node detail for free. Anything else gets a `disc-` prefix:
 * a vertex with no node id, and -- importantly -- every vertex on a node that
 * carries more than one, which the application graph does whenever an
 * application watches several services on the same node. Reusing the node id
 * there would silently merge them into one canvas node.
 */
const discoveredNodeCanvasId = (
  vertex: GraphApiVertex,
  nodeIdCounts: Map<number, number>
): string => {
  const nodeId = vertexNodeId(vertex)
  return nodeId !== undefined && nodeIdCounts.get(nodeId) === 1
    ? placedIdFor(String(nodeId))
    : `disc-${vertex.id}`
}

// Fields the canvas model already carries, or that are internal plumbing; the
// rest is provider vocabulary worth showing verbatim in the inspector.
const MODELLED_VERTEX_FIELDS = new Set([
  'id', 'namespace', 'label', 'name', 'nodeID', 'nodeCriteria', 'iconKey', 'tooltipText', 'x', 'y'
])

const vertexProperties = (vertex: GraphApiVertex): Record<string, string> | undefined => {
  const out: Record<string, string> = {}
  for (const [key, value] of Object.entries(vertex as unknown as Record<string, unknown>)) {
    if (MODELLED_VERTEX_FIELDS.has(key) || value == null || typeof value === 'object') {
      continue
    }
    // Java's InetAddress.toString() leads with a slash; it is a serialization
    // artifact, not part of the address an operator should be shown.
    out[key] = key === 'ipAddress' ? String(value).replace(/^\//, '') : String(value)
  }
  return Object.keys(out).length > 0 ? out : undefined
}

/**
 * Pure transform of a Graph REST API response into a normalized
 * DiscoveredGraph. Exported for unit testing against captured payloads.
 * Positions are zeroed -- the caller auto-lays-out before rendering.
 */
/**
 * A GraphML author's `preferred-layout` in our terms. Only the hierarchy case is
 * meaningful here: the rest of the legacy algorithms (Circle, Grid, FR, KK, ...)
 * are variations on force-directed, which is already the default.
 */
/**
 * An edge label worth showing. Every provider sets one, but enlinkd and VMware
 * set it to the namespace-qualified edge id ("nodes:Layer2:572|581"), which is
 * machine noise. A GraphML author's "10G wave" is not, and telling them apart is
 * exactly that test.
 */
const authoredEdgeLabel = (edge: GraphApiEdge): string | undefined => {
  const label = edge.label
  if (!label || label === edge.id || label.endsWith(`:${edge.id}`)) {
    return undefined
  }
  return label
}

const declaredLayout = (preferred?: string): 'force' | 'hierarchy' | undefined => {
  if (!preferred) {
    return undefined
  }
  return /hierarch/i.test(preferred) ? 'hierarchy' : 'force'
}

const mapDiscoveredGraph = (
  data: GraphApiResponse,
  source: DiscoveredGraphSource
): DiscoveredGraph => {
  const vertices = data.vertices ?? []
  // How many vertices sit on each node, so the canvas id can tell a
  // one-vertex-per-node graph (enlinkd) from a many-per-node one (application).
  const nodeIdCounts = new Map<number, number>()
  for (const v of vertices) {
    const nodeId = vertexNodeId(v)
    if (nodeId !== undefined) {
      nodeIdCounts.set(nodeId, (nodeIdCounts.get(nodeId) ?? 0) + 1)
    }
  }
  // vertex id (the id edges reference) -> canvas node id
  const canvasIdByVertexId = new Map(
    vertices.map(v => [v.id, discoveredNodeCanvasId(v, nodeIdCounts)])
  )
  const nodes: CanvasNode[] = vertices.map(v => ({
    id: canvasIdByVertexId.get(v.id) as string,
    nodeId: vertexNodeId(v),
    label: v.label ?? v.name ?? v.id,
    x: 0,
    y: 0,
    icon: v.iconKey,
    vertexId: v.id,
    namespace: v.namespace,
    properties: vertexProperties(v)
  }))
  const links: CanvasLink[] = (data.edges ?? [])
    .filter(
      e =>
        e.source &&
        e.target &&
        canvasIdByVertexId.has(e.source.id) &&
        canvasIdByVertexId.has(e.target.id)
    )
    .map(e => ({
      id: e.id,
      sourceId: canvasIdByVertexId.get(e.source!.id) as string,
      targetId: canvasIdByVertexId.get(e.target!.id) as string,
      origin: 'discovered' as const,
      label: authoredEdgeLabel(e)
    }))
  return {
    source,
    label: data.label ?? source.namespace,
    layout: declaredLayout(data['preferred-layout']),
    nodes,
    links
  }
}

const loadDiscoveredGraph = async (
  source: DiscoveredGraphSource
): Promise<DiscoveredGraph | false> => {
  try {
    const resp = await v2.get<GraphApiResponse>(
      `${graphsEndpoint}/${source.container}/${source.namespace}`
    )
    if (!resp.data) {
      return false
    }
    return mapDiscoveredGraph(resp.data, source)
  } catch {
    return false
  }
}

/**
 * A graph container as listed by `GET /api/v2/graphs`: an id, a display label,
 * and the graphs (namespaces) it holds. This is what makes the topology source
 * menu self-registering rather than hardcoded: providers bridged in from the
 * legacy topology map (Application, BSM, VMware) and operator-defined GraphML
 * topologies all show up here without a UI change.
 */
export interface GraphContainerMeta {
  id: string
  label?: string
  description?: string
  graphs: Array<{ namespace: string, label?: string, description?: string }>
}

/**
 * Every container the Graph REST API serves. Returns [] rather than throwing so
 * a failure degrades to the curated source list instead of an empty menu.
 */
const listGraphContainers = async (): Promise<GraphContainerMeta[]> => {
  try {
    const resp = await v2.get<GraphContainerMeta[]>(graphsEndpoint)
    if (!Array.isArray(resp.data)) {
      return []
    }
    // A container with no graphs cannot be displayed, so drop it here rather
    // than leaving every consumer to guard.
    return resp.data
      .filter(c => c?.id && Array.isArray(c.graphs) && c.graphs.length > 0)
      .map(c => ({
        id: c.id,
        label: c.label,
        description: c.description,
        graphs: c.graphs.filter(g => g?.namespace)
      }))
      .filter(c => c.graphs.length > 0)
  } catch {
    return []
  }
}

/**
 * One operator-configured info-panel item for a node: a titled HTML fragment
 * rendered server-side from an etc/infopanel Jinjava template. The HTML must be
 * sanitized before rendering (see the Inspector).
 */
export interface NodeInfoPanelItem {
  title: string
  order: number
  html: string
}

const applicationsEndpoint = 'applications'
const outagesEndpoint = 'outages'

/**
 * An application and the locations it is watched from. The Application graph
 * models only application-to-service edges, so an application's perspective
 * locations are reachable nowhere else in that view.
 */
export interface TopologyApplication {
  id: number
  name: string
  perspectiveLocations: string[]
}

/** Every application, with its perspective locations flattened to names. */
const getApplications = async (): Promise<TopologyApplication[]> => {
  try {
    const resp = await v2.get<{ application?: Array<Record<string, unknown>> }>(
      applicationsEndpoint, { params: { limit: 1000 }}
    )
    const rows = resp.data?.application
    if (!Array.isArray(rows)) {
      return []
    }
    return rows.map(row => ({
      id: Number(row.id),
      name: String(row.name ?? row.id),
      // The resource renders a location as an object keyed `location-name`.
      perspectiveLocations: Array.isArray(row.perspectiveLocations)
        ? (row.perspectiveLocations as Array<Record<string, unknown>>)
          .map(l => String(l['location-name'] ?? l.locationName ?? l.name ?? ''))
          .filter(Boolean)
        : []
    }))
  } catch {
    return []
  }
}

/** An outage a perspective (a Minion location) currently sees on a service. */
export interface PerspectiveOutage {
  id: number
  nodeId?: number
  nodeLabel: string
  serviceName: string
  perspective: string
  lostAt?: number
}

/**
 * Open outages seen from a perspective, for the given nodes.
 *
 * The perspective cannot be filtered server-side: the column is mapped as a
 * monitoring-location entity, and `_s=perspective!=0` makes the outages
 * resource throw while resolving `locationName`. So the node filter narrows the
 * result and the perspective and open-ness are matched here.
 */
const getPerspectiveOutages = async (nodeIds: number[]): Promise<PerspectiveOutage[]> => {
  if (nodeIds.length === 0) {
    return []
  }
  try {
    const resp = await v2.get<{ outage?: Array<Record<string, unknown>> }>(
      outagesEndpoint,
      { params: { _s: nodeIds.map(id => `node.id==${id}`).join(','), limit: 1000 }}
    )
    const rows = resp.data?.outage
    if (!Array.isArray(rows)) {
      return []
    }
    return rows
      .filter(row => row.perspective && row.ifRegainedService == null)
      .map(row => ({
        id: Number(row.id),
        nodeId: row.nodeId != null ? Number(row.nodeId) : undefined,
        nodeLabel: String(row.nodeLabel ?? row.nodeId ?? ''),
        serviceName: String(
          ((row.monitoredService as Record<string, unknown> | undefined)
            ?.serviceType as Record<string, unknown> | undefined)?.name ?? ''
        ),
        perspective: String(row.perspective),
        lostAt: row.ifLostService != null ? Number(row.ifLostService) : undefined
      }))
  } catch {
    return []
  }
}

const infopanelEndpoint = 'topology/infopanel'

/**
 * Fetch the rendered info-panel items for a node (sorted by order server-side).
 * Returns [] on any error or when the install has no etc/infopanel templates --
 * the Inspector simply shows nothing extra.
 */
/**
 * Device-icon ids for a set of nodes, keyed by node id. Resolves each node's
 * sysObjectId to a device type the way the legacy map does (see deviceIcons).
 * Only recognized device types are returned -- unresolved nodes are omitted so
 * the canvas leaves them as plain circles. Mirrors getNodeSeverities' bulk FIQL
 * lookup; returns {} on error.
 */
const getNodeIconIds = async (nodeIds: number[]): Promise<Record<number, DeviceIconId>> => {
  if (nodeIds.length === 0) {
    return {}
  }
  // The /nodes endpoint filters on `id` (the /alarms endpoint uses `node.id`).
  // Chunked for the same reason as the severity and category queries: unchunked
  // this built a 30 KB query on a large view and answered 414, and the catch
  // below turned that into "no icons" rather than an error.
  const out: Record<number, DeviceIconId> = {}
  for (const chunk of chunkByQueryLength(nodeIds, id => `id==${id}`)) {
    try {
      const resp = await getNodes({ _s: chunk.map(id => `id==${id}`).join(','), limit: chunk.length })
      if (!resp || !resp.node) {
        continue
      }
      for (const n of resp.node) {
        const icon = deviceIconForSysObjectId(n.sysObjectId)
        const id = Number(n.id)
        if (icon && Number.isFinite(id)) {
          out[id] = icon
        }
      }
    } catch {
      // A failed chunk costs those nodes their glyph, not every node's.
    }
  }
  return out
}

/**
 * Which categories each node belongs to, keyed by OnmsNode id.
 *
 * Read off the node payload, which already carries `categories`, rather than
 * filtering by category: the v2 /nodes endpoint has its `categories` alias join
 * commented out (NodeRestService, "add this alias via a CriteriaBehavior"), so
 * any `category`-prefixed filter returns a 500 from Hibernate.
 *
 * Chunked because the filter is an id-per-clause FIQL string, which would
 * otherwise outgrow the URL on a large topology.
 */
const getNodeCategories = async (nodeIds: number[]): Promise<Record<number, string[]>> => {
  const out: Record<number, string[]> = {}
  for (const chunk of chunkByQueryLength(nodeIds, id => `id==${id}`)) {
    try {
      const resp = await getNodes({ _s: chunk.map(id => `id==${id}`).join(','), limit: chunk.length })
      if (!resp || !resp.node) {
        continue
      }
      for (const n of resp.node) {
        const id = Number(n.id)
        const names = (n.categories ?? []).map(c => c.name).filter(Boolean)
        if (Number.isFinite(id) && names.length) {
          out[id] = names
        }
      }
    } catch {
      // A failed chunk costs those nodes' categories, not the whole search.
    }
  }
  return out
}

const getEdgeInfoPanel = async (
  sourceNodeId: number,
  targetNodeId: number,
  binding?: { protocol: string; sourcePort?: string; targetPort?: string }
): Promise<NodeInfoPanelItem[]> => {
  try {
    const resp = await v2.get<NodeInfoPanelItem[]>(`${infopanelEndpoint}/edge`, {
      params: {
        sourceNodeId,
        targetNodeId,
        sourcePort: binding?.sourcePort,
        targetPort: binding?.targetPort,
        protocol: binding?.protocol
      }
    })
    return Array.isArray(resp.data) ? resp.data : []
  } catch {
    return []
  }
}

const getNodeInfoPanel = async (nodeId: number): Promise<NodeInfoPanelItem[]> => {
  try {
    const resp = await v2.get<NodeInfoPanelItem[]>(infopanelEndpoint, { params: { nodeId }})
    return Array.isArray(resp.data) ? resp.data : []
  } catch {
    return []
  }
}

/**
 * Topology image assets (/api/v2/topology/assets): server-stored images that
 * views reference by id -- background images (floor plans, rack diagrams) and
 * custom node icons, distinguished by `kind`. Uploads are the raw image bytes
 * under their own content type (no multipart); the server enforces a
 * raster-only allowlist and a per-kind size cap (icons 512 KiB, backgrounds
 * 10 MiB).
 */
export type TopologyAssetKind = 'background' | 'icon'

export interface TopologyAssetMeta {
  id: string
  name: string
  kind: TopologyAssetKind
  mimeType: string
  sizeBytes: number
  owner?: string
}

const assetsEndpoint = 'topology/assets'

/**
 * The URL an asset's bytes are served from. Same-origin with session auth, so
 * it works directly as an <img> / sigma image source; the server sends an
 * ETag + max-age so repeated renders revalidate cheaply.
 */
const assetUrl = (id: string): string => {
  const base = v2.defaults?.baseURL ?? '/opennms/api/v2'
  return `${base}/${assetsEndpoint}/${encodeURIComponent(id)}`
}

/** List asset metadata, optionally only one kind. Returns [] on error. */
const listAssets = async (kind?: TopologyAssetKind): Promise<TopologyAssetMeta[]> => {
  try {
    const resp = await v2.get<TopologyAssetMeta[]>(assetsEndpoint, { params: kind ? { kind } : {}})
    return Array.isArray(resp.data) ? resp.data : []
  } catch {
    return []
  }
}

/**
 * Upload an image file as a new asset; the file's own MIME type travels as
 * the request content type. Returns the created metadata, or false on any
 * failure (oversized, non-raster type, missing name).
 */
const uploadAsset = async (
  name: string,
  kind: TopologyAssetKind,
  file: File | Blob
): Promise<TopologyAssetMeta | false> => {
  try {
    const resp = await v2.post<TopologyAssetMeta>(assetsEndpoint, file, {
      params: { name, kind },
      headers: { 'Content-Type': file.type || 'application/octet-stream' }
    })
    return resp.data ?? false
  } catch {
    return false
  }
}

/** Delete an asset (metadata and bytes). */
const deleteAsset = async (id: string): Promise<boolean> => {
  try {
    await v2.delete(`${assetsEndpoint}/${encodeURIComponent(id)}`)
    return true
  } catch {
    return false
  }
}

export {
  fetchPaletteNodes,
  getNodeSeverities,
  listViews,
  getView,
  saveView,
  deleteView,
  getNodeNeighbors,
  parseEnlinkdNeighbors,
  loadDiscoveredGraph,
  listGraphContainers,
  getApplications,
  getPerspectiveOutages,
  mapDiscoveredGraph,
  getEdgeInfoPanel,
  getNodeInfoPanel,
  getNodeIconIds,
  getNodeCategories,
  assetUrl,
  listAssets,
  uploadAsset,
  deleteAsset
}
