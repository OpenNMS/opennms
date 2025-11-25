import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { setActivePinia } from 'pinia'
import CreateEventConfigurationDialog from '@/components/EventConfiguration/Dialog/CreateEventConfigurationDialog.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import { FeatherDialog } from '@featherds/dialog'

describe('CreateEventConfigurationDialog.vue', () => {
  let store: ReturnType<typeof useEventConfigStore>
  let wrapper: ReturnType<typeof mount>

  beforeEach(async () => {
    vi.useFakeTimers()
    const pinia = createTestingPinia({ createSpy: vi.fn })
    setActivePinia(pinia)
    store = useEventConfigStore()
    store.createEventConfigSourceDialogState = { visible: true }

    wrapper = mount(CreateEventConfigurationDialog, {
      attachTo: document.body,
      global: {
        plugins: [pinia],
        components: { FeatherButton, FeatherInput, FeatherDialog }
      }
    })
    await flushPromises()
    vi.runAllTimers()
  })

  afterEach(() => {
    vi.runAllTimers()
    wrapper.unmount()
    document.body.innerHTML = ''
    vi.useRealTimers()
  })

  function getModalBody(): HTMLElement | null {
    return document.body.querySelector('.modal-body')
  }

  it('renders the dialog when visible is true', () => {
    const header = document.body.querySelector('[data-ref-id="feather-dialog-header"]')
    expect(header).not.toBeNull()
    expect(header!.textContent).toBe('Create Event Configuration')
  })

  it('renders informational note', () => {
    const p = document.body.querySelector('.modal-body p')
    expect(p).not.toBeNull()
    expect(p!.textContent).toContain('will be created with 0 events')
  })

  it('renders input field with correct label', () => {
    const inputComp = wrapper.findComponent(FeatherInput)
    expect(inputComp.exists()).toBe(true)
    expect(inputComp.props('label')).toBe('Event Configuration Name')
  })

  it('renders Cancel and Create buttons', () => {
    const buttons = wrapper.findAllComponents(FeatherButton)
    expect(buttons.length).toBe(2)
    expect(buttons[0].text()).toContain('Cancel')
    expect(buttons[1].text()).toContain('Create')
  })

  it('shows error when input empty', async () => {
    (wrapper.vm as any).configName = ''
    await wrapper.vm.$nextTick()
    const inputComp = wrapper.findComponent(FeatherInput)
    expect(inputComp.props('error')).toBe('Event Configuration Name is required')
  })

  it('clears error when input has value', async () => {
    (wrapper.vm as any).configName = 'X'
    await wrapper.vm.$nextTick()
    const inputComp = wrapper.findComponent(FeatherInput)
    expect(inputComp.props('error')).toBe('')
  })

  it('disables Create button when invalid', async () => {
    (wrapper.vm as any).configName = '   '
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    expect(createBtn.attributes('aria-disabled') === 'true' || createBtn.attributes('disabled')).toBeTruthy()
  })

  it('enables Create button when valid', async () => {
    (wrapper.vm as any).configName = 'Valid'
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    expect(createBtn.attributes('aria-disabled')).toBeUndefined()
  })

  it('cancel calls hideCreateEventConfigSourceDialog', async () => {
    store.hideCreateEventConfigSourceDialog = vi.fn()
    const cancelBtn = wrapper.findAllComponents(FeatherButton)[0]
    await cancelBtn.trigger('click')
    expect(store.hideCreateEventConfigSourceDialog).toHaveBeenCalledTimes(1)
  })

  it('does not save when invalid create clicked', async () => {
    store.hideCreateEventConfigSourceDialog = vi.fn();
    (wrapper.vm as any).configName = '   '
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    await createBtn.trigger('click')
    expect(store.hideCreateEventConfigSourceDialog).not.toHaveBeenCalled()
  })

  it('saves and closes when valid', async () => {
    store.hideCreateEventConfigSourceDialog = vi.fn();
    (wrapper.vm as any).configName = 'ConfigA'
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    await createBtn.trigger('click')
    expect(store.hideCreateEventConfigSourceDialog).toHaveBeenCalledTimes(1)
  })

  it('resets form after save', async () => {
    store.hideCreateEventConfigSourceDialog = vi.fn();
    (wrapper.vm as any).configName = 'ResetMe'
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    await createBtn.trigger('click')
    expect((wrapper.vm as any).configName).toBe('')
  })

  it('whitespace-only treated invalid', async () => {
    (wrapper.vm as any).configName = '   '
    await wrapper.vm.$nextTick()
    const inputComp = wrapper.findComponent(FeatherInput)
    expect(inputComp.props('error')).toBe('Event Configuration Name is required')
  })

  it('multiple valid saves call hide each time', async () => {
    store.hideCreateEventConfigSourceDialog = vi.fn()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1];

    (wrapper.vm as any).configName = 'One'
    await wrapper.vm.$nextTick()
    await createBtn.trigger('click');

    (wrapper.vm as any).configName = 'Two'
    await wrapper.vm.$nextTick()
    await createBtn.trigger('click')

    expect(store.hideCreateEventConfigSourceDialog).toHaveBeenCalledTimes(2)
  })

  it('modal-body has expected structure', () => {
    const body = getModalBody()
    expect(body).not.toBeNull()
    const divs = body!.querySelectorAll('div')
    expect(divs.length).toBeGreaterThanOrEqual(1)
    expect(body!.querySelector('p')).not.toBeNull()
  })

  it('maintains form state before save', async () => {
    (wrapper.vm as any).configName = 'Persist'
    await wrapper.vm.$nextTick()
    const inputComp = wrapper.findComponent(FeatherInput)
    expect(inputComp.props('modelValue')).toBe('Persist')
  })

  it('visibility reactive (v-model)', async () => {
    expect(getModalBody()).not.toBeNull()
    store.createEventConfigSourceDialogState.visible = false
    await wrapper.vm.$nextTick()
    vi.runAllTimers()
    expect(getModalBody()).toBeNull()
  })

  it('unmounts without errors', () => {
    expect(() => wrapper.unmount()).not.toThrow()
  })

  it('shows error on initial mount (empty name)', () => {
    const input = wrapper.findComponent(FeatherInput)
    expect(input.props('error')).toBe('Event Configuration Name is required')
  })

  it('treats trimmed non-empty as valid', async () => {
    (wrapper.vm as any).configName = '   X   '
    await wrapper.vm.$nextTick()
    const input = wrapper.findComponent(FeatherInput)
    expect(input.props('error')).toBe('')
  })

  it('@hidden event triggers store hide', async () => {
    store.hideCreateEventConfigSourceDialog = vi.fn()
    const dialog = wrapper.findComponent(FeatherDialog)
    expect(dialog.exists()).toBe(true)
    dialog.vm.$emit('hidden')
    expect(store.hideCreateEventConfigSourceDialog).toHaveBeenCalledTimes(1)
  })

  it('hide-close prop applied', () => {
    const dialog = wrapper.findComponent(FeatherDialog)
    expect(dialog.exists()).toBe(true)
    expect(dialog.props('hideClose')).toBe(true)
  })

  it('role dialog & aria-modal present', () => {
    const roleEl = document.body.querySelector('[role="dialog"]')
    expect(roleEl).not.toBeNull()
    expect(roleEl!.getAttribute('aria-modal')).toBe('true')
  })

  it('disabled state updates when reverting to empty', async () => {
    (wrapper.vm as any).configName = 'Valid'
    await wrapper.vm.$nextTick();
    (wrapper.vm as any).configName = ''
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    expect(createBtn.attributes('aria-disabled') === 'true' || createBtn.attributes('disabled')).toBeTruthy()
  })
})

