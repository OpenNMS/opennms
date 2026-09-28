import MinionDeleteDialog from '@/components/ManageMinions/MinionDeleteDialog.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

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

const NOW = new Date('2026-09-24T12:00:00Z').getTime()
const MIN = 60_000
const RECHECK = 30_000

const minion = (over: Record<string, any> = {}) =>
  ({ id: 'm1', label: 'm1', location: 'Default', type: 'Minion', status: 'down', version: '1', date: NOW - 10 * MIN, properties: {}, ...over })

interface Opts {
  // what GET /minions/m1 answers; defaults to the row the list passed in
  fresh?: Record<string, any> | null
}

const mountDialog = async (over: Record<string, any> = {}, { fresh = minion(over) }: Opts = {}) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const store = useMinionAdminStore()
  vi.mocked(store.getMinion).mockResolvedValue(fresh as any)
  vi.mocked(store.deleteMinion).mockResolvedValue({ success: true, message: '' })
  const wrapper = mount(MinionDeleteDialog, {
    props: { visible: false, minion: minion(over) as any, now: NOW },
    global: { plugins: [PrimeVue, pinia], stubs: { Dialog: DialogStub, transition: false }}
  })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return { wrapper, store }
}

const deleteDisabled = (wrapper: VueWrapper<any>) => wrapper.find('[data-test="delete-button"]').attributes('disabled') !== undefined
const status = (wrapper: VueWrapper<any>) => wrapper.find('[data-test="status-callout"]')

describe('MinionDeleteDialog.vue', () => {
  beforeEach(() => {
    showToast.mockClear()
    vi.useFakeTimers({ toFake: ['setInterval', 'clearInterval'] })
  })
  afterEach(() => vi.useRealTimers())

  it('names the Minion in the title, re-reads its row on open and asks for no typed confirmation', async () => {
    const { wrapper, store } = await mountDialog()
    expect(wrapper.find('[data-test="header"]').text()).toBe('Delete m1?')
    expect(wrapper.text()).toContain('Use this for Minions you have decommissioned. It cannot be undone.')
    expect(store.getMinion).toHaveBeenCalledWith('m1')
    expect(wrapper.find('[data-test="confirm-input"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="alarms-callout"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="requisition-callout"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="cancel-button"]').attributes('autofocus')).toBeDefined()
  })

  it('refuses a Minion that is UP, with its heartbeat, and says to stop it first', async () => {
    const { wrapper } = await mountDialog({ status: 'up', date: NOW - 36_000 })
    expect(status(wrapper).classes()).toContain('p-message-error')
    expect(status(wrapper).text()).toContain('m1 is UP — its last heartbeat arrived 36 s ago. A running Minion cannot be deleted: stop the Minion process, wait for its status here to turn DOWN, then delete it.')
    expect(status(wrapper).text()).toContain('registers again within 30 seconds')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('refuses an UP Minion even with an old heartbeat, as the table does', async () => {
    const { wrapper } = await mountDialog({ status: 'up', date: NOW - 10 * MIN })
    expect(status(wrapper).text()).toContain('m1 is UP — its last heartbeat arrived 10 min ago.')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('allows a Minion that is DOWN and says what goes with it', async () => {
    const { wrapper } = await mountDialog({ status: 'down', date: NOW - 10 * MIN })
    expect(status(wrapper).classes()).toContain('p-message-success')
    expect(status(wrapper).text()).toContain('m1 is DOWN — its heartbeats stopped; the last one arrived 10 min ago. It can be deleted: the Minion and the alarms raised through it are removed, its events are kept, and its node stays until the Minions requisition is synchronized.')
    expect(deleteDisabled(wrapper)).toBe(false)
  })

  it('allows a Minion whose status is unknown, since it has not been heard from since the core started', async () => {
    const { wrapper } = await mountDialog({ status: 'unknown', date: NOW - 30_000 })
    expect(status(wrapper).classes()).toContain('p-message-warn')
    expect(status(wrapper).text()).toContain('m1 is UNKNOWN — it has not been heard from since the core started; its last recorded heartbeat arrived 30 s ago. It can be deleted:')
    expect(deleteDisabled(wrapper)).toBe(false)
    const missing = (await mountDialog({ status: null, date: null })).wrapper
    expect(status(missing).text()).toContain('m1 is UNKNOWN')
    expect(status(missing).text()).toContain('arrived at an unknown time')
    expect(deleteDisabled(missing)).toBe(false)
  })

  it('gates on the freshly read row, not on the row the paused list passed in', async () => {
    const { wrapper } = await mountDialog({ status: 'down' }, { fresh: minion({ status: 'up', date: NOW - 20_000 }) })
    expect(status(wrapper).text()).toContain('m1 is UP — its last heartbeat arrived 20 s ago.')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('re-reads the row every 30 s and follows a status change either way', async () => {
    const { wrapper, store } = await mountDialog({ status: 'up', date: NOW - 20_000 })
    expect(deleteDisabled(wrapper)).toBe(true)
    vi.mocked(store.getMinion).mockResolvedValue(minion({ status: 'down', date: NOW - 20_000 }) as any)
    vi.advanceTimersByTime(RECHECK)
    await flushPromises()
    expect(store.getMinion).toHaveBeenCalledTimes(2)
    expect(status(wrapper).classes()).toContain('p-message-success')
    expect(deleteDisabled(wrapper)).toBe(false)
    vi.mocked(store.getMinion).mockResolvedValue(minion({ status: 'up', date: NOW }) as any)
    vi.advanceTimersByTime(RECHECK)
    await flushPromises()
    expect(status(wrapper).classes()).toContain('p-message-error')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('keeps the last decision when a re-read fails, and the clock alone never changes it', async () => {
    const { wrapper, store } = await mountDialog({ status: 'up', date: NOW - 30_000 })
    vi.mocked(store.getMinion).mockResolvedValue(null)
    await wrapper.setProps({ now: NOW + 10 * MIN })
    vi.advanceTimersByTime(RECHECK)
    await flushPromises()
    expect(status(wrapper).text()).toContain('m1 is UP — its last heartbeat arrived 10 min ago.')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('stops re-reading once the dialog closes', async () => {
    const { wrapper, store } = await mountDialog()
    await wrapper.setProps({ visible: false })
    vi.advanceTimersByTime(3 * RECHECK)
    await flushPromises()
    expect(store.getMinion).toHaveBeenCalledTimes(1)
  })

  it('keeps Delete disabled and shows a loading state until the row is read', async () => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
    const store = useMinionAdminStore()
    let resolveRow: (m: any) => void = () => {}
    vi.mocked(store.getMinion).mockReturnValue(new Promise((resolve) => {
      resolveRow = resolve
    }))
    const wrapper = mount(MinionDeleteDialog, {
      props: { visible: false, minion: minion() as any, now: NOW },
      global: { plugins: [PrimeVue, pinia], stubs: { Dialog: DialogStub, transition: false }}
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
    expect(wrapper.find('[data-test="loading"]').exists()).toBe(true)
    expect(status(wrapper).exists()).toBe(false)
    expect(deleteDisabled(wrapper)).toBe(true)
    resolveRow(minion())
    await flushPromises()
    expect(wrapper.find('[data-test="loading"]').exists()).toBe(false)
    expect(status(wrapper).classes()).toContain('p-message-success')
    expect(deleteDisabled(wrapper)).toBe(false)
  })

  it('deletes, toasts and closes', async () => {
    const { wrapper, store } = await mountDialog()
    await wrapper.find('[data-test="delete-button"]').trigger('click')
    await flushPromises()
    expect(store.deleteMinion).toHaveBeenCalledWith('m1')
    expect(showToast).toHaveBeenCalledWith({ message: 'Minion \'m1\' deleted.', severity: 'success' })
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('shows the failure inline and stays open', async () => {
    const { wrapper, store } = await mountDialog()
    vi.mocked(store.deleteMinion).mockResolvedValue({ success: false, message: 'Minion \'m1\' could not be deleted.' })
    await wrapper.find('[data-test="delete-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toBe('Minion \'m1\' could not be deleted.')
    expect(wrapper.emitted('update:visible')).toBeUndefined()
    expect(showToast).not.toHaveBeenCalled()
  })

  it('Cancel closes without deleting', async () => {
    const { wrapper, store } = await mountDialog()
    await wrapper.find('[data-test="cancel-button"]').trigger('click')
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    expect(store.deleteMinion).not.toHaveBeenCalled()
  })
})
