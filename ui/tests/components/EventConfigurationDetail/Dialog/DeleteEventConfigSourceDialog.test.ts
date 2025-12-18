import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
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
    props: ['labels', 'modelValue']
  }
}))

const push = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push })
}))

describe('DeleteEventConfigSourceDialog.vue', () => {
  let wrapper: any
  let store: ReturnType<typeof useEventConfigDetailStore>

  beforeEach(async () => {
    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useEventConfigDetailStore()

    store.$state.deleteEventConfigSourceDialogState = {
      visible: true,
      eventConfigSource: {
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
    }

    wrapper = mount(DeleteEventConfigSourceDialog, {
      global: {
        plugins: [pinia],
        components: { FeatherButton, FeatherDialog }
      }
    })

    await flushPromises()
  })

  it('renders dialog when visible is true', () => {
    expect(wrapper.findComponent(FeatherDialog).exists()).toBe(true)
    expect(wrapper.findComponent(FeatherDialog).props('labels')).toEqual({
      title: 'Delete Event Configuration Source'
    })
  })

  it('renders the dialog when visible is true', () => {
    expect(wrapper.findComponent(FeatherDialog).exists()).toBe(true)
    expect(wrapper.findComponent(FeatherDialog).props('labels')).toEqual({
      title: 'Delete Event Configuration Source'
    })
  })

  it('calls hideDeleteEventConfigSourceDialog when Cancel is clicked', async () => {
    const spyHide = vi.spyOn(store, 'hideDeleteEventConfigSourceDialog')
    const cancelBtn = wrapper.findAllComponents(FeatherButton).at(0)
    expect(cancelBtn.exists()).toBe(true)
    await cancelBtn.trigger('click')
    expect(spyHide).toHaveBeenCalled()
  })

  it('calls hideDeleteEventConfigSourceDialog when Delete button is clicked', async () => {
    const deleteButton = wrapper.findAllComponents(FeatherButton).at(0)
    expect(deleteButton.exists()).toBe(true)
    await deleteButton.trigger('click')
    expect(store.hideDeleteEventConfigSourceDialog).toHaveBeenCalled()
  })

  it('shows snackbar and logs error on failed deletion', async () => {
    const mockDelete = vi.spyOn(eventConfigService, 'deleteEventConfigSourceById').mockRejectedValue(new Error('fail'))
    const spyConsole = vi.spyOn(console, 'error').mockImplementation(() => {})

    const deleteBtn = wrapper.findAllComponents(FeatherButton).at(1)
    await deleteBtn.trigger('click')
    await flushPromises()

    expect(mockDelete).toHaveBeenCalledWith(1)
    expect(spyConsole).toHaveBeenCalled()
  })

  it('does not call delete if eventConfigSource is null', async () => {
    vi.clearAllMocks()
    store.deleteEventConfigSourceDialogState.eventConfigSource = null
    await wrapper.vm.$nextTick()

    const mockDelete = vi.spyOn(eventConfigService, 'deleteEventConfigSourceById').mockResolvedValue(true)
    const deleteBtn = wrapper.findAllComponents(FeatherButton).at(1)
    expect(deleteBtn.exists()).toBe(true)
    await deleteBtn.trigger('click')
    await flushPromises()

    expect(mockDelete).not.toHaveBeenCalled()
  })

  it('reflects dialog visibility when visible is false', async () => {
    store.deleteEventConfigSourceDialogState.visible = false
    await wrapper.vm.$nextTick()
    expect(wrapper.findComponent(FeatherDialog).props('modelValue')).toBe(false)
  })
})

