import OnmsIconButton from '@/components/Common/OnmsIconButton.vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { defineComponent, h } from 'vue'

// Minimal stand-in for a vendored OnmsIcon svg component.
const StubIcon = defineComponent({
  name: 'StubIcon',
  render() {
    return h('svg', { 'data-test': 'stub-icon' })
  }
})

describe('OnmsIconButton.vue', () => {
  it('renders a button containing the icon', () => {
    const wrapper = mount(OnmsIconButton, { props: { icon: StubIcon }})
    expect(wrapper.find('button').exists()).toBe(true)
    expect(wrapper.find('svg[data-test="stub-icon"]').exists()).toBe(true)
  })

  it('applies the default 1.5rem icon size', () => {
    const wrapper = mount(OnmsIconButton, { props: { icon: StubIcon }})
    const iconWrap = wrapper.find('.onms-icon-button__icon')
    expect(iconWrap.attributes('style')).toContain('font-size: 1.5rem')
  })

  it('applies a custom icon size', () => {
    const wrapper = mount(OnmsIconButton, { props: { icon: StubIcon, iconSize: '2rem' }})
    const iconWrap = wrapper.find('.onms-icon-button__icon')
    expect(iconWrap.attributes('style')).toContain('font-size: 2rem')
  })

  it('forwards title to the icon as its accessible name', () => {
    const wrapper = mount(OnmsIconButton, { props: { icon: StubIcon, title: 'Remove search term' }})
    expect(wrapper.find('svg').attributes('aria-label')).toBe('Remove search term')
  })

  it('applies title to the button as a native tooltip', () => {
    const wrapper = mount(OnmsIconButton, { props: { icon: StubIcon, title: 'Refresh' }})
    expect(wrapper.find('button').attributes('title')).toBe('Refresh')
  })

  it('renders no title attribute on the button when title is not provided', () => {
    const wrapper = mount(OnmsIconButton, { props: { icon: StubIcon }})
    expect(wrapper.find('button').attributes('title')).toBeUndefined()
  })

  it('forwards click and data-test to the button', async () => {
    const onClick = vi.fn()
    const wrapper = mount(OnmsIconButton, {
      props: { icon: StubIcon },
      attrs: { 'data-test': 'my-btn', onClick }
    })
    expect(wrapper.find('button').attributes('data-test')).toBe('my-btn')
    await wrapper.find('button').trigger('click')
    expect(onClick).toHaveBeenCalledTimes(1)
  })
})
