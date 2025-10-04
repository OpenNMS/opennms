import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import EventConfiguration from '@/containers/EventConfiguration.vue'

describe('EventConfig.vue', () => {
  it('renders heading text', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true
        }
      }
    })

    expect(wrapper.find('h1').text()).toBe('Event Configuration')

    expect(wrapper.findComponent(EventConfigTabContainer).exists()).toBe(true)
  })
})

