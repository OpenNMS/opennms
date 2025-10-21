import { mount, VueWrapper, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherButton } from '@featherds/button'
import ChangeEventConfigEventStatusDialog from '@/components/EventConfigurationDetail/Dialog/ChangeEventConfigEventStatusDialog.vue'

describe('ChangeEventConfigEventStatusDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigDetailStore>

  beforeEach(() => {
    const pinia = createTestingPinia({
      createSpy: vi.fn
    })

    store = useEventConfigDetailStore(pinia)

    // mock store state
    store.changeEventConfigEventStatusDialogState.visible = true
    store.selectedSource = { id: 1, name: 'Test Source' } as any
    store.changeEventConfigEventStatusDialogState.eventConfigEvent = {
      id: 10,
      eventLabel: 'High CPU Usage',
      enabled: true
    } as any

    wrapper = mount(ChangeEventConfigEventStatusDialog, {
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
    expect(dialog.props('labels')?.title).toBe('Change Event Configuration Event Status')
  })

  it('calls hideChangeEventConfigEventStatusDialog on Cancel click', async () => {
    const cancelBtn = wrapper
      .findAllComponents(FeatherButton)
      .find((btn) => btn.text().toLowerCase().includes('cancel'))

    expect(cancelBtn).toBeTruthy()
    await cancelBtn?.trigger('click')

    expect(store.hideChangeEventConfigEventStatusDialog).toHaveBeenCalledTimes(1)
  })

  it('calls disableEventConfigEvent when event is enabled and Save clicked', async () => {
    const saveBtn = wrapper
      .findAllComponents(FeatherButton)
      .find((btn) => btn.text().toLowerCase().includes('save'))

    await saveBtn?.trigger('click')
    await flushPromises()

    expect(store.disableEventConfigEvent).toHaveBeenCalledWith(10)
    expect(store.hideChangeEventConfigEventStatusDialog).toHaveBeenCalled()
  })

  it('calls enableEventConfigEvent when event is disabled and Save clicked', async () => {
    store.changeEventConfigEventStatusDialogState.eventConfigEvent = {
      id: 11,
      eventLabel: 'Network Down',
      enabled: false
    } as any
    await wrapper.vm.$nextTick()

    const saveBtn = wrapper
      .findAllComponents(FeatherButton)
      .find((btn) => btn.text().toLowerCase().includes('save'))

    await saveBtn?.trigger('click')
    await flushPromises()

    expect(store.enableEventConfigEvent).toHaveBeenCalledWith(11)
    expect(store.hideChangeEventConfigEventStatusDialog).toHaveBeenCalled()
  })

  it('handles missing eventConfigEvent safely', async () => {
    store.changeEventConfigEventStatusDialogState.eventConfigEvent = null as any
    await wrapper.vm.$nextTick()

    const saveBtn = wrapper
      .findAllComponents(FeatherButton)
      .find((btn) => btn.text().toLowerCase().includes('save'))

    await saveBtn?.trigger('click')
    await flushPromises()

    expect(store.disableEventConfigEvent).not.toHaveBeenCalled()
    expect(store.enableEventConfigEvent).not.toHaveBeenCalled()
  })

  it('renders FeatherDialog with visible prop true', () => {
    const dialog = wrapper.findComponent(FeatherDialog)
    expect(dialog.props('modelValue')).toBe(true)
  })
})
