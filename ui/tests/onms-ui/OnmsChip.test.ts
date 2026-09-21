import { OnmsChip } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

describe('OnmsChip contract', () => {
  it('maps label and removable', () => {
    const wrapper = mount(OnmsChip, {
      props: { label: 'Down nodes only', removable: true },
      global: { plugins: [PrimeVue] }
    })
    const chip = wrapper.findComponent({ name: 'Chip' })
    expect(chip.props('label')).toBe('Down nodes only')
    expect(chip.props('removable')).toBe(true)
  })

  it('defaults removable to false and forwards remove', async () => {
    const wrapper = mount(OnmsChip, {
      props: { label: 'x' },
      global: { plugins: [PrimeVue] }
    })
    const chip = wrapper.findComponent({ name: 'Chip' })
    expect(chip.props('removable')).toBe(false)
    await chip.vm.$emit('remove', new Event('click'))
    expect(wrapper.emitted('remove')).toHaveLength(1)
  })

  it('renders default slot content', () => {
    const wrapper = mount(OnmsChip, {
      global: { plugins: [PrimeVue] },
      slots: { default: '<span class="inner">2024-01-01</span>' }
    })
    expect(wrapper.find('.inner').exists()).toBe(true)
  })
})
