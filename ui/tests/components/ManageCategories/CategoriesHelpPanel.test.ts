import CategoriesHelpPanel from '@/components/ManageCategories/CategoriesHelpPanel.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const TogglePanelStub = { name: 'TogglePanel', template: '<div><slot name="header" /><slot /></div>' }

describe('CategoriesHelpPanel.vue', () => {
  it('renders the help content', () => {
    const wrapper = mount(CategoriesHelpPanel, {
      global: { plugins: [PrimeVue], stubs: { TogglePanel: TogglePanelStub } }
    })
    expect(wrapper.text()).toContain('About Surveillance Categories')
    expect(wrapper.text()).toContain('surveillance category')
  })
})
