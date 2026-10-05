import { OnmsMenubar, OnmsMenuItem } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

const items: OnmsMenuItem[] = [
  {
    label: 'Inventory',
    items: [
      { label: 'Assets', command: () => {} },
      { label: 'Metadata', command: () => {} }
    ]
  },
  { label: 'Graphs', items: [{ label: 'Resource Graphs', command: () => {} }] }
]

const mountBar = (props: object = {}, slots: Record<string, string> = {}) =>
  mount(OnmsMenubar, { props: { items, ...props }, slots, global: { plugins: [PrimeVue] }})

describe('OnmsMenubar contract', () => {
  it('maps items to the PrimeVue model', () => {
    expect(mountBar().findComponent({ name: 'Menubar' }).props('model')).toEqual(items)
  })

  it('renders the top-level items in a row, their children as collapsed submenus', () => {
    const wrapper = mountBar()

    // The submenus are rendered but hidden; only the root list's own items show in the bar.
    const topLevel = Array.from(wrapper.find('.p-menubar-root-list').element.children)
      .map(li => li.querySelector('.p-menubar-item-label')?.textContent?.trim())
      .filter(Boolean)

    expect(topLevel).toEqual(['Inventory', 'Graphs'])
    expect(wrapper.findAll('.p-menubar-submenu-icon')).toHaveLength(2)
  })

  it('runs an item\'s command when it is chosen', async () => {
    const command = vi.fn()
    const wrapper = mountBar({ items: [{ label: 'Go', command }] })

    await wrapper.find('.p-menubar-item-content').trigger('click')

    expect(command).toHaveBeenCalledWith(expect.objectContaining({ item: expect.objectContaining({ label: 'Go' }) }))
  })

  it('forwards the #start and #end slots', () => {
    const wrapper = mountBar({}, { start: '<span class="custom-start">S</span>', end: '<span class="custom-end">E</span>' })

    expect(wrapper.find('.custom-start').exists()).toBe(true)
    expect(wrapper.find('.custom-end').exists()).toBe(true)
  })

  it('forwards the #item slot', () => {
    const wrapper = mountBar({ items: [{ label: 'X' }] }, {
      item: '<template #item="{ item }"><span class="custom-item">{{ item.label }}</span></template>'
    })

    expect(wrapper.find('.custom-item').text()).toBe('X')
  })

  it('passes unsafePt through as pt', () => {
    const pt = { root: { class: 'escape-hatch' }}

    expect(mountBar({ unsafePt: pt }).findComponent({ name: 'Menubar' }).props('pt')).toEqual(pt)
  })
})
