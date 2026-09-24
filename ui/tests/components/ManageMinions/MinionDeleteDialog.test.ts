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
  alarms?: number | null
  // what GET /minions/m1 answers; defaults to the row the list passed in
  fresh?: Record<string, any> | null
}

const mountDialog = async (over: Record<string, any> = {}, { alarms = 0, fresh = minion(over) }: Opts = {}) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const store = useMinionAdminStore()
  vi.mocked(store.getAlarmCount).mockResolvedValue(alarms)
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
const callout = (wrapper: VueWrapper<any>, name: string) => wrapper.find(`[data-test="${name}-callout"]`)

describe('MinionDeleteDialog.vue', () => {
  beforeEach(() => {
    showToast.mockClear()
    vi.useFakeTimers({ toFake: ['setInterval', 'clearInterval'] })
  })
  afterEach(() => vi.useRealTimers())

  it('names the Minion in the title, re-reads its row and counts its alarms on open', async () => {
    const { wrapper, store } = await mountDialog()
    expect(wrapper.find('[data-test="header"]').text()).toBe('Delete m1?')
    expect(wrapper.text()).toContain('Use this for Minions you have decommissioned.')
    expect(store.getAlarmCount).toHaveBeenCalledWith('m1')
    expect(store.getMinion).toHaveBeenCalledWith('m1')
  })

  it('refuses a Minion whose heartbeat is recent and says to stop it first', async () => {
    const { wrapper } = await mountDialog({ date: NOW - 90_000 })
    const heartbeat = callout(wrapper, 'heartbeat')
    expect(heartbeat.classes()).toContain('p-message-error')
    expect(heartbeat.text()).toContain('Last heartbeat 1 min ago — this Minion is still running. Stop the Minion process first, then delete it here; otherwise it registers again within 30 seconds.')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('gates on the freshly read row, not on the row the paused list passed in', async () => {
    const { wrapper } = await mountDialog({ date: NOW - 10 * MIN }, { fresh: minion({ date: NOW - 20_000 }) })
    expect(callout(wrapper, 'heartbeat').text()).toContain('Last heartbeat 20 s ago — this Minion is still running.')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('treats an UP Minion with no usable heartbeat time as running', async () => {
    const { wrapper } = await mountDialog({ date: null, status: 'up' })
    expect(callout(wrapper, 'heartbeat').text()).toContain('Last heartbeat unknown — this Minion is still running.')
    expect(deleteDisabled(wrapper)).toBe(true)
    const down = (await mountDialog({ date: null, status: 'down' })).wrapper
    expect(callout(down, 'heartbeat').classes()).toContain('p-message-success')
    expect(deleteDisabled(down)).toBe(false)
  })

  it('treats an UP Minion with an old heartbeat as running, in case the clocks disagree', async () => {
    const { wrapper } = await mountDialog({ date: NOW - 10 * MIN, status: 'up' })
    expect(callout(wrapper, 'heartbeat').classes()).toContain('p-message-error')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('allows a Minion whose heartbeat is old', async () => {
    const { wrapper } = await mountDialog({ date: NOW - 10 * MIN })
    const heartbeat = callout(wrapper, 'heartbeat')
    expect(heartbeat.classes()).toContain('p-message-success')
    expect(heartbeat.text()).toContain('Last heartbeat 10 min ago — it looks stopped. A Minion that is still running registers again within 30 seconds, with its label and properties cleared.')
    expect(deleteDisabled(wrapper)).toBe(false)
  })

  it('keeps a running Minion blocked across the 30 s re-read while the clock passes two minutes', async () => {
    const { wrapper, store } = await mountDialog({ date: NOW - 30_000, status: 'unknown' })
    expect(deleteDisabled(wrapper)).toBe(true)
    await wrapper.setProps({ now: NOW + 3 * MIN })
    expect(callout(wrapper, 'heartbeat').text()).toContain('Last heartbeat 3 min ago — this Minion is still running.')
    expect(deleteDisabled(wrapper)).toBe(true)
    vi.mocked(store.getMinion).mockResolvedValue(minion({ date: NOW + 3 * MIN - 20_000, status: 'unknown' }) as any)
    vi.advanceTimersByTime(RECHECK)
    await flushPromises()
    expect(store.getMinion).toHaveBeenCalledTimes(2)
    expect(callout(wrapper, 'heartbeat').text()).toContain('Last heartbeat 20 s ago — this Minion is still running.')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('unblocks only when a re-read returns an older heartbeat', async () => {
    const { wrapper, store } = await mountDialog({ date: NOW - 30_000, status: 'unknown' })
    expect(deleteDisabled(wrapper)).toBe(true)
    vi.mocked(store.getMinion).mockResolvedValue(minion({ date: NOW - 10 * MIN, status: 'down' }) as any)
    vi.advanceTimersByTime(RECHECK)
    await flushPromises()
    expect(callout(wrapper, 'heartbeat').classes()).toContain('p-message-success')
    expect(deleteDisabled(wrapper)).toBe(false)
  })

  it('never lets the clock alone unblock, and a re-read that fails keeps the last decision', async () => {
    const { wrapper, store } = await mountDialog({ date: NOW - 30_000, status: 'unknown' })
    vi.mocked(store.getMinion).mockResolvedValue(null)
    await wrapper.setProps({ now: NOW + 10 * MIN })
    vi.advanceTimersByTime(RECHECK)
    await flushPromises()
    expect(callout(wrapper, 'heartbeat').text()).toContain('Last heartbeat 10 min ago — this Minion is still running.')
    expect(deleteDisabled(wrapper)).toBe(true)
  })

  it('stops re-reading once the dialog closes', async () => {
    const { wrapper, store } = await mountDialog()
    await wrapper.setProps({ visible: false })
    vi.advanceTimersByTime(3 * RECHECK)
    await flushPromises()
    expect(store.getMinion).toHaveBeenCalledTimes(1)
  })

  it('counts the alarms that go with the Minion and says its events stay', async () => {
    const many = callout((await mountDialog({}, { alarms: 7 })).wrapper, 'alarms')
    expect(many.classes()).toContain('p-message-error')
    expect(many.text()).toContain('Its alarms are deleted — 7 alarms raised through this Minion, such as traps and syslog it received, are removed with it. Its events are kept.')
    const none = callout((await mountDialog({}, { alarms: 0 })).wrapper, 'alarms')
    expect(none.classes()).toContain('p-message-info')
    expect(none.text()).toBe('No alarms were raised through this Minion. Its events are kept.')
    const unknown = callout((await mountDialog({}, { alarms: null })).wrapper, 'alarms')
    expect(unknown.classes()).toContain('p-message-info')
    expect(unknown.text()).toBe('Its alarms could not be counted; any alarms raised through this Minion are removed with it. Its events are kept.')
  })

  it('keeps Delete disabled and shows a loading state while the row and the alarms are read', async () => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
    const store = useMinionAdminStore()
    let resolveAlarms: (n: number) => void = () => {}
    vi.mocked(store.getAlarmCount).mockReturnValue(new Promise((resolve) => {
      resolveAlarms = resolve
    }))
    vi.mocked(store.getMinion).mockResolvedValue(minion() as any)
    const wrapper = mount(MinionDeleteDialog, {
      props: { visible: false, minion: minion() as any, now: NOW },
      global: { plugins: [PrimeVue, pinia], stubs: { Dialog: DialogStub, transition: false }}
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
    expect(wrapper.find('[data-test="loading"]').exists()).toBe(true)
    expect(callout(wrapper, 'alarms').exists()).toBe(false)
    expect(deleteDisabled(wrapper)).toBe(true)
    resolveAlarms(0)
    await flushPromises()
    expect(wrapper.find('[data-test="loading"]').exists()).toBe(false)
    expect(deleteDisabled(wrapper)).toBe(false)
  })

  it('always explains that the node stays until the Minions requisition is synchronized, with a link', async () => {
    const { wrapper } = await mountDialog({ date: NOW - 30_000 })
    const requisition = callout(wrapper, 'requisition')
    expect(requisition.classes()).toContain('p-message-info')
    expect(requisition.text()).toContain('Its node stays until the Minions requisition is synchronized — the node is removed from the pending Minions requisition only (or the one named by opennms.minion.provisioning.foreignSourcePattern), so it keeps being monitored until that requisition is synchronized.')
    const link = requisition.find('[data-test="requisition-link"]')
    expect(link.text()).toBe('Open the Minions requisition')
    expect(link.attributes('href')).toMatch(/admin\/ng-requisitions\/index\.jsp#\/requisitions\/Minions$/)
    expect(link.attributes('target')).toBe('_self')
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
