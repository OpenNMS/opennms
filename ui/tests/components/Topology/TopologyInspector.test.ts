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

import { OnmsColorPicker } from '@opennms/onms-ui'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it, vi } from 'vitest'
import TopologyInspector from '@/components/Topology/TopologyInspector.vue'
import { powerStateColor } from '@/components/Topology/deviceIcons'
import { getInterfaceState } from '@/services/topologyService'
import { useTopologyStore } from '@/stores/topologyStore'

import { getNodeById } from '@/services/nodeService'

vi.mock('@/services/nodeService', () => ({ getNodeById: vi.fn().mockResolvedValue(null) }))
// The store refreshes icons and severities whenever the visible node set
// changes, which these tests trigger by assigning discoveredGraph, so the mock
// has to carry them. Returning the real shapes, not null: getNodeSeverities and
// getNodeIconIds both resolve to a record.
vi.mock('@/services/topologyService', () => ({
  listAssets: vi.fn().mockResolvedValue([]),
  assetUrl: vi.fn(),
  uploadAsset: vi.fn(),
  getNodeInfoPanel: vi.fn().mockResolvedValue([]),
  getEdgeInfoPanel: vi.fn().mockResolvedValue([]),
  getInterfaceState: vi.fn().mockResolvedValue(null),
  getDiscoveredNeighbors: vi.fn().mockResolvedValue([]),
  getNodeSeverities: vi.fn().mockResolvedValue({}),
  getNodeIconIds: vi.fn().mockResolvedValue({}),
  getNodeNeighbors: vi.fn().mockResolvedValue(null),
  parseEnlinkdNeighbors: vi.fn(() => [])
}))

const shapeA = { id: 'shape-a', type: 'rect', x: 0, y: 0, w: 10, h: 10, stroke: '#aaaaaa', fill: '#eeeeee' }
const shapeB = { id: 'shape-b', type: 'rect', x: 20, y: 0, w: 10, h: 10, stroke: '#bbbbbb', fill: '#dddddd' }

const mountInspector = async () => {
  const wrapper = mount(TopologyInspector, {
    props: { canvas: null, variant: 'props' },
    global: { plugins: [PrimeVue, createTestingPinia({ stubActions: false })] }
  })
  const store = useTopologyStore()
  store.shapes = [{ ...shapeA }, { ...shapeB }] as never
  store.isEditMode = true as never
  store.selectedIds = [shapeA.id] as never
  await flushPromises()
  return { wrapper, store }
}

// Each color field is a .ti-field with its label, so the picker is located by
// the label rather than by position among the section's pickers.
const picker = (wrapper: Awaited<ReturnType<typeof mountInspector>>['wrapper'], label: string) => {
  const field = wrapper.findAll('.ti-field').find(f => f.text().includes(label))
  expect(field, `no "${label}" field rendered`).toBeTruthy()
  const found = field!.findComponent(OnmsColorPicker)
  expect(found.exists(), `no color picker under "${label}"`).toBe(true)
  return found
}

// A discovered graph can hold vertices that are not OnmsNodes (an application)
// and vertices that are on a node but not identified by the canvas id (two
// services on one host). Both used to fall through to the link branch and
// render an empty panel.
describe('TopologyInspector discovered vertices', () => {
  const mountFull = async (nodes: unknown[], selected: string) => {
    const wrapper = mount(TopologyInspector, {
      props: { canvas: null, variant: 'full' },
      global: { plugins: [PrimeVue, createTestingPinia({ stubActions: false })] }
    })
    const store = useTopologyStore()
    store.discoveredGraph = {
      source: { container: 'application', namespace: 'application' },
      label: 'Application Graph',
      nodes,
      links: []
    } as never
    store.selectedIds = [selected] as never
    await flushPromises()
    return { wrapper, store }
  }

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows an application vertex by name with its provider properties', async () => {
    const { wrapper } = await mountFull([
      {
        id: 'disc-Application:1', label: 'Review Billing', x: 0, y: 0,
        properties: { vertexType: 'Application', applicationId: '1' }
      }
    ], 'disc-Application:1')

    expect(wrapper.text()).toContain('Review Billing')
    expect(wrapper.text()).toContain('Technical Details')
    // Keys are de-camel-cased for display rather than mapped.
    expect(wrapper.text()).toContain('Vertex type')
    expect(wrapper.text()).toContain('Application ID')
    expect(wrapper.text()).not.toContain('Select a node, link, label, or box')
  })

  it('resolves the node behind a vertex the canvas id cannot identify', async () => {
    const { wrapper } = await mountFull([
      {
        id: 'disc-Service:1', label: 'HTTP-8080', nodeId: 7, x: 0, y: 0,
        properties: { vertexType: 'Service', ipAddress: '127.0.0.1' }
      }
    ], 'disc-Service:1')

    // nodeId came off the vertex, not out of the id, so the node branch runs
    // and the service's own identity is shown alongside it.
    expect(getNodeById).toHaveBeenCalledWith('7')
    expect(wrapper.text()).toContain('HTTP-8080')
  })

  // The legacy map's Technical Details: the provider's own name, id and icon,
  // which exist even when it sent no other properties.
  it('shows Technical Details for a vertex with no extra properties', async () => {
    const { wrapper } = await mountFull(
      [{ id: 'disc-group-a', label: 'Group A', vertexId: 'group-a', namespace: 'acme', x: 0, y: 0 }],
      'disc-group-a'
    )
    expect(wrapper.text()).toContain('Technical Details')
    expect(wrapper.text()).toContain('Group A')
    expect(wrapper.text()).toContain('acme:group-a')
  })

  it('puts the icon key in Technical Details when the provider sent one', async () => {
    const { wrapper } = await mountFull(
      [{ id: 'disc-1', label: 'core', vertexId: '1', namespace: 'nodes', icon: 'linkd.system', x: 0, y: 0 }],
      'disc-1'
    )
    expect(wrapper.text()).toContain('Icon key')
    expect(wrapper.text()).toContain('linkd.system')
  })

  // The canvas badge carries no label, so this is where a reader finds out what
  // its color means.
  it('names the power state beside its own color', async () => {
    const { wrapper } = await mountFull(
      [{
        id: 'disc-vm-105', label: 'vm-app-02', vertexId: 'vcenter1.lab/vm-105',
        namespace: 'vmware', icon: 'vmware.VIRTUALMACHINE_ICON_SUSPENDED', x: 0, y: 0
      }],
      'disc-vm-105'
    )
    expect(wrapper.text()).toContain('Power state')
    expect(wrapper.text()).toContain('Suspended')
    // Compared against the source of truth, so the swatch cannot drift from the
    // color the canvas badge is drawn with.
    const dot = wrapper.find('.ti-detail-dot')
    expect(dot.exists()).toBe(true)
    expect(dot.attributes('style')).toContain(powerStateColor('suspended'))
  })

  it('distinguishes powered off from powered on', async () => {
    const off = await mountFull(
      [{ id: 'd', label: 'vm', vertexId: 'v', namespace: 'vmware',
        icon: 'vmware.VIRTUALMACHINE_ICON_OFF', x: 0, y: 0 }],
      'd'
    )
    expect(off.wrapper.text()).toContain('Powered off')
    expect(off.wrapper.text()).not.toContain('Powered on')
  })

  it('shows no power state where the provider names none', async () => {
    const { wrapper } = await mountFull(
      [{ id: 'dc', label: 'Lab Datacenter', vertexId: 'vcenter1.lab', namespace: 'vmware',
        icon: 'vmware.DATACENTER_ICON', x: 0, y: 0 }],
      'dc'
    )
    expect(wrapper.text()).toContain('Icon key')
    expect(wrapper.text()).not.toContain('Power state')
    expect(wrapper.find('.ti-detail-dot').exists()).toBe(false)
  })
})

describe('TopologyInspector node details', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  const mountWithNode = async (node: Record<string, unknown>) => {
    vi.mocked(getNodeById).mockResolvedValue(node as never)
    const wrapper = mount(TopologyInspector, {
      props: { canvas: null, variant: 'full' },
      global: {
        plugins: [PrimeVue, createTestingPinia({ stubActions: false })],
        // Leaflet needs real layout, which happy-dom has none of; the props it
        // is handed are what matters here.
        stubs: {
          TopologyLocationMap: {
            name: 'TopologyLocationMap',
            props: ['lat', 'lon'],
            template: '<div class="map-stub" />'
          }
        }
      }
    })
    const store = useTopologyStore()
    store.selectedIds = ['placed-7'] as never
    await flushPromises()
    return { wrapper, store }
  }

  it('titles the block and names the severity as the highest across alarms', async () => {
    const { wrapper } = await mountWithNode({ id: 7, label: 'core-sw1', location: 'HQ' })
    expect(wrapper.text()).toContain('Node Details')
    expect(wrapper.text()).toContain('Highest Alarm Severity')
    // "Severity" alone read as the node's own state, and did not say it is a
    // maximum across every alarm on the node.
    expect(wrapper.text()).not.toMatch(/(?<!Highest Alarm )Severity/)
  })

  // The legacy map calls sysObjectId the Enterprise OID, and omits the row
  // entirely for a node with no SNMP data rather than showing a blank.
  it('shows the Enterprise OID when the node has one', async () => {
    const { wrapper } = await mountWithNode({
      id: 7, label: 'core-sw1', sysObjectId: '.1.3.6.1.4.1.9.1.485'
    })
    expect(wrapper.text()).toContain('Enterprise OID')
    expect(wrapper.text()).toContain('.1.3.6.1.4.1.9.1.485')
  })

  it('omits the Enterprise OID row when the node has none', async () => {
    const { wrapper } = await mountWithNode({ id: 7, label: 'core-sw1' })
    expect(wrapper.text()).not.toContain('Enterprise OID')
  })
})

describe('TopologyInspector color pickers', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows the selected shape\'s current colors', async () => {
    const { wrapper } = await mountInspector()
    expect(picker(wrapper, 'Border color').props('modelValue')).toBe('#aaaaaa')
    expect(picker(wrapper, 'Fill color').props('modelValue')).toBe('#eeeeee')
  })

  it('writes the border color to the selected shape', async () => {
    const { wrapper, store } = await mountInspector()
    picker(wrapper, 'Border color').vm.$emit('update:modelValue', '#123456')
    await flushPromises()

    expect(store.getShape('shape-a')?.stroke).toBe('#123456')
    expect(store.getShape('shape-b')?.stroke).toBe('#bbbbbb')
  })

  it('writes the fill color to the selected shape', async () => {
    const { wrapper, store } = await mountInspector()
    picker(wrapper, 'Fill color').vm.$emit('update:modelValue', '#654321')
    await flushPromises()

    expect(store.getShape('shape-a')?.fill).toBe('#654321')
    expect(store.getShape('shape-b')?.fill).toBe('#dddddd')
  })

  it('follows the selection to another shape', async () => {
    const { wrapper, store } = await mountInspector()

    store.selectedIds = [shapeB.id] as never
    await flushPromises()

    expect(picker(wrapper, 'Border color').props('modelValue')).toBe('#bbbbbb')
    picker(wrapper, 'Border color').vm.$emit('update:modelValue', '#0f0f0f')
    await flushPromises()

    expect(store.getShape('shape-b')?.stroke).toBe('#0f0f0f')
    expect(store.getShape('shape-a')?.stroke).toBe('#aaaaaa')
  })

})

// A node's asset coordinates, shown as the Vaadin map's info panel did. Read off
// the node the inspector already fetched, so no extra request is made.
describe('TopologyInspector geographic location', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  const mountWithNode = async (node: Record<string, unknown>) => {
    vi.mocked(getNodeById).mockResolvedValue(node as never)
    const wrapper = mount(TopologyInspector, {
      props: { canvas: null, variant: 'full' },
      global: {
        plugins: [PrimeVue, createTestingPinia({ stubActions: false })],
        stubs: {
          TopologyLocationMap: {
            name: 'TopologyLocationMap',
            props: ['lat', 'lon'],
            template: '<div class="map-stub" />'
          }
        }
      }
    })
    const store = useTopologyStore()
    store.selectedIds = ['placed-7'] as never
    await flushPromises()
    return { wrapper }
  }

  it('maps a node that has coordinates, and captions it', async () => {
    const { wrapper } = await mountWithNode({
      id: 7, label: 'core-sw1',
      assetRecord: { latitude: 35.7796, longitude: -78.6382, city: 'Raleigh', state: 'NC' }
    })
    expect(wrapper.text()).toContain('Geographic Location')
    expect(wrapper.text()).toContain('Raleigh, NC')
    const map = wrapper.findComponent({ name: 'TopologyLocationMap' })
    expect(map.props()).toEqual({ lat: 35.7796, lon: -78.6382 })
  })

  it('shows nothing at all for a node with no coordinates', async () => {
    const { wrapper } = await mountWithNode({
      id: 7, label: 'core-sw1', assetRecord: { city: 'Raleigh' }
    })
    expect(wrapper.text()).not.toContain('Geographic Location')
    expect(wrapper.find('.map-stub').exists()).toBe(false)
  })

  it('treats a half-populated position as none, being unplaceable', async () => {
    const { wrapper } = await mountWithNode({
      id: 7, label: 'core-sw1', assetRecord: { latitude: 35.7796 }
    })
    expect(wrapper.find('.map-stub').exists()).toBe(false)
  })

  // The API sends an unset asset field as JSON null, and Number(null) is 0, not
  // NaN -- so this plotted a node with only a longitude on the equator.
  it('treats an explicitly null coordinate as absent, not as zero', async () => {
    const { wrapper } = await mountWithNode({
      id: 7, label: 'core-sw1', assetRecord: { latitude: null, longitude: -87.6658 }
    })
    expect(wrapper.find('.map-stub').exists()).toBe(false)
  })

  it('treats an empty-string coordinate the same way', async () => {
    const { wrapper } = await mountWithNode({
      id: 7, label: 'core-sw1', assetRecord: { latitude: '', longitude: -87.6658 }
    })
    expect(wrapper.find('.map-stub').exists()).toBe(false)
  })

  it('treats 0,0 as unset rather than the Gulf of Guinea', async () => {
    const { wrapper } = await mountWithNode({
      id: 7, label: 'core-sw1', assetRecord: { latitude: 0, longitude: 0 }
    })
    expect(wrapper.find('.map-stub').exists()).toBe(false)
  })

  it('accepts coordinates sent as strings', async () => {
    const { wrapper } = await mountWithNode({
      id: 7, label: 'core-sw1', assetRecord: { latitude: '35.7796', longitude: '-78.6382' }
    })
    const map = wrapper.findComponent({ name: 'TopologyLocationMap' })
    expect(map.props()).toEqual({ lat: 35.7796, lon: -78.6382 })
  })

  it('does not confuse the monitoring location with a place', async () => {
    const { wrapper } = await mountWithNode({
      id: 7, label: 'core-sw1', location: 'Default', assetRecord: {}
    })
    expect(wrapper.text()).toContain('Default')
    expect(wrapper.text()).not.toContain('Geographic Location')
  })
})

// The link's interface state, from /nodes/{id}/snmpinterfaces. Named rather than
// mapped to up/down, and shown with how it got there: the same two numbers are
// seconds old where the SNMP Interface Poller runs and a day old where only the
// node scan writes them.
describe('TopologyInspector link interface state', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  const mountWithLink = async (iface: Record<string, unknown> | null) => {
    vi.mocked(getInterfaceState).mockResolvedValue(iface as never)
    const canvas = {
      getLink: vi.fn(() => ({
        id: 'e1',
        sourceId: 'placed-1',
        targetId: 'placed-2',
        sourceLabel: 'core-01',
        targetLabel: 'dist-02',
        origin: 'discovered',
        binding: { protocol: 'lldp', sourcePort: 'Gi0/2', targetPort: 'Gi0/1', sourceIfIndex: 2 }
      })),
      setLinkLabel: vi.fn(),
      getNodeIconOverride: vi.fn(),
      setNodeIconOverride: vi.fn()
    }
    const wrapper = mount(TopologyInspector, {
      props: { canvas: canvas as never, variant: 'full' },
      global: { plugins: [PrimeVue, createTestingPinia({ stubActions: false })] }
    })
    const store = useTopologyStore()
    store.selectedIds = ['e1'] as never
    await flushPromises()
    return { wrapper }
  }

  it('names the raw oper status and says when it was polled', async () => {
    const { wrapper } = await mountWithLink({
      ifIndex: 2, ifName: 'Gi0/2', ifAdminStatus: 1, ifOperStatus: 7,
      lastSnmpPoll: Date.now() - 90_000, lastCapsdPoll: Date.now() - 6 * 3600_000
    })
    const text = wrapper.text()
    expect(text).toContain('lowerLayerDown')
    expect(text).toContain('polled')
    // The interface's name rides along with its index.
    expect(text).toContain('Gi0/2')
  })

  it('says the state came from the node scan when the poller has not run', async () => {
    const { wrapper } = await mountWithLink({
      ifIndex: 2, ifOperStatus: 1, lastSnmpPoll: null, lastCapsdPoll: Date.now() - 6 * 3600_000
    })
    expect(wrapper.text()).toContain('from the last node scan')
    expect(wrapper.text()).not.toContain('polled ')
  })

  it('shows no state rows when the interface cannot be read', async () => {
    const { wrapper } = await mountWithLink(null)
    expect(wrapper.text()).toContain('Source ifIndex')
    expect(wrapper.text()).not.toContain('Oper status')
    expect(wrapper.text()).not.toContain('Admin status')
  })

  it('asks for the interface enlinkd named, on the link\'s source node', async () => {
    await mountWithLink({ ifIndex: 2, ifOperStatus: 1 })
    expect(vi.mocked(getInterfaceState)).toHaveBeenCalledWith(1, 2)
  })
})
