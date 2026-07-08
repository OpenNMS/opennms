import { mount, VueWrapper } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import ChangeEventConfigSourceStatusDialog from '@/components/EventConfiguration/Dialog/ChangeEventConfigSourceStatusDialog.vue'

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
          ConfirmationDialog: ConfirmationDialogStub
        }
      }
    })
  })

  afterEach(() => {
    wrapper.unmount()
    vi.clearAllMocks()
  })

  it('renders dialog correctly with title and message', () => {
    const dialog = wrapper.findComponent({ name: 'ConfirmationDialog' })
    expect(dialog.exists()).toBe(true)
    expect(dialog.props('title')).toBe('Change Event Configuration Source Status')
  })

  it('calls hideChangeEventConfigSourceStatusDialog on Cancel click', async () => {
    const cancelBtn = wrapper.find('.cancel-btn')

    expect(cancelBtn.exists()).toBe(true)
    await cancelBtn.trigger('click')

    expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalledTimes(1)
  })

  it('calls disableEventConfigSource on Save click when enabled', async () => {
    const saveBtn = wrapper.find('.action-btn')

    await saveBtn.trigger('click')

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

    const saveBtn = wrapper.find('.action-btn')

    await saveBtn.trigger('click')

    expect(store.enableEventConfigSource).toHaveBeenCalledWith(1)
    expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
  })

  it('does not crash if dialog state is missing', async () => {
    store.changeEventConfigSourceStatusDialogState = { visible: false } as any
    await wrapper.vm.$nextTick()
    expect(wrapper.exists()).toBe(true)
  })

  it('renders ConfirmationDialog with visible prop true', () => {
    const dialog = wrapper.findComponent({ name: 'ConfirmationDialog' })
    expect(dialog.props('visible')).toBe(true)
  })
})
