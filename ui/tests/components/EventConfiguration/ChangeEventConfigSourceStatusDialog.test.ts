import { mount, VueWrapper } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherButton } from '@featherds/button'
import ChangeEventConfigSourceStatusDialog from '@/components/EventConfiguration/Dialog/ChangeEventConfigSourceStatusDialog.vue'

describe('ChangeEventConfigSourceStatusDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigStore>

  beforeEach(() => {
    const pinia = createTestingPinia({
      createSpy: vi.fn
    })

    store = useEventConfigStore(pinia)
    store.changeEventConfigSourceStatusDialogState.visible = true
    store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
      id: 1,
      name: 'Test Source',
      vendor: 'Cisco',
      description: '',
      enabled: true,
      eventCount: 5,
      fileOrder: 1,
      uploadedBy: 'user',
      createdTime: new Date(),
      lastModified: new Date()
    }

    wrapper = mount(ChangeEventConfigSourceStatusDialog, {
      global: {
        plugins: [pinia],
        stubs: {
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

  it('renders dialog correctly with title and message', () => {
    const dialog = wrapper.findComponent(FeatherDialog)
    expect(dialog.exists()).toBe(true)
    expect(dialog.props('labels')?.title).toBe('Change Event Configuration Source Status')
  })

  it('calls hideChangeEventConfigSourceStatusDialog on Cancel click', async () => {
    const cancelBtn = wrapper.findAllComponents(FeatherButton).find((btn) => btn.text().toLowerCase() === 'cancel')

    expect(cancelBtn).toBeTruthy()
    await cancelBtn?.trigger('click')

    expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalledTimes(1)
  })

  it('calls disableEventConfigSource on Save click when enabled', async () => {
    const saveBtn = wrapper.findAllComponents(FeatherButton).find((btn) => btn.text().toLowerCase() === 'save')

    await saveBtn?.trigger('click')

    expect(store.disableEventConfigSource).toHaveBeenCalledWith(1)
    expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
  })

  it('calls enableEventConfigSource on Save click when disabled', async () => {
    store.changeEventConfigSourceStatusDialogState = {
      visible: true,
      eventConfigSource: {
        id: 1,
        name: 'Test Source',
        vendor: 'Cisco',
        description: '',
        enabled: false,
        eventCount: 5,
        fileOrder: 1,
        uploadedBy: 'user',
        createdTime: new Date(),
        lastModified: new Date()
      }
    }
    await wrapper.vm.$nextTick()

    const saveBtn = wrapper.findAllComponents(FeatherButton).find((btn) => btn.text().toLowerCase() === 'save')

    await saveBtn?.trigger('click')

    expect(store.enableEventConfigSource).toHaveBeenCalledWith(1)
    expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
  })

  it('does not crash if dialog state is missing', async () => {
    store.changeEventConfigSourceStatusDialogState = {} as any
    await wrapper.vm.$nextTick()
    expect(wrapper.exists()).toBe(true)
  })

  it('renders FeatherDialog with visible prop true', () => {
    const dialog = wrapper.findComponent(FeatherDialog)
    expect(dialog.props('modelValue')).toBe(true)
  })
})

