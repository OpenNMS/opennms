import AvailabilityBadge from '@/components/Common/AvailabilityBadge.vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

describe('AvailabilityBadge.vue', () => {
  it.each(['normal', 'warning', 'critical'] as const)('marks the %s level', (level) => {
    const wrapper = mount(AvailabilityBadge, { props: { text: '99%', level }})
    const badge = wrapper.get('[data-test="availability-badge"]')

    expect(badge.classes()).toContain('availability-badge')
    expect(badge.classes()).toContain(`availability-badge--${level}`)
    expect(badge.attributes('data-level')).toBe(level)
  })

  it('renders a plain span without a level', () => {
    const wrapper = mount(AvailabilityBadge, { props: { text: 'Unmanaged' }})

    expect(wrapper.element.tagName).toBe('SPAN')
    expect(wrapper.classes()).toEqual(['availability-badge'])
    expect(wrapper.attributes('data-level')).toBeUndefined()
  })

  it('renders the text', () => {
    const wrapper = mount(AvailabilityBadge, { props: { text: '98.76%', level: 'warning' }})

    expect(wrapper.text()).toBe('98.76%')
  })
})
