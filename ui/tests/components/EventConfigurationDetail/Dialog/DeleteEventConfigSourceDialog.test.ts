import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises, VueWrapper } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import * as eventConfigService from '@/services/eventConfigService'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import DeleteEventConfigSourceDialog from '@/components/EventConfigurationDetail/Dialog/DeleteEventConfigSourceDialog.vue'

vi.mock('@featherds/dialog', () => ({
  FeatherDialog: {
    name: 'FeatherDialog',
    template: '<div><slot></slot><slot name="footer"></slot></div>',
    props: ['labels', 'modelValue'],
    emits: ['hidden']
  }
}))

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: mockShowSnackBar
  })
}))

describe('DeleteEventConfigSourceDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigDetailStore>

  const mockEventConfigSource = {
    id: 1,
    name: 'Test Source',
    vendor: 'Cisco',
    description: 'Mock description',
    enabled: true,
    eventCount: 5,
    fileOrder: 1,
    uploadedBy: 'Admin',
    createdTime: new Date(),
    lastModified: new Date()
  }

  beforeEach(async () => {
    vi.clearAllMocks()
    mockPush.mockClear()
    mockShowSnackBar.mockClear()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useEventConfigDetailStore()

    store.$state.deleteEventConfigSourceDialogState = {
      visible: true,
      eventConfigSource: { ...mockEventConfigSource }
    }

    wrapper = mount(DeleteEventConfigSourceDialog, {
      global: {
        plugins: [pinia],
        components: { FeatherButton, FeatherDialog }
      }
    })

    await flushPromises()
  })

  afterEach(() => {
    wrapper.unmount()
  })

  describe('Dialog Rendering', () => {
    it('renders dialog when visible is true', () => {
      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.exists()).toBe(true)
      expect(dialog.props('labels')).toEqual({
        title: 'Delete Event Configuration Source'
      })
    })

    it('hides dialog when visible is false', async () => {
      store.deleteEventConfigSourceDialogState.visible = false
      await wrapper.vm.$nextTick()
      expect(wrapper.findComponent(FeatherDialog).props('modelValue')).toBe(false)
    })

    it('renders Cancel and Delete buttons', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons.length).toBe(2)
      expect(buttons.at(0)?.text()).toContain('Cancel')
      expect(buttons.at(1)?.text()).toContain('Delete')
    })

    it('renders confirmation question', () => {
      expect(wrapper.text()).toContain('Are you sure you want to proceed?')
    })
  })

  describe('Message Content', () => {
    it('displays source name correctly', () => {
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('Test Source')
    })

    it('displays event count correctly', () => {
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('5')
      expect(modalBody.text()).toContain('events associated with it')
    })

    it('handles empty source name gracefully', async () => {
      store.$state.deleteEventConfigSourceDialogState.eventConfigSource = {
        ...mockEventConfigSource,
        name: ''
      }
      await wrapper.vm.$nextTick()
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(true)
    })

    it('handles zero event count', async () => {
      store.$state.deleteEventConfigSourceDialogState.eventConfigSource = {
        ...mockEventConfigSource,
        eventCount: 0
      }
      await wrapper.vm.$nextTick()
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('0')
    })

    it('handles null eventConfigSource gracefully', async () => {
      store.$state.deleteEventConfigSourceDialogState.eventConfigSource = null as any
      await wrapper.vm.$nextTick()
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(true)
    })
  })

  describe('Cancel Button', () => {
    it('calls hideDeleteEventConfigSourceDialog when Cancel is clicked', async () => {
      const cancelBtn = wrapper.findAllComponents(FeatherButton).at(0)
      expect(cancelBtn?.exists()).toBe(true)
      await cancelBtn?.trigger('click')
      expect(store.hideDeleteEventConfigSourceDialog).toHaveBeenCalled()
    })
  })

  describe('Delete Button - Success Cases', () => {
    it('calls deleteEventConfigSourceById and navigates on success', async () => {
      vi.spyOn(eventConfigService, 'deleteEventConfigSourceById').mockResolvedValue(true)

      const deleteBtn = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteBtn?.trigger('click')
      await flushPromises()

      expect(eventConfigService.deleteEventConfigSourceById).toHaveBeenCalledWith(1)
      expect(store.hideDeleteEventConfigSourceDialog).toHaveBeenCalled()
      expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration' })
    })

    it('does not show snackbar on successful deletion', async () => {
      vi.spyOn(eventConfigService, 'deleteEventConfigSourceById').mockResolvedValue(true)

      const deleteBtn = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteBtn?.trigger('click')
      await flushPromises()

      expect(mockShowSnackBar).not.toHaveBeenCalled()
    })
  })

  describe('Delete Button - Failure Cases', () => {
    it('shows snackbar and logs error when delete returns false', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.spyOn(eventConfigService, 'deleteEventConfigSourceById').mockResolvedValue(false)

      const deleteBtn = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteBtn?.trigger('click')
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Failed to delete event configuration source')
      expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Failed to delete event configuration source', error: true })
      expect(mockPush).not.toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })

    it('shows snackbar and logs error when delete throws', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const testError = new Error('Delete failed')
      vi.spyOn(eventConfigService, 'deleteEventConfigSourceById').mockRejectedValue(testError)

      const deleteBtn = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteBtn?.trigger('click')
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error deleting event configuration source:', testError)
      expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Failed to delete event configuration source', error: true })
      consoleErrorSpy.mockRestore()
    })

    it('does not call delete if eventConfigSource is null', async () => {
      store.$state.deleteEventConfigSourceDialogState.eventConfigSource = null as any
      await wrapper.vm.$nextTick()

      const mockDelete = vi.spyOn(eventConfigService, 'deleteEventConfigSourceById')
      const deleteBtn = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteBtn?.trigger('click')
      await flushPromises()

      expect(mockDelete).not.toHaveBeenCalled()
    })

    it('handles source with id 0 correctly', async () => {
      store.$state.deleteEventConfigSourceDialogState.eventConfigSource = {
        ...mockEventConfigSource,
        id: 0
      }
      vi.spyOn(eventConfigService, 'deleteEventConfigSourceById').mockResolvedValue(true)
      await wrapper.vm.$nextTick()

      const deleteBtn = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteBtn?.trigger('click')
      await flushPromises()

      expect(eventConfigService.deleteEventConfigSourceById).toHaveBeenCalledWith(0)
      expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration' })
    })
  })

  describe('Dialog Hidden Event', () => {
    it('calls hideDeleteEventConfigSourceDialog when dialog emits hidden event', async () => {
      const dialog = wrapper.findComponent(FeatherDialog)
      await dialog.vm.$emit('hidden')
      expect(store.hideDeleteEventConfigSourceDialog).toHaveBeenCalled()
    })
  })
})

