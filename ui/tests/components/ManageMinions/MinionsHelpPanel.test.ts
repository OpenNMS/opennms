import MinionsHelpPanel from '@/components/ManageMinions/MinionsHelpPanel.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const TogglePanelStub = { name: 'TogglePanel', template: '<div><slot name="header" /><slot /></div>' }

describe('MinionsHelpPanel.vue', () => {
  it('renders the help content', () => {
    const wrapper = mount(MinionsHelpPanel, {
      global: { plugins: [PrimeVue], stubs: { TogglePanel: TogglePanelStub } }
    })
    expect(wrapper.text()).toContain('About Minions')
  })
})
