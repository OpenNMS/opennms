import { OnmsTag } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

describe('OnmsTag', () => {
  it('renders the value text', () => {
    const wrapper = mount(OnmsTag, { props: { value: 'ACTIVE' }})
    expect(wrapper.text()).toBe('ACTIVE')
  })

  it('forwards severity', () => {
    const inner = mount(OnmsTag, { props: { value: 'x', severity: 'success' }}).findComponent({ name: 'Tag' })
    expect(inner.props('severity')).toBe('success')
  })

  it('renders slot content when provided', () => {
    const wrapper = mount(OnmsTag, {
      props: { severity: 'success' },
      slots: {
        default: '<span data-test="custom">Custom Content</span>'
      }
    })
    expect(wrapper.find('[data-test="custom"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Custom Content')
  })

  it('slot content replaces value when both provided', () => {
    const wrapper = mount(OnmsTag, {
      props: { value: 'IGNORED', severity: 'success' },
      slots: {
        default: '<span data-test="slot">Slot Text</span>'
      }
    })
    expect(wrapper.find('[data-test="slot"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Slot Text')
    expect(wrapper.text()).not.toContain('IGNORED')
  })
})
