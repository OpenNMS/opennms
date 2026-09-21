import LocationsAbout from '@/components/ManageMonitoringLocations/LocationsAbout.vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

describe('LocationsAbout.vue', () => {
  it('renders the help content including the delete prerequisites', () => {
    const wrapper = mount(LocationsAbout)
    expect(wrapper.text()).toContain('What monitoring locations are for')
    expect(wrapper.text()).toContain('How to use this page')
    expect(wrapper.text()).toContain('no nodes are assigned')
    expect(wrapper.text()).toContain('re-registered')
  })
})
