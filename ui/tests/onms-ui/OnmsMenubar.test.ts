import { OnmsMenubar, OnmsMenuItem } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

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

    expect(mountBar({ unsafePt: pt }).findComponent({ name: 'Menubar' }).props('pt')).toEqual(expect.objectContaining(pt))
  })

  describe('collapsed', () => {
    const isMobile = (wrapper: ReturnType<typeof mountBar>) => wrapper.find('.p-menubar').classes('p-menubar-mobile')

    it('leaves the viewport breakpoint to PrimeVue when it is left out', () => {
      expect(mountBar().findComponent({ name: 'Menubar' }).props('breakpoint')).toBe('960px')
    })

    // PrimeVue sets the mobile state in its own mounted hook, so the class lands a tick later.
    it('shows the hamburger when true and the full row when false, whatever the viewport', async () => {
      const collapsed = mountBar({ collapsed: true })
      const expanded = mountBar({ collapsed: false })
      await nextTick()

      expect(isMobile(collapsed)).toBe(true)
      expect(isMobile(expanded)).toBe(false)
    })

    // PrimeVue reads its breakpoint only when it mounts, so a change has to reach it some other way.
    it('follows a change after mounting', async () => {
      const wrapper = mountBar({ collapsed: false })

      await wrapper.setProps({ collapsed: true })
      expect(isMobile(wrapper)).toBe(true)

      await wrapper.setProps({ collapsed: false })
      expect(isMobile(wrapper)).toBe(false)
    })
  })

  // PrimeVue renders the target but no rel, so a new tab could reach back through window.opener.
  it('adds rel="noopener noreferrer" to an item that opens in a new tab, and only to that one', () => {
    const wrapper = mountBar({ items: [
      { label: 'Docs', url: 'https://example.com', target: '_blank' },
      { label: 'Here', url: '/here' }
    ] })

    const links = wrapper.findAll('a.p-menubar-item-link')
    expect(links[0].attributes('target')).toBe('_blank')
    expect(links[0].attributes('rel')).toBe('noopener noreferrer')
    expect(links[1].attributes('rel')).toBeUndefined()
  })
})
