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

import NodeDetailsLinks from '@/components/Nodes/NodeDetailsLinks.vue'
import { useAuthStore } from '@/stores/authStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

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
})
