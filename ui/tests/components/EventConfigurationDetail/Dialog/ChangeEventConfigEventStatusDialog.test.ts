import { mount, VueWrapper, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherButton } from '@featherds/button'
import ChangeEventConfigEventStatusDialog from '@/components/EventConfigurationDetail/Dialog/ChangeEventConfigEventStatusDialog.vue'
import { VENDOR_OPENNMS } from '@/lib/utils'

vi.mock('@featherds/dialog', () => ({
  FeatherDialog: {
    name: 'FeatherDialog',
    template: '<div><slot></slot><slot name="footer"></slot></div>',
    props: ['labels', 'modelValue']
  }
}))

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
        components: {
          FeatherDialog,
          FeatherButton
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
      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.exists()).toBe(true)
      expect(dialog.props('labels')).toEqual({ title: 'Change Event Configuration Event Status' })
    })

    it('renders FeatherDialog with visible prop true when dialog is visible', () => {
      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('modelValue')).toBe(true)
    })

    it('renders FeatherDialog with visible prop false when dialog is hidden', async () => {
      store.changeEventConfigEventStatusDialogState.visible = false
      await wrapper.vm.$nextTick()
      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('modelValue')).toBe(false)
    })

    it('renders Cancel and Save buttons', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons.length).toBe(2)
      const cancelBtn = buttons.find((btn) => btn.text().toLowerCase().includes('cancel'))
      const saveBtn = buttons.find((btn) => btn.text().toLowerCase().includes('save'))
      expect(cancelBtn).toBeTruthy()
      expect(saveBtn).toBeTruthy()
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
      const cancelBtn = wrapper
        .findAllComponents(FeatherButton)
        .find((btn) => btn.text().toLowerCase().includes('cancel'))

      await cancelBtn?.trigger('click')
      expect(store.hideChangeEventConfigEventStatusDialog).toHaveBeenCalledTimes(1)
    })
  })

  describe('Save Button - Change Status', () => {
    it('calls disableEventConfigEvent when event is enabled and Save clicked', async () => {
      const saveBtn = wrapper.findAllComponents(FeatherButton).find((btn) => btn.text().toLowerCase().includes('save'))

      await saveBtn?.trigger('click')
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

      const saveBtn = wrapper.findAllComponents(FeatherButton).find((btn) => btn.text().toLowerCase().includes('save'))

      await saveBtn?.trigger('click')
      await flushPromises()

      expect(store.enableEventConfigEvent).toHaveBeenCalledWith(11)
      expect(store.hideChangeEventConfigEventStatusDialog).toHaveBeenCalled()
    })

    it('handles missing eventConfigEvent safely', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      store.changeEventConfigEventStatusDialogState.eventConfigEvent = null as any
      await wrapper.vm.$nextTick()

      const saveBtn = wrapper.findAllComponents(FeatherButton).find((btn) => btn.text().toLowerCase().includes('save'))

      await saveBtn?.trigger('click')
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

      const saveBtn = wrapper.findAllComponents(FeatherButton).find((btn) => btn.text().toLowerCase().includes('save'))

      await saveBtn?.trigger('click')
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

      const saveBtn = wrapper.findAllComponents(FeatherButton).find((btn) => btn.text().toLowerCase().includes('save'))

      await saveBtn?.trigger('click')
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

      const saveBtn = wrapper.findAllComponents(FeatherButton).find((btn) => btn.text().toLowerCase().includes('save'))

      await saveBtn?.trigger('click')
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error changing event configuration event status:', testError)
      consoleErrorSpy.mockRestore()
    })
  })

  describe('Dialog Hidden Event', () => {
    it('calls hideChangeEventConfigEventStatusDialog when dialog emits hidden event', async () => {
      const dialog = wrapper.findComponent(FeatherDialog)
      await dialog.vm.$emit('hidden')
      expect(store.hideChangeEventConfigEventStatusDialog).toHaveBeenCalled()
    })
  })
})

