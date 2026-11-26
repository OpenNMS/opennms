import XmlObjectTable from '@/components/XmlCollectionConfiguration/XmlObjectTable.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import type { XmlObject } from '@/types/xmlCollectionConfig'

const objects = (): XmlObject[] => [{ name: 'status', type: 'gauge', xpath: 'status', mappings: [{ from: 'green', to: '0' }] }]

const mountTable = (modelValue = objects()) =>
  mount(XmlObjectTable, { props: { modelValue, idPrefix: 't' }, global: { plugins: [PrimeVue] }, attachTo: document.body })

describe('XmlObjectTable.vue', () => {
  it('edits an object', async () => {
    const wrapper = mountTable()
    await wrapper.find('[data-test="t-object-xpath-0"]').setValue('health/status')

    expect(wrapper.emitted('update:modelValue')?.[0][0]).toEqual([{ ...objects()[0], xpath: 'health/status' }])
    wrapper.unmount()
  })

  it('adds and removes objects', async () => {
    const wrapper = mountTable()
    await wrapper.find('[data-test="t-object-add"]').trigger('click')
    await wrapper.find('[data-test="t-object-remove-0"]').trigger('click')

    expect(wrapper.emitted('update:modelValue')?.[0][0]).toHaveLength(2)
    expect(wrapper.emitted('update:modelValue')?.[1][0]).toEqual([])
    wrapper.unmount()
  })

  it('edits the mappings in a dialog and applies them', async () => {
    const wrapper = mountTable()
    expect(wrapper.find('[data-test="t-object-mappings-0"]').text()).toBe('1 mapping')

    await wrapper.find('[data-test="t-object-mappings-0"]').trigger('click')
    const dialog = document.body
    ;(dialog.querySelector('[data-test="xml-mapping-add"]') as HTMLElement).click()
    await wrapper.vm.$nextTick()
    const to = dialog.querySelector('[data-test="xml-mapping-to-1"]') as HTMLInputElement
    to.value = '9'
    to.dispatchEvent(new Event('input'))
    await wrapper.vm.$nextTick()
    ;(dialog.querySelector('[data-test="xml-mappings-apply"]') as HTMLElement).click()
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')?.[0][0]).toEqual([{ ...objects()[0], mappings: [{ from: 'green', to: '0' }, { from: null, to: '9' }] }])
    wrapper.unmount()
  })

  it('keeps an aliased stored type selectable', () => {
    const wrapper = mountTable([{ name: 'x', type: 'COUNTER32', xpath: 'x', mappings: [] }])

    expect(wrapper.find('[data-test="t-object-type-0"]').text()).toContain('COUNTER32')
    wrapper.unmount()
  })
})
