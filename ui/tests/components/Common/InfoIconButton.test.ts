import InfoIconButton from '@/components/Common/InfoIconButton.vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

describe('InfoIconButton.vue', () => {
  const mountComponent = () =>
    mount(InfoIconButton, { props: { ariaLabel: 'About thresholds' }, attrs: { 'data-test': 'some-info' }})

  it('is a labelled, focusable button that keeps the caller data-test', () => {
    const icon = mountComponent().find('[data-test="some-info"]')

    expect(icon.attributes('role')).toBe('button')
    expect(icon.attributes('tabindex')).toBe('0')
    expect(icon.attributes('aria-label')).toBe('About thresholds')
  })

  it('emits click on a click', async () => {
    const wrapper = mountComponent()

    await wrapper.find('[data-test="some-info"]').trigger('click')

    expect(wrapper.emitted('click')).toHaveLength(1)
  })

  it.each(['Enter', ' '])('emits click on the %j key', async (key) => {
    const wrapper = mountComponent()

    await wrapper.find('[data-test="some-info"]').trigger('keydown', { key })

    expect(wrapper.emitted('click')).toHaveLength(1)
  })
})
