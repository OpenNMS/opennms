import { OnmsMessage } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const mountMessage = (props: Record<string, unknown> = {}, slot = 'Body text') =>
  mount(OnmsMessage, { props, slots: { default: slot }, global: { plugins: [PrimeVue], stubs: { transition: false }}})

describe('OnmsMessage', () => {
  it('renders the slot content', () => {
    const wrapper = mountMessage({}, '<span data-test="body">Nothing here</span>')
    expect(wrapper.find('[data-test="body"]').text()).toBe('Nothing here')
  })

  it('defaults to info and forwards the severity', () => {
    expect(mountMessage().findComponent({ name: 'Message' }).props('severity')).toBe('info')
    const wrapper = mountMessage({ severity: 'error' })
    expect(wrapper.findComponent({ name: 'Message' }).props('severity')).toBe('error')
    expect(wrapper.find('.p-message').classes()).toContain('p-message-error')
  })

  it('is never closable', () => {
    expect(mountMessage({ severity: 'warn' }).findComponent({ name: 'Message' }).props('closable')).toBe(false)
    expect(mountMessage().find('.p-message-close-button').exists()).toBe(false)
  })

  it('lets class and data attributes fall through to the root', () => {
    const wrapper = mountMessage({ class: 'callout', 'data-test': 'nodes-callout', severity: 'warn' })
    const root = wrapper.find('[data-test="nodes-callout"]')
    expect(root.classes()).toEqual(expect.arrayContaining(['callout', 'p-message', 'p-message-warn']))
  })
})
