import { OnmsSpinner } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

describe('OnmsSpinner', () => {
  it('renders the spinner at the default 2.5rem size', () => {
    const inner = mount(OnmsSpinner).findComponent({ name: 'ProgressSpinner' })
    expect(inner.props('strokeWidth')).toBe('4')
    expect(inner.attributes('style')).toContain('width: 2.5rem')
  })

  it('accepts a custom size', () => {
    const inner = mount(OnmsSpinner, { props: { size: '1rem' }}).findComponent({ name: 'ProgressSpinner' })
    expect(inner.attributes('style')).toContain('width: 1rem')
  })
})
