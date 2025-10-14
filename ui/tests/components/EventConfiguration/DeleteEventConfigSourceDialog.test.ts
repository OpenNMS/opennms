import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import * as eventConfigService from '@/services/eventConfigService'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import DeleteEventConfigSourceDialog from '@/components/EventConfiguration/Dialog/DeleteEventConfigSourceDialog.vue'

vi.mock('@featherds/dialog', () => ({
  FeatherDialog: {
    name: 'FeatherDialog',
    template: '<div><slot></slot><slot name="footer"></slot></div>',
    props: ['labels', 'modelValue']
  }
}))

describe('DeleteEventConfigSourceDialog', () => {
  let wrapper: any
  let store: ReturnType<typeof useEventConfigStore>

  beforeEach(async () => {
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
      uploadedSourceNames: [],
      uploadedEventConfigFilesReportDialogState: { visible: false },
      changeEventConfigSourceStatusDialogState: { visible: false, eventConfigSource: null }
    }

    wrapper = mount(DeleteEventConfigSourceDialog, {
      global: {
        plugins: [pinia],
        components: {
          FeatherButton,
          FeatherDialog
        }
      }
    })

    await flushPromises()
  })

  it('renders the dialog when visible is true', () => {
    expect(wrapper.findComponent(FeatherDialog).exists()).toBe(true)
    expect(wrapper.findComponent(FeatherDialog).props('labels')).toEqual({
      title: 'Delete Event Configuration Source'
    })
  })

  it('displays the correct event configuration source name and event count', () => {
    const modalBody = wrapper.find('.modal-body')
    expect(modalBody.exists()).toBe(true)
    expect(modalBody.text()).toContain('This will delete the event configuration source: Test Source')
    expect(modalBody.text()).toContain('This event configuration source has 5 events associated with it')
  })

  it('calls hideDeleteEventConfigSourceModal when Cancel button is clicked', async () => {
    const cancelButton = wrapper.findAllComponents(FeatherButton).at(0)
    expect(cancelButton.exists()).toBe(true)
    await cancelButton.trigger('click')
    expect(store.hideDeleteEventConfigSourceModal).toHaveBeenCalled()
  })

  it('calls deleteEventConfigSourceById and handles success when Delete button is clicked', async () => {
    vi.spyOn(eventConfigService, 'deleteEventConfigSourceById').mockResolvedValue(true)
    const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
    expect(deleteButton.exists()).toBe(true)
    await deleteButton.trigger('click')
    await flushPromises()
    expect(eventConfigService.deleteEventConfigSourceById).toHaveBeenCalledWith(1)
    expect(store.hideDeleteEventConfigSourceModal).toHaveBeenCalled()
    expect(store.resetSourcesPagination).toHaveBeenCalled()
    expect(store.fetchEventConfigs).toHaveBeenCalled()
  })

  it('does not attempt to delete if eventConfigSource is null', async () => {
    store.$state.deleteEventConfigSourceDialogState.eventConfigSource = null
    await wrapper.vm.$nextTick()
    vi.spyOn(eventConfigService, 'deleteEventConfigSourceById').mockResolvedValue(true)
    const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
    expect(deleteButton.exists()).toBe(true)
    await deleteButton.trigger('click')
    await flushPromises()
    expect(eventConfigService.deleteEventConfigSourceById).not.toHaveBeenCalled()
  })

  it('hides the dialog when visible is false', async () => {
    store.$state.deleteEventConfigSourceDialogState.visible = false
    await wrapper.vm.$nextTick()
    expect(wrapper.findComponent(FeatherDialog).props('modelValue')).toBe(false)
  })
})