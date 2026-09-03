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

import NodeInterfacePicker from '@/components/ScheduledOutages/NodeInterfacePicker.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/scheduledOutagesService', () => ({
  searchOutageNodes: vi.fn().mockResolvedValue([]),
  searchOutageInterfaces: vi.fn().mockResolvedValue([])
}))

const mountPicker = (props: any) => mount(NodeInterfacePicker, {
  props,
  global: { plugins: [PrimeVue] }
})

describe('NodeInterfacePicker.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows resolved node labels and flags ids no longer in inventory', () => {
    const wrapper = mountPicker({
      mode: 'node',
      items: [{ id: 5 }, { id: 99 }],
      nodeLabels: { 5: 'core-router' }
    })
    const chips = wrapper.findAll('[data-test="picker-node-chip"]').map(c => c.text())
    expect(chips[0]).toContain('core-router (id 5)')
    expect(chips[1]).toContain('Node id 99 (not found)')
  })

  it('shows interface addresses as-is', () => {
    const wrapper = mountPicker({ mode: 'interface', items: [{ address: '10.0.0.1' }] })
    expect(wrapper.find('[data-test="picker-interface-chip"]').text()).toContain('10.0.0.1')
  })

  it('emits remove with the item index', async () => {
    const wrapper = mountPicker({ mode: 'interface', items: [{ address: '10.0.0.1' }, { address: '10.0.0.2' }] })
    await wrapper.findAll('[data-test="picker-interface-chip"] .p-chip-remove-icon')[1].trigger('click')
    expect(wrapper.emitted('remove')).toEqual([[1]])
  })

  it('renders friendly all-selected chips for match-any', () => {
    // the wire value is the match-any pseudo-interface; users see All Nodes /
    // All Interfaces instead of the internal token
    const nodeSide = mountPicker({ mode: 'node', items: [], matchAny: true })
    expect(nodeSide.find('[data-test="picker-node-all"]').text()).toContain('All Nodes')
    expect(nodeSide.find('[data-test="picker-node-empty"]').exists()).toBe(false)

    const ifaceSide = mountPicker({ mode: 'interface', items: [{ address: 'match-any' }], matchAny: true })
    expect(ifaceSide.find('[data-test="picker-interface-chip"]').text()).toContain('All Interfaces')
    expect(ifaceSide.text()).not.toContain('match-any')
  })
})
