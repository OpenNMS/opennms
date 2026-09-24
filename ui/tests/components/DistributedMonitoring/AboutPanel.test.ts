import AboutPanel from '@/components/DistributedMonitoring/AboutPanel.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const mountPanel = () => mount(AboutPanel, { global: { plugins: [PrimeVue] }})

// the panel content is toggled with v-show, so an inline display: none means collapsed
const contentHidden = (wrapper: ReturnType<typeof mountPanel>) =>
  (wrapper.find('.p-panel-content-container').attributes('style') ?? '').includes('display: none')

describe('AboutPanel.vue', () => {
  it('renders the header and the section titles', () => {
    const wrapper = mountPanel()
    expect(wrapper.text()).toContain('About Minions and Locations')
    expect(wrapper.text()).toContain('Monitoring locations')
    expect(wrapper.text()).toContain('Created automatically')
    expect(wrapper.text()).toContain('Monitoring location and version')
    expect(wrapper.text()).toContain('org.opennms.minion.controller')
  })

  it('is collapsed by default and expands on toggle', async () => {
    const wrapper = mountPanel()
    const toggle = wrapper.find('.p-panel-toggle-button')
    expect(toggle.attributes('aria-expanded')).toBe('false')
    expect(contentHidden(wrapper)).toBe(true)

    await toggle.trigger('click')
    expect(toggle.attributes('aria-expanded')).toBe('true')
    expect(contentHidden(wrapper)).toBe(false)

    await toggle.trigger('click')
    expect(contentHidden(wrapper)).toBe(true)
  })
})
