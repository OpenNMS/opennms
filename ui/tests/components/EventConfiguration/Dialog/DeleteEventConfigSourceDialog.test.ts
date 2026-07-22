import DeleteEventConfigSourceDialog from '@/components/EventConfiguration/Dialog/DeleteEventConfigSourceDialog.vue'
import * as eventConfigService from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

// Stub the Common ConfirmationDialog wrapper so these tests exercise this
// component's logic via the wrapper's public API (props + ok/cancel events),
// independent of the underlying dialog library.
const ConfirmationDialogStub = {
  name: 'ConfirmationDialog',
  template: '<div class="confirmation-dialog"><div class="modal-body"><slot name="content"></slot></div><button class="action-btn" @click="$emit(\'ok\')">{{ actionButtonText }}</button><button class="cancel-btn" @click="$emit(\'cancel\')">{{ cancelButtonText || \'Cancel\' }}</button></div>',
  props: ['visible', 'title', 'actionButtonText', 'cancelButtonText']
}

describe('DeleteEventConfigSourceDialog', () => {
  let wrapper: any
  let store: ReturnType<typeof useEventConfigStore>

  beforeEach(async () => {
    vi.clearAllMocks()
    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useEventConfigStore()

    store.$state = {
      deleteEventConfigSourceDialogState: {
        visible: true,
        eventConfigSource: {
          id: 1,
          name: 'Test Source',
          vendor: 'Test Vendor',
          description: 'Test Description',
          enabled: true,
          eventCount: 5,
          fileOrder: 1,
          uploadedBy: 'test-user',
          createdTime: new Date('2025-10-01T12:00:00Z'),
          lastModified: new Date('2025-10-02T12:00:00Z')
        }
      },
      sources: [],
      sourcesPagination: { page: 1, pageSize: 10, total: 0 },
      sourcesSearchTerm: '',
      sourcesSorting: { sortOrder: 'desc', sortKey: 'createdTime' },
      isLoading: false,
      activeTab: 0,
      uploadedSources: [],
      uploadedEventConfigFilesReportDialogState: { visible: false },
      changeEventConfigSourceStatusDialogState: { visible: false, eventConfigSource: null },
      createEventConfigSourceDialogState: { visible: false }
    }

    wrapper = mount(DeleteEventConfigSourceDialog, {
      global: {
        plugins: [pinia],
        stubs: {
          ConfirmationDialog: ConfirmationDialogStub
        }
      }
    })

    await flushPromises()
  })

  it('renders the dialog when visible is true', () => {
    expect(wrapper.findComponent({ name: 'ConfirmationDialog' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'ConfirmationDialog' }).props('title')).toBe('Delete Event Configuration Source')
  })

  it('displays the correct event configuration source name and event count', () => {
    const modalBody = wrapper.find('.modal-body')
    expect(modalBody.exists()).toBe(true)
    expect(modalBody.text()).toContain('This will delete the event configuration source: Test Source')
    expect(modalBody.text()).toContain('This event configuration source has 5 events associated with it')
  })

  it('calls hideDeleteEventConfigSourceModal when Cancel button is clicked', async () => {
    const cancelButton = wrapper.find('.cancel-btn')
    expect(cancelButton.exists()).toBe(true)
    await cancelButton.trigger('click')
    expect(store.hideDeleteEventConfigSourceModal).toHaveBeenCalled()
  })

  it('calls deleteEventConfigSourceById and handles success when Delete button is clicked', async () => {
    vi.spyOn(eventConfigService, 'deleteEventConfigSourceById').mockResolvedValue(true)
    vi.spyOn(eventConfigService, 'filterEventConfigSources').mockResolvedValue({
      sources: [],
      totalRecords: 0
    })
    vi.spyOn(eventConfigService, 'getAllSourceNames').mockResolvedValue([])
    const deleteButton = wrapper.find('.action-btn')
    expect(deleteButton.exists()).toBe(true)
    await deleteButton.trigger('click')
    await flushPromises()
    expect(eventConfigService.deleteEventConfigSourceById).toHaveBeenCalledWith(1)
    expect(store.hideDeleteEventConfigSourceModal).toHaveBeenCalled()
    expect(store.resetSourcesPagination).toHaveBeenCalled()
    expect(store.fetchEventConfigs).toHaveBeenCalled()
  })

  it('does not attempt to delete if eventConfigSource is null', async () => {
    vi.restoreAllMocks()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: true
    })

    const localStore = useEventConfigStore()
    localStore.$state = {
      deleteEventConfigSourceDialogState: {
        visible: true,
        eventConfigSource: null
      },
      sources: [],
      sourcesPagination: { page: 1, pageSize: 10, total: 0 },
      sourcesSearchTerm: '',
      sourcesSorting: { sortOrder: 'desc', sortKey: 'createdTime' },
      isLoading: false,
      activeTab: 0,
      uploadedSources: [],
      uploadedEventConfigFilesReportDialogState: { visible: false },
      changeEventConfigSourceStatusDialogState: { visible: false, eventConfigSource: null },
      createEventConfigSourceDialogState: { visible: false }
    }

    const localWrapper = mount(DeleteEventConfigSourceDialog, {
      global: {
        plugins: [pinia],
        stubs: {
          ConfirmationDialog: ConfirmationDialogStub
        }
      }
    })

    await flushPromises()

    const mockDelete = vi.spyOn(eventConfigService, 'deleteEventConfigSourceById').mockResolvedValue(true)
    const deleteButton = localWrapper.find('.action-btn')
    expect(deleteButton.exists()).toBe(true)
    await deleteButton.trigger('click')
    await flushPromises()
    expect(mockDelete).not.toHaveBeenCalled()
  })

  it('hides the dialog when visible is false', async () => {
    store.$state.deleteEventConfigSourceDialogState.visible = false
    await wrapper.vm.$nextTick()
    expect(wrapper.findComponent({ name: 'ConfirmationDialog' }).props('visible')).toBe(false)
  })
})
