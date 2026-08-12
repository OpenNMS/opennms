import { OnmsIcon } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { defineComponent, h } from 'vue'

const StubIcon = defineComponent({ render: () => h('svg', { 'data-test': 'stub' }) })

describe('OnmsIcon', () => {
  it('renders the icon component with img role', () => {
    const wrapper = mount(OnmsIcon, { props: { icon: StubIcon }})
    const svg = wrapper.find('svg')
    expect(svg.exists()).toBe(true)
    expect(svg.attributes('role')).toBe('img')
    expect(svg.attributes('aria-hidden')).toBe('true')
  })

  it('exposes title as accessible name', () => {
    const wrapper = mount(OnmsIcon, { props: { icon: StubIcon, title: 'Alarms' }})
    expect(wrapper.find('svg').attributes('aria-label')).toBe('Alarms')
    expect(wrapper.find('svg').attributes('aria-hidden')).toBe('false')
  })
})
