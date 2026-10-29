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

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import GraphPickerDialog from '@/components/GraphCollections/GraphPickerDialog.vue'

const { searchOutageNodes, getResourceForNode, getGraphDefinitionsByResourceId } = vi.hoisted(() => ({
  searchOutageNodes: vi.fn(),
  getResourceForNode: vi.fn(),
  getGraphDefinitionsByResourceId: vi.fn()
}))
vi.mock('@/services/scheduledOutagesService', () => ({ searchOutageNodes }))
vi.mock('@/services/resourceService', () => ({ getResourceForNode }))
vi.mock('@/services/graphService', () => ({ getGraphDefinitionsByResourceId }))

const timespans = [{ id: '1_day', label: '1 day' }, { id: '7_day', label: '7 day' }]

// the dialog teleports to <body>; render its content in place
const DialogStub = { props: ['visible'], template: '<div v-if="visible"><slot /></div>' }

const deferred = <T>() => {
  let resolve!: (value: T) => void
  const promise = new Promise<T>((r) => {
    resolve = r
  })
  return { promise, resolve }
}

const nodeWith = (id: number, children: Record<string, unknown>[]) => ({ id: `node[${id}]`, label: `node-${id}`, children: { resource: children }})

const mountPicker = async (graph: any = null) => {
  const wrapper = mount(GraphPickerDialog, {
    props: { visible: false, graph, timespans },
    global: { plugins: [PrimeVue], stubs: { OnmsDialog: DialogStub }}
  })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return wrapper
}

describe('GraphPickerDialog.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    searchOutageNodes.mockResolvedValue([{ id: 1, label: 'rtr-01' }])
    getResourceForNode.mockResolvedValue({
      id: 'node[1]', label: 'rtr-01',
      children: { resource: [
        { id: 'node[1].nodeSnmp[]', label: 'Node-level Performance Data', typeLabel: 'SNMP Node Data' },
        { id: 'node[1].interfaceSnmp[ge-0_0_0]', label: 'ge-0/0/0', typeLabel: 'SNMP Interface Data' }
      ] }
    })
    getGraphDefinitionsByResourceId.mockResolvedValue({ name: ['mib2.bits', 'mib2.errors'] })
  })

  it('walks node, resource and graph type and emits the new entry', async () => {
    const wrapper = await mountPicker()
    expect(wrapper.find('[data-test="picker-save"]').attributes('disabled')).toBeDefined()

    const node = wrapper.findComponent({ name: 'OnmsAutoComplete' })
    node.vm.$emit('complete', 'rtr')
    await flushPromises()
    expect(searchOutageNodes).toHaveBeenCalledWith('rtr')
    node.vm.$emit('update:modelValue', { id: 1, label: 'rtr-01' })
    await flushPromises()
    expect(getResourceForNode).toHaveBeenCalledWith('1')

    wrapper.findComponent({ name: 'OnmsSearchInput' }).vm.$emit('update:modelValue', 'ge-0')
    await flushPromises()
    const resources = wrapper.findComponent({ name: 'OnmsListbox' })
    expect(resources.props('options')).toHaveLength(1)
    resources.vm.$emit('update:modelValue', { id: 'node[1].interfaceSnmp[ge-0_0_0]', label: 'ge-0/0/0', typeLabel: 'SNMP Interface Data' })
    await flushPromises()
    expect(getGraphDefinitionsByResourceId).toHaveBeenCalledWith('node[1].interfaceSnmp[ge-0_0_0]')
    expect(wrapper.find('[data-test="picker-current"]').text()).toContain('ge-0/0/0')

    const selects = wrapper.findAllComponents({ name: 'OnmsSelect' })
    selects[0].vm.$emit('update:modelValue', 'mib2.bits')
    await wrapper.find('[data-test="picker-title"]').setValue(' Uplink ')
    selects[1].vm.$emit('update:modelValue', '1_day')
    await flushPromises()
    await wrapper.find('[data-test="picker-save"]').trigger('click')

    expect(wrapper.emitted('save')?.[0][0]).toMatchObject({
      kind: 'prefab', title: 'Uplink', resourceId: 'node[1].interfaceSnmp[ge-0_0_0]', resourceLabel: 'ge-0/0/0',
      graphtype: 'mib2.bits', timespan: '1_day', valid: true
    })
    expect(wrapper.emitted('update:visible')?.[0]).toEqual([false])
  })

  it('opens an existing entry on its resource with the stored graph type kept selectable', async () => {
    getGraphDefinitionsByResourceId.mockResolvedValue({ name: ['mib2.bits'] })
    const wrapper = await mountPicker({
      key: 3, title: 'Old', resourceId: 'node[1].nodeSnmp[]', resourceLabel: 'Node-level Performance Data', parentLabel: 'rtr-01',
      graphtype: 'gone.graph', timespan: '7_day', nodeId: '1'
    })
    expect(wrapper.find('[data-test="picker-current"]').text()).toContain('Node-level Performance Data')
    const graphtype = wrapper.findAllComponents({ name: 'OnmsSelect' })[0]
    expect(graphtype.props('modelValue')).toBe('gone.graph')
    expect((graphtype.props('options') as { id: string }[]).map(o => o.id)).toEqual(['gone.graph', 'mib2.bits'])
    expect(wrapper.find('[data-test="picker-save"]').attributes('disabled')).toBeUndefined()

    await wrapper.find('[data-test="picker-save"]').trigger('click')
    // the resource did not change, so its parent and older address stay as they were
    expect(wrapper.emitted('save')?.[0][0]).toMatchObject({ key: 3, title: 'Old', graphtype: 'gone.graph', timespan: '7_day', parentLabel: 'rtr-01', nodeId: '1' })
  })

  it('gives a re-pointed entry its new parent and drops the old address', async () => {
    getResourceForNode.mockResolvedValue(nodeWith(2, [{ id: 'node[2].nodeSnmp[]', label: 'Node-level Performance Data', typeLabel: 'SNMP Node Data' }]))
    const wrapper = await mountPicker({
      key: 3, title: 'Old', resourceId: 'node[1].nodeSnmp[]', graphtype: 'mib2.bits', timespan: '7_day',
      parentLabel: 'rtr-01', nodeId: '1', interfaceId: 'eth0'
    })
    await wrapper.find('[data-test="picker-change"]').trigger('click')
    wrapper.findComponent({ name: 'OnmsAutoComplete' }).vm.$emit('update:modelValue', { id: 2, label: 'rtr-02' })
    await flushPromises()
    wrapper.findComponent({ name: 'OnmsListbox' }).vm.$emit('update:modelValue', { id: 'node[2].nodeSnmp[]', label: 'Node-level Performance Data', typeLabel: 'SNMP Node Data' })
    await flushPromises()
    wrapper.findAllComponents({ name: 'OnmsSelect' })[0].vm.$emit('update:modelValue', 'mib2.bits')
    await flushPromises()
    await wrapper.find('[data-test="picker-save"]').trigger('click')
    expect(wrapper.emitted('save')?.[0][0]).toMatchObject({
      key: 3, resourceId: 'node[2].nodeSnmp[]', parentLabel: 'rtr-02', nodeId: null, nodeSource: null, domain: null, interfaceId: null
    })
  })

  it('lets the user change the resource of an existing entry', async () => {
    const wrapper = await mountPicker({ key: 3, title: 'Old', resourceId: 'node[1].nodeSnmp[]', graphtype: 'x', timespan: '7_day' })
    await wrapper.find('[data-test="picker-change"]').trigger('click')
    expect(wrapper.find('[data-test="picker-current"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="picker-save"]').attributes('disabled')).toBeDefined()
    expect(wrapper.findComponent({ name: 'OnmsAutoComplete' }).exists()).toBe(true)
  })

  it('reports a node whose resources cannot be read', async () => {
    getResourceForNode.mockResolvedValue(null)
    const wrapper = await mountPicker()
    wrapper.findComponent({ name: 'OnmsAutoComplete' }).vm.$emit('update:modelValue', { id: 1, label: 'rtr-01' })
    await flushPromises()
    expect(wrapper.find('[data-test="picker-resources-empty"]').text()).toBe('No resources were found for this node, or they could not be read.')
  })

  it('leaves out children that report no graph attributes', async () => {
    getResourceForNode.mockResolvedValue(nodeWith(1, [
      { id: 'node[1].nodeSnmp[]', label: 'Node-level Performance Data', typeLabel: 'SNMP Node Data', rrdGraphAttributes: { 'loadavg1': {}}},
      { id: 'node[1].hrStorageIndex[]', label: 'Storage', typeLabel: 'Storage', rrdGraphAttributes: {}}
    ]))
    const wrapper = await mountPicker()
    wrapper.findComponent({ name: 'OnmsAutoComplete' }).vm.$emit('update:modelValue', { id: 1, label: 'rtr-01' })
    await flushPromises()
    expect((wrapper.findComponent({ name: 'OnmsListbox' }).props('options') as { label: string }[]).map(o => o.label)).toEqual(['Node-level Performance Data'])

    getResourceForNode.mockResolvedValue(nodeWith(2, [{ id: 'node[2].x[]', label: 'Bare', typeLabel: 'x', rrdGraphAttributes: {}}]))
    wrapper.findComponent({ name: 'OnmsAutoComplete' }).vm.$emit('update:modelValue', { id: 2, label: 'rtr-02' })
    await flushPromises()
    expect(wrapper.find('[data-test="picker-resources-empty"]').text()).toBe('This node has no resources with graphs.')
  })

  it('ignores the resources of a node picked earlier when they arrive late', async () => {
    const first = deferred<unknown>()
    const second = deferred<unknown>()
    getResourceForNode.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise)
    const wrapper = await mountPicker()
    const node = wrapper.findComponent({ name: 'OnmsAutoComplete' })
    node.vm.$emit('update:modelValue', { id: 1, label: 'rtr-01' })
    node.vm.$emit('update:modelValue', { id: 2, label: 'rtr-02' })
    second.resolve(nodeWith(2, [{ id: 'node[2].nodeSnmp[]', label: 'Second', typeLabel: 'SNMP Node Data' }]))
    await flushPromises()
    first.resolve(nodeWith(1, [{ id: 'node[1].nodeSnmp[]', label: 'First', typeLabel: 'SNMP Node Data' }]))
    await flushPromises()
    expect(wrapper.find('[data-test="picker-resources-loading"]').exists()).toBe(false)
    expect((wrapper.findComponent({ name: 'OnmsListbox' }).props('options') as { label: string }[]).map(o => o.label)).toEqual(['Second'])
  })

  it('ignores the graph types of a resource that was changed away from', async () => {
    const old = deferred<{ name: string[] }>()
    const fresh = deferred<{ name: string[] }>()
    getGraphDefinitionsByResourceId.mockReturnValueOnce(old.promise).mockReturnValueOnce(fresh.promise)
    const wrapper = await mountPicker({ key: 3, title: 'Old', resourceId: 'node[1].nodeSnmp[]', graphtype: '', timespan: '7_day' })
    await wrapper.find('[data-test="picker-change"]').trigger('click')
    wrapper.findComponent({ name: 'OnmsAutoComplete' }).vm.$emit('update:modelValue', { id: 1, label: 'rtr-01' })
    await flushPromises()
    wrapper.findComponent({ name: 'OnmsListbox' }).vm.$emit('update:modelValue', { id: 'node[1].interfaceSnmp[ge-0_0_0]', label: 'ge-0/0/0', typeLabel: 'SNMP Interface Data' })
    // the old resource's single graph would otherwise be picked for the new one
    old.resolve({ name: ['only.one'] })
    await flushPromises()
    fresh.resolve({ name: ['mib2.bits', 'mib2.errors'] })
    await flushPromises()
    const graphtype = wrapper.findAllComponents({ name: 'OnmsSelect' })[0]
    expect(graphtype.props('modelValue')).toBe('')
    expect((graphtype.props('options') as { id: string }[]).map(o => o.id)).toEqual(['mib2.bits', 'mib2.errors'])
  })

  it('says when a resource has no graphs and keeps the entry from being added', async () => {
    getGraphDefinitionsByResourceId.mockResolvedValue({ name: [] })
    const wrapper = await mountPicker()
    wrapper.findComponent({ name: 'OnmsAutoComplete' }).vm.$emit('update:modelValue', { id: 1, label: 'rtr-01' })
    await flushPromises()
    wrapper.findComponent({ name: 'OnmsListbox' }).vm.$emit('update:modelValue', { id: 'node[1].nodeSnmp[]', label: 'Node-level Performance Data', typeLabel: 'SNMP Node Data' })
    await flushPromises()
    expect(wrapper.text()).toContain('No graphs are available for this resource.')
    expect(wrapper.find('[data-test="picker-save"]').attributes('disabled')).toBeDefined()
  })
})
