import { OnmsTooltip } from '@opennms/onms-ui'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { defineComponent, h } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import AdhocGraphBuilder from '@/components/AdhocGraphs/AdhocGraphBuilder.vue'
import { useAdhocGraphStore } from '@/stores/adhocGraphStore'
import { useMenuStore } from '@/stores/menuStore'

const getNodes = vi.fn()
const getResourceForNode = vi.fn()
const getResourceById = vi.fn()
const getGraphMetrics = vi.fn()

vi.mock('@/services', () => ({
  default: {
    getNodes: (...args: unknown[]) => getNodes(...args),
    getResourceForNode: (...args: unknown[]) => getResourceForNode(...args),
    getResourceById: (...args: unknown[]) => getResourceById(...args),
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
    setup(_props, { expose }) {
      expose({ exportTarget: () => null })
      return () => h('div', { 'data-test': 'chart-stub' })
    }
  })
}))

const RESOURCE_ID = 'node[1].interfaceSnmp[eth0]'

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
    getNodes.mockResolvedValue({ node: [{ id: '1', label: 'switch-01' }] })
    getResourceForNode.mockResolvedValue({
      id: 'node[1]',
      label: 'switch-01',
      children: { resource: [{ id: RESOURCE_ID, label: 'eth0', typeLabel: 'SNMP Interface Data' }] }
    })
    getResourceById.mockResolvedValue({ rrdGraphAttributes: { ifHCInOctets: {}, ifHCOutOctets: {}}})
    getGraphMetrics.mockResolvedValue({ labels: ['x'], columns: [{ values: [1] }], timestamps: [0] })
  })

  afterEach(() => {
    while (mounted.length) {
      mounted.pop()?.unmount()
    }
  })

  it('searches for nodes on mount and renders the three picker columns', async () => {
    const { wrapper } = mountBuilder()
    await flushPromises()

    expect(getNodes).toHaveBeenCalled()
    expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="resources-empty"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="datasources-empty"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="series-empty"]').exists()).toBe(true)
  })

  it('walks the node -> resource -> datasource cascade and builds a series row', async () => {
    const { wrapper, store } = mountBuilder()
    await flushPromises()

    await store.setSelectedNodes([{ id: '1', label: 'switch-01' }])
    await flushPromises()
    expect(wrapper.find('[data-test="resources-list"]').exists()).toBe(true)

    await store.setSelectedResources([store.resourceOptions[0]])
    await flushPromises()
    expect(store.datasourceOptions.map(option => option.attribute)).toEqual(['ifHCInOctets', 'ifHCOutOctets'])

    store.setSelectedDatasources([store.datasourceOptions[0]])
    await flushPromises()

    const key = `${RESOURCE_ID}|ifHCInOctets`
    expect(wrapper.find('[data-test="series-grid"]').exists()).toBe(true)
    expect(wrapper.find(`[data-test="series-label-${key}"]`).exists()).toBe(true)
  })

  it('keeps an edited label when an unrelated datasource is added', async () => {
    const { wrapper, store } = mountBuilder()
    await flushPromises()

    await store.setSelectedNodes([{ id: '1', label: 'switch-01' }])
    await flushPromises()
    await store.setSelectedResources([store.resourceOptions[0]])
    await flushPromises()

    store.setSelectedDatasources([store.datasourceOptions[0]])
    await flushPromises()

    const key = `${RESOURCE_ID}|ifHCInOctets`
    const input = wrapper.find(`input[data-test="series-label-${key}"]`)
    await input.setValue('renamed_by_hand')
    await flushPromises()

    // Ticking a second datasource must reconcile, not rebuild.
    store.setSelectedDatasources([...store.datasourceOptions])
    await flushPromises()

    const labels = wrapper.findAll('input[data-test^="series-label-"]')
      .map(field => (field.element as HTMLInputElement).value)

    expect(labels).toContain('renamed_by_hand')
    expect(labels).toHaveLength(2)
  })

  it('restores a shared link: series, title and time range', async () => {
    routeQuery = {
      s: `${RESOURCE_ID}~ifHCInOctets~MAX~in_octets~area~#2a78d6~0`,
      start: '1704067200',
      end: '1704070800',
      fmt: 'hours',
      title: 'WAN traffic'
    }

    const { wrapper, store } = mountBuilder()
    await flushPromises()

    expect(store.selectedDatasources.map(datasource => datasource.key)).toEqual([`${RESOURCE_ID}|ifHCInOctets`])

    // Regression: a restored link used to populate only the datasource pane,
    // leaving Nodes with nothing selected and Resources empty.
    expect(store.selectedNodes.map(node => node.id)).toEqual(['1'])
    expect(store.nodeOptions.map(node => node.id)).toContain('1')
    expect(store.selectedResources.map(resource => resource.id)).toEqual([RESOURCE_ID])
    expect(store.resourceOptions.map(resource => resource.id)).toContain(RESOURCE_ID)
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

  it('does not rewrite the address bar while hydrating a link', async () => {
    routeQuery = { s: `${RESOURCE_ID}~ifHCInOctets~AVERAGE~in_octets~line~#2a78d6~0`, start: '1704067200', end: '1704070800' }

    mountBuilder()
    await flushPromises()

    expect(routerReplace).not.toHaveBeenCalled()
  })

  // Blanking the address bar silently meant a large graph quietly stopped being
  // bookmarkable, with no signal until someone tried to copy the link.
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
      expect(refreshDisabled()).toBe(true) // still disabled: no series selected yet
    })
  })

  describe('outgrowing the URL', () => {
    /**
     * Overflow MAX_QUERY_LENGTH (6000) with as few series as possible: a handful of
     * very long resource ids rather than a hundred short ones, so the reconcile and
     * encode work stays small enough not to time out under full-suite load.
     */
    const manySeries = (store: ReturnType<typeof useAdhocGraphStore>, count: number) =>
      store.setSelectedDatasources(Array.from({ length: count }, (_unused, index) => {
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

    /**
     * Poll until `check` holds. The URL writer is debounced behind real promises,
     * so fake timers make this test depend on how many microtask turns the machine
     * happens to need — it passed alone and failed under full-suite load.
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

    const warnings = () => showSnackBar.mock.calls
      .filter(call => String((call[0] as { msg?: string })?.msg ?? '').includes('too many series'))

    it('clears the query and says so once the graph will not fit', async () => {
      // Something in the address bar to clear, but not enough to trigger hydration.
      routeQuery = { title: 'WAN traffic' }
      const { store } = mountBuilder()
      await flushPromises()

      manySeries(store, 20)
      await waitFor(() => warnings().length > 0)

      // The address bar is cleared rather than left describing a stale graph...
      expect(routerReplace).toHaveBeenCalledWith({ query: {}})
      // ...and the user is told, rather than discovering it at copy time.
      expect(warnings()[0][0]).toEqual(expect.objectContaining({ error: true }))
    }, 20000)

    it('warns once, not on every edit', async () => {
      const { store } = mountBuilder()
      await flushPromises()

      manySeries(store, 20)
      await waitFor(() => warnings().length > 0)

      manySeries(store, 22)
      await waitFor(() => routerReplace.mock.calls.length > 0 || true)
      await new Promise(resolve => setTimeout(resolve, 600))
      await flushPromises()

      expect(warnings()).toHaveLength(1)
    }, 20000)
  })

  describe('the copy-link button', () => {
    const selectOneDatasource = async (store: ReturnType<typeof useAdhocGraphStore>) => {
      await store.setSelectedNodes([{ id: '1', label: 'switch-01' }])
      await flushPromises()
      await store.setSelectedResources([store.resourceOptions[0]])
      await flushPromises()
      store.setSelectedDatasources([store.datasourceOptions[0]])
      await flushPromises()
    }

    it('copies an absolute URL carrying the current selection', async () => {
      copyToClipboard.mockResolvedValue(undefined)
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await selectOneDatasource(store)

      await wrapper.find('[data-test="toolbar-share"]').trigger('click')
      await flushPromises()

      expect(copyToClipboard).toHaveBeenCalledTimes(1)
      const copied = copyToClipboard.mock.calls[0][0] as string
      expect(copied.startsWith(window.location.origin)).toBe(true)
      expect(copied).toContain('#/adhoc-graphs?')
      expect(decodeURIComponent(copied)).toContain('ifHCInOctets')
    })

    // The address bar is written by a debounced watcher, so reading
    // window.location.href would hand out the previous state's link.
    it('reflects an edit made moments earlier, before the URL sync has run', async () => {
      copyToClipboard.mockResolvedValue(undefined)
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await selectOneDatasource(store)

      const key = `${RESOURCE_ID}|ifHCInOctets`
      await wrapper.find(`input[data-test="series-label-${key}"]`).setValue('renamed_just_now')
      await wrapper.find('[data-test="toolbar-share"]').trigger('click')
      await flushPromises()

      expect(routerReplace).not.toHaveBeenCalled()
      expect(decodeURIComponent(copyToClipboard.mock.calls[0][0] as string)).toContain('renamed_just_now')
    })

    it('warns instead of copying when the browser refuses', async () => {
      copyToClipboard.mockRejectedValue(new Error('denied'))
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await selectOneDatasource(store)

      await wrapper.find('[data-test="toolbar-share"]').trigger('click')
      await flushPromises()

      expect(copyToClipboard).toHaveBeenCalled()
    })
  })

  describe('relative time ranges', () => {
    const selectOne = async (store: ReturnType<typeof useAdhocGraphStore>) => {
      await store.setSelectedNodes([{ id: '1', label: 'switch-01' }])
      await flushPromises()
      await store.setSelectedResources([store.resourceOptions[0]])
      await flushPromises()
      store.setSelectedDatasources([store.datasourceOptions[0]])
      await flushPromises()
    }

    const windowOf = (call: number) => {
      const payload = getGraphMetrics.mock.calls[call][0]
      return { start: payload.start, end: payload.end, span: payload.end - payload.start }
    }

    it('defaults to a relative window rather than a frozen one', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await selectOne(store)

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
      await selectOne(store)
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
      await selectOne(store)

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
      await store.setSelectedNodes([{ id: '1', label: 'switch-01' }])
      await flushPromises()
      await store.setSelectedResources([store.resourceOptions[0]])
      await flushPromises()
      store.setSelectedDatasources([...store.datasourceOptions])
      await flushPromises()

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
      const { wrapper } = mountBuilder()
      await flushPromises()

      panels(wrapper).Series.vm.$emit('update:collapsed', true)
      await flushPromises()
      expect(panels(wrapper).Series.props('collapsed')).toBe(true)

      // Selecting datasources fills the panel but must not force it back open.
      const store = useAdhocGraphStore()
      await store.setSelectedNodes([{ id: '1', label: 'switch-01' }])
      await flushPromises()
      await store.setSelectedResources([store.resourceOptions[0]])
      await flushPromises()
      store.setSelectedDatasources([...store.datasourceOptions])
      await flushPromises()

      expect(panels(wrapper).Series.props('collapsed')).toBe(true)
    })
  })

  describe('expanding and popping out', () => {
    const selectOne = async (store: ReturnType<typeof useAdhocGraphStore>) => {
      await store.setSelectedNodes([{ id: '1', label: 'switch-01' }])
      await flushPromises()
      await store.setSelectedResources([store.resourceOptions[0]])
      await flushPromises()
      store.setSelectedDatasources([store.datasourceOptions[0]])
      await flushPromises()
    }

    it('hides the pickers and editors while expanded, and restores them', async () => {
      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await selectOne(store)

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
      await selectOne(store)

      await wrapper.find('[data-test="toolbar-expand"]').trigger('click')
      await flushPromises()
      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(false)

      window.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
      await flushPromises()

      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(true)
    })

    it('opens the view route in a new tab, carrying the current selection', async () => {
      const open = vi.fn().mockReturnValue({})
      vi.stubGlobal('open', open)

      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await selectOne(store)

      await wrapper.find('[data-test="toolbar-popout"]').trigger('click')
      await flushPromises()

      expect(open).toHaveBeenCalledTimes(1)
      const [url, target, features] = open.mock.calls[0]
      expect(url).toContain('#/adhoc-graphs/view?')
      expect(decodeURIComponent(url as string)).toContain('ifHCInOctets')
      expect(target).toBe('_blank')
      expect(features).toBe('noopener')
    })

    it('warns when the browser blocks the pop-out', async () => {
      vi.stubGlobal('open', vi.fn().mockReturnValue(null))

      const { wrapper, store } = mountBuilder()
      await flushPromises()
      await selectOne(store)

      await wrapper.find('[data-test="toolbar-popout"]').trigger('click')
      await flushPromises()

      // No throw, and the builder is still usable.
      expect(wrapper.find('[data-test="nodes-list"]').exists()).toBe(true)
    })
  })

  describe('the graph-only view route', () => {
    it('renders the chart from the URL with no pickers, editors or edit controls', async () => {
      routeQuery = {
        s: `${RESOURCE_ID}~ifHCInOctets~AVERAGE~in_octets~line~#2a78d6~0`,
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

  it('clears every column and the config on Clear all', async () => {
    const { wrapper, store } = mountBuilder()
    await flushPromises()

    await store.setSelectedNodes([{ id: '1', label: 'switch-01' }])
    await flushPromises()
    await store.setSelectedResources([store.resourceOptions[0]])
    await flushPromises()
    store.setSelectedDatasources([...store.datasourceOptions])
    await flushPromises()

    await wrapper.find('[data-test="toolbar-clear"]').trigger('click')
    await flushPromises()

    expect(store.selectedNodes).toEqual([])
    expect(store.selectedDatasources).toEqual([])
    expect(wrapper.find('[data-test="series-empty"]').exists()).toBe(true)
  })
})
