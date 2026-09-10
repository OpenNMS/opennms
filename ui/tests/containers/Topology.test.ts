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
import { defineComponent, h, nextTick } from 'vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import Topology from '@/containers/Topology.vue'
import { useTopologyStore } from '@/stores/topologyStore'

// Deleting a view moved off PrimeVue's imperative confirm service onto a
// rendered OnmsConfirmationDialog, which means the view being deleted is now
// held in component state between opening the dialog and answering it. These
// cover that hand-off: nothing is deleted until the dialog is confirmed, and
// cancelling leaves the view alone.
const showToast = vi.fn()
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  useOnmsToast: () => ({ showToast, hideAllToasts: vi.fn() })
}))

const router = createRouter({
  history: createWebHashHistory(),
  routes: [{ path: '/topology/:source?', name: 'Topology', component: { template: '<div />' }}]
})

// Hoisted so the mock factory below and the tests share one exposed object: the
// container grabs it through its template ref on mount, and reassigning a method
// on the component instance afterwards would not be seen.
const canvasApi = vi.hoisted(() => ({
  fit: vi.fn(),
  centerOnNode: vi.fn(),
  serialize: vi.fn(() => ({ nodes: [], links: [], viewport: { zoom: 1, panX: 0, panY: 0 }})),
  loadView: vi.fn(),
  loadDiscoveredGraph: vi.fn(),
  getLink: vi.fn(),
  setLinkLabel: vi.fn(),
  placeNeighbor: vi.fn(),
  getNodeIconOverride: vi.fn(),
  setNodeIconOverride: vi.fn(),
  exportImage: vi.fn()
}))

// The canvas is mocked at the module level, not stubbed at mount: it pulls in
// sigma, which touches WebGL2RenderingContext on import and so fails under
// happy-dom before any stub would apply. The other three only fetch on mount.
vi.mock('@/components/Topology/TopologyCanvas.vue', () => ({
  default: defineComponent({
    setup: (_props, { expose }) => {
      // The page calls into the canvas through its exposed API on nearly every
      // state change; a bare stub makes those calls throw mid-render.
      expose(canvasApi)
      return () => h('div')
    }
  })
}))

// The page loads a source on mount. Stub the service so nothing reaches the
// network and the store settles before the state below is applied.
vi.mock('@/services/topologyService', () => ({
  listViews: vi.fn().mockResolvedValue([]),
  getView: vi.fn().mockResolvedValue(null),
  saveView: vi.fn().mockResolvedValue(null),
  deleteView: vi.fn().mockResolvedValue(true),
  getNodeSeverities: vi.fn().mockResolvedValue({}),
  getNodeIconIds: vi.fn().mockResolvedValue({}),
  // Returns DiscoveredGraph | false, never null: false is the failure value.
  loadDiscoveredGraph: vi.fn().mockResolvedValue(false),
  listGraphContainers: vi.fn().mockResolvedValue([]),
  getNodeCategories: vi.fn().mockResolvedValue({}),
  fetchPaletteNodes: vi.fn().mockResolvedValue([]),
  assetUrl: vi.fn()
}))

const stubs = {
  TopologyPalette: { template: '<div />' },
  TopologyInspector: { template: '<div />' },
  TopologyExplorePanel: { template: '<div />' }
}

const currentView = { id: 'v1', name: 'Core switches' }

const mounted: { unmount: () => void }[] = []

const unmountAll = () => {
  while (mounted.length) {
    mounted.pop()!.unmount()
  }
}

const mountPage = async (source = 'custom') => {
  await router.push(`/topology/${source}`)
  await router.isReady()
  const wrapper = mount(Topology, {
    global: { plugins: [PrimeVue, router, createTestingPinia({ stubActions: false })], stubs },
    attachTo: document.body
  })
  mounted.push(wrapper)
  const store = useTopologyStore()
  vi.spyOn(store, 'refreshCatalog').mockResolvedValue(true)
  vi.spyOn(store, 'refreshStatus').mockResolvedValue(undefined as never)
  // Let the on-mount load settle first; it would otherwise overwrite this.
  await flushPromises()
  store.currentView = currentView as never
  store.catalog = [currentView] as never
  await flushPromises()
  // The on-mount load toasts about the empty stub catalog; only what the
  // delete flow emits from here on is under test.
  showToast.mockClear()
  return { wrapper, store }
}

const click = async (label: string, scope: ParentNode = document) => {
  const button = Array.from(scope.querySelectorAll('button'))
    .find(b => b.textContent?.trim() === label)
  expect(button, `no "${label}" button rendered`).toBeTruthy()
  ;(button as HTMLElement).click()
  await flushPromises()
  await nextTick()
  await flushPromises()
}

// Once the dialog is open the toolbar's Delete and the dialog's Delete are both
// on the page, so answering it has to be scoped to the dialog.
const clickInDialog = async (label: string) => {
  const dialog = document.querySelector('.p-dialog')
  expect(dialog, 'dialog is not open').toBeTruthy()
  await click(label, dialog!)
}

describe('Topology delete confirmation', () => {
  afterEach(() => {
    unmountAll()
    document.body.innerHTML = ''
    vi.restoreAllMocks()
  })

  it('does not delete until the dialog is confirmed', async () => {
    const { store } = await mountPage()
    const removeView = vi.spyOn(store, 'removeView').mockResolvedValue(true)
    await click('Delete')

    expect(document.body.textContent).toContain('Delete view "Core switches"?')
    expect(removeView).not.toHaveBeenCalled()
  })

  it('deletes the view held from when the dialog opened', async () => {
    const { store } = await mountPage()
    const removeView = vi.spyOn(store, 'removeView').mockResolvedValue(true)
    vi.spyOn(store, 'openView').mockResolvedValue(null as never)

    await click('Delete')
    await clickInDialog('Delete')

    expect(removeView).toHaveBeenCalledWith('v1')
    expect(showToast).toHaveBeenCalledWith({
      message: 'View "Core switches" deleted',
      severity: 'success',
      timeout: 3000
    })
  })

  it('leaves the view alone when the dialog is cancelled', async () => {
    const { store } = await mountPage()
    const removeView = vi.spyOn(store, 'removeView').mockResolvedValue(true)

    await click('Delete')
    await clickInDialog('Cancel')

    expect(removeView).not.toHaveBeenCalled()
    expect(showToast).not.toHaveBeenCalled()
    expect(document.body.textContent).not.toContain('Delete view "Core switches"?')
  })
})

// The gate defers layout, not fetching, so the graph is in hand and the page can
// offer a starting point instead of only telling the user to go and search.
describe('Topology large-graph gate', () => {
  afterEach(() => {
    unmountAll()
    document.body.innerHTML = ''
    vi.clearAllMocks()
  })

  // 3001 vertices to clear LARGE_GRAPH_THRESHOLD, with 'hub' wired to the first
  // 50 so it wins on degree outright.
  const hugeGraph = () => ({
    source: { container: 'enlinkd', namespace: 'nodes:Layer2' },
    label: 'Layer2',
    nodes: [
      { id: 'hub', label: 'core-router-1', x: 0, y: 0 },
      ...Array.from({ length: 3000 }, (_, i) => ({ id: `n${i}`, label: `node-${i}`, x: 0, y: 0 }))
    ],
    links: Array.from({ length: 50 }, (_, i) => ({
      id: `hub|n${i}`, sourceId: 'hub', targetId: `n${i}`, origin: 'discovered' as const
    }))
  })

  it('offers the most-connected node as a starting point, by label', async () => {
    const { store } = await mountPage()
    store.discoveredGraph = hugeGraph() as never
    await flushPromises()

    expect(document.querySelector('.large-graph-gate')).toBeTruthy()
    const labels = Array.from(document.querySelectorAll('.large-graph-gate button'))
      .map(b => b.textContent?.trim())
    expect(labels).toContain('Start focus at core-router-1')
    expect(labels).toContain('Render all 3,001 nodes')
  })

  // The gate's other escape hatch. It was asserted to exist as a button label
  // and never clicked, so nothing covered that it actually renders the graph.
  it('renders the graph when the opt-in is clicked', async () => {
    const { store } = await mountPage()
    store.discoveredGraph = hugeGraph() as never
    await flushPromises()
    expect(document.querySelector('.large-graph-gate')).toBeTruthy()

    await click('Render all 3,001 nodes')

    expect(store.isLargeGraphGated).toBe(false)
    expect(document.querySelector('.large-graph-gate')).toBeFalsy()
    expect(canvasApi.loadDiscoveredGraph).toHaveBeenCalled()
  })

  it('starting there focuses that node in the URL, so the view is shareable', async () => {
    // A discovered slug: focus only mirrors from the URL into the store there.
    const { store } = await mountPage('layer2')
    store.discoveredGraph = hugeGraph() as never
    await flushPromises()

    await click('Start focus at core-router-1')

    expect(router.currentRoute.value.query.focus).toBe('hub')
    expect(router.currentRoute.value.query.szl).toBe('2')
    expect(store.focusNodeId).toBe('hub')
    // Focus dismisses the gate: the focus subgraph is cheap to lay out.
    expect(document.querySelector('.large-graph-gate')).toBeFalsy()
  })
})

// Discovered structure changes only when enlinkd rescans (daily by default), so
// it is refetched on demand. The status button did not do this: it only recolored
// what was already on screen.
describe('Topology discovered refresh', () => {
  afterEach(() => {
    unmountAll()
    document.body.innerHTML = ''
    vi.clearAllMocks()
  })

  it('refetches the graph on a discovered source, keeping the focus from the URL', async () => {
    const service = await import('@/services/topologyService')
    const graph = {
      source: { container: 'enlinkd', namespace: 'nodes:Layer2' },
      label: 'Layer2',
      nodes: [{ id: 'a', label: 'a', x: 0, y: 0 }, { id: 'b', label: 'b', x: 0, y: 0 }],
      links: [{ id: 'a|b', sourceId: 'a', targetId: 'b', origin: 'discovered' }]
    }
    vi.mocked(service.loadDiscoveredGraph).mockResolvedValue(graph as never)

    // mountPage pushes the route itself, so the focus query goes on afterwards.
    const { store } = await mountPage('layer2')
    await router.replace({ path: '/topology/layer2', query: { focus: 'a', szl: '1' }})
    await flushPromises()
    expect(store.focusNodeId).toBe('a')
    vi.mocked(service.loadDiscoveredGraph).mockClear()

    await click('Refresh Graph')

    expect(vi.mocked(service.loadDiscoveredGraph)).toHaveBeenCalledTimes(1)
    // loadDiscoveredSource clears focus; the URL is what puts it back.
    expect(store.focusNodeId).toBe('a')
    expect(store.semanticZoomLevel).toBe(1)
  })

  it('keeps the status-only label and behavior on a custom source', async () => {
    const service = await import('@/services/topologyService')
    const { store } = await mountPage('custom')
    vi.mocked(service.loadDiscoveredGraph).mockClear()
    const refreshStatus = vi.spyOn(store, 'refreshStatus').mockResolvedValue(undefined as never)

    await click('Refresh status')

    expect(refreshStatus).toHaveBeenCalled()
    expect(vi.mocked(service.loadDiscoveredGraph)).not.toHaveBeenCalled()
  })
})

// Search is in the toolbar for both kinds of view; what picking a result does
// differs, because a custom view has no focus/SZL to reduce.
//
// Driven through the seam component's events rather than the DOM: PrimeVue's
// overlay panel does not render under happy-dom, so the suggestion list never
// reaches the document. The rendered second line is covered by the unit tests
// for searchFieldLabel.
describe('Topology search', () => {
  afterEach(() => {
    unmountAll()
    document.body.innerHTML = ''
    vi.clearAllMocks()
  })

  const searchBox = (wrapper: ReturnType<typeof mount>) => {
    const box = wrapper.findAllComponents({ name: 'OnmsAutoComplete' })
      .find(c => c.classes().includes('topology-search'))
    expect(box, 'search box is not rendered').toBeTruthy()
    return box!
  }

  const suggest = async (wrapper: ReturnType<typeof mount>, query: string) => {
    const box = searchBox(wrapper)
    box.vm.$emit('complete', query)
    await flushPromises()
    return box
  }

  it('finds a discovered node by IP and reports what matched', async () => {
    const service = await import('@/services/topologyService')
    vi.mocked(service.loadDiscoveredGraph).mockResolvedValue({
      source: { container: 'enlinkd', namespace: 'nodes:Layer2' },
      label: 'Layer2',
      nodes: [
        { id: 'a', label: 'core-01', nodeId: 1, x: 0, y: 0, properties: { ipAddress: '10.0.0.1' }},
        { id: 'b', label: 'dist-02', nodeId: 2, x: 0, y: 0, properties: { ipAddress: '192.168.5.9' }}
      ],
      links: []
    } as never)

    const { wrapper } = await mountPage('layer2')
    const box = await suggest(wrapper, '192.168')

    expect(box.props('suggestions')).toEqual([
      {
        kind: 'node',
        label: 'dist-02',
        node: expect.objectContaining({ id: 'b', label: 'dist-02' }),
        matchedOn: { key: 'ipAddress', value: '192.168.5.9' }
      }
    ])
  })

  it('focuses the picked node on a discovered view', async () => {
    const service = await import('@/services/topologyService')
    vi.mocked(service.loadDiscoveredGraph).mockResolvedValue({
      source: { container: 'enlinkd', namespace: 'nodes:Layer2' },
      label: 'Layer2',
      nodes: [{ id: 'a', label: 'core-01', nodeId: 1, x: 0, y: 0 }],
      links: []
    } as never)

    const { wrapper } = await mountPage('layer2')
    const box = await suggest(wrapper, 'core')
    box.vm.$emit('option-select', box.props('suggestions')[0])
    await flushPromises()

    expect(router.currentRoute.value.query.focus).toBe('a')
  })

  it('searches a custom view, where picking a result selects and pans instead', async () => {
    const { wrapper, store } = await mountPage('custom')
    // A custom view's nodes come from the canvas's own graph, not the store.
    canvasApi.serialize.mockReturnValue({
      nodes: [{ id: 'n1', label: 'edge-switch', x: 5, y: 5 }],
      links: [],
      viewport: { zoom: 1, panX: 0, panY: 0 }
    } as never)

    const box = await suggest(wrapper, 'edge')
    expect(box.props('suggestions')).toEqual([
      { kind: 'node', label: 'edge-switch', node: expect.objectContaining({ id: 'n1' }) }
    ])

    box.vm.$emit('option-select', box.props('suggestions')[0])
    await flushPromises()

    expect(store.selectedIds).toEqual(['n1'])
    expect(canvasApi.centerOnNode).toHaveBeenCalledWith('n1')
    // A custom view has no focus/SZL, so the URL must stay clean.
    expect(router.currentRoute.value.query.focus).toBeUndefined()
  })

  it('selects a category\'s members, leaving focus alone', async () => {
    const service = await import('@/services/topologyService')
    vi.mocked(service.loadDiscoveredGraph).mockResolvedValue({
      source: { container: 'enlinkd', namespace: 'nodes:Layer2' },
      label: 'Layer2',
      nodes: [
        { id: 'a', label: 'core-01', nodeId: 1, x: 0, y: 0 },
        { id: 'b', label: 'dist-02', nodeId: 2, x: 0, y: 0 },
        { id: 'c', label: 'access-03', nodeId: 3, x: 0, y: 0 }
      ],
      links: []
    } as never)
    vi.mocked(service.getNodeCategories).mockResolvedValue({ 1: ['Core'], 2: ['Core'], 3: ['Access'] })

    const { wrapper, store } = await mountPage('layer2')
    const box = await suggest(wrapper, 'core')

    const category = box.props('suggestions')[0]
    expect(category).toEqual({ kind: 'category', label: 'Core', canvasIds: ['a', 'b'] })

    box.vm.$emit('option-select', category)
    await flushPromises()

    expect(store.selectedIds).toEqual(['a', 'b'])
    // A category is not a place, so it must not move the focus.
    expect(router.currentRoute.value.query.focus).toBeUndefined()
  })

  // A slow category fetch used to land after a newer keystroke and replace its
  // suggestions, so the list showed hits for a query the box no longer held.
  // Exercised across two different node sets, because same-set keystrokes now
  // share one in-flight fetch and cannot race by construction.
  it('does not let a slow query overwrite a newer one', async () => {
    const service = await import('@/services/topologyService')
    const graph = (nodes: unknown[]) => ({
      source: { container: 'enlinkd', namespace: 'nodes:Layer2' },
      label: 'Layer2',
      nodes,
      links: []
    })
    vi.mocked(service.loadDiscoveredGraph).mockResolvedValue(
      graph([{ id: 'a', label: 'core-01', nodeId: 1, x: 0, y: 0 }]) as never
    )

    let release: (v: Record<number, string[]>) => void = () => {}
    vi.mocked(service.getNodeCategories).mockImplementationOnce(
      () => new Promise((resolve) => {
        release = resolve
      })
    )

    const { wrapper, store } = await mountPage('layer2')
    const box = searchBox(wrapper)

    box.vm.$emit('complete', 'core') // fetch for node set {1}: deferred
    await flushPromises()

    // The graph changes under the search, so the next keystroke has its own key
    // and its own (immediately resolving) fetch.
    store.discoveredGraph = graph([{ id: 'b', label: 'dist-02', nodeId: 2, x: 0, y: 0 }]) as never
    box.vm.$emit('complete', 'dist')
    await flushPromises()

    release({}) // the first, older query's fetch finally lands
    await flushPromises()

    expect(box.props('suggestions').map((m: { label: string }) => m.label)).toEqual(['dist-02'])
  })

  // The hop stepper says "2 hops" with no indication of from where, so the box
  // carries the focused node. Driven from the focus, not the select handler, so
  // it is right however the focus was set.
  it('shows the focused node in the search box', async () => {
    const service = await import('@/services/topologyService')
    vi.mocked(service.loadDiscoveredGraph).mockResolvedValue({
      source: { container: 'enlinkd', namespace: 'nodes:Layer2' },
      label: 'Layer2',
      nodes: [
        { id: 'a', label: 'core-01', nodeId: 1, x: 0, y: 0 },
        { id: 'b', label: 'dist-02', nodeId: 2, x: 0, y: 0 }
      ],
      links: []
    } as never)

    const { wrapper, store } = await mountPage('layer2')
    expect(searchBox(wrapper).props('modelValue')).toBe('')

    // Set via the URL, i.e. not through the select handler at all.
    await router.replace({ path: '/topology/layer2', query: { focus: 'b', szl: '2' }})
    await flushPromises()
    expect(store.focusNodeId).toBe('b')
    expect(searchBox(wrapper).props('modelValue')).toBe('dist-02')

    // And it empties when the focus is dropped.
    await router.replace({ path: '/topology/layer2', query: {}})
    await flushPromises()
    expect(searchBox(wrapper).props('modelValue')).toBe('')
  })

  it('fetches categories once per graph', async () => {
    const service = await import('@/services/topologyService')
    const { wrapper } = await mountPage('layer2')
    vi.mocked(service.getNodeCategories).mockClear()

    await suggest(wrapper, 'a')
    await suggest(wrapper, 'ab')

    expect(vi.mocked(service.getNodeCategories)).toHaveBeenCalledTimes(1)
  })
})

// New, Save As and Rename share one dialog. What differs per mode is the seed,
// which names count as taken, and what the answer does; these cover that, and
// that nothing happens until the dialog is answered.
describe('Topology view naming', () => {
  afterEach(() => {
    unmountAll()
    document.body.innerHTML = ''
    vi.restoreAllMocks()
  })

  const nameField = () => document.querySelector('.p-dialog input') as HTMLInputElement

  const typeName = async (value: string) => {
    const input = nameField()
    expect(input, 'the name dialog is not open').toBeTruthy()
    input.value = value
    input.dispatchEvent(new Event('input'))
    await nextTick()
  }

  it('seeds Rename with the open view and Save As with it too, New with nothing', async () => {
    const { wrapper } = await mountPage()

    await click('Rename')
    expect(nameField().value).toBe('Core switches')
    await clickInDialog('Cancel')

    await click('Save As')
    expect(nameField().value).toBe('Core switches')
    await clickInDialog('Cancel')

    await click('New')
    expect(nameField().value).toBe('')
    expect(wrapper.exists()).toBe(true)
  })

  it('renames nothing until the dialog is answered', async () => {
    const { store } = await mountPage()
    const renameView = vi.spyOn(store, 'renameView').mockResolvedValue(true)

    await click('Rename')
    await typeName('Distribution')
    expect(renameView).not.toHaveBeenCalled()

    await clickInDialog('Rename')
    expect(renameView).toHaveBeenCalledWith('v1', 'Distribution')
  })

  it('cancelling Rename leaves the view alone', async () => {
    const { store } = await mountPage()
    const renameView = vi.spyOn(store, 'renameView').mockResolvedValue(true)

    await click('Rename')
    await typeName('Distribution')
    await clickInDialog('Cancel')

    expect(renameView).not.toHaveBeenCalled()
    expect(store.currentView!.name).toBe('Core switches')
  })

  // The two differ on the open view's own name: Save As has to create a new
  // entry, so reusing it is a collision, while Rename may keep it.
  it('treats the open view name as taken for Save As but not for Rename', async () => {
    await mountPage()

    await click('Save As')
    expect(document.querySelector('.p-dialog')!.textContent)
      .toContain('A view named "Core switches" already exists.')
    await clickInDialog('Cancel')

    await click('Rename')
    expect(document.querySelector('.p-dialog')!.textContent)
      .not.toContain('already exists')
  })

  it('does not rename when the name comes back unchanged', async () => {
    const { store } = await mountPage()
    const renameView = vi.spyOn(store, 'renameView').mockResolvedValue(true)

    await click('Rename')
    await clickInDialog('Rename')

    expect(renameView).not.toHaveBeenCalled()
  })

  it('blocks a Save As onto another existing view, before any request', async () => {
    const { store } = await mountPage()
    store.catalog = [currentView, { id: 'v2', name: 'Edge routers' }] as never
    await nextTick()
    const saveAs = vi.spyOn(store, 'saveCurrentViewAs').mockResolvedValue(true)

    await click('Save As')
    await typeName('Edge routers')
    const action = Array.from(document.querySelectorAll('.p-dialog button'))
      .find(b => b.textContent?.trim() === 'Save') as HTMLButtonElement
    expect(action.disabled).toBe(true)

    await typeName('Edge routers 2')
    await clickInDialog('Save')
    expect(saveAs).toHaveBeenCalledWith('Edge routers 2', expect.anything())
  })

  it('creates a new view under the name given, and saves it', async () => {
    const service = await import('@/services/topologyService')
    const { store } = await mountPage()
    // The real save action runs, so the name has to survive the round trip:
    // the server's record replaces the open view.
    vi.mocked(service.saveView).mockImplementation(
      async view => ({ ...view, id: 'v9' }) as never
    )

    await click('New')
    await typeName('Spine fabric')
    await clickInDialog('Create')

    expect(vi.mocked(service.saveView).mock.calls[0][0]).toMatchObject({ name: 'Spine fabric' })
    expect(store.currentView).toMatchObject({ id: 'v9', name: 'Spine fabric' })
    expect(store.isEditMode).toBe(true)
  })
})
