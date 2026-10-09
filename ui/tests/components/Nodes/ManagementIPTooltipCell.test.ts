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

import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import PrimeVue from 'primevue/config'
import { OnmsTooltip } from '@opennms/onms-ui'
import ManagementIPTooltipCell from '@/components/Nodes/ManagementIPTooltipCell.vue'
import { useMenuStore } from '@/stores/menuStore'
import { IpInterface, Node } from '@/types'

// The cell builds its own link off menuStore now, rather than taking a computeNodeIpInterfaceLink
// prop from NodesTable — so the link is exercised here, at the component that renders it.
const node = { id: '42', label: 'srv-42' } as unknown as Node

const mountCell = (ipInterfaces: Partial<IpInterface>[]) => {
  const pinia = createTestingPinia({ createSpy: vi.fn })
  useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as never

  return mount(ManagementIPTooltipCell, {
    props: {
      node,
      nodeToIpInterfaceMap: new Map([['42', ipInterfaces as IpInterface[]]])
    },
    global: {
      plugins: [pinia, PrimeVue],
      directives: { 'onms-tooltip': OnmsTooltip }
    }
  })
}

const managedPrimary = (ipAddress: string) => ({ id: '1', ipAddress, isManaged: 'M', snmpPrimary: 'P' })

describe('ManagementIPTooltipCell.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('links the address to the interface page for this node', () => {
    const wrapper = mountCell([managedPrimary('10.0.0.44')])

    expect(wrapper.find('a').text()).toBe('10.0.0.44')
    expect(wrapper.find('a').attributes('href'))
      .toBe('/opennms/element/interface.jsp?node=42&intf=10.0.0.44')
  })

  // The reason the link moved into linkUtils: this call site used to interpolate the address
  // raw, so a zone index's '%' reached the legacy page as the start of an escape sequence.
  it('encodes an IPv6 address', () => {
    const wrapper = mountCell([managedPrimary('fe80::1%eth0')])

    expect(wrapper.find('a').attributes('href'))
      .toBe('/opennms/element/interface.jsp?node=42&intf=fe80%3A%3A1%25eth0')
  })

  it('renders nothing when the node has no interface to show', () => {
    const wrapper = mountCell([])

    expect(wrapper.find('a').exists()).toBe(false)
  })
})
