import { mount, VueWrapper, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherButton } from '@featherds/button'
import ChangeEventConfigSourceStatusDialog from '@/components/EventConfigurationDetail/Dialog/ChangeEventConfigSourceStatusDialog.vue'
import { VENDOR_OPENNMS } from '@/lib/utils'

// mock feather components so we can actually render the buttons
vi.mock('@featherds/button', () => ({
  FeatherButton: {
    template: '<button @click="$emit(\'click\')"><slot /></button>'
  }
}))

vi.mock('@featherds/dialog', () => ({
  FeatherDialog: {
    props: ['modelValue', 'labels', 'hideClose'],
    emits: ['update:modelValue', 'hidden'],
    template: `
      <div data-test="feather-dialog">
        <slot></slot>
        <slot name="footer"></slot>
      </div>
    `
  }
}))

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
        plugins: [pinia]
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
      expect(dialog.props('labels')).toEqual({ title: 'Change Event Configuration Source Status' })
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
      const cancelBtn = wrapper.findAllComponents(FeatherButton).find((btn) => btn.text().toLowerCase() === 'cancel')
      expect(cancelBtn).toBeTruthy()
      await cancelBtn!.trigger('click')
      expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
    })
  })

  describe('Save Button - Change Status', () => {
    it('calls disableEventConfigSource when source is enabled and Save clicked', async () => {
      const saveBtn = wrapper.findAll('button').find((btn) =>
        btn.text().toLowerCase().includes('save')
      )
      expect(saveBtn).toBeTruthy()

      await saveBtn!.trigger('click')
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

      const saveBtn = wrapper.findAll('button').find((btn) =>
        btn.text().toLowerCase().includes('save')
      )
      expect(saveBtn).toBeTruthy()

      await saveBtn!.trigger('click')
      await flushPromises()

      expect(store.enableEventConfigSource).toHaveBeenCalledWith(2)
      expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
    })

    it('handles missing eventConfigSource safely', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      store.changeEventConfigSourceStatusDialogState.eventConfigSource = null as any
      await wrapper.vm.$nextTick()

      const saveBtn = wrapper.findAll('button').find((btn) =>
        btn.text().toLowerCase().includes('save')
      )

      await saveBtn!.trigger('click')
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

      const saveBtn = wrapper.findAll('button').find((btn) =>
        btn.text().toLowerCase().includes('save')
      )

      await saveBtn!.trigger('click')
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

      const saveBtn = wrapper.findAll('button').find((btn) =>
        btn.text().toLowerCase().includes('save')
      )

      await saveBtn!.trigger('click')
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

      const saveBtn = wrapper.findAll('button').find((btn) =>
        btn.text().toLowerCase().includes('save')
      )

      await saveBtn!.trigger('click')
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error changing event configuration event status:', testError)
      consoleErrorSpy.mockRestore()
    })
  })

  describe('Dialog Hidden Event', () => {
    it('calls hideChangeEventConfigSourceStatusDialog when dialog emits hidden event', async () => {
      const dialog = wrapper.findComponent(FeatherDialog)
      await dialog.vm.$emit('hidden')
      expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
    })
  })
})
