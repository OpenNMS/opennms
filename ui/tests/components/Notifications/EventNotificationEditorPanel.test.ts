import EventNotificationEditorPanel from '@/components/Notifications/EventNotificationEditorPanel.vue'
import API from '@/services'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { EventNotification } from '@/types/notificationConfig'
import { createFailureResult, createResultWithPayload, createSuccessResponse } from '@/types/validation'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBar } = vi.hoisted(() => ({ showSnackBar: vi.fn() }))
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar })
}))

vi.mock('@/services', () => ({
  default: {
    getNotificationServices: vi.fn(),
    validateNotificationRule: vi.fn(),
    searchEventConfUeis: vi.fn()
  }
}))

const existing: EventNotification = {
  name: 'nodeDown',
  status: 'on',
  uei: 'uei.opennms.org/nodes/nodeDown',
  destinationPath: 'Email-Admin',
  'text-message': 'Node %nodelabel% is down.',
  rule: { value: 'IPADDR IPLIKE *.*.*.*' }
}

const activeTab = (wrapper: VueWrapper) =>
  wrapper.find('[data-test="event-notification-tabs"] [role="tab"][aria-selected="true"]').text()

const openTab = async (wrapper: VueWrapper, testId: string) => {
  await wrapper.find(`[data-test="${testId}"]`).trigger('click')
  await flushPromises()
}

const save = async (wrapper: VueWrapper) => {
  await wrapper.find('[data-test="save-button"]').trigger('click')
  await flushPromises()
}

describe('EventNotificationEditorPanel', () => {
  let wrapper: VueWrapper
  let store: ReturnType<typeof useNotificationConfigStore>

  const mountPanel = (notification: EventNotification | null) => {
    wrapper = mount(EventNotificationEditorPanel, {
      props: { notification },
      attachTo: document.body,
      global: {
        plugins: [PrimeVue, createTestingPinia({ stubActions: true })]
      }
    })
    store = useNotificationConfigStore()
    store.destinationPaths = [{ name: 'Email-Admin', target: [] }]
    return wrapper
  }

  beforeEach(() => {
    showSnackBar.mockClear()
    vi.mocked(API.getNotificationServices).mockResolvedValue(createResultWithPayload(true, '', ['ICMP', 'SNMP']))
    vi.mocked(API.validateNotificationRule).mockResolvedValue(createResultWithPayload(true, '', { valid: true, matchCount: 1, matches: [] }))
  })

  afterEach(() => {
    wrapper?.unmount()
    vi.clearAllMocks()
  })

  describe('a failed save lands on the tab showing the error', () => {
    it('switches from Advanced to Event when required Event fields are missing', async () => {
      mountPanel(null)
      await openTab(wrapper, 'tab-advanced')
      expect(activeTab(wrapper)).toBe('Advanced')

      await save(wrapper)

      expect(activeTab(wrapper)).toBe('Event')
      expect(wrapper.text()).toContain('Name is required.')
      expect(wrapper.text()).toContain('Event UEI is required.')
      // the cheap checks failed, so the rule round-trip is skipped and nothing is saved
      expect(API.validateNotificationRule).not.toHaveBeenCalled()
      expect(store.addEventNotification).not.toHaveBeenCalled()
    })

    it('switches from Event to Filter Rules when only the rule is invalid', async () => {
      vi.mocked(API.validateNotificationRule).mockResolvedValue(
        createResultWithPayload(true, '', { valid: false, error: 'bad column', matchCount: 0, matches: [] }))
      mountPanel(existing)
      expect(activeTab(wrapper)).toBe('Event')

      await save(wrapper)

      expect(activeTab(wrapper)).toBe('Filter Rules')
      expect(wrapper.find('[data-test="rule-error"]').text()).toContain('bad column')
      expect(store.updateEventNotification).not.toHaveBeenCalled()
    })

    it('stays on the current tab when validation passes', async () => {
      mountPanel(existing)
      vi.mocked(store.updateEventNotification).mockResolvedValue(createSuccessResponse())
      await openTab(wrapper, 'tab-advanced')

      await save(wrapper)

      expect(activeTab(wrapper)).toBe('Advanced')
    })
  })

  describe('save feedback', () => {
    it('saves an edit under its original name, reports success and closes', async () => {
      mountPanel(existing)
      vi.mocked(store.updateEventNotification).mockResolvedValue(createSuccessResponse())

      await save(wrapper)

      expect(store.updateEventNotification).toHaveBeenCalledWith('nodeDown', expect.objectContaining({ name: 'nodeDown' }))
      expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Event notification \'nodeDown\' updated.' })
      expect(wrapper.emitted('close')).toHaveLength(1)
    })

    it('shows the server reason as an error, keeps the banner and stays open on a rejected save', async () => {
      mountPanel(existing)
      vi.mocked(store.updateEventNotification).mockResolvedValue(createFailureResult('Destination path does not exist.'))

      await save(wrapper)

      expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Destination path does not exist.', error: true })
      expect(wrapper.find('[data-test="save-error"]').exists()).toBe(true)
      expect(wrapper.emitted('close')).toBeUndefined()
    })

    it('says when the rule cannot be validated but still saves (the server has the final say)', async () => {
      vi.mocked(API.validateNotificationRule).mockResolvedValue(createResultWithPayload(false, 'Failed to validate the rule.'))
      mountPanel(existing)
      vi.mocked(store.updateEventNotification).mockResolvedValue(createSuccessResponse())

      await save(wrapper)

      expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Failed to validate the rule.', error: true })
      expect(store.updateEventNotification).toHaveBeenCalled()
    })

    it('reports a failed list refresh after a successful save', async () => {
      mountPanel(existing)
      vi.mocked(store.updateEventNotification).mockResolvedValue({ ...createSuccessResponse(), errors: ['Failed to load event notifications.'] })

      await save(wrapper)

      expect(showSnackBar.mock.calls).toEqual([
        [{ msg: 'Event notification \'nodeDown\' updated.' }],
        [{ msg: 'Failed to load event notifications.', error: true }]
      ])
      expect(wrapper.emitted('close')).toHaveLength(1)
    })
  })

  it('shows a failed service-list load as an error', async () => {
    vi.mocked(API.getNotificationServices).mockResolvedValue(createResultWithPayload(false, 'Failed to load the service list.'))

    mountPanel(existing)
    await flushPromises()

    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Failed to load the service list.', error: true })
  })
})
