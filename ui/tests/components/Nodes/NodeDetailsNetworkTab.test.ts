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

import NodeDetailsNetworkTab from '@/components/Nodes/NodeDetailsNetworkTab.vue'
import { useEnlinkdStore } from '@/stores/enlinkdStore'
import { createTestingPinia } from '@pinia/testing'
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useRoute } from 'vue-router'

vi.mock('vue-router', async () => {
  const { reactive } = await import('vue')
  const route = reactive({ params: { id: '144' }})

  return { useRoute: () => route }
})

enableAutoUnmount(afterEach)

const all = {
  lldp: { lldpChassisId: 'c1', lldpSysName: 'sw1', lldpLastPollTime: 'T1' },
  cdp: { cdpGlobalDeviceId: 'd1', cdpGlobalRun: 'true', cdpLastPollTime: 'T1' },
  ospf: { ospfRouterId: '10.0.0.1', ospfAdminStat: 'enabled', ospfVersionNumber: 2, ospfLastPollTime: 'T1' },
  isis: { isisSysID: 's1', isisSysAdminState: 'on', isisLastPollTime: 'T1' },
  bridges: [{ baseBridgeAddress: 'aa', baseType: 'srt', baseNumPorts: 2, vlan: 1, vlanname: 'default' }, { baseNumPorts: 1 }]
}

describe('NodeDetailsNetworkTab.vue', () => {
  let store: ReturnType<typeof useEnlinkdStore>

  // Resolves as the store would: the slice replaced and stamped with the node id.
  const answerWith = (elements: unknown, success = true) => {
    const s = store
    s.getNodeElements = vi.fn(async (nodeId: string) => {
      s.nodeElements = elements as never
      s.nodeElementsNodeId = nodeId

      return { success, message: '' }
    }) as never
  }

  const mountTab = (setup: () => void) => {
    const pinia = createTestingPinia({ createSpy: vi.fn })
    store = useEnlinkdStore(pinia)
    setup()

    return mount(NodeDetailsNetworkTab, { global: { plugins: [pinia, PrimeVue] }})
  }

  beforeEach(() => {
    ;(useRoute() as any).params.id = '144'
  })

  it('fetches the node\'s elements when shown', async () => {
    mountTab(() => answerWith(all))
    await flushPromises()

    expect(store.getNodeElements).toHaveBeenCalledWith('144')
  })

  it('shows a panel for each protocol with data, link layer left and routing right', async () => {
    const wrapper = mountTab(() => answerWith(all))
    await flushPromises()

    const columns = wrapper.findAll('.onms-col-6')
    const panelsIn = (i: number) => columns[i].findAll('[data-test$="-panel"]').map(p => p.attributes('data-test'))

    expect(panelsIn(0)).toEqual(['lldp-panel', 'cdp-panel', 'bridge-panel'])
    expect(panelsIn(1)).toEqual(['ospf-panel', 'isis-panel'])
    expect(wrapper.find('[data-test="lldp-panel"]').text()).toContain('sw1')
  })

  it('shows only the panels with data', async () => {
    const wrapper = mountTab(() => answerWith({ ospf: all.ospf, bridges: [] }))
    await flushPromises()

    expect(wrapper.findAll('[data-test$="-panel"]').map(p => p.attributes('data-test'))).toEqual(['ospf-panel'])
  })

  it('gives each bridge its own group', async () => {
    const wrapper = mountTab(() => answerWith(all))
    await flushPromises()

    const titles = wrapper.findAll('[data-test="bridge-group"] .bridge-group-title').map(t => t.text())
    expect(titles).toEqual(['Vlan default (vlanid 1) · 2 ports', 'Default · 1 port'])
  })

  it('says so when the node has no network information', async () => {
    const wrapper = mountTab(() => answerWith({ bridges: [] }))
    await flushPromises()

    expect(wrapper.find('[data-test="network-empty"]').exists()).toBe(true)
    expect(wrapper.findAll('[data-test$="-panel"]')).toHaveLength(0)
  })

  it('says it is loading until the first answer', () => {
    const wrapper = mountTab(() => {
      store.getNodeElements = vi.fn(() => new Promise(() => undefined)) as never
    })

    expect(wrapper.find('[data-test="network-loading"]').exists()).toBe(true)
  })

  it('says it could not load when nothing came back', async () => {
    const wrapper = mountTab(() => answerWith({ bridges: [] }, false))
    await flushPromises()

    expect(wrapper.find('[data-test="network-failed"]').exists()).toBe(true)
  })

  it('shows what did load, and says the rest did not', async () => {
    const wrapper = mountTab(() => answerWith({ lldp: all.lldp, bridges: [] }, false))
    await flushPromises()

    expect(wrapper.find('[data-test="lldp-panel"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="network-partial"]').exists()).toBe(true)
  })

  // KeepAlive keeps the tab across nodes; it follows the route like the other tabs' tables.
  it('refetches for a new node, and does not show the previous node\'s panels meanwhile', async () => {
    const wrapper = mountTab(() => answerWith(all))
    await flushPromises()

    store.getNodeElements = vi.fn(() => new Promise(() => undefined)) as never
    ;(useRoute() as any).params.id = '161'
    await flushPromises()

    expect(store.getNodeElements).toHaveBeenCalledWith('161')
    expect(wrapper.findAll('[data-test$="-panel"]')).toHaveLength(0)
    expect(wrapper.find('[data-test="network-loading"]').exists()).toBe(true)
  })
})
