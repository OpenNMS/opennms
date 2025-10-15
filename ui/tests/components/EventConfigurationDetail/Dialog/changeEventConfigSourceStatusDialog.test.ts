import { mount, VueWrapper, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherButton } from '@featherds/button'
import ChangeEventConfigSourceStatusDialog from '@/components/EventConfigurationDetail/Dialog/ChangeEventConfigSourceStatusDialog.vue'

// mock feather components so we can actually render the buttons
vi.mock('@featherds/button', () => ({
  FeatherButton: {
    template: `<button @click="$emit('click')"><slot /></button>`
  }
}))

vi.mock('@featherds/dialog', () => ({
  FeatherDialog: {
    props: ['modelValue', 'labels', 'hideClose'],
    emits: ['update:modelValue', 'hidden'],
    template: `
      <div data-test="feather-dialog">
        <slot></slot>
        <slot name="footer"></slot>
      </div>
    `
  }
}))

describe('ChangeEventConfigSourceStatusDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigDetailStore>

  beforeEach(() => {
    const pinia = createTestingPinia({
      createSpy: vi.fn
    })

    store = useEventConfigDetailStore(pinia)

    // define spies manually for required store methods
    store.hideChangeEventConfigSourceStatusDialog = vi.fn()
    store.disableEventConfigSource = vi.fn().mockResolvedValue(true)
    store.enableEventConfigSource = vi.fn().mockResolvedValue(true)

    store.changeEventConfigSourceStatusDialogState.visible = true
    store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
      id: 1,
      name: 'Test Source',
      enabled: true
    } as any

    wrapper = mount(ChangeEventConfigSourceStatusDialog, {
      global: {
        plugins: [pinia]
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
    await cancelBtn!.trigger('click')
    expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalledTimes(2)
  })

  it('calls disableEventConfigSource when source is enabled and Save clicked', async () => {
    const saveBtn = wrapper.findAll('button').find((btn) =>
      btn.text().toLowerCase().includes('save')
    )
    expect(saveBtn).toBeTruthy()

    await saveBtn!.trigger('click')
    await flushPromises()

    expect(store.disableEventConfigSource).toHaveBeenCalledWith(1)
    expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
  })

  it('calls enableEventConfigSource when source is disabled and Save clicked', async () => {
    store.changeEventConfigSourceStatusDialogState.eventConfigSource = {
      id: 2,
      name: 'Another Source',
      enabled: false
    } as any
    await wrapper.vm.$nextTick()

    const saveBtn = wrapper.findAll('button').find((btn) =>
      btn.text().toLowerCase().includes('save')
    )
    expect(saveBtn).toBeTruthy()

    await saveBtn!.trigger('click')
    await flushPromises()

    expect(store.enableEventConfigSource).toHaveBeenCalledWith(2)
    expect(store.hideChangeEventConfigSourceStatusDialog).toHaveBeenCalled()
  })

  it('handles missing eventConfigSource safely', async () => {
    store.changeEventConfigSourceStatusDialogState.eventConfigSource = null as any
    await wrapper.vm.$nextTick()

    const saveBtn = wrapper.findAll('button').find((btn) =>
      btn.text().toLowerCase().includes('save')
    )

    await saveBtn!.trigger('click')
    await flushPromises()

    expect(store.disableEventConfigSource).not.toHaveBeenCalled()
    expect(store.enableEventConfigSource).not.toHaveBeenCalled()
  })

  it('renders dialog visible = true', async () => {
    const stateVisible = store.changeEventConfigSourceStatusDialogState.visible
    expect(stateVisible).toBe(true)
  })
})
