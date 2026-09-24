import LocationDeleteDialog from '@/components/ManageMonitoringLocations/LocationDeleteDialog.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { showToast } = vi.hoisted(() => ({ showToast: vi.fn() }))
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  useOnmsToast: () => ({ showToast })
}))

// the real Transition is kept so OnmsMessage's fallthrough data-test lands on the callout element
const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><h2 data-test="header">{{ header }}</h2><slot /><slot name="footer" /></div>'
}

const loc = (name: string) => ({
  'location-name': name, 'monitoring-area': 'Area', name, area: 'Area',
  geolocation: null, latitude: 1, longitude: 2, priority: 5, tags: []
})

const minion = (id: string, location: string) =>
  ({ id, label: id, location, type: 'Minion', status: 'up', version: '1', date: 0, properties: {}})

interface Deps {
  nodes?: number | null
  minions?: string[]
  apps?: { id: number; name: string }[] | null
  outages?: number | null
}

const mountDialog = async ({ nodes = 0, minions = [], apps = [], outages = 0 }: Deps = {}) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const store = useMonitoringLocationAdminStore()
  const minionStore = useMinionAdminStore()
  minionStore.minions = minions.map(id => minion(id, 'Raleigh')) as any
  vi.mocked(store.getNodeCount).mockResolvedValue(nodes)
  vi.mocked(store.getApplicationsUsingPerspective).mockResolvedValue(apps)
  vi.mocked(store.getPerspectiveOutageCount).mockResolvedValue(outages)
  vi.mocked(store.deleteLocation).mockResolvedValue({ success: true, message: '' })
  const wrapper = mount(LocationDeleteDialog, {
    props: { visible: false, location: loc('Raleigh') as any },
    global: { plugins: [PrimeVue, pinia], stubs: { Dialog: DialogStub, transition: false }}
  })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return { wrapper, store }
}

const deleteDisabled = (wrapper: VueWrapper<any>) => wrapper.find('[data-test="delete-button"]').attributes('disabled') !== undefined
const callout = (wrapper: VueWrapper<any>, name: string) => wrapper.find(`[data-test="${name}-callout"]`)
const typeName = (wrapper: VueWrapper<any>, text: string) => wrapper.find('[data-test="confirm-input"]').setValue(text)

describe('LocationDeleteDialog.vue', () => {
  beforeEach(() => showToast.mockClear())

  it('names the location in the title and subtitle, and looks everything up on open', async () => {
    const { wrapper, store } = await mountDialog()
    expect(wrapper.find('[data-test="header"]').text()).toBe('Delete monitoring location Raleigh?')
    expect(wrapper.text()).toContain('Here is what this changes. It cannot be undone.')
    expect(store.getNodeCount).toHaveBeenCalledWith('Raleigh')
    expect(store.getApplicationsUsingPerspective).toHaveBeenCalledWith('Raleigh')
    expect(store.getPerspectiveOutageCount).toHaveBeenCalledWith('Raleigh')
    expect(wrapper.find('[data-test="loading"]').exists()).toBe(false)
  })

  it('shows a loading state and keeps Delete disabled until the lookups finish', async () => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
    const store = useMonitoringLocationAdminStore()
    let resolveNodes: (n: number) => void = () => {}
    vi.mocked(store.getNodeCount).mockReturnValue(new Promise((resolve) => {
      resolveNodes = resolve
    }))
    vi.mocked(store.getApplicationsUsingPerspective).mockResolvedValue([])
    vi.mocked(store.getPerspectiveOutageCount).mockResolvedValue(0)
    const wrapper = mount(LocationDeleteDialog, {
      props: { visible: false, location: loc('Raleigh') as any },
      global: { plugins: [PrimeVue, pinia], stubs: { Dialog: DialogStub, transition: false }}
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
    expect(wrapper.find('[data-test="loading"]').exists()).toBe(true)
    expect(callout(wrapper, 'nodes').exists()).toBe(false)
    await typeName(wrapper, 'Raleigh')
    expect(deleteDisabled(wrapper)).toBe(true)
    resolveNodes(0)
    await flushPromises()
    expect(wrapper.find('[data-test="loading"]').exists()).toBe(false)
    expect(callout(wrapper, 'nodes').classes()).toContain('p-message-success')
    expect(deleteDisabled(wrapper)).toBe(false)
  })

  it('is blocked while nodes are still in the location, even with the name typed', async () => {
    const { wrapper } = await mountDialog({ nodes: 3 })
    const nodes = callout(wrapper, 'nodes')
    expect(nodes.classes()).toContain('p-message-error')
    expect(nodes.text()).toContain('3 nodes and 0 Minions are still here')
    expect(nodes.text()).toContain('a monitoring location with nodes cannot be deleted, and that includes each running Minion\'s own node. Move or delete them first.')
    await typeName(wrapper, 'Raleigh')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('is blocked while Minions are still in the location', async () => {
    const { wrapper } = await mountDialog({ nodes: 1, minions: ['m1'] })
    const nodes = callout(wrapper, 'nodes')
    expect(nodes.classes()).toContain('p-message-error')
    expect(nodes.text()).toContain('1 node and 1 Minion are still here')
    await typeName(wrapper, 'Raleigh')
    expect(deleteDisabled(wrapper)).toBe(true)
    expect(wrapper.find('[data-test="confirm-input"]').attributes('disabled')).toBeDefined()
  })

  it('confirms an empty location in green', async () => {
    const { wrapper } = await mountDialog()
    const nodes = callout(wrapper, 'nodes')
    expect(nodes.classes()).toContain('p-message-success')
    expect(nodes.text()).toContain('No nodes or Minions here — a monitoring location with nodes cannot be deleted, and that includes each running Minion\'s own node.')
  })

  it('warns, without blocking, when the node count could not be determined', async () => {
    const { wrapper } = await mountDialog({ nodes: null })
    const nodes = callout(wrapper, 'nodes')
    expect(nodes.classes()).toContain('p-message-warn')
    expect(nodes.text()).toContain('The node count could not be checked')
    await typeName(wrapper, 'Raleigh')
    expect(deleteDisabled(wrapper)).toBe(false)
  })

  it('lists the applications that use the location as a perspective, with a link to Manage Applications', async () => {
    const { wrapper } = await mountDialog({ apps: [{ id: 1, name: 'Web Shop' }, { id: 2, name: 'VPN' }] })
    const perspective = callout(wrapper, 'perspective')
    expect(perspective.classes()).toContain('p-message-warn')
    expect(perspective.text()).toContain('Removed from 2 applications as a perspective — Web Shop, VPN stop being polled from this monitoring location, and nothing asks you to replace it.')
    const link = perspective.find('[data-test="applications-link"]')
    expect(link.text()).toBe('Open Manage Applications')
    expect(link.attributes('href')).toMatch(/admin\/applications\.htm$/)
    expect(link.attributes('target')).toBe('_self')
  })

  it('omits the perspective callout when no application uses the location, and explains when it could not be checked', async () => {
    const { wrapper } = await mountDialog({ apps: [] })
    expect(callout(wrapper, 'perspective').exists()).toBe(false)
    const unknown = (await mountDialog({ apps: null })).wrapper
    expect(callout(unknown, 'perspective').classes()).toContain('p-message-info')
    expect(callout(unknown, 'perspective').text()).toContain('Applications could not be checked')
  })

  it('counts the perspective outages that go with the location and omits the callout at zero', async () => {
    const { wrapper } = await mountDialog({ outages: 12 })
    const outages = callout(wrapper, 'outages')
    expect(outages.classes()).toContain('p-message-error')
    expect(outages.text()).toContain('Perspective outage history is deleted — 12 outages recorded from this monitoring location\'s perspective are removed with it.')
    expect(callout((await mountDialog({ outages: 0 })).wrapper, 'outages').exists()).toBe(false)
    const unknown = callout((await mountDialog({ outages: null })).wrapper, 'outages')
    expect(unknown.classes()).toContain('p-message-info')
    expect(unknown.text()).toContain('Perspective outage history could not be counted; any outages recorded from this perspective are removed with the location.')
  })

  it('enables Delete only when the typed name matches exactly', async () => {
    const { wrapper } = await mountDialog()
    expect(wrapper.text()).toContain('Type Raleigh to confirm')
    expect(deleteDisabled(wrapper)).toBe(true)
    await typeName(wrapper, 'raleigh')
    expect(deleteDisabled(wrapper)).toBe(true)
    await typeName(wrapper, 'Raleigh ')
    expect(deleteDisabled(wrapper)).toBe(true)
    await typeName(wrapper, 'Raleigh')
    expect(deleteDisabled(wrapper)).toBe(false)
    await typeName(wrapper, 'Raleig')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('deletes, closes and reports what went with it, without a toast', async () => {
    const { wrapper, store } = await mountDialog({ apps: [{ id: 1, name: 'Web Shop' }], outages: 2 })
    await typeName(wrapper, 'Raleigh')
    await wrapper.find('[data-test="delete-button"]').trigger('click')
    await flushPromises()
    expect(store.deleteLocation).toHaveBeenCalledWith('Raleigh')
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    expect(wrapper.emitted('deleted')).toEqual([[{ name: 'Raleigh', applications: [{ id: 1, name: 'Web Shop' }], outageCount: 2 }]])
    expect(showToast).not.toHaveBeenCalled()
  })

  it('shows the failure inline and stays open', async () => {
    const { wrapper, store } = await mountDialog()
    vi.mocked(store.deleteLocation).mockResolvedValue({ success: false, message: 'Monitoring location \'Raleigh\' could not be deleted. Make sure no nodes are assigned to it.' })
    await typeName(wrapper, 'Raleigh')
    await wrapper.find('[data-test="delete-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('Make sure no nodes are assigned')
    expect(wrapper.emitted('update:visible')).toBeUndefined()
    expect(wrapper.emitted('deleted')).toBeUndefined()
    expect(showToast).not.toHaveBeenCalled()
  })

  it('Cancel closes without deleting', async () => {
    const { wrapper, store } = await mountDialog()
    await wrapper.find('[data-test="cancel-button"]').trigger('click')
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    expect(store.deleteLocation).not.toHaveBeenCalled()
  })

  it('starts over on every open', async () => {
    const { wrapper, store } = await mountDialog({ nodes: 2 })
    await wrapper.setProps({ visible: false })
    vi.mocked(store.getNodeCount).mockResolvedValue(0)
    await wrapper.setProps({ visible: true })
    await flushPromises()
    expect(callout(wrapper, 'nodes').classes()).toContain('p-message-success')
    expect((wrapper.find('[data-test="confirm-input"]').element as HTMLInputElement).value).toBe('')
  })
})
