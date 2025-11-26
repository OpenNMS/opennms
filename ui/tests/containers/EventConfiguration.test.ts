import { mount } from '@vue/test-utils'
import { describe, it, expect, beforeEach } from 'vitest'
import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import EventConfiguration from '@/containers/EventConfiguration.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { setActivePinia } from 'pinia'
import { createTestingPinia } from '@pinia/testing'

describe('EventConfig.vue', () => {
  let store: ReturnType<typeof useEventConfigStore>

  beforeEach(() => {
    setActivePinia(createTestingPinia())
    store = useEventConfigStore()
  })

  it('renders heading text', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true
        }
      }
    })

    // use the store so it is not reported as unused
    expect(store).toBeDefined()

    expect(wrapper.find('h1').text()).toBe('Event Configuration')

    expect(wrapper.findComponent(EventConfigTabContainer).exists()).toBe(true)
  })
})

