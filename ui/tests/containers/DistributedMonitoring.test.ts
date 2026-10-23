import DistributedMonitoring from '@/containers/DistributedMonitoring.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createRouter, createWebHashHistory } from 'vue-router'

const { showToast } = vi.hoisted(() => ({ showToast: vi.fn() }))
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  useOnmsToast: () => ({ showToast })
}))

// child tables are stubbed to their emit surface; the searchFor hand-off is a spy
const searchFor = vi.fn()
const MinionsTableStub = {
  name: 'MinionsTable',
  props: ['locationFilter', 'now'],
  emits: ['update:locationFilter', 'showLocation', 'dialogOpen'],
  template: '<div data-test="minions-table-stub">{{ locationFilter }}</div>'
}
const LocationsTableStub = {
  name: 'LocationsTable',
  emits: ['showMinions', 'dialogOpen'],
  setup: (_props: unknown, { expose }: { expose: (api: Record<string, unknown>) => void }) => {
    expose({ searchFor })
  },
  template: '<div data-test="locations-table-stub" />'
}

const mounted: VueWrapper<any>[] = []

// document.hidden is a prototype getter; override it per test and restore afterwards
let hidden = false
const setHidden = (value: boolean) => {
  hidden = value
}

const mountPage = async (opts: { tab?: string, minionsOk?: boolean, locationsOk?: boolean } = {}) => {
  const router = createRouter({
    history: createWebHashHistory(),
    routes: [{ path: '/distributed-monitoring', name: 'Manage Minions and Locations', component: DistributedMonitoring }]
  })
  await router.push({ path: '/distributed-monitoring', query: opts.tab ? { tab: opts.tab } : {}})
  await router.isReady()

  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const minionStore = useMinionAdminStore(pinia)
  const locationStore = useMonitoringLocationAdminStore(pinia)
  vi.mocked(minionStore.getMinions).mockResolvedValue(opts.minionsOk ?? true)
  vi.mocked(locationStore.getLocations).mockResolvedValue(opts.locationsOk ?? true)
  vi.mocked(locationStore.getNodeCounts).mockResolvedValue(undefined)
  vi.mocked(minionStore.getCoreVersion).mockResolvedValue('34.0.0')

  const wrapper = mount(DistributedMonitoring, {
    global: {
      plugins: [PrimeVue, pinia, router],
      stubs: { MinionsTable: MinionsTableStub, LocationsTable: LocationsTableStub, BreadCrumbs: true }
    }
  })
  mounted.push(wrapper)
  await flushPromises()
  return { wrapper, router, minionStore, locationStore }
}

describe('DistributedMonitoring.vue (container)', () => {
  beforeEach(() => {
    showToast.mockClear()
    searchFor.mockClear()
    hidden = false
    Object.defineProperty(document, 'hidden', { configurable: true, get: () => hidden })
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout', 'setInterval', 'clearInterval', 'Date'] })
    vi.setSystemTime(new Date('2026-09-24T12:00:00Z'))
  })
  afterEach(() => {
    mounted.splice(0).forEach(w => w.unmount())
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
    delete (document as any).hidden
  })

  it('loads both stores and the core version on mount and renders the header', async () => {
    const { wrapper, minionStore, locationStore } = await mountPage()
    expect(minionStore.getMinions).toHaveBeenCalledTimes(1)
    expect(locationStore.getLocations).toHaveBeenCalledTimes(1)
    expect(minionStore.getCoreVersion).toHaveBeenCalledTimes(1)
    expect(wrapper.find('.page-title').text()).toBe('Manage Minions and Locations')
    expect(wrapper.find('.page-subtitle').text()).toBe('Monitoring locations and the Minions that poll and collect from them.')
    expect(showToast).not.toHaveBeenCalled()
  })

  it('shows the tab labels with counts', async () => {
    const { wrapper, minionStore, locationStore } = await mountPage()
    minionStore.minions = [{ id: 'm1' }, { id: 'm2' }] as any
    locationStore.locations = [{ 'location-name': 'Default' }] as any
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-test="tab-minions"]').text()).toBe('Minions (2)')
    expect(wrapper.find('[data-test="tab-locations"]').text()).toBe('Monitoring locations (1)')
  })

  it('opens on the Minions tab by default and on the tab named in the query string', async () => {
    const first = await mountPage()
    expect(first.wrapper.find('[data-test="tab-minions"]').attributes('aria-selected')).toBe('true')
    const second = await mountPage({ tab: 'locations' })
    expect(second.wrapper.find('[data-test="tab-locations"]').attributes('aria-selected')).toBe('true')
  })

  it('falls back to the Minions tab for an unknown ?tab= value', async () => {
    const { wrapper } = await mountPage({ tab: 'bogus' })
    expect(wrapper.find('[data-test="tab-minions"]').attributes('aria-selected')).toBe('true')
    expect(wrapper.find('[data-test="tab-locations"]').attributes('aria-selected')).toBe('false')
  })

  it('writes the active tab to ?tab= and follows a query change', async () => {
    const { wrapper, router } = await mountPage()
    await wrapper.find('[data-test="tab-locations"]').trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.query.tab).toBe('locations')

    await router.replace({ query: { tab: 'minions' }})
    await flushPromises()
    expect(wrapper.find('[data-test="tab-minions"]').attributes('aria-selected')).toBe('true')
  })

  it('toasts when the initial load fails', async () => {
    await mountPage({ minionsOk: false })
    expect(showToast).toHaveBeenCalledWith({ message: 'Failed to load minions.', severity: 'error' })
    showToast.mockClear()
    await mountPage({ locationsOk: false })
    expect(showToast).toHaveBeenCalledWith({ message: 'Failed to load monitoring locations.', severity: 'error' })
  })

  it('Refresh reloads both stores and toasts a failure', async () => {
    const { wrapper, minionStore, locationStore } = await mountPage()
    vi.mocked(locationStore.getLocations).mockResolvedValueOnce(false)
    await wrapper.find('[data-test="refresh-button"]').trigger('click')
    await flushPromises()
    expect(minionStore.getMinions).toHaveBeenCalledTimes(2)
    expect(locationStore.getLocations).toHaveBeenCalledTimes(2)
    expect(showToast).toHaveBeenCalledWith({ message: 'Failed to load monitoring locations.', severity: 'error' })
  })

  it('ticks the Updated label every second and hands the same clock to the Minions table', async () => {
    const { wrapper } = await mountPage()
    const start = Date.now()
    expect(wrapper.find('[data-test="updated-label"]').text()).toBe('Updated 0 s ago · auto-refresh every 30 s')
    expect(wrapper.findComponent(MinionsTableStub).props('now')).toBe(start)
    await vi.advanceTimersByTimeAsync(11_000)
    expect(wrapper.find('[data-test="updated-label"]').text()).toBe('Updated 11 s ago · auto-refresh every 30 s')
    expect(wrapper.findComponent(MinionsTableStub).props('now')).toBe(start + 11_000)
  })

  it('does not claim an update when a load failed', async () => {
    const { wrapper } = await mountPage({ minionsOk: false })
    expect(wrapper.find('[data-test="updated-label"]').text()).toContain('Not updated yet')
  })

  it('auto-refreshes both stores every 30 s without toasting a failure', async () => {
    const { minionStore, locationStore } = await mountPage()
    vi.mocked(minionStore.getMinions).mockResolvedValue(false)
    await vi.advanceTimersByTimeAsync(30_000)
    expect(minionStore.getMinions).toHaveBeenCalledTimes(2)
    expect(locationStore.getLocations).toHaveBeenCalledTimes(2)
    expect(showToast).not.toHaveBeenCalled()
    await vi.advanceTimersByTimeAsync(30_000)
    expect(minionStore.getMinions).toHaveBeenCalledTimes(3)
  })

  it('does not start another refresh while one is still in flight', async () => {
    const { minionStore } = await mountPage()
    let release!: (ok: boolean) => void
    vi.mocked(minionStore.getMinions).mockReturnValueOnce(new Promise((resolve) => {
      release = resolve
    }))
    await vi.advanceTimersByTimeAsync(30_000)
    expect(minionStore.getMinions).toHaveBeenCalledTimes(2)
    await vi.advanceTimersByTimeAsync(30_000)
    expect(minionStore.getMinions).toHaveBeenCalledTimes(2)
    release(true)
    await flushPromises()
    await vi.advanceTimersByTimeAsync(30_000)
    expect(minionStore.getMinions).toHaveBeenCalledTimes(3)
  })

  it('pauses the auto-refresh while a dialog is open and resumes after it closes', async () => {
    const { wrapper, minionStore } = await mountPage()
    wrapper.findComponent(LocationsTableStub).vm.$emit('dialogOpen', true)
    await vi.advanceTimersByTimeAsync(60_000)
    expect(minionStore.getMinions).toHaveBeenCalledTimes(1)
    wrapper.findComponent(LocationsTableStub).vm.$emit('dialogOpen', false)
    await vi.advanceTimersByTimeAsync(30_000)
    expect(minionStore.getMinions).toHaveBeenCalledTimes(2)
  })

  it('skips the tick while the page is hidden and refreshes once it is visible again', async () => {
    const { minionStore } = await mountPage()
    setHidden(true)
    await vi.advanceTimersByTimeAsync(90_000)
    expect(minionStore.getMinions).toHaveBeenCalledTimes(1)
    document.dispatchEvent(new Event('visibilitychange'))
    await flushPromises()
    expect(minionStore.getMinions).toHaveBeenCalledTimes(1)
    setHidden(false)
    document.dispatchEvent(new Event('visibilitychange'))
    await flushPromises()
    expect(minionStore.getMinions).toHaveBeenCalledTimes(2)
    expect(showToast).not.toHaveBeenCalled()
  })

  it('does not reload on becoming visible when the data is younger than 10 s', async () => {
    const { minionStore } = await mountPage()
    setHidden(true)
    await vi.advanceTimersByTimeAsync(5_000)
    setHidden(false)
    document.dispatchEvent(new Event('visibilitychange'))
    await flushPromises()
    expect(minionStore.getMinions).toHaveBeenCalledTimes(1)

    setHidden(true)
    await vi.advanceTimersByTimeAsync(5_000)
    setHidden(false)
    document.dispatchEvent(new Event('visibilitychange'))
    await flushPromises()
    expect(minionStore.getMinions).toHaveBeenCalledTimes(2)
  })

  it('reloads on becoming visible when nothing has loaded yet', async () => {
    const { minionStore } = await mountPage({ minionsOk: false })
    setHidden(true)
    setHidden(false)
    document.dispatchEvent(new Event('visibilitychange'))
    await flushPromises()
    expect(minionStore.getMinions).toHaveBeenCalledTimes(2)
  })

  it('stops the timers and the visibility listener on unmount', async () => {
    const { wrapper, minionStore } = await mountPage()
    wrapper.unmount()
    mounted.splice(0)
    await vi.advanceTimersByTimeAsync(60_000)
    document.dispatchEvent(new Event('visibilitychange'))
    await flushPromises()
    expect(minionStore.getMinions).toHaveBeenCalledTimes(1)
  })

  describe('node counts', () => {
    it('are not fetched while the Minions tab is active', async () => {
      const { wrapper, locationStore } = await mountPage()
      expect(locationStore.getNodeCounts).not.toHaveBeenCalled()
      await wrapper.find('[data-test="refresh-button"]').trigger('click')
      await flushPromises()
      await vi.advanceTimersByTimeAsync(30_000)
      expect(locationStore.getNodeCounts).not.toHaveBeenCalled()
    })

    it('are fetched with the initial load when the page opens on the Locations tab', async () => {
      const { locationStore } = await mountPage({ tab: 'locations' })
      expect(locationStore.getNodeCounts).toHaveBeenCalledTimes(1)
    })

    it('are fetched on tab activation, on Refresh and on the tick while the Locations tab is active', async () => {
      const { wrapper, locationStore } = await mountPage()
      await wrapper.find('[data-test="tab-locations"]').trigger('click')
      await flushPromises()
      expect(locationStore.getNodeCounts).toHaveBeenCalledTimes(1)
      await wrapper.find('[data-test="refresh-button"]').trigger('click')
      await flushPromises()
      expect(locationStore.getNodeCounts).toHaveBeenCalledTimes(2)
      await vi.advanceTimersByTimeAsync(30_000)
      expect(locationStore.getNodeCounts).toHaveBeenCalledTimes(3)

      await wrapper.find('[data-test="tab-minions"]').trigger('click')
      await flushPromises()
      await vi.advanceTimersByTimeAsync(30_000)
      expect(locationStore.getNodeCounts).toHaveBeenCalledTimes(3)
    })

    it('are skipped when the locations themselves failed to load', async () => {
      const { locationStore } = await mountPage({ tab: 'locations', locationsOk: false })
      expect(locationStore.getNodeCounts).not.toHaveBeenCalled()
    })

    it('are enabled in the store only while the Locations tab is active', async () => {
      const { wrapper, locationStore } = await mountPage()
      expect(locationStore.countsEnabled).toBe(false)
      await wrapper.find('[data-test="tab-locations"]').trigger('click')
      await flushPromises()
      expect(locationStore.countsEnabled).toBe(true)
      await wrapper.find('[data-test="tab-minions"]').trigger('click')
      await flushPromises()
      expect(locationStore.countsEnabled).toBe(false)

      const opened = await mountPage({ tab: 'locations' })
      expect(opened.locationStore.countsEnabled).toBe(true)
      opened.wrapper.unmount()
      mounted.splice(mounted.indexOf(opened.wrapper), 1)
      expect(opened.locationStore.countsEnabled).toBe(false)
    })
  })

  it('showMinions from the Locations tab switches to Minions filtered by that location', async () => {
    const { wrapper, router } = await mountPage({ tab: 'locations' })
    wrapper.findComponent(LocationsTableStub).vm.$emit('showMinions', 'dc-east')
    await flushPromises()
    expect(wrapper.find('[data-test="tab-minions"]').attributes('aria-selected')).toBe('true')
    expect(router.currentRoute.value.query.tab).toBe('minions')
    expect(wrapper.findComponent(MinionsTableStub).props('locationFilter')).toBe('dc-east')

    wrapper.findComponent(MinionsTableStub).vm.$emit('update:locationFilter', null)
    await flushPromises()
    expect(wrapper.findComponent(MinionsTableStub).props('locationFilter')).toBeNull()
  })

  it('showLocation from the Minions tab switches to Locations and searches for the name', async () => {
    const { wrapper, router } = await mountPage()
    wrapper.findComponent(MinionsTableStub).vm.$emit('showLocation', 'dc-east')
    await flushPromises()
    expect(wrapper.find('[data-test="tab-locations"]').attributes('aria-selected')).toBe('true')
    expect(router.currentRoute.value.query.tab).toBe('locations')
    expect(searchFor).toHaveBeenCalledWith('dc-east')
  })
})
