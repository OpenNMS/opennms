import { OnmsConfirmationDialog } from '@opennms/onms-ui'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it } from 'vitest'

const globalPlugins = { plugins: [PrimeVue] }

describe('OnmsConfirmationDialog', () => {
  let wrapper: VueWrapper

  const mountDialog = async (props: Record<string, unknown> = {}, slots: Record<string, string> = {}) => {
    wrapper = mount(OnmsConfirmationDialog, {
      props: { visible: true, ...props },
      slots,
      global: globalPlugins
    })
    await flushPromises()
    return wrapper
  }

  afterEach(() => {
    wrapper?.unmount()
  })

  it('renders the inner OnmsDialog as non-closable and modal', async () => {
    await mountDialog({ title: 'Confirm' })
    const dialog = wrapper.findComponent({ name: 'Dialog' })
    expect(dialog.exists()).toBe(true)
    expect(dialog.props('closable')).toBe(false)
    expect(dialog.props('modal')).toBe(true)
    expect(dialog.props('header')).toBe('Confirm')
  })

  it('defaults the action button label to OK', async () => {
    await mountDialog()
    const buttons = document.body.querySelectorAll('button')
    expect(buttons[0].textContent).toBe('OK')
  })

  it('respects actionButtonText', async () => {
    await mountDialog({ actionButtonText: 'Proceed' })
    const buttons = document.body.querySelectorAll('button')
    expect(buttons[0].textContent).toBe('Proceed')
  })

  it('defaults the cancel button label to Cancel, using the text variant', async () => {
    await mountDialog()
    const buttons = document.body.querySelectorAll('button')
    expect(buttons[1].textContent).toBe('Cancel')
    expect(buttons[1].classList.contains('p-button-text')).toBe(true)
  })

  it('respects cancelButtonText', async () => {
    await mountDialog({ cancelButtonText: 'Nevermind' })
    const buttons = document.body.querySelectorAll('button')
    expect(buttons[1].textContent).toBe('Nevermind')
  })

  it('emits ok when the action button is clicked', async () => {
    await mountDialog()
    const buttons = document.body.querySelectorAll('button')
    ;(buttons[0] as HTMLElement).click()
    await flushPromises()
    expect(wrapper.emitted('ok')).toHaveLength(1)
    expect(wrapper.emitted('cancel')).toBeUndefined()
  })

  it('emits cancel when the cancel button is clicked', async () => {
    await mountDialog()
    const buttons = document.body.querySelectorAll('button')
    ;(buttons[1] as HTMLElement).click()
    await flushPromises()
    expect(wrapper.emitted('cancel')).toHaveLength(1)
    expect(wrapper.emitted('ok')).toBeUndefined()
  })

  it('emits cancel on hide when unresolved (e.g. Esc dismissal)', async () => {
    await mountDialog()
    const dialog = wrapper.findComponent({ name: 'Dialog' })
    dialog.vm.$emit('hide')
    await flushPromises()
    expect(wrapper.emitted('cancel')).toHaveLength(1)
  })

  it('renders the content slot', async () => {
    await mountDialog({}, { content: '<p data-test="body">Are you sure?</p>' })
    expect(document.body.querySelector('[data-test="body"]')?.textContent).toBe('Are you sure?')
  })
})
