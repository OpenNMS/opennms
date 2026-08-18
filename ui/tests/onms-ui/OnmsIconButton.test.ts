import { OnmsIconButton } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'
import { defineComponent, h, nextTick } from 'vue'

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

  describe('tooltip prop', () => {
    // PrimeVue's Tooltip directive stashes its state on the host element, so the
    // element itself is the observable contract.
    const tooltipState = (el: Element) => ({
      value: (el as never as Record<string, unknown>).$_ptooltipValue,
      zIndex: (el as never as Record<string, unknown>).$_ptooltipZIndex
    })

    const mountWithTooltip = (props: Record<string, unknown>) => mount(OnmsIconButton, {
      props: { icon: StubIcon, ...props },
      global: { plugins: [PrimeVue] }
    })

    it('mounts the tooltip directive with the prop text', () => {
      const wrapper = mountWithTooltip({ tooltip: 'Redraw the graph' })
      expect(tooltipState(wrapper.find('button').element).value).toBe('Redraw the graph')
    })

    it('leaves the directive inert when no tooltip is given', () => {
      const wrapper = mountWithTooltip({ title: 'Refresh' })
      expect(tooltipState(wrapper.find('button').element).value).toBeUndefined()
    })

    it('drops the native title attribute so the browser tooltip does not double up', () => {
      const wrapper = mountWithTooltip({ title: 'Refresh', tooltip: 'Redraw the graph' })
      expect(wrapper.find('button').attributes('title')).toBeUndefined()
      // ...while title still names the button for assistive tech
      expect(wrapper.find('svg').attributes('aria-label')).toBe('Refresh')
    })

    it('names the button from the tooltip when there is no title', () => {
      const wrapper = mountWithTooltip({ tooltip: 'Clear every selection' })
      expect(wrapper.find('svg').attributes('aria-label')).toBe('Clear every selection')
    })

    // Regression guard for the z-index capture: PrimeVue only reads the
    // configured tooltip z-index in beforeMount, so a tooltip appearing after
    // mount has to remount the host or it paints behind the fixed menubar.
    it('captures the tooltip z-index when the tooltip arrives after mount', async () => {
      const wrapper = mount(OnmsIconButton, {
        props: { icon: StubIcon, tooltip: undefined as string | undefined },
        global: { plugins: [PrimeVue, { install: () => {} }], config: {}}
      })
      expect(tooltipState(wrapper.find('button').element).value).toBeUndefined()

      await wrapper.setProps({ tooltip: 'Now I have something to say' })
      await nextTick()

      const state = tooltipState(wrapper.find('button').element)
      expect(state.value).toBe('Now I have something to say')
      // set only by beforeMount — proves the host was remounted
      expect(state.zIndex).toBeDefined()
    })
  })

  it('supports the ghost variant (text + outlined both true)', () => {
    const ghost = mount(OnmsIconButton, { props: { icon: StubIcon, variant: 'ghost' }}).findComponent({ name: 'Button' })
    expect(ghost.props('text')).toBe(true)
    expect(ghost.props('outlined')).toBe(true)
  })
})
