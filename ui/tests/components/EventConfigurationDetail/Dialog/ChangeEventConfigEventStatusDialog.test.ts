import { mount, VueWrapper, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import ChangeEventConfigEventStatusDialog from '@/components/EventConfigurationDetail/Dialog/ChangeEventConfigEventStatusDialog.vue'
import { VENDOR_OPENNMS } from '@/lib/utils'

// Stub the Common ConfirmationDialog wrapper so these tests exercise this
// component's logic via the wrapper's public API (props + ok/cancel events),
// independent of the underlying dialog library.
const ConfirmationDialogStub = {
  name: 'ConfirmationDialog',
  template: `<div class="confirmation-dialog"><div class="modal-body"><slot name="content"></slot></div><button class="action-btn" @click="$emit('ok')">{{ actionButtonText }}</button><button class="cancel-btn" @click="$emit('cancel')">{{ cancelButtonText || 'Cancel' }}</button></div>`,
  props: ['visible', 'title', 'actionButtonText', 'cancelButtonText']
}

describe('ChangeEventConfigEventStatusDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigDetailStore>

  beforeEach(() => {
    const pinia = createTestingPinia({
      createSpy: vi.fn
    })

    store = useEventConfigDetailStore(pinia)

    // mock store state
    store.changeEventConfigEventStatusDialogState.visible = true
    store.selectedSource = { id: 1, name: 'Test Source' } as any
    store.changeEventConfigEventStatusDialogState.eventConfigEvent = {
      id: 10,
      eventLabel: 'High CPU Usage',
      enabled: true,
      vendor: 'custom-vendor'
    } as any

    wrapper = mount(ChangeEventConfigEventStatusDialog, {
      global: {
        plugins: [pinia],
        stubs: {
          ConfirmationDialog: ConfirmationDialogStub
        }
      }
    })
  })

  afterEach(() => {
    wrapper.unmount()
    vi.clearAllMocks()
  })

  describe('Dialog Rendering', () => {
    it('renders dialog correctly with title', () => {
      const dialog = wrapper.findComponent({ name: 'ConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
      expect(dialog.props('title')).toBe('Change Event Configuration Event Status')
    })

    it('renders ConfirmationDialog with visible prop true when dialog is visible', () => {
      const dialog = wrapper.findComponent({ name: 'ConfirmationDialog' })
      expect(dialog.props('visible')).toBe(true)
    })

    it('renders ConfirmationDialog with visible prop false when dialog is hidden', async () => {
      store.changeEventConfigEventStatusDialogState.visible = false
      await wrapper.vm.$nextTick()
      const dialog = wrapper.findComponent({ name: 'ConfirmationDialog' })
      expect(dialog.props('visible')).toBe(false)
    })

    it('renders Cancel and Save buttons', () => {
      const cancelBtn = wrapper.find('.cancel-btn')
      const saveBtn = wrapper.find('.action-btn')
      expect(cancelBtn.exists()).toBe(true)
      expect(saveBtn.exists()).toBe(true)
      expect(cancelBtn.text().toLowerCase()).toContain('cancel')
      expect(saveBtn.text().toLowerCase()).toContain('save')
    })

    it('renders confirmation question', () => {
      expect(wrapper.text()).toContain('Are you sure you want to proceed?')
    })
  })

  describe('Message Content', () => {
    it('displays correct message for disabling an enabled event', () => {
      const message = wrapper.find('.modal-body p').html()
      expect(message).toContain('disable')
      expect(message).toContain('High CPU Usage')
      expect(message).toContain('Test Source')
    })

    it('displays correct message for enabling a disabled event', async () => {
      store.changeEventConfigEventStatusDialogState.eventConfigEvent = {
        id: 11,
        eventLabel: 'Network Down',
        enabled: false,
        vendor: 'custom-vendor'
      } as any
      await wrapper.vm.$nextTick()
      const message = wrapper.find('.modal-body p').html()
      expect(message).toContain('enable')
      expect(message).toContain('Network Down')
      expect(message).toContain('Test Source')
    })

    it('handles empty eventLabel gracefully', async () => {
      store.changeEventConfigEventStatusDialogState.eventConfigEvent = {
        id: 12,
        eventLabel: '',
        enabled: true,
        vendor: 'custom-vendor'
      } as any
      await wrapper.vm.$nextTick()
      const message = wrapper.find('.modal-body p').html()
      expect(message).toContain('disable')
      expect(message).toContain('Test Source')
    })

    it('handles empty sourceName gracefully', async () => {
      store.selectedSource = { id: 1, name: '' } as any
      await wrapper.vm.$nextTick()
      const message = wrapper.find('.modal-body p').html()
      expect(message).toContain('disable')
      expect(message).toContain('High CPU Usage')
    })

    it('handles null selectedSource gracefully', async () => {
      store.selectedSource = null as any
      await wrapper.vm.$nextTick()
      const message = wrapper.find('.modal-body p').html()
      expect(message).toContain('disable')
      expect(message).toContain('High CPU Usage')
    })
  })

  describe('OpenNMS Vendor Warning', () => {
    it('shows warning note for OpenNMS vendor events', async () => {
      store.changeEventConfigEventStatusDialogState.eventConfigEvent = {
        id: 10,
        eventLabel: 'OpenNMS Event',
        enabled: true,
        vendor: VENDOR_OPENNMS
      } as any
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('OpenNMS event configuration event may effect the OpenNMS system')
    })

    it('does not show warning note for non-OpenNMS vendor events', () => {
      expect(wrapper.text()).not.toContain('OpenNMS event configuration event may effect the OpenNMS system')
    })

    it('does not show warning note when vendor is undefined', async () => {
      store.changeEventConfigEventStatusDialogState.eventConfigEvent = {
        id: 10,
        eventLabel: 'Test Event',
        enabled: true
      } as any
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).not.toContain('OpenNMS event configuration event may effect the OpenNMS system')
    })
  })

  describe('Cancel Button', () => {
    it('calls hideChangeEventConfigEventStatusDialog on Cancel click', async () => {
      const cancelBtn = wrapper.find('.cancel-btn')

      await cancelBtn.trigger('click')
      expect(store.hideChangeEventConfigEventStatusDialog).toHaveBeenCalledTimes(1)
    })
  })

  describe('Save Button - Change Status', () => {
    it('calls disableEventConfigEvent when event is enabled and Save clicked', async () => {
      const saveBtn = wrapper.find('.action-btn')

      await saveBtn.trigger('click')
      await flushPromises()

      expect(store.disableEventConfigEvent).toHaveBeenCalledWith(10)
      expect(store.hideChangeEventConfigEventStatusDialog).toHaveBeenCalled()
    })

    it('calls enableEventConfigEvent when event is disabled and Save clicked', async () => {
      store.changeEventConfigEventStatusDialogState.eventConfigEvent = {
        id: 11,
        eventLabel: 'Network Down',
        enabled: false
      } as any
      await wrapper.vm.$nextTick()

      const saveBtn = wrapper.find('.action-btn')

      await saveBtn.trigger('click')
      await flushPromises()

      expect(store.enableEventConfigEvent).toHaveBeenCalledWith(11)
      expect(store.hideChangeEventConfigEventStatusDialog).toHaveBeenCalled()
    })

    it('handles missing eventConfigEvent safely', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      store.changeEventConfigEventStatusDialogState.eventConfigEvent = null as any
      await wrapper.vm.$nextTick()

      const saveBtn = wrapper.find('.action-btn')

      await saveBtn.trigger('click')
      await flushPromises()

      expect(store.disableEventConfigEvent).not.toHaveBeenCalled()
      expect(store.enableEventConfigEvent).not.toHaveBeenCalled()
      expect(consoleErrorSpy).toHaveBeenCalledWith('No event configuration event selected')
      consoleErrorSpy.mockRestore()
    })

    it('handles event with id 0 correctly', async () => {
      store.changeEventConfigEventStatusDialogState.eventConfigEvent = {
        id: 0,
        eventLabel: 'Zero ID Event',
        enabled: true
      } as any
      await wrapper.vm.$nextTick()

      const saveBtn = wrapper.find('.action-btn')

      await saveBtn.trigger('click')
      await flushPromises()

      expect(store.disableEventConfigEvent).toHaveBeenCalledWith(0)
      expect(store.hideChangeEventConfigEventStatusDialog).toHaveBeenCalled()
    })
  })

  describe('Error Handling', () => {
    it('logs error when disableEventConfigEvent throws', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const testError = new Error('Disable failed')
      store.disableEventConfigEvent = vi.fn().mockRejectedValue(testError)

      const saveBtn = wrapper.find('.action-btn')

      await saveBtn.trigger('click')
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error changing event configuration event status:', testError)
      consoleErrorSpy.mockRestore()
    })

    it('logs error when enableEventConfigEvent throws', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      store.changeEventConfigEventStatusDialogState.eventConfigEvent = {
        id: 11,
        eventLabel: 'Network Down',
        enabled: false
      } as any
      const testError = new Error('Enable failed')
      store.enableEventConfigEvent = vi.fn().mockRejectedValue(testError)
      await wrapper.vm.$nextTick()

      const saveBtn = wrapper.find('.action-btn')

      await saveBtn.trigger('click')
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error changing event configuration event status:', testError)
      consoleErrorSpy.mockRestore()
    })
  })

  describe('Dialog Cancel Event', () => {
    it('calls hideChangeEventConfigEventStatusDialog when dialog emits cancel event', async () => {
      const dialog = wrapper.findComponent({ name: 'ConfirmationDialog' })
      await dialog.vm.$emit('cancel')
      expect(store.hideChangeEventConfigEventStatusDialog).toHaveBeenCalled()
    })
  })
})
