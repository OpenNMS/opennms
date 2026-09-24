import MinionDeleteDialog from '@/components/ManageMinions/MinionDeleteDialog.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
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

const NOW = new Date('2026-09-24T12:00:00Z').getTime()
const MIN = 60_000

const minion = (over: Record<string, any> = {}) =>
  ({ id: 'm1', label: 'm1', location: 'Default', type: 'Minion', status: 'up', version: '1', date: NOW - 10 * MIN, properties: {}, ...over })

const mountDialog = async (over: Record<string, any> = {}, alarms: number | null = 0) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const store = useMinionAdminStore()
  vi.mocked(store.getAlarmCount).mockResolvedValue(alarms)
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
  beforeEach(() => showToast.mockClear())

  it('names the Minion in the title and counts its alarms on open', async () => {
    const { wrapper, store } = await mountDialog()
    expect(wrapper.find('[data-test="header"]').text()).toBe('Delete m1?')
    expect(wrapper.text()).toContain('Use this for Minions you have decommissioned.')
    expect(store.getAlarmCount).toHaveBeenCalledWith('m1')
  })

  it('refuses a Minion whose heartbeat is recent and says to stop it first', async () => {
    const { wrapper } = await mountDialog({ date: NOW - 90_000 })
    const heartbeat = callout(wrapper, 'heartbeat')
    expect(heartbeat.classes()).toContain('p-message-error')
    expect(heartbeat.text()).toContain('Last heartbeat 1 min ago — this Minion is still running. Stop the Minion process first, then delete it here; otherwise it registers again within 30 seconds.')
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

  it('allows a Minion whose heartbeat is old', async () => {
    const { wrapper } = await mountDialog({ date: NOW - 10 * MIN })
    const heartbeat = callout(wrapper, 'heartbeat')
    expect(heartbeat.classes()).toContain('p-message-success')
    expect(heartbeat.text()).toContain('Last heartbeat 10 min ago — it looks stopped. A Minion that is still running registers again within 30 seconds, with its label and properties cleared.')
    expect(deleteDisabled(wrapper)).toBe(false)
  })

  it('follows the shared clock, so a heartbeat ages into deletable while the dialog is open', async () => {
    const { wrapper } = await mountDialog({ date: NOW - 90_000 })
    expect(deleteDisabled(wrapper)).toBe(true)
    await wrapper.setProps({ now: NOW + MIN })
    expect(callout(wrapper, 'heartbeat').text()).toContain('Last heartbeat 2 min ago — it looks stopped.')
    expect(deleteDisabled(wrapper)).toBe(false)
  })

  it('counts the alarms that go with the Minion', async () => {
    const many = callout((await mountDialog({}, 7)).wrapper, 'alarms')
    expect(many.classes()).toContain('p-message-error')
    expect(many.text()).toContain('Its alarms are deleted — 7 alarms raised through this Minion, such as traps and syslog it received, are removed with it.')
    const none = callout((await mountDialog({}, 0)).wrapper, 'alarms')
    expect(none.classes()).toContain('p-message-info')
    expect(none.text()).toBe('No alarms were raised through this Minion.')
    const unknown = callout((await mountDialog({}, null)).wrapper, 'alarms')
    expect(unknown.classes()).toContain('p-message-info')
    expect(unknown.text()).toBe('Its alarms could not be counted; any alarms raised through this Minion are removed with it.')
  })

  it('keeps Delete disabled and shows a loading state while the alarms are counted', async () => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
    const store = useMinionAdminStore()
    let resolveAlarms: (n: number) => void = () => {}
    vi.mocked(store.getAlarmCount).mockReturnValue(new Promise((resolve) => {
      resolveAlarms = resolve
    }))
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
    expect(requisition.text()).toContain('Its node stays until the Minions requisition is synchronized — the node is removed from the pending Minions requisition only, so it keeps being monitored until that requisition is synchronized.')
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
