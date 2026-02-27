import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises, VueWrapper } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import * as eventConfigService from '@/services/eventConfigService'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import DeleteEventConfigEventDialog from '@/components/EventConfigurationDetail/Dialog/DeleteEventConfigEventDialog.vue'
import type { EventConfigSource } from '@/types/eventConfig'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

vi.mock('@featherds/dialog', () => ({
  FeatherDialog: {
    name: 'FeatherDialog',
    template: '<div><slot></slot><slot name="footer"></slot></div>',
    props: ['labels', 'modelValue'],
    emits: ['hidden']
  }
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: mockShowSnackBar
  })
}))

describe('DeleteEventConfigEventDialog', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigDetailStore>

  const mockSource: EventConfigSource = {
    id: 5,
    name: 'Test Source',
    vendor: 'Cisco',
    description: 'Sample Source',
    enabled: true,
    eventCount: 3,
    fileOrder: 1,
    uploadedBy: 'admin',
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

    store.$state = {
      deleteEventConfigEventDialogState: {
        visible: true,
        eventConfigEvent: {
          id: 10,
          eventLabel: 'Test Event'
        }
      },
      selectedSource: { ...mockSource },
      eventsPagination: { page: 1, pageSize: 10, total: 0 },
      isLoading: false
    } as any

    wrapper = mount(DeleteEventConfigEventDialog, {
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
    it('renders the dialog when visible is true', () => {
      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.exists()).toBe(true)
      expect(dialog.props('labels')).toEqual({
        title: 'Delete Event Configuration Event'
      })
    })

    it('hides the dialog when visible is false', async () => {
      store.$state.deleteEventConfigEventDialogState.visible = false
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
    it('displays correct event and source names', () => {
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('Test Event')
      expect(modalBody.text()).toContain('Test Source')
    })

    it('handles empty eventLabel gracefully', async () => {
      store.$state.deleteEventConfigEventDialogState.eventConfigEvent = {
        id: 10,
        eventLabel: ''
      } as any
      await wrapper.vm.$nextTick()
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('Test Source')
    })

    it('handles null eventConfigEvent gracefully', async () => {
      store.$state.deleteEventConfigEventDialogState.eventConfigEvent = null as any
      await wrapper.vm.$nextTick()
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(true)
    })

    it('handles null selectedSource gracefully', async () => {
      store.$state.selectedSource = null as any
      await wrapper.vm.$nextTick()
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(true)
    })
  })

  describe('Cancel Button', () => {
    it('calls hideDeleteEventConfigEventDialog when Cancel is clicked', async () => {
      const cancelButton = wrapper.findAllComponents(FeatherButton).at(0)
      expect(cancelButton?.exists()).toBe(true)
      await cancelButton?.trigger('click')
      expect(store.hideDeleteEventConfigEventDialog).toHaveBeenCalled()
    })
  })

  describe('Delete Button - Success Cases', () => {
    it('calls deleteEventConfigEventBySourceId and handles success', async () => {
      vi.spyOn(eventConfigService, 'deleteEventConfigEventBySourceId').mockResolvedValue(true)
      vi.spyOn(eventConfigService, 'filterEventConfigEvents').mockResolvedValue({
        events: [],
        totalRecords: 0
      })

      const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteButton?.trigger('click')
      await flushPromises()

      expect(eventConfigService.deleteEventConfigEventBySourceId).toHaveBeenCalledWith(5, [10])
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Event configuration event deleted successfully',
        error: false
      })
      expect(store.hideDeleteEventConfigEventDialog).toHaveBeenCalled()
    })

    it('navigates to Event Configuration when eventCount is 0', async () => {
      store.$state.selectedSource = { ...mockSource, eventCount: 0 }
      vi.spyOn(eventConfigService, 'deleteEventConfigEventBySourceId').mockResolvedValue(true)

      const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteButton?.trigger('click')
      await flushPromises()

      expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration' })
    })

    it('fetches events when eventCount > 0 after successful delete', async () => {
      vi.spyOn(eventConfigService, 'deleteEventConfigEventBySourceId').mockResolvedValue(true)
      vi.spyOn(eventConfigService, 'filterEventConfigEvents').mockResolvedValue({
        events: [],
        totalRecords: 0
      })

      const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteButton?.trigger('click')
      await flushPromises()

      expect(store.fetchEventsBySourceId).toHaveBeenCalled()
      expect(mockPush).not.toHaveBeenCalled()
    })
  })

  describe('Delete Button - Failure Cases', () => {
    it('shows snackbar error when delete returns false', async () => {
      vi.spyOn(eventConfigService, 'deleteEventConfigEventBySourceId').mockResolvedValue(false)

      const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteButton?.trigger('click')
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Failed to delete event configuration event', error: true })
      expect(store.hideDeleteEventConfigEventDialog).not.toHaveBeenCalled()
    })

    it('shows snackbar error when delete throws an error', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const testError = new Error('Delete failed')
      vi.spyOn(eventConfigService, 'deleteEventConfigEventBySourceId').mockRejectedValue(testError)

      const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteButton?.trigger('click')
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error deleting event configuration event:', testError)
      expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Failed to delete event configuration event', error: true })
      consoleErrorSpy.mockRestore()
    })

    it('shows snackbar error if selectedSource is null', async () => {
      store.$state.selectedSource = null as any
      await wrapper.vm.$nextTick()

      const spy = vi.spyOn(eventConfigService, 'deleteEventConfigEventBySourceId')
      const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteButton?.trigger('click')
      await flushPromises()

      expect(spy).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Missing source or event ID', error: true })
    })

    it('shows snackbar error if eventConfigEvent id is missing', async () => {
      store.$state.deleteEventConfigEventDialogState.eventConfigEvent = {
        eventLabel: 'Test Event'
      } as any
      await wrapper.vm.$nextTick()

      const spy = vi.spyOn(eventConfigService, 'deleteEventConfigEventBySourceId')
      const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteButton?.trigger('click')
      await flushPromises()

      expect(spy).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Missing source or event ID', error: true })
    })

    it('shows snackbar error if eventConfigEvent is null', async () => {
      store.$state.deleteEventConfigEventDialogState.eventConfigEvent = null as any
      await wrapper.vm.$nextTick()

      const spy = vi.spyOn(eventConfigService, 'deleteEventConfigEventBySourceId')
      const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteButton?.trigger('click')
      await flushPromises()

      expect(spy).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Missing source or event ID', error: true })
    })

    it('handles event with id 0 as falsy and shows error', async () => {
      store.$state.deleteEventConfigEventDialogState.eventConfigEvent = {
        id: 0,
        eventLabel: 'Zero ID Event'
      } as any
      await wrapper.vm.$nextTick()

      const spy = vi.spyOn(eventConfigService, 'deleteEventConfigEventBySourceId')
      const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
      await deleteButton?.trigger('click')
      await flushPromises()

      expect(spy).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Missing source or event ID', error: true })
    })
  })

  describe('Dialog Hidden Event', () => {
    it('calls hideDeleteEventConfigEventDialog when dialog emits hidden event', async () => {
      const dialog = wrapper.findComponent(FeatherDialog)
      await dialog.vm.$emit('hidden')
      expect(store.hideDeleteEventConfigEventDialog).toHaveBeenCalled()
    })
  })
})

