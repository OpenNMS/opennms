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

import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it, vi } from 'vitest'
import TopologyExplorePanel from '@/components/Topology/TopologyExplorePanel.vue'
import { getAlarms } from '@/services/alarmService'
import { useTopologyStore } from '@/stores/topologyStore'

// vi.mock is hoisted above the module body, so the fixtures live inside the
// factories rather than as consts it would not be able to reach.
vi.mock('@/services/nodeService', () => ({
  getNodes: vi.fn().mockResolvedValue({
    node: [
      { id: 7, label: 'core-sw1', location: 'HQ' },
      { id: 8, label: 'edge-sw2', location: 'DC' },
      { id: 9, label: 'edge-sw3', location: 'DC' }
    ]
  })
}))
vi.mock('@/services/alarmService', () => ({
  getAlarms: vi.fn().mockResolvedValue({
    alarm: [
      { id: 1, nodeId: 7, nodeLabel: 'core-sw1', severity: 'MAJOR', logMessage: 'link down', lastEventTime: 0 },
      { id: 2, nodeId: 8, nodeLabel: 'edge-sw2', severity: 'MINOR', logMessage: 'iface flap', lastEventTime: 0 },
      // A third, so selecting two of three distinguishes filtered from not.
      { id: 3, nodeId: 9, nodeLabel: 'edge-sw3', severity: 'WARNING', logMessage: 'high latency', lastEventTime: 0 }
    ]
  })
}))
// Only the two the panel adds; the store imports plenty else from this module.
vi.mock('@/services/topologyService', async importOriginal => ({
  ...(await importOriginal<typeof import('@/services/topologyService')>()),
  // The store refreshes these off placedNodeIds; stubbed so the suite makes no
  // network calls of its own.
  getNodeSeverities: vi.fn().mockResolvedValue({}),
  getNodeIconIds: vi.fn().mockResolvedValue({}),
  getApplications: vi.fn().mockResolvedValue([
    { id: 1, name: 'Review Billing', perspectiveLocations: ['Default'] },
    { id: 2, name: 'Review Storefront', perspectiveLocations: [] }
  ]),
  getPerspectiveOutages: vi.fn().mockResolvedValue([
    { id: 900001, nodeId: 7, nodeLabel: 'core-sw1', serviceName: 'HTTP-8080', perspective: 'Default', lostAt: 0 },
    { id: 900002, nodeId: 8, nodeLabel: 'edge-sw2', serviceName: 'HTTP-8080', perspective: 'Default', lostAt: 0 },
    { id: 900003, nodeId: 9, nodeLabel: 'edge-sw3', serviceName: 'HTTP-8080', perspective: 'Default', lostAt: 0 }
  ])
}))

const mountPanel = async () => {
  const wrapper = mount(TopologyExplorePanel, {
    global: { plugins: [PrimeVue, createTestingPinia({ stubActions: false })] }
  })
  const store = useTopologyStore()
  store.currentView = {
    name: 'v',
    nodes: [
      { id: 'placed-7', nodeId: 7, label: 'core-sw1', x: 0, y: 0 },
      { id: 'placed-8', nodeId: 8, label: 'edge-sw2', x: 0, y: 0 },
      { id: 'placed-9', nodeId: 9, label: 'edge-sw3', x: 0, y: 0 }
    ],
    links: [],
    labels: []
  } as never
  store.placedNodeIds = new Set(['7', '8', '9']) as never
  await flushPromises()
  // The panel opens collapsed, and the tabs and the fetch both hang off that.
  await wrapper.find('.te-toggle').trigger('click')
  await flushPromises()
  return { wrapper, store }
}

const tabLabels = (wrapper: Awaited<ReturnType<typeof mountPanel>>['wrapper']) =>
  wrapper.findAll('.te-tabs button').map(b => b.text().replace(/\s*\(\d+\)$/, ''))

const rowTexts = (wrapper: Awaited<ReturnType<typeof mountPanel>>['wrapper']) =>
  wrapper.findAll('tbody tr').map(r => r.text())

describe('TopologyExplorePanel staying current', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  // The column used to copy the severity at fetch time, so it froze while the
  // canvas recoloured on every status poll.
  it('reads the severity column from the store rather than freezing it', async () => {
    const { wrapper, store } = await mountPanel()
    const nodesTab = wrapper.findAll('.te-tabs button').find(b => b.text().includes('Nodes'))!
    await nodesTab.trigger('click')
    expect(rowTexts(wrapper).join(' ')).not.toContain('CRITICAL')

    // A status poll landing, with no refetch of the rows themselves.
    store.severities = { 7: 'CRITICAL' } as never
    await flushPromises()

    expect(rowTexts(wrapper).join(' ')).toContain('CRITICAL')
  })

  // Alarms were fetched once and left, so the canvas could show a node as newly
  // critical while the Alarms tab beside it still did not list the alarm.
  it('refetches when the status poll ticks', async () => {
    const { store } = await mountPanel()
    const before = vi.mocked(getAlarms).mock.calls.length

    store.statusRevision = store.statusRevision + 1
    await flushPromises()

    expect(vi.mocked(getAlarms).mock.calls.length).toBeGreaterThan(before)
  })

  it('does not refetch on a status tick while collapsed', async () => {
    const { wrapper, store } = await mountPanel()
    await wrapper.find('.te-toggle').trigger('click')
    await flushPromises()
    const before = vi.mocked(getAlarms).mock.calls.length

    store.statusRevision = store.statusRevision + 1
    await flushPromises()

    expect(vi.mocked(getAlarms).mock.calls.length).toBe(before)
  })
})

describe('TopologyExplorePanel selection filtering', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows everything when nothing is selected', async () => {
    const { wrapper } = await mountPanel()
    expect(rowTexts(wrapper)).toHaveLength(3)
  })

  it('filters to a single selected node', async () => {
    const { wrapper, store } = await mountPanel()
    store.selectedIds = ['placed-7'] as never
    await flushPromises()

    const rows = rowTexts(wrapper)
    expect(rows).toHaveLength(1)
    expect(rows[0]).toContain('core-sw1')
  })

  // Two or more used to fall back to showing everything unfiltered.
  it('filters to all of several selected nodes', async () => {
    const { wrapper, store } = await mountPanel()
    store.selectedIds = ['placed-7', 'placed-8'] as never
    await flushPromises()

    const rows = rowTexts(wrapper)
    expect(rows).toHaveLength(2)
    expect(rows.join(' ')).toContain('core-sw1')
    expect(rows.join(' ')).toContain('edge-sw2')
    // The unselected third node is what makes this different from no filter.
    expect(rows.join(' ')).not.toContain('edge-sw3')
  })

  it('excludes a selected node with no rows of its own', async () => {
    const { wrapper, store } = await mountPanel()
    store.selectedIds = ['placed-8'] as never
    await flushPromises()

    const rows = rowTexts(wrapper)
    expect(rows).toHaveLength(1)
    expect(rows[0]).toContain('edge-sw2')
  })

  // A vertex the canvas id cannot identify still resolves through the graph.
  it('resolves a selected discovered vertex to its node', async () => {
    const { wrapper, store } = await mountPanel()
    store.discoveredGraph = {
      source: { container: 'application', namespace: 'application' },
      label: 'Application Graph',
      nodes: [{ id: 'disc-Service:1', label: 'HTTP-8080', nodeId: 7, x: 0, y: 0 }],
      links: []
    } as never
    store.selectedIds = ['disc-Service:1'] as never
    await flushPromises()

    expect(rowTexts(wrapper).join(' ')).toContain('core-sw1')
  })
})

describe('TopologyExplorePanel application tabs', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  const withApplicationGraph = async () => {
    const mounted = await mountPanel()
    mounted.store.discoveredGraph = {
      source: { container: 'application', namespace: 'application' },
      label: 'Application Graph',
      nodes: [
        { id: 'disc-Application:1', label: 'Review Billing', x: 0, y: 0, properties: { applicationId: '1' }},
        { id: 'disc-Service:1', label: 'HTTP-8080', nodeId: 7, x: 0, y: 0 }
      ],
      links: [{ id: 'l1', sourceId: 'disc-Application:1', targetId: 'disc-Service:1', origin: 'discovered' }]
    } as never
    await flushPromises()
    return mounted
  }

  it('hides both tabs for a non-application graph', async () => {
    const { wrapper } = await mountPanel()
    expect(tabLabels(wrapper)).toEqual(['Alarms', 'Nodes'])
  })

  it('leads with alarms then perspective outages for the application graph', async () => {
    const { wrapper } = await withApplicationGraph()
    expect(tabLabels(wrapper)).toEqual(['Alarms', 'Perspective Outages', 'Applications', 'Nodes'])
  })

  it('lists applications with their perspectives and service count', async () => {
    const { wrapper } = await withApplicationGraph()
    const appTab = wrapper.findAll('.te-tabs button').find(b => b.text().includes('Applications'))!
    await appTab.trigger('click')

    const rows = rowTexts(wrapper)
    expect(rows.join(' ')).toContain('Review Billing')
    // One application-to-service edge in the graph above.
    expect(rows[0]).toContain('1')
    expect(rows[0]).toContain('Default')
    // An application with no perspective location reads as a dash, not blank.
    expect(rows[1]).toContain('—')
  })

  it('lists perspective outages and filters them by selection', async () => {
    const { wrapper, store } = await withApplicationGraph()
    const outageTab = wrapper.findAll('.te-tabs button').find(b => b.text().includes('Perspective Outages'))!
    await outageTab.trigger('click')
    expect(rowTexts(wrapper)).toHaveLength(3)

    store.selectedIds = ['placed-7'] as never
    await flushPromises()
    const rows = rowTexts(wrapper)
    expect(rows).toHaveLength(1)
    expect(rows[0]).toContain('core-sw1')
    expect(rows[0]).toContain('Default')
  })

  // The label counts used to show the unfiltered totals, so they never moved.
  it('counts what the tab will render, not the whole set', async () => {
    const { wrapper, store } = await withApplicationGraph()
    const counts = () => wrapper.findAll('.te-tabs button')
      .map(b => Number(/\((\d+)\)$/.exec(b.text().trim())?.[1]))

    expect(counts()).toEqual([3, 3, 2, 3])

    store.selectedIds = ['placed-7'] as never
    await flushPromises()
    // Alarms, perspective outages and nodes narrow to the one node; the
    // applications list is not node-scoped, so it stays whole.
    expect(counts()).toEqual([1, 1, 2, 1])
  })

  // Selecting an application stands for everything hanging off it.
  it('narrows to an application\'s services when the application is selected', async () => {
    const { wrapper, store } = await withApplicationGraph()
    store.selectedIds = ['disc-Application:1'] as never
    await flushPromises()

    const counts = wrapper.findAll('.te-tabs button')
      .map(b => Number(/\((\d+)\)$/.exec(b.text().trim())?.[1]))
    // The application's one service sits on node 7, so the node-scoped tabs
    // narrow to it, and the Applications tab narrows to the application itself.
    expect(counts).toEqual([1, 1, 1, 1])

    const appTab = wrapper.findAll('.te-tabs button').find(b => b.text().includes('Applications'))!
    await appTab.trigger('click')
    const rows = rowTexts(wrapper)
    expect(rows).toHaveLength(1)
    expect(rows[0]).toContain('Review Billing')
  })

  it('falls back to Alarms when the application graph goes away', async () => {
    const { wrapper, store } = await withApplicationGraph()
    const appTab = wrapper.findAll('.te-tabs button').find(b => b.text().includes('Applications'))!
    await appTab.trigger('click')

    store.discoveredGraph = null as never
    await flushPromises()

    const active = wrapper.findAll('.te-tabs button').filter(b => b.classes('active'))
    expect(active).toHaveLength(1)
    expect(active[0].text()).toContain('Alarms')
  })
})

describe('TopologyExplorePanel tabs', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('puts Alarms before Nodes', async () => {
    const { wrapper } = await mountPanel()
    expect(tabLabels(wrapper)).toEqual(['Alarms', 'Nodes'])
  })

  // The first tab has to be the active one, or the panel looks broken on open.
  it('opens on Alarms', async () => {
    const { wrapper } = await mountPanel()
    const active = wrapper.findAll('.te-tabs button').filter(b => b.classes('active'))
    expect(active).toHaveLength(1)
    expect(active[0].text()).toContain('Alarms')
  })

  it('switches to Nodes when that tab is clicked', async () => {
    const { wrapper } = await mountPanel()
    const nodesTab = wrapper.findAll('.te-tabs button').find(b => b.text().includes('Nodes'))!
    await nodesTab.trigger('click')

    const active = wrapper.findAll('.te-tabs button').filter(b => b.classes('active'))
    expect(active).toHaveLength(1)
    expect(active[0].text()).toContain('Nodes')
  })
})

// One FIQL clause per node id: a 3400-vertex view built a 30 KB URI and the
// server answered 414, so every tab came up empty.
describe('TopologyExplorePanel large views', () => {
  it('splits the node and alarm queries so the URI stays legal', async () => {
    const nodeService = await import('@/services/nodeService')
    const alarmService = await import('@/services/alarmService')
    const { store } = await mountPanel()
    vi.mocked(nodeService.getNodes).mockClear()
    vi.mocked(alarmService.getAlarms).mockClear()

    store.placedNodeIds = new Set(
      Array.from({ length: 700 }, (_, i) => String(1000 + i))
    ) as never
    await flushPromises()

    // Encoded bytes, not clause count: `node.id==` is five bytes longer per
    // clause than `id==` and axios percent-encodes `=`, so one clause count
    // cannot keep both endpoints inside Jetty's 4000-byte request budget. The
    // first version of this test counted clauses and passed while the alarm
    // query was still answering 414.
    const encodedBytes = (calls: unknown[][]) =>
      calls.map(([params]) => new URLSearchParams(
        Object.entries(params as Record<string, unknown>).map(([k, v]) => [k, String(v)])
      ).toString().length)

    const nodeCalls = vi.mocked(nodeService.getNodes).mock.calls
    expect(Math.max(...encodedBytes(nodeCalls))).toBeLessThan(3000)
    expect(Math.max(...encodedBytes(vi.mocked(alarmService.getAlarms).mock.calls)))
      .toBeLessThan(3000)

    // Split, but complete: every id is still asked for.
    const ids = nodeCalls.flatMap(([params]) =>
      String((params as { _s: string })._s).split(',').map(c => c.replace('id==', '')))
    expect(new Set(ids).size).toBe(700)
  })

  // The split is only half of it: the rows from every chunk have to survive.
  // Keeping just the last chunk passed the original test, because the mock
  // returned the same rows for every chunk and no row was ever asserted.
  it('aggregates rows across chunks rather than keeping the last', async () => {
    const nodeService = await import('@/services/nodeService')
    const { wrapper, store } = await mountPanel()

    let call = 0
    vi.mocked(nodeService.getNodes).mockImplementation(async () => {
      call += 1
      return { node: [{ id: 9000 + call, label: `chunk-${call}`, location: 'HQ' }] } as never
    })

    store.placedNodeIds = new Set(
      Array.from({ length: 700 }, (_, i) => String(1000 + i))
    ) as never
    await flushPromises()

    const nodesTab = wrapper.findAll('.te-tabs button').find(b => b.text().includes('Nodes'))!
    await nodesTab.trigger('click')
    await flushPromises()

    // One row per chunk, each labelled with its chunk number. Keeping only the
    // last chunk leaves exactly one row; aggregating leaves one per chunk.
    const rows = rowTexts(wrapper)
    expect(rows.length).toBeGreaterThan(1)
    expect(new Set(rows).size).toBe(rows.length)
  })
})
