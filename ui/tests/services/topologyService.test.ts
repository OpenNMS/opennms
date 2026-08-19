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

import { describe, it, expect, beforeEach, vi } from 'vitest'
import {
  listViews,
  getView,
  saveView,
  deleteView,
  getNodeNeighbors,
  parseEnlinkdNeighbors,
  loadDiscoveredGraph,
  listGraphContainers,
  mapDiscoveredGraph,
  getNodeInfoPanel,
  getNodeIconIds,
  assetUrl,
  listAssets,
  uploadAsset,
  deleteAsset
} from '@/services/topologyService'
import { v2 } from '@/services/axiosInstances'
import type { TopologyView } from '@/types/topology'

vi.mock('@/services/axiosInstances', () => ({
  v2: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

const flatView = (overrides: Partial<TopologyView> = {}): TopologyView => ({
  name: 'Core DC',
  nodes: [{ id: 'placed-7', nodeId: 7, label: 'core-sw1', x: 10, y: 20, color: '#1f5fb0' }],
  links: [{ id: 'e1', sourceId: 'placed-7', targetId: 'placed-8', origin: 'user' }],
  labels: [{ id: 'label-1', text: 'DC core', x: 5, y: 5 }],
  viewport: { zoom: 1.5, panX: 3, panY: 4 },
  ...overrides
})

// The server's nested shape: canvas under `definition`, metadata as siblings.
const dtoFor = (id: number, view: TopologyView) => ({
  id,
  name: view.name,
  owner: 'admin',
  created: 1700000000000,
  lastModified: null,
  definition: {
    nodes: view.nodes,
    links: view.links,
    labels: view.labels,
    shapes: view.shapes,
    viewport: view.viewport
  }
})

describe('topologyService views catalog', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('listViews', () => {
    it('maps server documents down to catalog summaries with string ids', async () => {
      vi.mocked(v2.get).mockResolvedValue({
        data: [dtoFor(12, flatView()), dtoFor(34, flatView({ name: 'Edge' }))]
      })

      const result = await listViews()

      expect(v2.get).toHaveBeenCalledWith('topology/views')
      expect(result).toEqual([
        { id: '12', name: 'Core DC' },
        { id: '34', name: 'Edge' }
      ])
    })

    it('returns false when the request fails', async () => {
      vi.mocked(v2.get).mockRejectedValue(new Error('boom'))
      expect(await listViews()).toBe(false)
    })
  })

  describe('getView', () => {
    it('unwraps the definition into the flat front-end shape with a string id', async () => {
      const view = flatView()
      vi.mocked(v2.get).mockResolvedValue({ data: dtoFor(99, view) })

      const result = await getView('99')

      expect(v2.get).toHaveBeenCalledWith('topology/views/99')
      expect(result).toMatchObject({
        id: '99',
        name: 'Core DC',
        nodes: view.nodes,
        links: view.links,
        labels: view.labels,
        viewport: view.viewport
      })
    })

    it('falls back to an empty canvas when definition fields are absent', async () => {
      vi.mocked(v2.get).mockResolvedValue({ data: { id: 1, name: 'Bare', definition: {}}})

      const result = await getView('1')

      expect(result).toMatchObject({
        id: '1',
        name: 'Bare',
        nodes: [],
        links: [],
        labels: [],
        viewport: { zoom: 1, panX: 0, panY: 0 }
      })
    })
  })

  describe('saveView (create)', () => {
    it('POSTs the nested definition, reads the Location id, and re-fetches', async () => {
      const view = flatView()
      vi.mocked(v2.post).mockResolvedValue({
        status: 201,
        headers: { location: 'http://localhost:8980/opennms/api/v2/topology/views/55' },
        data: ''
      })
      vi.mocked(v2.get).mockResolvedValue({ data: dtoFor(55, view) })

      const result = await saveView(view)

      // POST body nests the canvas under `definition`, metadata at the top.
      expect(v2.post).toHaveBeenCalledWith('topology/views', {
        name: 'Core DC',
        definition: {
          nodes: view.nodes,
          links: view.links,
          labels: view.labels,
          viewport: view.viewport,
          background: undefined
        }
      })
      // Re-fetch uses the id parsed from the Location header.
      expect(v2.get).toHaveBeenCalledWith('topology/views/55')
      expect(result).toMatchObject({ id: '55', name: 'Core DC' })
    })

    it('returns false when no Location header comes back', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 201, headers: {}, data: '' })
      expect(await saveView(flatView())).toBe(false)
      expect(v2.get).not.toHaveBeenCalled()
    })
  })

  describe('saveView (update)', () => {
    it('PUTs to the id and re-fetches the canonical record', async () => {
      const view = flatView({ id: '55', name: 'Renamed' })
      vi.mocked(v2.put).mockResolvedValue({ status: 204 })
      vi.mocked(v2.get).mockResolvedValue({ data: dtoFor(55, view) })

      const result = await saveView(view)

      expect(v2.put).toHaveBeenCalledWith(
        'topology/views/55',
        expect.objectContaining({ name: 'Renamed', definition: expect.any(Object) })
      )
      expect(v2.get).toHaveBeenCalledWith('topology/views/55')
      expect(result).toMatchObject({ id: '55', name: 'Renamed' })
    })
  })

  describe('deleteView', () => {
    it('DELETEs by id and returns true', async () => {
      vi.mocked(v2.delete).mockResolvedValue({ status: 204 })
      expect(await deleteView('7')).toBe(true)
      expect(v2.delete).toHaveBeenCalledWith('topology/views/7')
    })

    it('returns false on failure', async () => {
      vi.mocked(v2.delete).mockRejectedValue(new Error('nope'))
      expect(await deleteView('7')).toBe(false)
    })
  })
})

// Shape captured from a live /api/v2/enlinkd/{nodeId} response for a
// distribution node: two core uplinks plus an access downlink over LLDP.
const enlinkdResponse = {
  lldpLinkNodes: [
    {
      lldpLocalPort: 'eth1(interfaceName:port-100011-1)',
      lldpRemChassisId: 'core-01(macAddress:cs-100001)',
      lldpRemChassisIdUrl: 'element/linkednode.jsp?node=100001',
      lldpRemInfo: 'core-01',
      ldpRemPort: 'eth-to-100011(interfaceName:port-100001)'
    },
    {
      lldpLocalPort: 'eth2(interfaceName:port-100011-2)',
      lldpRemChassisId: 'core-02(macAddress:cs-100002)',
      lldpRemChassisIdUrl: 'element/linkednode.jsp?node=100002',
      lldpRemInfo: 'core-02',
      ldpRemPort: 'eth-to-100011(interfaceName:port-100002)'
    },
    {
      lldpLocalPort: 'eth3(interfaceName:port-100011-3)',
      lldpRemChassisId: 'access-01(macAddress:cs-100101)',
      lldpRemChassisIdUrl: 'element/linkednode.jsp?node=100101',
      lldpRemInfo: 'access-01',
      ldpRemPort: 'eth-to-100011(interfaceName:port-100101)'
    }
  ],
  bridgeLinkNodes: [],
  cdpLinkNodes: [],
  ospfLinkNodes: [],
  isisLinkNodes: []
}

describe('topologyService discovered neighbors', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('parseEnlinkdNeighbors', () => {
    it('flattens LLDP links into normalized neighbors with parsed node ids', () => {
      const neighbors = parseEnlinkdNeighbors(enlinkdResponse, 100011)
      expect(neighbors).toEqual([
        {
          neighborNodeId: 100001,
          neighborLabel: 'core-01',
          linkType: 'lldp',
          localPort: 'eth1(interfaceName:port-100011-1)',
          remotePort: 'eth-to-100011(interfaceName:port-100001)'
        },
        {
          neighborNodeId: 100002,
          neighborLabel: 'core-02',
          linkType: 'lldp',
          localPort: 'eth2(interfaceName:port-100011-2)',
          remotePort: 'eth-to-100011(interfaceName:port-100002)'
        },
        {
          neighborNodeId: 100101,
          neighborLabel: 'access-01',
          linkType: 'lldp',
          localPort: 'eth3(interfaceName:port-100011-3)',
          remotePort: 'eth-to-100011(interfaceName:port-100101)'
        }
      ])
    })

    it('drops self-links and collapses a neighbor reached over multiple ports', () => {
      const data = {
        lldpLinkNodes: [
          { lldpRemChassisIdUrl: 'element/linkednode.jsp?node=100011', lldpRemInfo: 'self' },
          { lldpRemChassisIdUrl: 'element/linkednode.jsp?node=100001', lldpRemInfo: 'core-01' },
          { lldpRemChassisIdUrl: 'element/linkednode.jsp?node=100001', lldpRemInfo: 'core-01' }
        ]
      }
      const neighbors = parseEnlinkdNeighbors(data, 100011)
      expect(neighbors).toHaveLength(1)
      expect(neighbors[0].neighborNodeId).toBe(100001)
    })

    it('falls back to a generated label when no remote name field is present', () => {
      const data = { cdpLinkNodes: [{ cdpCacheAddressUrl: 'element/linkednode.jsp?node=42' }] }
      const neighbors = parseEnlinkdNeighbors(data, 1)
      expect(neighbors).toEqual([
        { neighborNodeId: 42, neighborLabel: 'Node 42', linkType: 'cdp', localPort: undefined, remotePort: undefined }
      ])
    })

    it('returns an empty array for empty or missing data', () => {
      expect(parseEnlinkdNeighbors(null, 1)).toEqual([])
      expect(parseEnlinkdNeighbors({}, 1)).toEqual([])
      expect(parseEnlinkdNeighbors({ lldpLinkNodes: [] }, 1)).toEqual([])
    })
  })

  describe('getNodeNeighbors', () => {
    it('GETs the enlinkd endpoint and returns parsed neighbors', async () => {
      vi.mocked(v2.get).mockResolvedValue({ data: enlinkdResponse })
      const neighbors = await getNodeNeighbors(100011)
      expect(v2.get).toHaveBeenCalledWith('enlinkd/100011')
      expect(neighbors.map(n => n.neighborNodeId)).toEqual([100001, 100002, 100101])
    })

    it('returns an empty array on request failure', async () => {
      vi.mocked(v2.get).mockRejectedValue(new Error('boom'))
      expect(await getNodeNeighbors(100011)).toEqual([])
    })
  })
})

// Shape captured from a live /api/v2/graphs/enlinkd/nodes:Layer2 response.
const layer2Source = { container: 'enlinkd', namespace: 'nodes:Layer2' }
const graphApiResponse = {
  label: 'Layer2',
  namespace: 'nodes:Layer2',
  vertices: [
    { id: '100001', label: 'core-01', nodeID: '100001', iconKey: 'linkd.system', x: '0', y: '0' },
    { id: '100002', label: 'core-02', nodeID: '100002', iconKey: 'linkd.system', x: '0', y: '0' }
  ],
  edges: [
    {
      id: '572|581',
      label: 'nodes:Layer2:572|581',
      source: { namespace: 'nodes:Layer2', id: '100001' },
      target: { namespace: 'nodes:Layer2', id: '100002' }
    }
  ]
}

describe('topologyService discovered graph (Graph REST API)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('mapDiscoveredGraph', () => {
    it('maps node vertices to placed-<nodeId> CanvasNodes and edges to discovered CanvasLinks', () => {
      const graph = mapDiscoveredGraph(graphApiResponse, layer2Source)
      expect(graph.label).toBe('Layer2')
      expect(graph.source).toEqual(layer2Source)
      // Real node vertices reuse the custom-view placed-<nodeId> convention so
      // they inherit severity coloring + inspector detail.
      expect(graph.nodes).toEqual([
        {
          id: 'placed-100001', nodeId: 100001, label: 'core-01', x: 0, y: 0,
          icon: 'linkd.system', vertexId: '100001', namespace: undefined, properties: undefined
        },
        {
          id: 'placed-100002', nodeId: 100002, label: 'core-02', x: 0, y: 0,
          icon: 'linkd.system', vertexId: '100002', namespace: undefined, properties: undefined
        }
      ])
      expect(graph.links).toEqual([
        { id: '572|581', sourceId: 'placed-100001', targetId: 'placed-100002', origin: 'discovered' }
      ])
    })

    it('drops edges whose endpoints are not present as vertices', () => {
      const data = {
        vertices: [{ id: '100001', label: 'core-01', nodeID: '100001' }],
        edges: [{ id: 'e1', source: { id: '100001' }, target: { id: '999' }}]
      }
      expect(mapDiscoveredGraph(data, layer2Source).links).toEqual([])
    })

    it('falls back to a disc- id and undefined nodeId for a non-node vertex', () => {
      const data = { vertices: [{ id: 'group-a', label: 'Group A' }], edges: [] }
      const graph = mapDiscoveredGraph(data, layer2Source)
      expect(graph.nodes[0]).toEqual({
        id: 'disc-group-a', nodeId: undefined, label: 'Group A', x: 0, y: 0,
        icon: undefined, vertexId: 'group-a', namespace: undefined, properties: undefined
      })
    })

    it('handles an empty graph', () => {
      expect(mapDiscoveredGraph({}, layer2Source)).toEqual({
        source: layer2Source,
        label: 'nodes:Layer2',
        nodes: [],
        links: []
      })
    })

    // The application graph names vertices with `name`, not `label`; without
    // this every vertex rendered as its internal id.
    it('takes the label from `name` when `label` is absent', () => {
      const data = { vertices: [{ id: 'Application:1', name: 'Review Billing' }], edges: [] }
      expect(mapDiscoveredGraph(data, layer2Source).nodes[0].label).toBe('Review Billing')
    })

    // ...and references its node with `nodeCriteria`, not `nodeID`.
    it('resolves a node id from `nodeCriteria`', () => {
      const data = { vertices: [{ id: 'Service:1', name: 'HTTP', nodeCriteria: '7' }], edges: [] }
      expect(mapDiscoveredGraph(data, layer2Source).nodes[0]).toMatchObject({
        id: 'placed-7', nodeId: 7, label: 'HTTP'
      })
    })

    it('ignores a nodeCriteria that is not a bare node id', () => {
      const data = { vertices: [{ id: 'Service:1', name: 'HTTP', nodeCriteria: 'acme:host-1' }], edges: [] }
      expect(mapDiscoveredGraph(data, layer2Source).nodes[0]).toMatchObject({
        id: 'disc-Service:1', nodeId: undefined
      })
    })

    // Reusing placed-<nodeId> for several vertices on one node would collapse
    // them into a single canvas node, silently losing all but one.
    it('keeps vertices distinct when several sit on the same node', () => {
      const data = {
        vertices: [
          { id: 'Service:1', name: 'HTTP', nodeCriteria: '7' },
          { id: 'Service:2', name: 'SSH', nodeCriteria: '7' },
          { id: 'Service:3', name: 'ICMP', nodeCriteria: '8' }
        ],
        edges: []
      }
      const nodes = mapDiscoveredGraph(data, layer2Source).nodes
      expect(nodes.map(n => n.id)).toEqual(['disc-Service:1', 'disc-Service:2', 'placed-8'])
      // The node id survives regardless, so status and node detail still resolve.
      expect(nodes.map(n => n.nodeId)).toEqual([7, 7, 8])
    })

    it('carries the provider properties, minus what the model already holds', () => {
      const data = {
        vertices: [{
          id: 'Service:1', name: 'HTTP', namespace: 'application', nodeCriteria: '7',
          vertexType: 'Service', serviceTypeId: '2', ipAddress: '/127.0.0.1'
        }],
        edges: []
      }
      expect(mapDiscoveredGraph(data, layer2Source).nodes[0].properties).toEqual({
        vertexType: 'Service',
        serviceTypeId: '2',
        // Java's InetAddress.toString() leads with a slash.
        ipAddress: '127.0.0.1'
      })
    })

    it('leaves properties undefined when the vertex has nothing extra', () => {
      const data = { vertices: [{ id: '1', label: 'core', nodeID: '1' }], edges: [] }
      expect(mapDiscoveredGraph(data, layer2Source).nodes[0].properties).toBeUndefined()
    })
  })

  describe('loadDiscoveredGraph', () => {
    it('GETs the container/namespace and returns the mapped graph', async () => {
      vi.mocked(v2.get).mockResolvedValue({ data: graphApiResponse })
      const graph = await loadDiscoveredGraph(layer2Source)
      expect(v2.get).toHaveBeenCalledWith('graphs/enlinkd/nodes:Layer2')
      expect(graph).not.toBe(false)
      const g = graph as Exclude<typeof graph, false>
      expect(g.nodes).toHaveLength(2)
      expect(g.links).toHaveLength(1)
      expect(g.nodes[0].id).toBe('placed-100001')
    })

    it('returns false on request failure', async () => {
      vi.mocked(v2.get).mockRejectedValue(new Error('boom'))
      expect(await loadDiscoveredGraph(layer2Source)).toBe(false)
    })
  })

  describe('getNodeInfoPanel', () => {
    it('returns the rendered items as-is', async () => {
      const items = [
        { title: 'Node Overview', order: 10, html: '<b>x</b>' },
        { title: 'Net-SNMP', order: 20, html: 'y' }
      ]
      vi.mocked(v2.get).mockResolvedValue({ data: items })
      expect(await getNodeInfoPanel(2)).toEqual(items)
      expect(v2.get).toHaveBeenCalledWith('topology/infopanel', { params: { nodeId: 2 }})
    })

    it('returns [] on error or a non-array body', async () => {
      vi.mocked(v2.get).mockRejectedValue(new Error('boom'))
      expect(await getNodeInfoPanel(2)).toEqual([])
      vi.mocked(v2.get).mockResolvedValue({ data: null })
      expect(await getNodeInfoPanel(2)).toEqual([])
    })
  })

  describe('getNodeIconIds', () => {
    it('maps each node sysObjectId to a device-type icon, omitting unresolved ones', async () => {
      vi.mocked(v2.get).mockResolvedValue({
        status: 200,
        data: {
          node: [
            { id: '23', sysObjectId: '.1.3.6.1.4.1.9.1.559' }, // router
            { id: '29', sysObjectId: '.1.3.6.1.4.1.9.1.283' }, // switch
            { id: '99', sysObjectId: '.1.2.3.4' } // unknown -> omitted
          ]
        }
      })
      const result = await getNodeIconIds([23, 29, 99])
      expect(result).toEqual({ 23: 'router', 29: 'switch' })
      // The /nodes endpoint filters on `id`, not `node.id`.
      const url = vi.mocked(v2.get).mock.calls[0][0] as string
      expect(decodeURIComponent(url)).toContain('id==23')
      expect(decodeURIComponent(url)).not.toContain('node.id==')
    })

    it('returns {} for an empty id list without calling the API', async () => {
      expect(await getNodeIconIds([])).toEqual({})
      expect(v2.get).not.toHaveBeenCalled()
    })
  })

  describe('view shapes round-trip', () => {
    it('saveView sends shapes inside definition and getView restores them', async () => {
      const view = flatView({
        shapes: [{ id: 'shape-1', type: 'rect', x: -10, y: 20, width: 300, height: 150, label: 'DC-1' }]
      })
      vi.mocked(v2.post).mockResolvedValue({ status: 201, headers: { location: '/x/9' }, data: dtoFor(9, view) })
      await saveView(view)
      const sent = vi.mocked(v2.post).mock.calls[0][1] as { definition: { shapes?: unknown[] }}
      expect(sent.definition.shapes).toHaveLength(1)

      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: dtoFor(9, view) })
      const fetched = await getView('9')
      expect(fetched).not.toBe(false)
      expect((fetched as TopologyView).shapes).toEqual(view.shapes)
    })
  })

  describe('topology assets', () => {
    it('assetUrl points at the v2 assets endpoint and escapes the id', () => {
      expect(assetUrl('abc-123')).toBe('/opennms/api/v2/topology/assets/abc-123')
      expect(assetUrl('a/b')).toBe('/opennms/api/v2/topology/assets/a%2Fb')
    })

    it('listAssets passes the kind filter and returns [] on error', async () => {
      vi.mocked(v2.get).mockResolvedValue({
        status: 200,
        data: [{ id: 'a1', name: 'glyph', kind: 'icon', mimeType: 'image/png', sizeBytes: 10 }]
      })
      const icons = await listAssets('icon')
      expect(icons).toHaveLength(1)
      expect(vi.mocked(v2.get).mock.calls[0][1]).toEqual({ params: { kind: 'icon' }})

      vi.mocked(v2.get).mockRejectedValue(new Error('boom'))
      expect(await listAssets()).toEqual([])
    })

    it('uploadAsset posts the file bytes under its own content type', async () => {
      vi.mocked(v2.post).mockResolvedValue({
        status: 201,
        data: { id: 'a2', name: 'floor', kind: 'background', mimeType: 'image/png', sizeBytes: 3 }
      })
      const file = new File([new Uint8Array([1, 2, 3])], 'floor.png', { type: 'image/png' })
      const created = await uploadAsset('floor', 'background', file)
      expect(created).not.toBe(false)
      expect((created as { id: string }).id).toBe('a2')
      const [url, body, config] = vi.mocked(v2.post).mock.calls[0]
      expect(url).toBe('topology/assets')
      expect(body).toBe(file)
      expect(config).toMatchObject({
        params: { name: 'floor', kind: 'background' },
        headers: { 'Content-Type': 'image/png' }
      })
    })

    it('uploadAsset returns false on a server rejection (e.g. 413/415)', async () => {
      vi.mocked(v2.post).mockRejectedValue(new Error('413'))
      expect(await uploadAsset('big', 'icon', new Blob([new Uint8Array(4)], { type: 'image/png' }))).toBe(false)
    })

    it('deleteAsset reports success and failure', async () => {
      vi.mocked(v2.delete).mockResolvedValue({ status: 204 })
      expect(await deleteAsset('a1')).toBe(true)
      vi.mocked(v2.delete).mockRejectedValue(new Error('404'))
      expect(await deleteAsset('gone')).toBe(false)
    })
  })

  describe('listGraphContainers', () => {
    it('returns the containers the API reports', async () => {
      vi.mocked(v2.get).mockResolvedValue({
        data: [
          { id: 'bsm', label: 'Business Service Graph', graphs: [{ namespace: 'bsm', label: 'Business Service Graph' }] }
        ]
      })
      expect(await listGraphContainers()).toEqual([
        { id: 'bsm', label: 'Business Service Graph', description: undefined, graphs: [{ namespace: 'bsm', label: 'Business Service Graph' }] }
      ])
      expect(vi.mocked(v2.get)).toHaveBeenCalledWith('graphs')
    })

    // A container with no usable graph cannot be displayed, so it is dropped
    // here rather than leaving every consumer to guard.
    it('drops containers with no id and containers with no graphs', async () => {
      vi.mocked(v2.get).mockResolvedValue({
        data: [
          { id: 'empty', label: 'Empty', graphs: [] },
          { label: 'No id', graphs: [{ namespace: 'x' }] },
          { id: 'blanks', label: 'Blank namespaces', graphs: [{ label: 'no namespace' }] },
          { id: 'ok', label: 'Fine', graphs: [{ namespace: 'ok' }] }
        ]
      })
      expect((await listGraphContainers()).map(c => c.id)).toEqual(['ok'])
    })

    // Degrades to the curated source list instead of an empty menu.
    it('returns [] on a failure or a non-array body', async () => {
      vi.mocked(v2.get).mockRejectedValue(new Error('503'))
      expect(await listGraphContainers()).toEqual([])
      vi.mocked(v2.get).mockResolvedValue({ data: { containers: [] }})
      expect(await listGraphContainers()).toEqual([])
    })
  })
})
