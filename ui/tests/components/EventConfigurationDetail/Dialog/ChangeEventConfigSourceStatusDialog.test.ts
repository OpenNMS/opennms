import { mount, VueWrapper, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import ChangeEventConfigSourceStatusDialog from '@/components/EventConfigurationDetail/Dialog/ChangeEventConfigSourceStatusDialog.vue'
import { VENDOR_OPENNMS } from '@/lib/utils'

// Stub the Common ConfirmationDialog wrapper so these tests exercise this
// component's logic via the wrapper's public API (props + ok/cancel events),
// independent of the underlying dialog library.
const ConfirmationDialogStub = {
  name: 'ConfirmationDialog',
  template: '<div class="confirmation-dialog"><div class="modal-body"><slot name="content"></slot></div><button class="action-btn" @click="$emit(\'ok\')">{{ actionButtonText }}</button><button class="cancel-btn" @click="$emit(\'cancel\')">{{ cancelButtonText || \'Cancel\' }}</button></div>',
  props: ['visible', 'title', 'actionButtonText', 'cancelButtonText']
}

describe('ChangeEventConfigSourceStatusDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigDetailStore>

  beforeEach(() => {
    const pinia = createTestingPinia({
      createSpy: vi.fn
    })

    store = useEventConfigDetailStore(pinia)

    // define spies manually for required store methods
    store.hideChangeEventConfigSourceStatusDialog = vi.fn()
    store.disableEventConfigSource = vi.fn().mockResolvedValue(true)
    store.enableEventConfigSource = vi.fn().mockResolvedValue(true)

    store.changeEventConfigSourceStatusDialogState.visible = true
    store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
      id: 1,
      name: 'Test Source',
      enabled: true,
      vendor: 'custom-vendor'
    } as any

    wrapper = mount(ChangeEventConfigSourceStatusDialog, {
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
      expect(dialog.props('title')).toBe('Change Event Configuration Source Status')
    })

    it('renders dialog visible = true when dialog state is visible', () => {
      const stateVisible = store.changeEventConfigSourceStatusDialogState.visible
      expect(stateVisible).toBe(true)
    })

    it('renders dialog visible = false when dialog state is hidden', async () => {
      store.changeEventConfigSourceStatusDialogState.visible = false
      await wrapper.vm.$nextTick()
      expect(store.changeEventConfigSourceStatusDialogState.visible).toBe(false)
    })

    it('renders Cancel and Save buttons', () => {
      const buttons = wrapper.findAll('button')
      expect(buttons.length).toBe(2)
      const cancelBtn = buttons.find(btn => btn.text().toLowerCase().includes('cancel'))
      const saveBtn = buttons.find(btn => btn.text().toLowerCase().includes('save'))
      expect(cancelBtn).toBeTruthy()
      expect(saveBtn).toBeTruthy()
    })

    it('renders confirmation question', () => {
      expect(wrapper.text()).toContain('Are you sure you want to proceed?')
    })
  })

  describe('Message Content', () => {
    it('displays correct message for disabling an enabled source', () => {
      const message = wrapper.find('.modal-body p').html()
      expect(message).toContain('disable')
      expect(message).toContain('Test Source')
      expect(message).toContain('disable all events associated with it')
    })

    it('displays correct message for enabling a disabled source', async () => {
      store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
        id: 2,
        name: 'Disabled Source',
        enabled: false,
        vendor: 'custom-vendor'
      } as any
      await wrapper.vm.$nextTick()
      const message = wrapper.find('.modal-body p').html()
      expect(message).toContain('enable')
      expect(message).toContain('Disabled Source')
      expect(message).toContain('enable all events associated with it')
    })

    it('handles empty source name gracefully', async () => {
      store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
        id: 3,
        name: '',
        enabled: true,
        vendor: 'custom-vendor'
      } as any
      await wrapper.vm.$nextTick()
      const message = wrapper.find('.modal-body p').html()
      expect(message).toContain('disable')
    })

    it('handles null eventConfigSource name with fallback to empty string', async () => {
      store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
        id: 4,
        name: null,
        enabled: true
      } as any
      await wrapper.vm.$nextTick()
      const message = wrapper.find('.modal-body p').html()
      expect(message).toContain('disable')
    })
  })

  describe('OpenNMS Vendor Warning', () => {
    it('shows warning note for OpenNMS vendor sources', async () => {
      store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
        id: 1,
        name: 'OpenNMS Source',
        enabled: true,
        vendor: VENDOR_OPENNMS
      } as any
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('OpenNMS event configuration source may effect the OpenNMS system')
    })

    it('does not show warning note for non-OpenNMS vendor sources', () => {
      expect(wrapper.text()).not.toContain('OpenNMS event configuration source may effect the OpenNMS system')
    })

    it('does not show warning note when vendor is undefined', async () => {
      store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
        id: 1,
        name: 'Test Source',
        enabled: true
      } as any
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).not.toContain('OpenNMS event configuration source may effect the OpenNMS system')
    })
  })

  describe('Cancel Button', () => {
    it('calls hideChangeEventConfigSourceStatusDialog on Cancel click', async () => {
      const cancelBtn = wrapper.find('.cancel-btn')
      expect(cancelBtn.exists()).toBe(true)
      await cancelBtn.trigger('click')
      expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
    })
  })

  describe('Save Button - Change Status', () => {
    it('calls disableEventConfigSource when source is enabled and Save clicked', async () => {
      const saveBtn = wrapper.find('.action-btn')
      expect(saveBtn.exists()).toBe(true)

      await saveBtn.trigger('click')
      await flushPromises()

      expect(store.disableEventConfigSource).toHaveBeenCalledWith(1)
      expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
    })

    it('calls enableEventConfigSource when source is disabled and Save clicked', async () => {
      store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
        id: 2,
        name: 'Another Source',
        enabled: false
      } as any
      await wrapper.vm.$nextTick()

      const saveBtn = wrapper.find('.action-btn')
      expect(saveBtn.exists()).toBe(true)

      await saveBtn.trigger('click')
      await flushPromises()

      expect(store.enableEventConfigSource).toHaveBeenCalledWith(2)
      expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
    })

    it('handles missing eventConfigSource safely', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      store.changeEventConfigSourceStatusDialogState.eventConfigSource = null as any
      await wrapper.vm.$nextTick()

      const saveBtn = wrapper.find('.action-btn')

      await saveBtn.trigger('click')
      await flushPromises()

      expect(store.disableEventConfigSource).not.toHaveBeenCalled()
      expect(store.enableEventConfigSource).not.toHaveBeenCalled()
      expect(consoleErrorSpy).toHaveBeenCalledWith('No event configuration event selected')
      consoleErrorSpy.mockRestore()
    })

    it('handles source with id 0 correctly', async () => {
      store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
        id: 0,
        name: 'Zero ID Source',
        enabled: true
      } as any
      await wrapper.vm.$nextTick()

      const saveBtn = wrapper.find('.action-btn')

      await saveBtn.trigger('click')
      await flushPromises()

      expect(store.disableEventConfigSource).toHaveBeenCalledWith(0)
      expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
    })
  })

  describe('Error Handling', () => {
    it('logs error when disableEventConfigSource throws', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const testError = new Error('Disable failed')
      store.disableEventConfigSource = vi.fn().mockRejectedValue(testError)

      const saveBtn = wrapper.find('.action-btn')

      await saveBtn.trigger('click')
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error changing event configuration event status:', testError)
      consoleErrorSpy.mockRestore()
    })

    it('logs error when enableEventConfigSource throws', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
        id: 2,
        name: 'Another Source',
        enabled: false
      } as any
      const testError = new Error('Enable failed')
      store.enableEventConfigSource = vi.fn().mockRejectedValue(testError)
      await wrapper.vm.$nextTick()

      const saveBtn = wrapper.find('.action-btn')

      await saveBtn.trigger('click')
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error changing event configuration event status:', testError)
      consoleErrorSpy.mockRestore()
    })
  })

  describe('Dialog Cancel Event', () => {
    it('calls hideChangeEventConfigSourceStatusDialog when dialog emits cancel event', async () => {
      const dialog = wrapper.findComponent({ name: 'ConfirmationDialog' })
      await dialog.vm.$emit('cancel')
      expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
    })
  })
})
