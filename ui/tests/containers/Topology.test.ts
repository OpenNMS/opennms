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

