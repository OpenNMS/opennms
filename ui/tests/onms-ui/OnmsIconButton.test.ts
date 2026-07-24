import { OnmsIconButton } from '@opennms/onms-ui'
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

  it('defaults to the text variant', () => {
    const inner = mount(OnmsIconButton, { props: { icon: StubIcon }}).findComponent({ name: 'Button' })
    expect(inner.props('text')).toBe(true)
    expect(inner.props('outlined')).toBe(false)
  })

  it('supports filled and outlined variants and danger severity', () => {
    const filled = mount(OnmsIconButton, { props: { icon: StubIcon, variant: 'filled' }}).findComponent({ name: 'Button' })
    expect(filled.props('text')).toBe(false)
    const danger = mount(OnmsIconButton, { props: { icon: StubIcon, severity: 'danger' }}).findComponent({ name: 'Button' })
    expect(danger.props('severity')).toBe('danger')
  })

  it('supports the ghost variant (text + outlined both true)', () => {
    const ghost = mount(OnmsIconButton, { props: { icon: StubIcon, variant: 'ghost' }}).findComponent({ name: 'Button' })
    expect(ghost.props('text')).toBe(true)
    expect(ghost.props('outlined')).toBe(true)
  })
})
