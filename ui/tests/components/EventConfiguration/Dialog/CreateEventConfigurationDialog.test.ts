import CreateEventConfigurationDialog from '@/components/EventConfiguration/Dialog/CreateEventConfigurationDialog.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

const mockAddEventConfigSource = vi.fn()
vi.mock('@/services/eventConfigService', () => ({
  addEventConfigSource: (...args: any[]) => mockAddEventConfigSource(...args)
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: mockShowSnackBar })
}))

// Stub the teleporting PrimeVue Dialog so its slots render inline; the real
// InputText/Button still render so validation/disabled wiring is exercised.
const DialogStub = {
  name: 'Dialog',
  template: '<div class="dialog-stub" v-if="visible"><slot></slot><div class="dialog-footer"><slot name="footer"></slot></div></div>',
  props: ['visible', 'header', 'modal', 'draggable', 'closable', 'closeOnEscape'],
  emits: ['update:visible']
}

describe('CreateEventConfigurationDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigStore>

  const mountComponent = () => mount(CreateEventConfigurationDialog, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs: { Dialog: DialogStub }
    }
  })

  const buttonByLabel = (label: string) =>
    wrapper.findAllComponents(Button).find(b => b.props('label') === label)

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountComponent()
    store = useEventConfigStore()
    store.createEventConfigSourceDialogState = { visible: true } as any
    store.hideCreateEventConfigSourceDialog = vi.fn()
    await nextTick()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Initial Rendering', () => {
    it('renders the dialog with the title', () => {
      expect(wrapper.vm.labels.title).toBe('Create New Event Source')
    })

    it('renders name and vendor inputs', () => {
      expect(wrapper.findAllComponents(InputText)).toHaveLength(2)
    })

    it('disables Create while required fields are empty', () => {
      expect((buttonByLabel('Create')?.element as HTMLButtonElement).disabled).toBe(true)
    })

    it('reports validation errors when fields are empty', () => {
      expect(wrapper.vm.error.name).toBe('Configuration name is required.')
      expect(wrapper.vm.error.vendor).toBe('Vendor is required.')
    })

    it('clears errors once both fields are filled', async () => {
      wrapper.vm.configName = 'My Source'
      wrapper.vm.vendor = 'Cisco'
      await nextTick()
      expect(wrapper.vm.error).toBeNull()
      expect((buttonByLabel('Create')?.element as HTMLButtonElement).disabled).toBe(false)
    })
  })

  describe('Save', () => {
    beforeEach(async () => {
      wrapper.vm.configName = 'My Source'
      wrapper.vm.vendor = 'Cisco'
      await nextTick()
    })

    it('shows the success view on a 201 response', async () => {
      mockAddEventConfigSource.mockResolvedValue({ id: 42, name: 'My Source', fileOrder: 0, status: 201 })
      await wrapper.vm.handleSave()
      await flushPromises()
      expect(mockAddEventConfigSource).toHaveBeenCalledWith('My Source', 'Cisco', '')
      expect(wrapper.vm.successMessage).toBe(true)
      expect(wrapper.html()).toContain('created successfully')
      expect(buttonByLabel('View Source')).toBeDefined()
    })

    it.each([
      [409, 'already exists'],
      [400, 'Invalid request'],
      [500, 'Failed to create']
    ])('shows an error snackbar on a %s response', async (code, fragment) => {
      mockAddEventConfigSource.mockResolvedValue(code)
      await wrapper.vm.handleSave()
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith(
        expect.objectContaining({ error: true, msg: expect.stringContaining(fragment) })
      )
      expect(wrapper.vm.successMessage).toBe(false)
    })

    it('shows an error snackbar when the service throws', async () => {
      // The component logs the caught error; suppress the expected noise.
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      mockAddEventConfigSource.mockRejectedValue(new Error('boom'))
      await wrapper.vm.handleSave()
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })

    it('does nothing when the name is blank', async () => {
      wrapper.vm.configName = '   '
      await nextTick()
      await wrapper.vm.handleSave()
      expect(mockAddEventConfigSource).not.toHaveBeenCalled()
    })
  })

  describe('Cancel / navigation', () => {
    it('resets the form and hides on cancel', async () => {
      wrapper.vm.configName = 'X'
      wrapper.vm.vendor = 'Y'
      await nextTick()
      wrapper.vm.handleCancel()
      expect(wrapper.vm.configName).toBe('')
      expect(wrapper.vm.vendor).toBe('')
      expect(store.hideCreateEventConfigSourceDialog).toHaveBeenCalled()
    })

    it('navigates to the created source detail on View Source', () => {
      wrapper.vm.newId = 99
      wrapper.vm.visitCreatedEventConfigSource()
      expect(store.hideCreateEventConfigSourceDialog).toHaveBeenCalled()
      expect(mockPush).toHaveBeenCalledWith({
        name: 'Event Configuration Detail',
        params: { id: 99 }
      })
    })

    it('falls back to the list when no new id is present', () => {
      // The component logs the missing id; suppress the expected noise.
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      wrapper.vm.newId = 0
      wrapper.vm.visitCreatedEventConfigSource()
      expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration' })
      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })
  })
})
