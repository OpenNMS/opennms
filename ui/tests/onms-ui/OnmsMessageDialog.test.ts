import { OnmsMessageDialog } from '@opennms/onms-ui'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const globalPlugins = { plugins: [PrimeVue] }

describe('OnmsMessageDialog', () => {
  it('renders a single button defaulting to Close', async () => {
    const wrapper = mount(OnmsMessageDialog, {
      props: { visible: true, relative: true },
      global: globalPlugins
    })
    await flushPromises()
    const buttons = wrapper.findAll('button')
    expect(buttons).toHaveLength(1)
    expect(buttons[0].text()).toBe('Close')
  })

  it('respects actionButtonText', async () => {
    const wrapper = mount(OnmsMessageDialog, {
      props: { visible: true, relative: true, actionButtonText: 'Dismiss' },
      global: globalPlugins
    })
    await flushPromises()
    expect(wrapper.find('button').text()).toBe('Dismiss')
  })

  it('emits close when the button is clicked', async () => {
    const wrapper = mount(OnmsMessageDialog, {
      props: { visible: true, relative: true },
      global: globalPlugins
    })
    await flushPromises()
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('emits close on hide when unresolved (e.g. Esc dismissal)', async () => {
    const wrapper = mount(OnmsMessageDialog, {
      props: { visible: true, relative: true },
      global: globalPlugins
    })
    await flushPromises()
    const dialog = wrapper.findComponent({ name: 'Dialog' })
    dialog.vm.$emit('hide')
    await flushPromises()
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('sets appendTo to self on the inner dialog when relative', async () => {
    const wrapper = mount(OnmsMessageDialog, {
      props: { visible: true, relative: true },
      global: globalPlugins
    })
    await flushPromises()
    const dialog = wrapper.findComponent({ name: 'Dialog' })
    expect(dialog.props('appendTo')).toBe('self')
  })

  it('defaults appendTo to body when not relative', async () => {
    const wrapper = mount(OnmsMessageDialog, {
      props: { visible: true },
      global: globalPlugins
    })
    await flushPromises()
    const dialog = wrapper.findComponent({ name: 'Dialog' })
    expect(dialog.props('appendTo')).toBe('body')
  })

  it('renders the content slot', async () => {
    const wrapper = mount(OnmsMessageDialog, {
      props: { visible: true, relative: true },
      slots: { content: '<p data-test="body">All done.</p>' },
      global: globalPlugins
    })
    await flushPromises()
    expect(wrapper.find('[data-test="body"]').text()).toBe('All done.')
  })
})
