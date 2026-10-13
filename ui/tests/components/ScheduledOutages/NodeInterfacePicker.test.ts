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
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { searchOutageInterfaces, searchOutageNodes } from '@/services/scheduledOutagesService'

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

  it('lists the selection before the search, with resolved labels and missing ids flagged', () => {
    const wrapper = mountPicker({
      mode: 'node',
      items: [{ id: 5 }, { id: 99 }],
      nodeLabels: { 5: 'core-router' }
    })
    const rows = wrapper.findAll('[data-test="picker-node-item"]').map(c => c.text())
    expect(rows[0]).toContain('core-router (id 5)')
    expect(rows[1]).toContain('Node id 99 (not found)')
    // an opened outage must read as filled in: its own nodes come before the add controls
    const html = wrapper.html()
    expect(html.indexOf('picker-node-selection')).toBeLessThan(html.indexOf('picker-node-search'))
    expect(wrapper.text()).toContain('Add a node')
  })

  it('shows interface addresses as-is', () => {
    const wrapper = mountPicker({ mode: 'interface', items: [{ address: '10.0.0.1' }] })
    expect(wrapper.find('[data-test="picker-interface-item"]').text()).toContain('10.0.0.1')
  })

  it('shows an empty-state line when nothing is selected', () => {
    const wrapper = mountPicker({ mode: 'interface', items: [] })
    expect(wrapper.find('[data-test="picker-interface-empty"]').text()).toContain('No specific interfaces selected')
    expect(wrapper.find('[data-test="picker-interface-item"]').exists()).toBe(false)
  })

  it('removes a row through its Delete button', async () => {
    const wrapper = mountPicker({ mode: 'interface', items: [{ address: '10.0.0.1' }, { address: '10.0.0.2' }] })
    const removes = wrapper.findAll('[data-test="picker-interface-remove"]')
    expect(removes[1].attributes('aria-label')).toBe('Remove 10.0.0.2')
    await removes[1].trigger('click')
    expect(wrapper.emitted('remove')).toEqual([[1]])
  })

  it('renders friendly all-selected rows for match-any', () => {
    // the wire value is the match-any pseudo-interface; users see All Nodes /
    // All Interfaces instead of the internal token
    const nodeSide = mountPicker({ mode: 'node', items: [], matchAny: true })
    expect(nodeSide.find('[data-test="picker-node-all"]').text()).toContain('All Nodes')
    expect(nodeSide.find('[data-test="picker-node-empty"]').exists()).toBe(false)
    expect(nodeSide.find('[data-test="picker-node-remove"]').exists()).toBe(false)

    const ifaceSide = mountPicker({ mode: 'interface', items: [{ address: 'match-any' }], matchAny: true })
    expect(ifaceSide.find('[data-test="picker-interface-item"]').text()).toContain('All Interfaces')
    expect(ifaceSide.text()).not.toContain('match-any')
  })

  it('keeps Add disabled for typed text until a suggestion is picked', async () => {
    // the autocomplete model holds the raw string while typing; adding it would
    // emit nothing usable, so only a picked suggestion enables Add
    const wrapper = mountPicker({ mode: 'node', items: [] })
    const vm = wrapper.vm as any
    vm.selection = 'core'
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-test="picker-node-add"]').attributes('disabled')).toBeDefined()
    vm.addSelection()
    expect(wrapper.emitted('add')).toBeUndefined()

    vm.selection = { label: 'core-router (id 5)', id: 5, nodeLabel: 'core-router' }
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-test="picker-node-add"]').attributes('disabled')).toBeUndefined()
    await wrapper.find('[data-test="picker-node-add"]').trigger('click')
    expect(wrapper.emitted('add')).toEqual([[{ id: 5 }, 'core-router']])
    expect(vm.selection).toBeNull()
  })

  it('emits an interface by address', async () => {
    const wrapper = mountPicker({ mode: 'interface', items: [] })
    const vm = wrapper.vm as any
    vm.selection = { label: '10.0.0.9 — edge', address: '10.0.0.9' }
    await wrapper.vm.$nextTick()
    await wrapper.find('[data-test="picker-interface-add"]').trigger('click')
    expect(wrapper.emitted('add')).toEqual([[{ address: '10.0.0.9' }]])
  })

  it('turns node search results into labelled suggestions', async () => {
    vi.mocked(searchOutageNodes).mockResolvedValueOnce([{ id: 5, label: 'core-router' }])
    const wrapper = mountPicker({ mode: 'node', items: [] })
    await (wrapper.vm as any).onComplete('core')
    await flushPromises()
    expect((wrapper.vm as any).suggestions).toEqual([{ label: 'core-router (id 5)', id: 5, nodeLabel: 'core-router' }])
  })

  it('labels interface suggestions with their node when known', async () => {
    vi.mocked(searchOutageInterfaces).mockResolvedValueOnce([{ address: '10.0.0.9', nodeLabel: 'edge' }, { address: '10.0.0.10', nodeLabel: '' }])
    const wrapper = mountPicker({ mode: 'interface', items: [] })
    await (wrapper.vm as any).onComplete('10.0.0')
    await flushPromises()
    expect((wrapper.vm as any).suggestions).toEqual([
      { label: '10.0.0.9 — edge', address: '10.0.0.9' },
      { label: '10.0.0.10', address: '10.0.0.10' }
    ])
  })
})
