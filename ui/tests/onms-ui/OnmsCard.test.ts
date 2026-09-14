import { OnmsCard } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

describe('OnmsCard contract', () => {
  it('forwards title and content slots', () => {
    const wrapper = mount(OnmsCard, {
      global: { plugins: [PrimeVue] },
      slots: { title: '<span class="t">Profiles</span>', content: '<div class="c">body</div>' }
    })
    expect(wrapper.find('.t').exists()).toBe(true)
    expect(wrapper.find('.c').exists()).toBe(true)
  })
})
