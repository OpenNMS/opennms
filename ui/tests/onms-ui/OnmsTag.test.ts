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
})
