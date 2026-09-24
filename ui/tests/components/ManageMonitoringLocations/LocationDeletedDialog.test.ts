import LocationDeletedDialog from '@/components/ManageMonitoringLocations/LocationDeletedDialog.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><h2 data-test="header">{{ header }}</h2><slot /><slot name="footer" /></div>'
}

const mountDialog = (summary: Record<string, unknown>) => mount(LocationDeletedDialog, {
  props: { visible: true, summary: summary as any },
  global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}
})

const line = (wrapper: ReturnType<typeof mountDialog>, name: string) => wrapper.find(`[data-test="${name}-line"]`).text()

describe('LocationDeletedDialog.vue', () => {
  it('lists what went with the location', () => {
    const wrapper = mountDialog({ name: 'Raleigh', applications: [{ id: 1, name: 'Web Shop' }, { id: 2, name: 'VPN' }], outageCount: 3 })
    expect(wrapper.find('[data-test="header"]').text()).toBe('Monitoring location Raleigh deleted')
    expect(line(wrapper, 'perspective')).toBe('Removed as a perspective from: Web Shop, VPN')
    expect(line(wrapper, 'outages')).toBe('3 perspective outages deleted.')
    expect(line(wrapper, 'minions')).toBe('Minions that still point at Raleigh keep the old name until they are re-registered.')
  })

  it('says when nothing depended on it', () => {
    const wrapper = mountDialog({ name: 'Raleigh', applications: [], outageCount: 0 })
    expect(line(wrapper, 'perspective')).toBe('Was not used as a perspective.')
    expect(line(wrapper, 'outages')).toBe('No perspective outage history.')
  })

  it('stays honest when the lookups were unavailable', () => {
    const wrapper = mountDialog({ name: 'Raleigh', applications: null, outageCount: null })
    expect(line(wrapper, 'perspective')).toBe('Removed as a perspective from any application that used it.')
    expect(line(wrapper, 'outages')).toBe('Any perspective outage history was deleted.')
  })

  it('has a single Close button', async () => {
    const wrapper = mountDialog({ name: 'Raleigh', applications: [], outageCount: 1 })
    expect(line(wrapper, 'outages')).toBe('1 perspective outage deleted.')
    expect(wrapper.findAll('button')).toHaveLength(1)
    await wrapper.find('[data-test="close-button"]').trigger('click')
    expect(wrapper.emitted('update:visible')).toEqual([[false]])
  })
})
