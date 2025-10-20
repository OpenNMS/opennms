import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import * as eventConfigService from '@/services/eventConfigService'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import DeleteEventConfigEventDialog from '@/components/EventConfigurationDetail/Dialog/DeleteEventConfigEventDialog.vue'
import type { EventConfigSource } from '@/types/eventConfig'

vi.mock('@featherds/dialog', () => ({
  FeatherDialog: {
    name: 'FeatherDialog',
    template: '<div><slot></slot><slot name="footer"></slot></div>',
    props: ['labels', 'modelValue']
  }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: vi.fn()
  })
}))

describe('DeleteEventConfigEventDialog', () => {
  let wrapper: any
  let store: ReturnType<typeof useEventConfigDetailStore>

  beforeEach(async () => {
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
      selectedSource: {
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
      } as EventConfigSource,
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

  it('renders the dialog when visible is true', () => {
    const dialog = wrapper.findComponent(FeatherDialog)
    expect(dialog.exists()).toBe(true)
    expect(dialog.props('labels')).toEqual({
      title: 'Delete Event Configuration Event'
    })
  })

  it('displays correct event and source names', () => {
    const modalBody = wrapper.find('.modal-body')
    expect(modalBody.text()).toContain('Test Event')
    expect(modalBody.text()).toContain('Test Source')
  })

  it('calls hideDeleteEventConfigEventDialog when Cancel is clicked', async () => {
    const cancelButton = wrapper.findAllComponents(FeatherButton).at(0)
    expect(cancelButton.exists()).toBe(true)
    await cancelButton.trigger('click')
    expect(store.hideDeleteEventConfigEventDialog).toHaveBeenCalled()
  })

  it('calls deleteEventConfigEventBySourceId and handles success', async () => {
    vi.spyOn(eventConfigService, 'deleteEventConfigEventBySourceId').mockResolvedValue(true)

    const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
    await deleteButton.trigger('click')
    await flushPromises()

    expect(eventConfigService.deleteEventConfigEventBySourceId).toHaveBeenCalledWith(5, [10])
    expect(store.hideDeleteEventConfigEventDialog).toHaveBeenCalled()
    expect(store.resetEventsPagination).toHaveBeenCalled()
    expect(store.fetchEventsBySourceId).toHaveBeenCalled()
  })

  it('shows snackbar error if IDs are missing', async () => {
    store.$state.selectedSource = null as any
    await wrapper.vm.$nextTick()

    const spy = vi.spyOn(eventConfigService, 'deleteEventConfigEventBySourceId')
    const deleteButton = wrapper.findAllComponents(FeatherButton).at(1)
    await deleteButton.trigger('click')
    await flushPromises()

    expect(spy).not.toHaveBeenCalled()
  })

  it('hides the dialog when visible is false', async () => {
    store.$state.deleteEventConfigEventDialogState.visible = false
    await wrapper.vm.$nextTick()
    expect(wrapper.findComponent(FeatherDialog).props('modelValue')).toBe(false)
  })
})
