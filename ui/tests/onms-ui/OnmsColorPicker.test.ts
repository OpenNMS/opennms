import { OnmsColorPicker } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { nextTick } from 'vue'
import { describe, expect, it } from 'vitest'

const mountIt = (props: Record<string, unknown> = {}) =>
  mount(OnmsColorPicker, { props, global: { plugins: [PrimeVue] }, attachTo: document.body })

// The panel is an OnmsPopover, so it renders to <body> once opened.
const openPanel = async (wrapper: ReturnType<typeof mountIt>) => {
  await wrapper.find('.onms-color-picker__trigger').trigger('click')
  await nextTick()
}

const swatches = () => Array.from(document.querySelectorAll<HTMLElement>('.onms-color-picker__swatch'))

// The panel content is teleported to <body>, so it is reached through the
// document rather than through the wrapper.
const clickCustomToggle = async () => {
  const toggle = document.querySelector<HTMLElement>('.onms-color-picker__custom-toggle')
  expect(toggle, 'no custom toggle rendered').toBeTruthy()
  toggle!.click()
  await nextTick()
}

describe('OnmsColorPicker contract', () => {
  it('shows the current color on the trigger without opening a panel', () => {
    const wrapper = mountIt({ modelValue: '#aabbcc' })
    expect(wrapper.find('.onms-color-picker__trigger').attributes('style')).toContain('#aabbcc')
    expect(document.querySelector('.onms-color-picker__grid')).toBeNull()
    wrapper.unmount()
  })

  it('opens a swatch grid in the page, not a native dialog', async () => {
    const wrapper = mountIt({ modelValue: '#64748b' })
    await openPanel(wrapper)

    expect(document.querySelector('.onms-color-picker__grid')).not.toBeNull()
    expect(swatches().length).toBeGreaterThan(20)
    wrapper.unmount()
  })

  it('emits the swatch that was clicked', async () => {
    const wrapper = mountIt({ modelValue: '#64748b', swatches: ['#111111', '#222222'] })
    await openPanel(wrapper)

    swatches()[1].click()
    await nextTick()

    expect(wrapper.emitted('update:modelValue')).toEqual([['#222222']])
    wrapper.unmount()
  })

  it('marks the swatch matching the current value, case-insensitively', async () => {
    const wrapper = mountIt({ modelValue: '#AABBCC', swatches: ['#111111', '#aabbcc'] })
    await openPanel(wrapper)

    expect(swatches()[0].getAttribute('aria-pressed')).toBe('false')
    expect(swatches()[1].getAttribute('aria-pressed')).toBe('true')
    wrapper.unmount()
  })

  it('reports a value that is not in the palette as custom', async () => {
    const onPalette = mountIt({ modelValue: '#111111', swatches: ['#111111'] })
    await openPanel(onPalette)
    expect(document.querySelector('.onms-color-picker__value')?.textContent).not.toContain('custom')
    onPalette.unmount()

    const offPalette = mountIt({ modelValue: '#9aa7b8', swatches: ['#111111'] })
    await openPanel(offPalette)
    const value = document.querySelector('.onms-color-picker__value')?.textContent
    expect(value).toContain('#9aa7b8')
    expect(value).toContain('custom')
    offPalette.unmount()
  })

  it('keeps the spectrum behind a toggle and emits from it', async () => {
    const wrapper = mountIt({ modelValue: '#111111' })
    await openPanel(wrapper)
    expect(wrapper.findComponent({ name: 'ColorPicker' }).exists()).toBe(false)

    await clickCustomToggle()
    const spectrum = wrapper.findComponent({ name: 'ColorPicker' })
    expect(spectrum.exists()).toBe(true)
    expect(spectrum.props('inline')).toBe(true)
    expect(spectrum.props('format')).toBe('hex')

    // PrimeVue accepts hex with or without the '#' but always emits it without,
    // and a bare `aabbcc` is not a valid CSS color.
    spectrum.vm.$emit('update:modelValue', '112233')
    expect(wrapper.emitted('update:modelValue')).toEqual([['#112233']])
    wrapper.unmount()
  })

  it('passes a non-hex spectrum value through rather than swallowing it', async () => {
    const wrapper = mountIt({ modelValue: '#111111' })
    await openPanel(wrapper)
    await clickCustomToggle()

    wrapper.findComponent({ name: 'ColorPicker' }).vm.$emit('update:modelValue', 'rgb(1,2,3)')
    expect(wrapper.emitted('update:modelValue')).toEqual([['rgb(1,2,3)']])
    wrapper.unmount()
  })

  it('disables the trigger so the panel cannot be opened', () => {
    const wrapper = mountIt({ modelValue: '#111111', disabled: true })
    expect(wrapper.find('.onms-color-picker__trigger').attributes('disabled')).toBeDefined()
    wrapper.unmount()
  })

  // The other half of case-insensitivity: the swatch list comes from callers,
  // which nothing constrains to lowercase.
  it('marks an uppercase swatch that matches the current value', async () => {
    const wrapper = mountIt({ modelValue: '#ff0000', swatches: ['#FF0000', '#00FF00'] })
    await openPanel(wrapper)

    expect(swatches()[0].getAttribute('aria-pressed')).toBe('true')
    expect(swatches()[0].className).toContain('is-selected')
    expect(swatches()[1].getAttribute('aria-pressed')).toBe('false')
    wrapper.unmount()
  })

  it('does not call a color custom when an uppercase swatch holds it', async () => {
    const wrapper = mountIt({ modelValue: '#FF0000', swatches: ['#FF0000'] })
    await openPanel(wrapper)
    expect(document.querySelector('.onms-color-picker__value')?.textContent).not.toContain('custom')
    wrapper.unmount()
  })

  it('emits a swatch in the same form it emits a picked color', async () => {
    const wrapper = mountIt({ modelValue: '#000000', swatches: ['#FF0000'] })
    await openPanel(wrapper)
    swatches()[0].click()
    await nextTick()

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['#ff0000'])
    wrapper.unmount()
  })

  // The palette documents itself as carrying the topology map's own defaults.
  it('carries the link color the topology map ships', async () => {
    const wrapper = mountIt({ modelValue: '#9aa7b8' })
    await openPanel(wrapper)
    expect(document.querySelector('.onms-color-picker__value')?.textContent).not.toContain('custom')
    wrapper.unmount()
  })
})
