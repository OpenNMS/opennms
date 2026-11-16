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

import NodeDetailsLinks from '@/components/Nodes/NodeDetails/NodeDetailsLinks.vue'
import { useAuthStore } from '@/stores/authStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'

// happy-dom does no layout, so the measured width of the full row of menus is set by each test.
const fullRowWidth = ref(0)
vi.mock('@vueuse/core', async importOriginal => ({
  ...(await importOriginal<typeof import('@vueuse/core')>()),
  useElementSize: () => ({ width: fullRowWidth, height: ref(0) })
}))

const node = { id: 42, label: 'srv-42', foreignSource: 'fs', foreignId: 'fid', assetRecord: {}} as any

// useRole binds the first pinia this file creates (see NodeActionsDropdown.test.ts), so one shared
// pinia, with roles set on it per test.
const pinia = createTestingPinia({ createSpy: vi.fn })
const authStore = useAuthStore(pinia)

const setRoles = (...roles: string[]) => {
  authStore.whoAmI = { ...authStore.whoAmI, roles } as never
}

const mountLinks = (props: object = {}) =>
  mount(NodeDetailsLinks, {
    props: { baseHref: '/opennms/', node, ...props },
    global: { plugins: [pinia, PrimeVue] }
  })

type Item = { label: string, items?: Item[], command?: () => void }

const menus = (wrapper: ReturnType<typeof mountLinks>) => (wrapper.vm as any).items as Item[]
const labels = (wrapper: ReturnType<typeof mountLinks>) => menus(wrapper).map(m => [m.label, m.items!.map(i => i.label)])

describe('NodeDetailsLinks.vue', () => {
  beforeEach(() => {
    setRoles('ROLE_ADMIN')
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('renders a menubar inside a labelled nav', () => {
    const wrapper = mountLinks()

    expect(wrapper.find('nav[aria-label="Node links"]').exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'Menubar' }).exists()).toBe(true)
  })

  describe('collapsing to the hamburger button', () => {
    const bars = (wrapper: ReturnType<typeof mountLinks>) => wrapper.findAllComponents({ name: 'OnmsMenubar' })

    beforeEach(() => {
      fullRowWidth.value = 400
    })

    it('leaves it to the viewport when the page gives no width', () => {
      expect(bars(mountLinks())[0].props('collapsed')).toBeUndefined()
    })

    it('shows the full row while the menus fit, and collapses once they do not', async () => {
      const wrapper = mountLinks({ availableWidth: 400 })
      expect(bars(wrapper)[0].props('collapsed')).toBe(false)

      await wrapper.setProps({ availableWidth: 399 })
      expect(bars(wrapper)[0].props('collapsed')).toBe(true)
    })

    // The bar shown, not the measured copy.
    const shownBar = (wrapper: ReturnType<typeof mountLinks>) =>
      bars(wrapper).find(bar => !bar.element.closest?.('.node-details-links-measure'))

    it('collapses when the badges leave no room at all', () => {
      expect(shownBar(mountLinks({ availableWidth: 0 }))!.props('collapsed')).toBe(true)
    })

    // OnmsMenubar mounts again for each change of `collapsed`, so a guess shown before the
    // measurements would mean a mount for the guess and another for the answer.
    it('shows no bar until the page has measured its room', async () => {
      const wrapper = mountLinks({ availableWidth: null })
      expect(shownBar(wrapper)).toBeUndefined()
      expect(wrapper.find('.node-details-links-measure').exists()).toBe(true)

      await wrapper.setProps({ availableWidth: 300 })
      expect(shownBar(wrapper)!.props('collapsed')).toBe(true)
    })

    it('shows no bar until the full row has been measured', async () => {
      fullRowWidth.value = 0
      const wrapper = mountLinks({ availableWidth: 300 })
      expect(shownBar(wrapper)).toBeUndefined()

      fullRowWidth.value = 400
      await wrapper.vm.$nextTick()
      expect(shownBar(wrapper)!.props('collapsed')).toBe(true)
    })

    it('keeps its answer while the page has no measurement', async () => {
      const wrapper = mountLinks({ availableWidth: 300 })
      const bar = shownBar(wrapper)!

      await wrapper.setProps({ availableWidth: null })
      expect(shownBar(wrapper)!.props('collapsed')).toBe(true)
      // The same bar, not one mounted again.
      expect(shownBar(wrapper)!.vm.$.uid).toBe(bar.vm.$.uid)
    })

    // Queries for the links (the smoke tests use //nav[@data-test='node-details-links']//...) must
    // find each menu once, not again in the copy.
    it('keeps the measured copy out of the nav', () => {
      const wrapper = mountLinks({ availableWidth: 500 })
      const nav = wrapper.get('nav[data-test="node-details-links"]')

      expect(nav.find('.node-details-links-measure').exists()).toBe(false)
      expect(nav.findAll('.p-menubar')).toHaveLength(1)
      expect(wrapper.find('.node-details-links-measure .p-menubar').exists()).toBe(true)
    })

    // The page lays out the wrapper; the smoke tests read data-* from the nav.
    it('puts class and style on the wrapper, and other attributes on the nav', () => {
      const wrapper = mountLinks({ class: 'node-summary-links', style: 'margin-left: auto', 'data-requisition-checked': 'true' })
      const nav = wrapper.get('nav')

      expect(wrapper.classes()).toEqual(expect.arrayContaining(['node-details-links', 'node-summary-links']))
      expect(wrapper.attributes('style')).toContain('margin-left: auto')
      expect(wrapper.attributes('data-requisition-checked')).toBeUndefined()
      expect(nav.attributes('data-requisition-checked')).toBe('true')
      expect(nav.classes()).not.toContain('node-summary-links')
    })

    // The second bar is the one measured: it has to stay a full row, and out of reach.
    it('measures a full row that is hidden from assistive technology and input', () => {
      const wrapper = mountLinks({ availableWidth: 100 })
      const measure = wrapper.find('.node-details-links-measure')

      expect(bars(wrapper)[1].props('collapsed')).toBe(false)
      expect(measure.attributes('aria-hidden')).toBe('true')
      expect(measure.attributes()).toHaveProperty('inert')
    })
  })

  it('groups the links under Inventory, Monitoring, Graphs and Admin', () => {
    const wrapper = mountLinks({ triggerNodeInfo: vi.fn(), snmpPrimaryIpAddress: '10.0.0.44', existsInRequisition: true })

    expect(labels(wrapper)).toEqual([
      ['Inventory', ['Info', 'Assets', 'Metadata', 'Hardware Inventory', 'Surveillance Categories', 'Node Link Details']],
      ['Monitoring', ['Alarms', 'Events', 'Outages', 'Availability']],
      ['Graphs', ['Resource Graphs', 'Topology Map']],
      ['Admin', ['Node Rescan', 'Update SNMP Information', 'Admin / Node Management', 'Schedule an Outage', 'Edit in Requisition']]
    ])
  })

  it('navigates a link item under baseHref', () => {
    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)
    const wrapper = mountLinks()

    menus(wrapper).find(m => m.label === 'Monitoring')!.items!.find(i => i.label === 'Alarms')!.command!()

    expect(assign).toHaveBeenCalledWith('/opennms/alarm/list.htm?filter=node%3D42')
  })

  it('opens the info dialog from Info', () => {
    const triggerNodeInfo = vi.fn()
    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)
    const wrapper = mountLinks({ triggerNodeInfo })

    menus(wrapper)[0].items![0].command!()

    expect(triggerNodeInfo).toHaveBeenCalled()
    expect(assign).not.toHaveBeenCalled()
  })

  it('leaves Info out without a handler for it', () => {
    expect(menus(mountLinks())[0].items!.map(i => i.label)).not.toContain('Info')
  })

  // Admin-only links go, and with them the whole Admin menu.
  it('shows a non-admin no Admin menu', () => {
    setRoles('ROLE_USER')

    expect(menus(mountLinks({ triggerNodeInfo: vi.fn() })).map(m => m.label)).toEqual(['Inventory', 'Monitoring', 'Graphs'])
  })

  // Edit in Requisition is admin-or-provision, so it alone keeps the Admin menu for a provision user.
  it('shows a provision user an Admin menu with only Edit in Requisition', () => {
    setRoles('ROLE_USER', 'ROLE_PROVISION')

    const admin = menus(mountLinks({ existsInRequisition: true })).find(m => m.label === 'Admin')
    expect(admin?.items!.map(i => i.label)).toEqual(['Edit in Requisition'])
  })

  describe('Services menu', () => {
    const services = [
      { serviceName: 'HTTP', ipAddress: '10.0.0.1' },
      { serviceName: 'SSH', ipAddress: '10.0.0.1' }
    ]

    it('comes just before Admin, with a link per service the node has', () => {
      const all = menus(mountLinks({ services }))

      expect(all.map(m => m.label)).toEqual(['Inventory', 'Monitoring', 'Graphs', 'Services', 'Admin'])
      expect(all.find(m => m.label === 'Services')!.items).toEqual([
        { label: 'SSH', url: 'ssh://10.0.0.1' },
        { label: 'HTTP', url: 'http://10.0.0.1/', target: '_blank' }
      ])
    })

    it('comes last when there is no Admin menu', () => {
      setRoles('ROLE_USER')

      expect(menus(mountLinks({ services })).map(m => m.label)).toEqual(['Inventory', 'Monitoring', 'Graphs', 'Services'])
    })

    it('is left out for a node with none of the services', () => {
      expect(menus(mountLinks({ services: [{ serviceName: 'ICMP', ipAddress: '10.0.0.1' }] })).map(m => m.label)).not.toContain('Services')
    })
  })

  // The legacy page's confirmAssetEdit(): a requisition sync or rescan rolls asset edits back.
  describe('Assets confirmation', () => {
    const assets = (wrapper: ReturnType<typeof mountLinks>) =>
      menus(wrapper).find(m => m.label === 'Inventory')!.items!.find(i => i.label === 'Assets')!
    const dialog = (wrapper: ReturnType<typeof mountLinks>) => wrapper.findComponent({ name: 'AssetEditConfirmDialog' })

    it('asks before opening the asset editor for a node from a requisition', async () => {
      const assign = vi.fn()
      vi.stubGlobal('location', { assign } as any)
      const wrapper = mountLinks()

      assets(wrapper).command!()
      await wrapper.vm.$nextTick()

      expect(assign).not.toHaveBeenCalled()
      expect(dialog(wrapper).props('visible')).toBe(true)
      expect(dialog(wrapper).props('foreignSource')).toBe('fs')

      dialog(wrapper).vm.$emit('ok')
      await wrapper.vm.$nextTick()

      expect(assign).toHaveBeenCalledWith('/opennms/asset/modify.jsp?node=42')
      expect(dialog(wrapper).props('visible')).toBe(false)
    })

    it('stays on the page when the user cancels', async () => {
      const assign = vi.fn()
      vi.stubGlobal('location', { assign } as any)
      const wrapper = mountLinks()

      assets(wrapper).command!()
      await wrapper.vm.$nextTick()
      dialog(wrapper).vm.$emit('cancel')
      await wrapper.vm.$nextTick()

      expect(assign).not.toHaveBeenCalled()
      expect(dialog(wrapper).props('visible')).toBe(false)
    })

    it('goes straight there for a node not from a requisition', () => {
      const assign = vi.fn()
      vi.stubGlobal('location', { assign } as any)
      const wrapper = mountLinks({ node: { ...node, foreignSource: null }})

      assets(wrapper).command!()

      expect(assign).toHaveBeenCalledWith('/opennms/asset/modify.jsp?node=42')
    })

    it('goes straight there for a read-only user', () => {
      setRoles('ROLE_USER', 'ROLE_READONLY')
      const assign = vi.fn()
      vi.stubGlobal('location', { assign } as any)
      const wrapper = mountLinks()

      assets(wrapper).command!()

      expect(assign).toHaveBeenCalledWith('/opennms/asset/modify.jsp?node=42')
    })
  })
})
