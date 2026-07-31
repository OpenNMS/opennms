import LocationsHelpPanel from '@/components/ManageMonitoringLocations/LocationsHelpPanel.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const TogglePanelStub = { name: 'TogglePanel', template: '<div><slot name="header" /><slot /></div>' }

describe('LocationsHelpPanel.vue', () => {
  it('renders the help content', () => {
    const wrapper = mount(LocationsHelpPanel, {
      global: { plugins: [PrimeVue], stubs: { TogglePanel: TogglePanelStub } }
    })
    expect(wrapper.text()).toContain('About Monitoring Locations')
  })
})
