import { OnmsTooltip } from '@opennms/onms-ui'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { defineComponent, h } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import AdhocGraphBuilder from '@/components/AdhocGraphs/AdhocGraphBuilder.vue'
import { useAdhocGraphStore } from '@/stores/adhocGraphStore'
import { useMenuStore } from '@/stores/menuStore'

const getNodes = vi.fn()
const getNodesByFilterRule = vi.fn()
const getResourceForNode = vi.fn()
const getGraphMetrics = vi.fn()

vi.mock('@/services', () => ({
  default: {
    getNodes: (...args: unknown[]) => getNodes(...args),
    getNodesByFilterRule: (...args: unknown[]) => getNodesByFilterRule(...args),
    getResourceForNode: (...args: unknown[]) => getResourceForNode(...args),
    getGraphMetrics: (...args: unknown[]) => getGraphMetrics(...args)
  }
}))

let routeQuery: Record<string, unknown> = {}
const routerReplace = vi.fn()
const copyToClipboard = vi.fn()
const showSnackBar = vi.fn()

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar, hideSnackbar: vi.fn() })
}))

vi.mock('@/composables/useClipboard', () => ({
  copyToClipboard: (text: string) => copyToClipboard(text),
  default: () => ({ copyToClipboard })
}))

vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({
    path: '/adhoc-graphs',
    get query() {
      return routeQuery
    }
  })),
  useRouter: vi.fn(() => ({ push: vi.fn(), replace: routerReplace }))
}))

// Chart.js needs a real 2d context; happy-dom's canvas has none. The chart itself
// is covered by the adhocQuery unit tests — what matters here is the page wiring.
vi.mock('@/components/AdhocGraphs/AdhocChart.vue', () => ({
  default: defineComponent({
    name: 'AdhocChart',
    props: {
      config: { type: Object, required: true },
      measurements: { type: Object, default: null },
      time: { type: Object, required: true },
      loading: { type: Boolean, default: false },
      error: { type: String, default: '' },
      expanded: { type: Boolean, default: false }
    },
    setup(props, { expose }) {
      expose({ exportTarget: () => null })
      return () => h('div', { 'data-test': 'chart-stub' }, props.error ? [h('p', { 'data-test': 'chart-error' }, props.error)] : [])
    }
  })
}))

const RESOURCE_ID = 'node[1].interfaceSnmp[eth0]'
const RESPONSE_TIME_ID = 'node[1].responseTime[127.0.0.1]'
const KEY_IN = `${RESOURCE_ID}|ifHCInOctets`
const KEY_OUT = `${RESOURCE_ID}|ifHCOutOctets`

/** Node 1 as fornode lists it: one SNMP interface and one response-time resource. */
const NODE_RESOURCE = {
  id: 'node[1]',
  label: 'switch-01',
  name: '1',
  children: {
    resource: [
      {
        id: RESOURCE_ID,
        label: 'eth0',
        name: 'eth0-005056b6b6b6',
        typeLabel: 'SNMP Interface Data',
        rrdGraphAttributes: { ifHCOutOctets: {}, ifHCInOctets: {}}
      },
      {
        id: RESPONSE_TIME_ID,
        label: 'Response Time for 127.0.0.1',
        name: '127.0.0.1',
        typeLabel: 'Response Time',
        rrdGraphAttributes: { icmp: {}}
      }
    ]
  }
}

// Every mounted builder is torn down after its test: the URL sync and the query
// are debounced, so a leaked instance's timer would otherwise fire during a later
// test and call the shared router mock.
const mounted: ReturnType<typeof mount>[] = []

const mountBuilder = (props: Record<string, unknown> = {}) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
  setActivePinia(pinia)
  useMenuStore().mainMenu = { homeUrl: '/opennms' } as never

  const wrapper = mount(AdhocGraphBuilder, {
    props,
    global: {
      plugins: [PrimeVue, pinia],
      stubs: { BreadCrumbs: true, RouterLink: true },
      // AdhocChartToolbar uses v-onms-tooltip; the app registers it in
      // src/theme/primevue-setup.ts, which tests don't run.
      directives: { 'onms-tooltip': OnmsTooltip }
    }
  })

  mounted.push(wrapper)

  return { wrapper, store: useAdhocGraphStore() }
}

/**
 * Poll until `check` holds. The URL writer and the query are debounced behind
 * real promises, so fake timers make tests depend on how many microtask turns
 * the machine happens to need.
 */
const waitFor = async (check: () => boolean, timeoutMs = 3000) => {
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    await flushPromises()
    if (check()) {
      return
    }
    await new Promise(resolve => setTimeout(resolve, 25))
  }
  throw new Error('condition never held')
}

/** The explicit-pick path: one node, one resource, one datasource. */
const pickOne = async (store: ReturnType<typeof useAdhocGraphStore>) => {
  await store.setPickedNodes([{ id: '1', label: 'switch-01' }])
  await flushPromises()
  store.setPickedResources([store.resourceOptions[0]])
  await flushPromises()
  store.setPickedDatasources([store.datasourceOptions[0]])
  await flushPromises()
}

/** The filter path: a rule, a resource pattern, an attribute pattern. */
const filterAll = async (store: ReturnType<typeof useAdhocGraphStore>) => {
  await store.setNodeFilter('catincRouters')
  await flushPromises()
  store.setResourceFilter('interfaceSnmp[eth*]')
  store.setDatasourceFilter('ifHC*Octets')
  await flushPromises()
}

const seriesLabels = (wrapper: ReturnType<typeof mount>) =>
  wrapper.findAll('input[data-test^="series-label-"]').map(field => (field.element as HTMLInputElement).value)

const component = (wrapper: ReturnType<typeof mount>, selector: string) => wrapper.findComponent(selector) as VueWrapper<any>

describe('AdhocGraphBuilder', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    routeQuery = {}
    // appStore reads the saved theme at setup; this happy-dom build does not put
    // localStorage on the global, so give it a minimal stand-in.
    vi.stubGlobal('localStorage', {
      getItem: () => null,
      setItem: () => undefined,
      removeItem: () => undefined
    })
    getNodes.mockResolvedValue({ node: [{ id: '1', label: 'switch-01' }], totalCount: 1, count: 1 })
    getNodesByFilterRule.mockResolvedValue({ nodes: [{ id: '1', label: 'switch-01', foreignSource: 'FS', foreignId: 'sw1' }] })
    getResourceForNode.mockResolvedValue(NODE_RESOURCE)
    getGraphMetrics.mockResolvedValue({ labels: ['x'], columns: [{ values: [1] }], timestamps: [0] })
  })

  afterEach(() => {
    while (mounted.length) {
      mounted.pop()?.unmount()
    }
  })

  it('lists the first page of nodes on mount and renders the three columns, graphing nothing yet', async () => {
    const { wrapper, store } = mountBuilder()
    await flushPromises()

    expect(getNodes).toHaveBeenCalled()
    expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="nodes-count"]').text()).toBe('1 available')
    expect(wrapper.find('[data-test="resources-empty"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="datasources-empty"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="series-empty"]').exists()).toBe(true)
    expect(store.effectiveNodes).toEqual([])
    expect(getGraphMetrics).not.toHaveBeenCalled()
  })

  describe('building a graph from filters', () => {
    it('describes the graph with three filters and no clicking', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()

      await filterAll(store)

      expect(getNodesByFilterRule).toHaveBeenCalledWith('catincRouters', 200)
      expect(wrapper.find('[data-test="nodes-count"]').text()).toBe('all 1 matching')
      expect(wrapper.find('[data-test="resources-count"]').text()).toBe('all 1 matching')
      expect(wrapper.find('[data-test="datasources-count"]').text()).toBe('all 2 matching')
      expect(seriesLabels(wrapper)).toEqual(['switch_01_eth0_ifHCInOctets', 'switch_01_eth0_ifHCOutOctets'])

      await waitFor(() => getGraphMetrics.mock.calls.length > 0)
      const payload = getGraphMetrics.mock.calls[0][0]
      expect(payload.source.map((source: { attribute: string }) => source.attribute)).toEqual(['ifHCInOctets', 'ifHCOutOctets'])
      expect(payload.relaxed).toBe(true)
    })

    it('reaches the server from the node box after the typing settles', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()

      await wrapper.find('input[data-test="nodes-filter"]').setValue('catincRouters')
      expect(getNodesByFilterRule).not.toHaveBeenCalled()
      // Until then the store does not know the new text, so the browse page's
      // nodes cannot masquerade as "all matching" the half-typed rule.
      expect(store.nodeFilter).toBe('')
      expect(wrapper.find('[data-test="nodes-count"]').text()).toBe('1 available')

      await waitFor(() => getNodesByFilterRule.mock.calls.length > 0)
      expect(getNodesByFilterRule).toHaveBeenCalledWith('catincRouters', 200)
    })

    it('settles the resource and datasource boxes too, rather than rebuilding on every keystroke', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await store.setNodeFilter('catincRouters')
      await flushPromises()

      await wrapper.find('input[data-test="resources-filter"]').setValue('interfaceSnmp[*]')
      expect(store.resourceFilter).toBe('')

      await waitFor(() => store.resourceFilter === 'interfaceSnmp[*]')
    })

    it('drops a filter still settling when the user clears, and empties the box', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()

      const input = wrapper.find('input[data-test="nodes-filter"]')
      await input.setValue('catincRouters')
      await wrapper.find('[data-test="toolbar-clear"]').trigger('click')
      await new Promise(resolve => setTimeout(resolve, 700))
      await flushPromises()

      expect(getNodesByFilterRule).not.toHaveBeenCalled()
      expect(store.nodeFilter).toBe('')
      expect((input.element as HTMLInputElement).value).toBe('')
    })

    it('tells the user when selecting turns a filtered column into a fixed list', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await filterAll(store)

      await wrapper.find('[data-test="datasources-select-all"]').trigger('click')
      await flushPromises()

      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ msg: expect.stringMatching(/Datasources are now a fixed selection of 2/) }))
      expect(store.pickedDatasources).toHaveLength(2)

      // Already a fixed list: no second notice for a further change.
      showSnackBar.mockClear()
      store.setPickedDatasources([store.datasourceOptions[0]])
      await flushPromises()
      expect(showSnackBar).not.toHaveBeenCalled()
    })

    // A rule that starts matching a node whose series sorts first must not
    // renumber the series an expression already refers to.
    it('keeps a generated label when a newcomer would otherwise take it', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await filterAll(store)
      expect(seriesLabels(wrapper)).toEqual(['switch_01_eth0_ifHCInOctets', 'switch_01_eth0_ifHCOutOctets'])

      // A second node with the same label, sorting first.
      getNodesByFilterRule.mockResolvedValue({ nodes: [{ id: '0', label: 'switch-01' }, { id: '1', label: 'switch-01' }] })
      getResourceForNode.mockImplementation((id: string) => Promise.resolve({
        ...NODE_RESOURCE,
        id: `node[${id}]`,
        children: { resource: NODE_RESOURCE.children.resource.map(child => ({ ...child, id: child.id.replace('node[1]', `node[${id}]`) })) }
      }))
      await store.setNodeFilter('catincRouters & catincSNMP')
      await flushPromises()

      expect(seriesLabels(wrapper)).toEqual([
        'switch_01_eth0_ifHCInOctets_2',
        'switch_01_eth0_ifHCOutOctets_2',
        'switch_01_eth0_ifHCInOctets',
        'switch_01_eth0_ifHCOutOctets'
      ])
    })

    it('says how many nodes would not give up their resources', async () => {
      getNodesByFilterRule.mockResolvedValue({ nodes: [{ id: '1', label: 'switch-01' }, { id: '2', label: 'switch-02' }] })
      getResourceForNode.mockImplementation((id: string) => (id === '2' ? Promise.reject(new Error('boom')) : Promise.resolve(NODE_RESOURCE)))
      const { wrapper, store } = mountBuilder()
      await flushPromises()

      await store.setNodeFilter('catincRouters')
      await flushPromises()

      expect(wrapper.find('[data-test="resources-note"]').text()).toBe('Resources could not be loaded for 1 node.')
    })

    it('narrows a column to its picks, and picking is still available at every level', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await filterAll(store)

      store.setPickedDatasources([store.datasourceOptions[1]])
      await flushPromises()

      expect(wrapper.find('[data-test="datasources-count"]').text()).toBe('1 of 2 selected')
      expect(seriesLabels(wrapper)).toEqual(['switch_01_eth0_ifHCOutOctets'])
    })

    it('removing a series from a generated set pins the rest', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await filterAll(store)

      component(wrapper, '[data-test="series-grid"]').vm.$emit('remove', KEY_IN)
      await flushPromises()

      expect(store.pickedDatasources.map(datasource => datasource.key)).toEqual([KEY_OUT])
      expect(seriesLabels(wrapper)).toEqual(['switch_01_eth0_ifHCOutOctets'])
      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ msg: expect.stringMatching(/fixed selection of 1/) }))
    })

    it('keeps an edited label when the filters re-evaluate', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await filterAll(store)

      await wrapper.find(`input[data-test="series-label-${KEY_IN}"]`).setValue('renamed_by_hand')
      await flushPromises()

      // A re-evaluation that produces the same series again.
      await store.setNodeFilter('catincRouters & catincSNMP')
      await flushPromises()

      expect(seriesLabels(wrapper)).toContain('renamed_by_hand')
      expect(seriesLabels(wrapper)).toHaveLength(2)
    })

    // The virtual scroller renders no rows under happy-dom, so the identity is
    // checked on the options the list is handed rather than on the text.
    it('carries the node identity the plugin shows, fs:fid, into the list', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await store.setNodeFilter('catincRouters')
      await flushPromises()

      expect(component(wrapper, '[data-test="nodes-list"]').props('options')).toEqual([
        { id: '1', label: 'switch-01', foreignSource: 'FS', foreignId: 'sw1' }
      ])
    })

    it('shows the server objection to a rule it cannot parse', async () => {
      getNodesByFilterRule.mockResolvedValue({ error: 'invalid' })
      const { wrapper, store } = mountBuilder()
      await flushPromises()

      await store.setNodeFilter('catinc(')
      await flushPromises()

      expect(wrapper.find('[data-test="nodes-error"]').text()).toMatch(/could not be parsed/)
      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(false)
    })

    it('keeps selected nodes listed, and the graph intact, when a rule fails', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await pickOne(store)

      getNodesByFilterRule.mockResolvedValue({ error: 'invalid' })
      await store.setNodeFilter('catinc(')
      await flushPromises()

      expect(wrapper.find('[data-test="nodes-error"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(true)
      expect(seriesLabels(wrapper)).toHaveLength(1)
    })

    it('removing the last series empties the graph rather than bringing everything back', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await filterAll(store)
      store.setDatasourceFilter('ifHCInOctets')
      await flushPromises()
      expect(seriesLabels(wrapper)).toHaveLength(1)

      component(wrapper, '[data-test="series-grid"]').vm.$emit('remove', KEY_IN)
      await flushPromises()

      expect(wrapper.find('[data-test="series-empty"]').exists()).toBe(true)
      expect(store.datasourceFilter).toBe('')
    })

    it('stops at the node limit and says so, without fetching anything', async () => {
      getNodesByFilterRule.mockResolvedValue({
        nodes: Array.from({ length: 201 }, (_unused, index) => ({ id: String(index), label: `n${index}` }))
      })
      const { wrapper, store } = mountBuilder()
      await flushPromises()

      await store.setNodeFilter('catincEverything')
      await flushPromises()

      expect(getResourceForNode).not.toHaveBeenCalled()
      expect(wrapper.find('[data-test="chart-error"]').text()).toContain('201 nodes; a graph can be built from at most 200')
      expect(wrapper.find('[data-test="nodes-note"]').text()).toContain('at most 200')
    })

    it('says when a label search is only showing its first page', async () => {
      getNodes.mockResolvedValue({ node: [{ id: '1', label: 'switch-01' }], totalCount: 2310, count: 1 })
      const { wrapper, store } = mountBuilder()
      await flushPromises()

      await store.setNodeFilter('sw')
      await flushPromises()

      expect(wrapper.find('[data-test="nodes-note"]').text()).toContain('first 1 of 2,310')
    })

    it('refuses to graph more series than the cap, and says why', async () => {
      getResourceForNode.mockResolvedValue({
        ...NODE_RESOURCE,
        children: { resource: [{
          id: RESOURCE_ID,
          label: 'eth0',
          name: 'eth0',
          typeLabel: 'SNMP Interface Data',
          rrdGraphAttributes: Object.fromEntries(Array.from({ length: 201 }, (_unused, index) => [`attr${index}`, {}]))
        }] }
      })
      const { wrapper, store } = mountBuilder()
      await flushPromises()

      await store.setNodeFilter('catincRouters')
      await flushPromises()
      store.setResourceFilter('*')
      store.setDatasourceFilter('*')
      await flushPromises()

      expect(wrapper.find('[data-test="chart-error"]').text()).toContain('201 series; the limit is 200')
      expect(wrapper.find('[data-test="series-empty"]').exists()).toBe(true)
      await new Promise(resolve => setTimeout(resolve, 700))
      expect(getGraphMetrics).not.toHaveBeenCalled()
    })
  })

  describe('explicit picks', () => {
    it('walk the node -> resource -> datasource cascade and build a series row', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()

      await store.setPickedNodes([{ id: '1', label: 'switch-01' }])
      await flushPromises()
      expect(wrapper.find('[data-test="resources-list"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="resources-count"]').text()).toBe('2 available')

      store.setPickedResources([store.resourceOptions[0]])
      await flushPromises()
      expect(store.datasourceOptions.map(option => option.attribute)).toEqual(['ifHCInOctets', 'ifHCOutOctets'])

      store.setPickedDatasources([store.datasourceOptions[0]])
      await flushPromises()

      expect(wrapper.find('[data-test="series-grid"]').exists()).toBe(true)
      expect(wrapper.find(`[data-test="series-label-${KEY_IN}"]`).exists()).toBe(true)
    })

    it('keep an edited label when an unrelated datasource is added', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await pickOne(store)

      await wrapper.find(`input[data-test="series-label-${KEY_IN}"]`).setValue('renamed_by_hand')
      await flushPromises()

      // Picking a second datasource must reconcile, not rebuild.
      store.setPickedDatasources([...store.datasourceOptions])
      await flushPromises()

      expect(seriesLabels(wrapper)).toContain('renamed_by_hand')
      expect(seriesLabels(wrapper)).toHaveLength(2)
    })
  })

  describe('links', () => {
    it('restores a legacy static link: series, title and time range', async () => {
      routeQuery = {
        s: `${RESOURCE_ID}~ifHCInOctets~MAX~in_octets~area~#2a78d6~0`,
        start: '1704067200',
        end: '1704070800',
        fmt: 'hours',
        title: 'WAN traffic'
      }

      const { wrapper, store } = mountBuilder()
      await flushPromises()

      expect(store.effectiveDatasources.map(datasource => datasource.key)).toEqual([KEY_IN])

      // All three panes show the picks, with real labels.
      expect(store.pickedNodes).toEqual([{ id: '1', label: 'switch-01' }])
      expect(store.nodeOptions.map(node => node.id)).toContain('1')
      expect(store.pickedResources.map(resource => resource.id)).toEqual([RESOURCE_ID])
      expect(wrapper.find('[data-test="resources-list"]').exists()).toBe(true)

      const titleField = wrapper.find('input[data-test="toolbar-title"]')
      expect((titleField.element as HTMLInputElement).value).toBe('WAN traffic')

      // Restoring a link must query immediately rather than waiting for a manual refresh.
      expect(getGraphMetrics).toHaveBeenCalled()
      const payload = getGraphMetrics.mock.calls[0][0]
      expect(payload.start).toBe(1_704_067_200_000)
      expect(payload.relaxed).toBe(true)
      expect(payload.source).toEqual([{
        aggregation: 'MAX',
        attribute: 'ifHCInOctets',
        label: 'in_octets',
        resourceId: RESOURCE_ID,
        transient: false
      }])
    })

    it('re-evaluates a filter link on open, and says when it now draws something different', async () => {
      routeQuery = { nf: 'catincRouters', rf: 'interfaceSnmp[eth*]', df: 'ifHC*Octets', n: '3', range: 'hours:1' }

      const { wrapper, store } = mountBuilder()
      await flushPromises()

      expect(getNodesByFilterRule).toHaveBeenCalledWith('catincRouters', 200)
      expect(getNodes).not.toHaveBeenCalled()
      expect((wrapper.find('input[data-test="nodes-filter"]').element as HTMLInputElement).value).toBe('catincRouters')
      expect((wrapper.find('input[data-test="resources-filter"]').element as HTMLInputElement).value).toBe('interfaceSnmp[eth*]')
      expect(store.effectiveDatasources).toHaveLength(2)
      expect(seriesLabels(wrapper)).toHaveLength(2)

      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({
        msg: expect.stringMatching(/now matches 2 series; it matched 3/)
      }))
      expect(getGraphMetrics).toHaveBeenCalled()
      expect(getGraphMetrics.mock.calls[0][0].source).toHaveLength(2)
    })

    it('says nothing when a re-evaluated link draws what it did', async () => {
      routeQuery = { nf: 'catincRouters', rf: 'interfaceSnmp[eth*]', df: 'ifHC*Octets', n: '2', range: 'hours:1' }

      mountBuilder()
      await flushPromises()

      expect(showSnackBar).not.toHaveBeenCalled()
    })

    it('does not blame a fetch failure on inventory', async () => {
      routeQuery = { nf: 'catincRouters', rf: 'interfaceSnmp[eth*]', df: 'ifHC*Octets', n: '2', range: 'hours:1' }
      getResourceForNode.mockRejectedValue(new Error('boom'))

      mountBuilder()
      await flushPromises()

      expect(showSnackBar).not.toHaveBeenCalledWith(expect.objectContaining({ msg: expect.stringMatching(/now matches/) }))
    })

    it('carries a generated label an expression depends on, so the reopened graph keeps it', async () => {
      copyToClipboard.mockResolvedValue(undefined)
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await filterAll(store)

      await wrapper.find('[data-test="expressions-panel"] button').trigger('click')
      const expressions = component(wrapper, '[data-test="expressions-panel"]')
      expressions.findComponent({ name: 'ExpressionEditor' }).vm.$emit('add')
      await flushPromises()
      expressions.findComponent({ name: 'ExpressionEditor' }).vm.$emit('update', 'expr-1', { value: 'switch_01_eth0_ifHCInOctets * 8' })
      await flushPromises()

      await wrapper.find('[data-test="toolbar-share"]').trigger('click')
      await flushPromises()

      const params = new URLSearchParams((copyToClipboard.mock.calls[0][0] as string).split('?')[1])
      expect(params.getAll('o')).toEqual([`${RESOURCE_ID}~ifHCInOctets~~switch_01_eth0_ifHCInOctets~~~`])
    })

    // The series are generated against stand-ins first, then against real data.
    // The first guess at a label must not stick, or an expression written against
    // the real label breaks on every reopen.
    it('labels a restored series from its real node and resource, not its stand-in', async () => {
      routeQuery = { pn: '1', pr: RESOURCE_ID, pd: KEY_IN, e: 'bits~switch_01_eth0_ifHCInOctets * 8~line~#eb6834', range: 'hours:1' }

      const { wrapper } = mountBuilder()
      await flushPromises()

      expect(seriesLabels(wrapper)).toEqual(['switch_01_eth0_ifHCInOctets'])
      expect(getGraphMetrics).toHaveBeenCalled()
      expect(getGraphMetrics.mock.calls[0][0].expression).toEqual([
        expect.objectContaining({ label: 'bits', value: 'switch_01_eth0_ifHCInOctets * 8' })
      ])
    })

    it('re-applies the edits a link carries to the series it now produces', async () => {
      routeQuery = {
        nf: 'catincRouters',
        rf: 'interfaceSnmp[eth*]',
        df: 'ifHC*Octets',
        o: `${RESOURCE_ID}~ifHCInOctets~MAX~in_bits~~#2a78d6~`,
        range: 'hours:1'
      }

      const { wrapper } = mountBuilder()
      await flushPromises()

      expect(seriesLabels(wrapper)).toEqual(['in_bits', 'switch_01_eth0_ifHCOutOctets'])
      const payload = getGraphMetrics.mock.calls[0][0]
      expect(payload.source[0]).toEqual(expect.objectContaining({ aggregation: 'MAX', label: 'in_bits' }))
      expect(payload.source[1]).toEqual(expect.objectContaining({ aggregation: 'AVERAGE' }))
    })

    it('does not rewrite the address bar while hydrating a link', async () => {
      routeQuery = { s: `${RESOURCE_ID}~ifHCInOctets~AVERAGE~in_octets~line~#2a78d6~0`, start: '1704067200', end: '1704070800' }

      mountBuilder()
      await flushPromises()

      expect(routerReplace).not.toHaveBeenCalled()
    })
  })

  describe('the toolbar', () => {
    // MenuHeaderIT locates this page by //div[@id='app']//h2[text()='Custom
    // Performance Graphs']; if the tag or the text changes, that smoke test breaks
    // in CI rather than here, so pin it.
    it('renders the page title as an h2 with the exact text the smoke test matches', async () => {
      const { wrapper } = mountBuilder()
      await flushPromises()

      const headings = wrapper.findAll('h2').map(h => h.text())
      expect(headings).toContain('Custom Performance Graphs')
      expect(wrapper.find('.header .heading h2').exists()).toBe(true)
    })

    it('labels the time range control, which otherwise only shows its value', async () => {
      const { wrapper } = mountBuilder()
      await flushPromises()

      expect(wrapper.text()).toContain('Time Range:')
      expect(wrapper.find('[data-test="toolbar-time-range"]').exists()).toBe(true)
    })

    // OnmsIconButton has no loading state, so an in-flight query disables Refresh
    // rather than spinning it; the chart shows the spinner.
    it('disables Refresh while a query is in flight', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()

      const refreshDisabled = () =>
        wrapper.find('[data-test="toolbar-refresh"]').attributes('disabled') !== undefined

      store.queryLoading = true
      await flushPromises()
      expect(refreshDisabled()).toBe(true)

      store.queryLoading = false
      await flushPromises()
      expect(refreshDisabled()).toBe(true) // still disabled: no series yet
    })
  })

  describe('outgrowing the URL', () => {
    /**
     * Overflow MAX_QUERY_LENGTH (6000) with as few picks as possible: a handful of
     * very long resource ids rather than a hundred short ones, so the reconcile and
     * encode work stays small enough not to time out under full-suite load.
     */
    const manyPicks = (store: ReturnType<typeof useAdhocGraphStore>, count: number) =>
      store.setPickedDatasources(Array.from({ length: count }, (_unused, index) => {
        const resourceId = `node[${index}].interfaceSnmp[${'Gigabit0-0-'.repeat(30)}${index}]`
        return {
          key: `${resourceId}|ifHCInOctets`,
          resourceId,
          resourceLabel: `interface-${index}`,
          nodeId: String(index),
          nodeLabel: `switch-${index}`,
          attribute: 'ifHCInOctets'
        }
      }))

    const warnings = () => showSnackBar.mock.calls
      .filter(call => String((call[0] as { msg?: string })?.msg ?? '').includes('too many selections'))

    it('clears the query and says so once the graph will not fit', async () => {
      routeQuery = { title: 'x' }
      const { store } = mountBuilder()
      await flushPromises()

      manyPicks(store, 20)
      await waitFor(() => warnings().length > 0)

      expect(routerReplace).toHaveBeenCalledWith({ query: {}})
    }, 10000)

    it('warns once, not on every edit', async () => {
      const { store } = mountBuilder()
      await flushPromises()

      manyPicks(store, 20)
      await waitFor(() => warnings().length > 0)

      manyPicks(store, 22)
      await new Promise(resolve => setTimeout(resolve, 600))
      await flushPromises()

      expect(warnings()).toHaveLength(1)
    }, 20000)
  })

  describe('the copy-link button', () => {
    it('copies an absolute URL carrying the filters and picks', async () => {
      copyToClipboard.mockResolvedValue(undefined)
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await filterAll(store)
      store.setPickedDatasources([store.datasourceOptions[0]])
      await flushPromises()

      await wrapper.find('[data-test="toolbar-share"]').trigger('click')
      await flushPromises()

      expect(copyToClipboard).toHaveBeenCalledTimes(1)
      const copied = copyToClipboard.mock.calls[0][0] as string
      expect(copied.startsWith(window.location.origin)).toBe(true)
      expect(copied).toContain('#/adhoc-graphs?')

      const params = new URLSearchParams(copied.split('?')[1])
      expect(params.get('nf')).toBe('catincRouters')
      expect(params.get('rf')).toBe('interfaceSnmp[eth*]')
      expect(params.get('df')).toBe('ifHC*Octets')
      expect(params.getAll('pd')).toEqual([KEY_IN])
      expect(params.get('n')).toBe('1')
      expect(params.get('s')).toBeNull()
    })

    // The address bar is written by a debounced watcher, so reading
    // window.location.href would hand out the previous state's link.
    it('reflects an edit made moments earlier, before the URL sync has run', async () => {
      copyToClipboard.mockResolvedValue(undefined)
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await pickOne(store)

      await wrapper.find(`input[data-test="series-label-${KEY_IN}"]`).setValue('renamed_just_now')
      await wrapper.find('[data-test="toolbar-share"]').trigger('click')
      await flushPromises()

      expect(routerReplace).not.toHaveBeenCalled()
      expect(decodeURIComponent(copyToClipboard.mock.calls[0][0] as string)).toContain('renamed_just_now')
    })

    it('warns instead of copying when the browser refuses', async () => {
      copyToClipboard.mockRejectedValue(new Error('denied'))
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await pickOne(store)

      await wrapper.find('[data-test="toolbar-share"]').trigger('click')
      await flushPromises()

      expect(copyToClipboard).toHaveBeenCalled()
    })
  })

  describe('relative time ranges', () => {
    const windowOf = (call: number) => {
      const payload = getGraphMetrics.mock.calls[call][0]
      return { start: payload.start, end: payload.end, span: payload.end - payload.start }
    }

    it('defaults to a relative window rather than a frozen one', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await pickOne(store)

      await wrapper.find('[data-test="toolbar-refresh"]').trigger('click')
      await flushPromises()

      expect(getGraphMetrics).toHaveBeenCalled()
      // 24h default.
      expect(windowOf(getGraphMetrics.mock.calls.length - 1).span).toBe(24 * 3600 * 1000)
    })

    // The reported bug: a bookmark held the instants current when it was made.
    it('resolves a bookmarked range against the clock now, not when it was saved', async () => {
      routeQuery = {
        s: `${RESOURCE_ID}~ifHCInOctets~AVERAGE~in_octets~line~#2a78d6~0`,
        range: 'hours:2'
      }

      const before = Date.now()
      mountBuilder()
      await flushPromises()

      const { start, end, span } = windowOf(0)
      expect(span).toBe(2 * 3600 * 1000)
      // The window ends about now — not at some timestamp baked into the link.
      expect(end).toBeGreaterThanOrEqual(before - 5000)
      expect(start).toBe(end - span)
    })

    it('still honors an absolute range from a custom-time link', async () => {
      routeQuery = {
        s: `${RESOURCE_ID}~ifHCInOctets~AVERAGE~in_octets~line~#2a78d6~0`,
        start: '1704067200',
        end: '1704070800'
      }

      mountBuilder()
      await flushPromises()

      expect(windowOf(0).start).toBe(1_704_067_200_000)
      expect(windowOf(0).end).toBe(1_704_070_800_000)
    })

    // Also broken before: Refresh re-sent the window captured at selection time.
    it('slides the window forward on Refresh', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await pickOne(store)
      await wrapper.find('[data-test="toolbar-refresh"]').trigger('click')
      await flushPromises()

      const first = windowOf(getGraphMetrics.mock.calls.length - 1)

      vi.setSystemTime(new Date(Date.now() + 3_600_000))
      await wrapper.find('[data-test="toolbar-refresh"]').trigger('click')
      await flushPromises()

      const second = windowOf(getGraphMetrics.mock.calls.length - 1)
      expect(second.end - first.end).toBeGreaterThanOrEqual(3_500_000)
      expect(second.span).toBe(first.span)
      vi.useRealTimers()
    })

    it('does not slide an absolute window on Refresh', async () => {
      routeQuery = {
        s: `${RESOURCE_ID}~ifHCInOctets~AVERAGE~in_octets~line~#2a78d6~0`,
        start: '1704067200',
        end: '1704070800'
      }

      const { wrapper } = mountBuilder()
      await flushPromises()

      vi.setSystemTime(new Date(Date.now() + 3_600_000))
      await wrapper.find('[data-test="toolbar-refresh"]').trigger('click')
      await flushPromises()

      const last = windowOf(getGraphMetrics.mock.calls.length - 1)
      expect(last.start).toBe(1_704_067_200_000)
      expect(last.end).toBe(1_704_070_800_000)
      vi.useRealTimers()
    })

    it('shares a relative link as a range, not as instants', async () => {
      copyToClipboard.mockResolvedValue(undefined)
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await pickOne(store)

      await wrapper.find('[data-test="toolbar-share"]').trigger('click')
      await flushPromises()

      const url = decodeURIComponent(copyToClipboard.mock.calls[0][0] as string)
      expect(url).toContain('range=hours:24')
      expect(url).not.toContain('start=')
    })
  })

  describe('the Series and Expressions panels', () => {
    const panels = (wrapper: ReturnType<typeof mount>) =>
      Object.fromEntries(wrapper.findAllComponents({ name: 'OnmsPanel' })
        .map(panel => [String(panel.props('header')).split(' (')[0], panel]))

    it('opens Series and closes Expressions on a fresh page', async () => {
      const { wrapper } = mountBuilder()
      await flushPromises()

      const found = panels(wrapper)
      expect(found.Series.props('toggleable')).toBe(true)
      expect(found.Series.props('collapsed')).toBe(false)
      expect(found.Expressions.props('collapsed')).toBe(true)
    })

    it('counts what each panel holds, so a closed one still says something', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await filterAll(store)

      expect(panels(wrapper).Series.props('header')).toBe('Series (2)')
      expect(panels(wrapper).Expressions.props('header')).toBe('Expressions (0)')
    })

    // A link carrying expressions should show them; hidden, the graph would look
    // like it came from its sources alone.
    it('opens Expressions when a restored link defines some', async () => {
      routeQuery = {
        s: `${RESOURCE_ID}~ifHCInOctets~AVERAGE~in_octets~line~#2a78d6~1`,
        e: 'bits~in_octets * 8~line~#eb6834',
        start: '1704067200',
        end: '1704070800'
      }

      const { wrapper } = mountBuilder()
      await flushPromises()

      const found = panels(wrapper)
      expect(found.Expressions.props('collapsed')).toBe(false)
      expect(found.Expressions.props('header')).toBe('Expressions (1)')
    })

    it('leaves Expressions closed when a restored link has none', async () => {
      routeQuery = {
        s: `${RESOURCE_ID}~ifHCInOctets~AVERAGE~in_octets~line~#2a78d6~0`,
        start: '1704067200',
        end: '1704070800'
      }

      const { wrapper } = mountBuilder()
      await flushPromises()

      expect(panels(wrapper).Expressions.props('collapsed')).toBe(true)
    })

    it('does not reopen a panel the user closed', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()

      panels(wrapper).Series.vm.$emit('update:collapsed', true)
      await flushPromises()
      expect(panels(wrapper).Series.props('collapsed')).toBe(true)

      // Filling the panel must not force it back open.
      await filterAll(store)

      expect(panels(wrapper).Series.props('collapsed')).toBe(true)
    })
  })

  describe('expanding and popping out', () => {
    it('hides the pickers and editors while expanded, and restores them', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await pickOne(store)

      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="series-grid"]').exists()).toBe(true)

      await wrapper.find('[data-test="toolbar-expand"]').trigger('click')
      await flushPromises()

      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(false)
      expect(wrapper.find('[data-test="series-grid"]').exists()).toBe(false)
      // The chart itself stays, and is told to fill the space.
      expect(wrapper.findComponent({ name: 'AdhocChart' }).props('expanded')).toBe(true)

      // The editing fields go too — expanded means "show me the graph", and they
      // cost two rows of the height the plot needs.
      expect(wrapper.find('[data-test="toolbar-title"]').exists()).toBe(false)
      expect(wrapper.find('[data-test="toolbar-resolution"]').exists()).toBe(false)

      // The root drives the flex layout that keeps everything on screen.
      expect(wrapper.find('.adhoc-builder').classes()).toContain('is-expanded')

      await wrapper.find('[data-test="toolbar-expand"]').trigger('click')
      await flushPromises()

      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="toolbar-title"]').exists()).toBe(true)
      expect(wrapper.find('.adhoc-builder').classes()).not.toContain('is-expanded')
    })

    it('leaves the expanded view on Escape', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await pickOne(store)

      await wrapper.find('[data-test="toolbar-expand"]').trigger('click')
      await flushPromises()
      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(false)

      window.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
      await flushPromises()

      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(true)
    })

    it('opens the view route in a new tab, carrying the current filters', async () => {
      const open = vi.fn().mockReturnValue({})
      vi.stubGlobal('open', open)

      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await filterAll(store)

      await wrapper.find('[data-test="toolbar-popout"]').trigger('click')
      await flushPromises()

      expect(open).toHaveBeenCalledTimes(1)
      const [url, target, features] = open.mock.calls[0]
      expect(url).toContain('#/adhoc-graphs/view?')
      expect(decodeURIComponent(url as string)).toContain('nf=catincRouters')
      expect(target).toBe('_blank')
      expect(features).toBe('noopener')
    })

    it('warns when the browser blocks the pop-out', async () => {
      vi.stubGlobal('open', vi.fn().mockReturnValue(null))

      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await pickOne(store)

      await wrapper.find('[data-test="toolbar-popout"]').trigger('click')
      await flushPromises()

      // No throw, and the builder is still usable.
      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(true)
    })
  })

  describe('the graph-only view route', () => {
    it('renders the chart from the URL with no pickers, editors or edit controls', async () => {
      routeQuery = {
        nf: 'catincRouters',
        rf: 'interfaceSnmp[eth*]',
        df: 'ifHCInOctets',
        start: '1704067200',
        end: '1704070800',
        title: 'WAN traffic'
      }

      const { wrapper } = mountBuilder({ viewOnly: true })
      await flushPromises()

      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(false)
      expect(wrapper.find('[data-test="series-grid"]').exists()).toBe(false)
      expect(wrapper.find('[data-test="toolbar-title"]').exists()).toBe(false)
      expect(wrapper.find('[data-test="toolbar-clear"]').exists()).toBe(false)
      expect(wrapper.find('[data-test="toolbar-expand"]').exists()).toBe(false)
      expect(wrapper.find('[data-test="toolbar-popout"]').exists()).toBe(false)

      // Still a graph, still refreshable and exportable, and titled.
      expect(wrapper.findComponent({ name: 'AdhocChart' }).props('expanded')).toBe(true)
      expect(wrapper.find('[data-test="toolbar-refresh"]').exists()).toBe(true)
      // The page heading names the page; the graph's own title is drawn on the plot.
      expect(wrapper.text()).toContain('Custom Performance Graphs')
      expect(wrapper.findComponent({ name: 'AdhocChart' }).props('config'))
        .toEqual(expect.objectContaining({ title: 'WAN traffic' }))
      expect(getGraphMetrics).toHaveBeenCalled()
    })

    it('offers a way back to the builder', async () => {
      routeQuery = { s: `${RESOURCE_ID}~ifHCInOctets~AVERAGE~in_octets~line~#2a78d6~0`, start: '1704067200', end: '1704070800' }

      const { wrapper } = mountBuilder({ viewOnly: true })
      await flushPromises()

      expect(wrapper.find('[data-test="adhoc-open-builder"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="adhoc-info-icon"]').exists()).toBe(false)
    })
  })

  it('clears every filter, pick and the config on Clear all', async () => {
    const { wrapper, store } = mountBuilder()
    await flushPromises()
    await filterAll(store)
    await wrapper.find('input[data-test="toolbar-title"]').setValue('WAN')

    await wrapper.find('[data-test="toolbar-clear"]').trigger('click')
    await flushPromises()

    expect(store.nodeFilter).toBe('')
    expect(store.resourceFilter).toBe('')
    expect(store.effectiveDatasources).toEqual([])
    // Back to the browse page, not stale matches under an empty box.
    expect(getNodes).toHaveBeenCalledTimes(2)
    expect(store.nodeOptions).toEqual([{ id: '1', label: 'switch-01' }])
    expect((wrapper.find('input[data-test="nodes-filter"]').element as HTMLInputElement).value).toBe('')
    expect((wrapper.find('input[data-test="toolbar-title"]').element as HTMLInputElement).value).toBe('')
    expect(wrapper.find('[data-test="series-empty"]').exists()).toBe(true)
  })
})
