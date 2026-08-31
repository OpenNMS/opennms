///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import SideMenu from '@/components/Menu/SideMenu.vue'
import { useMenuStore } from '@/stores/menuStore'
import { MainMenu } from '@/types/mainMenu'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

// Keep the suite hermetic: the real menuService reads/writes localStorage
// preferences and calls the REST API. The menu starts collapsed.
vi.mock('@/services/menuService', () => ({
  getIsSideMenuExpanded: vi.fn(() => false),
  setIsSideMenuExpanded: vi.fn(),
  loadIsSideMenuExpanded: vi.fn(),
  getMainMenu: vi.fn().mockResolvedValue(false),
  getNotificationSummary: vi.fn().mockResolvedValue(false)
}))

vi.mock('@/services/logoutService', () => ({
  performLogout: vi.fn()
}))

const ctrlBackslash = (init: KeyboardEventInit = {}) => new KeyboardEvent('keydown', {
  code: 'Backslash',
  ctrlKey: true,
  bubbles: true,
  cancelable: true,
  ...init
})

// The rail's own hover timings (SideMenu.vue). Kept as literals here so a
// change to either constant has to be a deliberate change to these tests too.
const HOVER_OPEN_DELAY_MS = 150
const HOVER_CLOSE_DELAY_MS = 200

const recordTooltip = (el: HTMLElement, binding: { value?: Record<string, unknown> }) => {
  el.setAttribute('data-test-tooltip', JSON.stringify(binding.value ?? {}))
}

// Two top-level entries covering both collapsed-hover cases: 'Info' has a
// submenu (flyout, no tooltip), 'Topology' is a direct link (tooltip, no flyout).
const testMainMenu = {
  username: 'admin',
  baseHref: '/opennms/',
  menus: [
    {
      id: 'info',
      name: 'Info',
      url: null,
      locationMatch: null,
      roles: null,
      items: [
        { id: 'nodes', name: 'Nodes', url: 'nodelist.htm', locationMatch: null, roles: null }
      ]
    },
    {
      id: 'topology',
      name: 'Topology',
      action: 'link',
      url: 'topology.jsp',
      locationMatch: null,
      roles: null
    }
  ]
} as unknown as MainMenu

describe('SideMenu.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useMenuStore>

  const isOpen = () => wrapper.get('nav').classes().includes('onms-side-menu--open')

  beforeEach(() => {
    wrapper = mount(SideMenu, {
      props: {
        pushedSelector: '#side-menu-test-content'
      },
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
        // The real directive renders tooltips into <body>. Record the binding
        // on the element instead, so the tooltip rules can be asserted.
        directives: { 'onms-tooltip': { mounted: recordTooltip, updated: recordTooltip }},
        // TieredMenu wraps its root list in a <transition>, and the list's own
        // class/role land on that component. Stubbing it (the VTU default)
        // would leave them on the stub instead of the <ul>, so the rail's
        // '.p-tieredmenu-root-list > .p-tieredmenu-item' selector — the same
        // one positionFlyouts uses in production — would match nothing.
        stubs: { transition: false }
      },
      attachTo: document.body
    })
    store = useMenuStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    wrapper.unmount()
  })

  describe('toggle button', () => {
    it('toggles the rail and persists the state', async () => {
      expect(isOpen()).toBe(false)

      await wrapper.get('.onms-side-menu__toggle').trigger('click')
      expect(isOpen()).toBe(true)
      expect(store.setSideMenuExpanded).toHaveBeenCalledWith(true)

      await wrapper.get('.onms-side-menu__toggle').trigger('click')
      expect(isOpen()).toBe(false)
      expect(store.setSideMenuExpanded).toHaveBeenCalledWith(false)
    })

    it('exposes state and the keyboard shortcut to assistive technology', async () => {
      const toggle = wrapper.get('.onms-side-menu__toggle')

      expect(toggle.attributes('aria-label')).toBe('Expand menu')
      expect(toggle.attributes('aria-expanded')).toBe('false')
      expect(toggle.attributes('aria-keyshortcuts')).toBe('Control+\\')

      await toggle.trigger('click')
      expect(toggle.attributes('aria-label')).toBe('Collapse menu')
      expect(toggle.attributes('aria-expanded')).toBe('true')
    })
  })

  // Pages the rail can't push with padding-left read this instead: bootstrap.jsp
  // emits #content only on its non-Vaadin branch, so the Vaadin topology page has
  // nothing to push and its own stylesheet tracks the live rail width from here.
  describe('--onms-side-menu-offset', () => {
    const offset = () => document.documentElement.style.getPropertyValue('--onms-side-menu-offset')

    it('publishes the collapsed offset on mount and the expanded one on toggle', async () => {
      expect(offset()).toBe('calc(var(--onms-header-height, 3.75rem) + 0.25rem)')

      await wrapper.get('.onms-side-menu__toggle').trigger('click')
      expect(offset()).toBe('calc(20rem + 0.25rem)')

      await wrapper.get('.onms-side-menu__toggle').trigger('click')
      expect(offset()).toBe('calc(var(--onms-header-height, 3.75rem) + 0.25rem)')
    })

    it('stops publishing it after unmount', () => {
      wrapper.unmount()
      expect(offset()).toBe('')
    })
  })

  describe('Ctrl+\\ global shortcut', () => {
    it('toggles the rail and consumes the event', async () => {
      const event = ctrlBackslash()
      window.dispatchEvent(event)
      await nextTick()

      expect(isOpen()).toBe(true)
      expect(store.setSideMenuExpanded).toHaveBeenCalledWith(true)
      expect(event.defaultPrevented).toBe(true)

      window.dispatchEvent(ctrlBackslash())
      await nextTick()
      expect(isOpen()).toBe(false)
      expect(store.setSideMenuExpanded).toHaveBeenCalledWith(false)
    })

    it.each([
      ['without Ctrl', { ctrlKey: false }],
      ['with Shift also held', { shiftKey: true }],
      ['with Alt also held', { altKey: true }],
      ['with Meta also held', { metaKey: true }],
      ['on key auto-repeat', { repeat: true }],
      ['for a different key', { code: 'KeyM' }]
    ])('does not toggle %s', async (_desc, init) => {
      const event = ctrlBackslash(init)
      window.dispatchEvent(event)
      await nextTick()

      expect(isOpen()).toBe(false)
      expect(store.setSideMenuExpanded).not.toHaveBeenCalled()
      expect(event.defaultPrevented).toBe(false)
    })

    it.each([
      ['an input', 'input'],
      ['a textarea', 'textarea'],
      ['a select', 'select'],
      ['a contenteditable element', 'div']
    ])('does not toggle while typing in %s', async (_desc, tagName) => {
      const el = document.createElement(tagName)

      if (tagName === 'div') {
        // Happy-DOM does not implement isContentEditable, so set the property
        // the component guard reads directly.
        Object.defineProperty(el, 'isContentEditable', { value: true })
      }

      document.body.appendChild(el)

      const event = ctrlBackslash()
      el.dispatchEvent(event)
      await nextTick()

      expect(isOpen()).toBe(false)
      expect(store.setSideMenuExpanded).not.toHaveBeenCalled()
      expect(event.defaultPrevented).toBe(false)

      el.remove()
    })

    it('stops listening after unmount', async () => {
      wrapper.unmount()

      window.dispatchEvent(ctrlBackslash())
      await nextTick()

      expect(store.setSideMenuExpanded).not.toHaveBeenCalled()
    })

    it('closes an open flyout when toggling, but leaves keyboard focus state alone otherwise', async () => {
      const tieredMenu = wrapper.findComponent({ name: 'TieredMenu' }).vm as any
      const hideSpy = vi.spyOn(tieredMenu, 'hide')

      // No flyout open (activeItemPath empty): hide() must not be called, since
      // it would reset TieredMenu's focusedItemInfo mid-keyboard-navigation.
      window.dispatchEvent(ctrlBackslash())
      await nextTick()
      expect(hideSpy).not.toHaveBeenCalled()

      // Open flyout: toggling closes it so it is not left with a stale
      // position after the rail width transition.
      tieredMenu.activeItemPath = [{ key: 'top_test' }]
      window.dispatchEvent(ctrlBackslash())
      await nextTick()
      expect(hideSpy).toHaveBeenCalled()
    })
  })

  // NMS-20273: hovering a top-level entry flies its submenu out, in either rail
  // state — what the rail never does on hover is change its own width. While
  // collapsed, entries that are direct links (no submenu) get a label tooltip
  // on the same beat instead; expanded, their labels are already visible.
  describe('hover flyouts', () => {
    const menu = () => wrapper.findComponent({ name: 'TieredMenu' }).vm as any
    const rootItems = () => wrapper.findAll('.p-tieredmenu-root-list > .p-tieredmenu-item')
    const activePath = () => menu().activeItemPath

    const dwellOn = async (index: number) => {
      await rootItems()[index].trigger('mouseover')
      vi.advanceTimersByTime(HOVER_OPEN_DELAY_MS)
      await nextTick()
    }

    beforeEach(async () => {
      store.mainMenu = testMainMenu
      await nextTick()
      vi.useFakeTimers()
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('renders both top-level entries', () => {
      expect(rootItems()).toHaveLength(2)
    })

    it('opens the flyout only after the dwell elapses, without widening the rail', async () => {
      await rootItems()[0].trigger('mouseover')
      vi.advanceTimersByTime(HOVER_OPEN_DELAY_MS - 1)
      await nextTick()
      expect(activePath()).toHaveLength(0)

      vi.advanceTimersByTime(1)
      await nextTick()
      expect(activePath()).toHaveLength(1)
      expect(isOpen()).toBe(false)
    })

    it('does not open a flyout when the pointer leaves before the dwell elapses', async () => {
      await rootItems()[0].trigger('mouseover')
      await wrapper.get('nav').trigger('mouseleave')
      vi.advanceTimersByTime(HOVER_OPEN_DELAY_MS)
      await nextTick()

      expect(activePath()).toHaveLength(0)
    })

    it('switches between icons without a second dwell, and a direct link closes the flyout', async () => {
      await dwellOn(0)
      expect(activePath()).toHaveLength(1)

      // Real pointer movement fires both; once TieredMenu is dirty its own
      // mouseenter handler — bound on the item's content wrapper — does the
      // switching, with no further dwell.
      await rootItems()[1].trigger('mouseover')
      await rootItems()[1].get('.p-tieredmenu-item-content').trigger('mouseenter')
      await nextTick()

      expect(activePath()).toHaveLength(0)
    })

    it('behaves the same way while the rail is pinned open', async () => {
      await wrapper.get('.onms-side-menu__toggle').trigger('click')
      expect(isOpen()).toBe(true)

      await dwellOn(0)
      expect(activePath()).toHaveLength(1)

      await wrapper.get('nav').trigger('mouseleave')
      vi.advanceTimersByTime(HOVER_CLOSE_DELAY_MS)
      await nextTick()
      expect(activePath()).toHaveLength(0)
    })

    it('closes the flyout and leaves hover mode once the pointer leaves the rail', async () => {
      await dwellOn(0)
      expect(menu().dirty).toBe(true)

      await wrapper.get('nav').trigger('mouseleave')
      vi.advanceTimersByTime(HOVER_CLOSE_DELAY_MS)
      await nextTick()

      expect(activePath()).toHaveLength(0)
      expect(menu().dirty).toBe(false)
    })

    it('keeps the flyout open when the pointer returns within the close grace period', async () => {
      await dwellOn(0)

      await wrapper.get('nav').trigger('mouseleave')
      vi.advanceTimersByTime(HOVER_CLOSE_DELAY_MS - 50)
      await rootItems()[0].trigger('mouseover')
      vi.advanceTimersByTime(HOVER_CLOSE_DELAY_MS)
      await nextTick()

      expect(activePath()).toHaveLength(1)
    })

    it('tooltips only the collapsed top-level icons that have no submenu', async () => {
      const tooltip = (index: number) => JSON.parse(
        rootItems()[index].get('.onms-side-menu__link').attributes('data-test-tooltip') as string
      )

      // 'Info' has a submenu: the flyout is the affordance, so no tooltip.
      expect(tooltip(0).disabled).toBe(true)

      // 'Topology' is a direct link: tooltip, on the same beat as the flyout.
      expect(tooltip(1).disabled).toBe(false)
      expect(tooltip(1).value).toBe('Topology')
      expect(tooltip(1).showDelay).toBe(HOVER_OPEN_DELAY_MS)

      // Pinned open, every label is already visible.
      await wrapper.get('.onms-side-menu__toggle').trigger('click')
      expect(tooltip(1).disabled).toBe(true)
    })
  })
})
