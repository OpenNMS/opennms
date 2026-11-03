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
import AddToGraphCollectionDialog from '@/components/GraphCollections/AddToGraphCollectionDialog.vue'
import { GraphCollectionGraph } from '@/types/graphCollection'

const { getGraphCollections, getGraphCollectionTimespans, addGraphToCollection, createGraphCollection, showSnackBar } = vi.hoisted(() => ({
  getGraphCollections: vi.fn(),
  getGraphCollectionTimespans: vi.fn(),
  addGraphToCollection: vi.fn(),
  createGraphCollection: vi.fn(),
  showSnackBar: vi.fn()
}))
vi.mock(import('@/services/graphCollectionsService'), async importOriginal => ({
  ...await importOriginal(),
  getGraphCollections: () => getGraphCollections(),
  getGraphCollectionTimespans: () => getGraphCollectionTimespans(),
  addGraphToCollection: (...a: unknown[]) => addGraphToCollection(...a),
  createGraphCollection: (...a: unknown[]) => createGraphCollection(...a)
}))
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))
vi.mock('@/composables/useSpinner', () => ({ default: () => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }) }))

interface DialogProps { graph: GraphCollectionGraph, details: { label: string, value: string }[] }
const graphProps: DialogProps = {
  graph: { kind: 'prefab', title: 'Bits In/Out', resourceId: 'node[1].interfaceSnmp[ge-0_0_0]', graphtype: 'mib2.bits', timespan: '1_day' },
  details: [{ label: 'Resource', value: 'ge-0/0/0' }, { label: 'Graph', value: 'mib2.bits' }]
}
const STATE = 'nf=rtr&df=ifHCInOctets&n=2&title=Uplinks'
const adhocProps: DialogProps = {
  graph: { kind: 'adhoc', title: 'Uplinks', resourceId: null, graphtype: null, timespan: '1_day', adhocState: STATE },
  details: [{ label: 'Graph', value: 'Uplinks' }, { label: 'Series', value: '2' }]
}
const collections = [
  { id: 0, title: 'Core', showTimespanButton: false, showGraphtypeButton: false, graphsPerLine: 0, graphs: [] },
  { id: 2, title: 'Edge', showTimespanButton: false, showGraphtypeButton: false, graphsPerLine: 0,
    graphs: [
      { kind: 'prefab', title: '', resourceId: 'node[1].interfaceSnmp[ge-0_0_0]', graphtype: 'mib2.bits', timespan: '7_day' },
      { kind: 'adhoc', title: '', resourceId: null, graphtype: null, timespan: '7_day', adhocState: STATE }
    ] }
]

const mountDialog = async (props: DialogProps = graphProps) => {
  const wrapper = mount(AddToGraphCollectionDialog, {
    props: { visible: true, ...props },
    global: { plugins: [PrimeVue], stubs: { OnmsDialog: { props: ['visible'], template: '<div v-if="visible"><slot /></div>' }}}
  })
  await flushPromises()
  return wrapper
}

const selects = (wrapper: ReturnType<typeof mount>) => {
  const [timespan, target] = wrapper.findAllComponents({ name: 'OnmsSelect' })
  return { timespan, target }
}

describe('AddToGraphCollectionDialog.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getGraphCollections.mockResolvedValue({ success: true, message: '', payload: collections })
    getGraphCollectionTimespans.mockResolvedValue({ success: true, message: '', payload: [{ id: '1_day', label: '1 day' }, { id: '7_day', label: '7 day' }] })
  })

  it('adds the graph to an existing collection with the graph title and a 1 day timespan by default', async () => {
    addGraphToCollection.mockResolvedValue({ success: true, message: '', payload: { collection: collections[0], etag: null }})
    const wrapper = await mountDialog()
    expect(wrapper.find('[data-test="atc-resource"]').text()).toContain('ge-0/0/0')
    expect((wrapper.find('[data-test="atc-title"]').element as HTMLInputElement).value).toBe('Bits In/Out')
    const { timespan, target } = selects(wrapper)
    expect(timespan.props('modelValue')).toBe('1_day')
    expect((target.props('options') as { label: string }[]).map(o => o.label)).toEqual(['Core', 'Edge', 'New collection...'])

    target.vm.$emit('update:modelValue', '0')
    await wrapper.find('[data-test="atc-save"]').trigger('click')
    await flushPromises()
    expect(addGraphToCollection).toHaveBeenCalledWith(0, {
      kind: 'prefab', title: 'Bits In/Out', resourceId: graphProps.graph.resourceId, graphtype: 'mib2.bits', timespan: '1_day'
    })
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Added "Bits In/Out" to "Core".' })
    expect(wrapper.emitted('added')?.[0][0]).toEqual(collections[0])
    expect(wrapper.emitted('update:visible')?.[0]).toEqual([false])
  })

  it('creates a new collection holding the graph and requires its title', async () => {
    createGraphCollection.mockResolvedValue({ success: true, message: '', payload: { collection: { ...collections[0], id: 5, title: 'Mine' }, etag: null }})
    const wrapper = await mountDialog()
    await wrapper.find('[data-test="atc-save"]').trigger('click')
    expect(wrapper.text()).toContain('Choose a collection.')

    selects(wrapper).target.vm.$emit('update:modelValue', 'new')
    await flushPromises()
    await wrapper.find('[data-test="atc-save"]').trigger('click')
    expect(wrapper.text()).toContain('A title is required.')
    expect(createGraphCollection).not.toHaveBeenCalled()

    await wrapper.find('[data-test="atc-new-title"]').setValue(' Mine ')
    await wrapper.find('[data-test="atc-title"]').setValue('')
    selects(wrapper).timespan.vm.$emit('update:modelValue', '7_day')
    await wrapper.find('[data-test="atc-save"]').trigger('click')
    await flushPromises()
    expect(createGraphCollection).toHaveBeenCalledWith({
      title: 'Mine', showTimespanButton: false, showGraphtypeButton: false, graphsPerLine: 0,
      graphs: [{ kind: 'prefab', title: '', resourceId: graphProps.graph.resourceId, graphtype: 'mib2.bits', timespan: '7_day' }]
    })
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Added "mib2.bits" to "Mine".' })
  })

  it('preselects a new collection when none exist yet', async () => {
    getGraphCollections.mockResolvedValue({ success: true, message: '', payload: [] })
    const wrapper = await mountDialog()
    expect(selects(wrapper).target.props('modelValue')).toBe('new')
    expect(wrapper.find('[data-test="atc-new-title"]').exists()).toBe(true)
  })

  it('points out a collection that already shows this graph', async () => {
    const wrapper = await mountDialog()
    expect(wrapper.find('[data-test="atc-duplicate"]').exists()).toBe(false)
    selects(wrapper).target.vm.$emit('update:modelValue', '2')
    await flushPromises()
    expect(wrapper.find('[data-test="atc-duplicate"]').text()).toContain('already in "Edge"')
    expect(wrapper.find('[data-test="atc-save"]').attributes('disabled')).toBeUndefined()
  })

  it('adds an ad-hoc graph as it was handed in, and knows when a collection already has it', async () => {
    addGraphToCollection.mockResolvedValue({ success: true, message: '', payload: { collection: collections[0], etag: null }})
    const wrapper = await mountDialog(adhocProps)
    expect(wrapper.find('[data-test="atc-resource"]').text()).toContain('Series: 2')
    expect((wrapper.find('[data-test="atc-title"]').element as HTMLInputElement).value).toBe('Uplinks')

    // the same stored state, not the same resource, is what makes it a duplicate
    selects(wrapper).target.vm.$emit('update:modelValue', '2')
    await flushPromises()
    expect(wrapper.find('[data-test="atc-duplicate"]').text()).toContain('already in "Edge"')

    selects(wrapper).target.vm.$emit('update:modelValue', '0')
    await flushPromises()
    expect(wrapper.find('[data-test="atc-duplicate"]').exists()).toBe(false)
    await wrapper.find('[data-test="atc-title"]').setValue('')
    await wrapper.find('[data-test="atc-save"]').trigger('click')
    await flushPromises()
    expect(addGraphToCollection).toHaveBeenCalledWith(0, {
      kind: 'adhoc', title: '', resourceId: null, graphtype: null, timespan: '1_day', adhocState: STATE
    })
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Added "the custom graph" to "Core".' })
  })

  it('lists the server problems and stays open', async () => {
    addGraphToCollection.mockResolvedValue({ success: false, status: 400, message: 'x', errors: ['unknown timespan \'1_day\'.', 'a graphtype is required.'] })
    const wrapper = await mountDialog()
    selects(wrapper).target.vm.$emit('update:modelValue', '0')
    await wrapper.find('[data-test="atc-save"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="atc-problems"]').findAll('p').map(p => p.text())).toEqual(['unknown timespan \'1_day\'.', 'a graphtype is required.'])
    expect(wrapper.emitted('update:visible')).toBeUndefined()
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('shows why the collections could not be read and does not offer Add', async () => {
    getGraphCollections.mockResolvedValue({ success: false, message: 'Failed to load the graph collections.' })
    const wrapper = await mountDialog()
    expect(wrapper.find('[data-test="atc-load-error"]').text()).toBe('Failed to load the graph collections.')
    expect(wrapper.find('[data-test="atc-save"]').attributes('disabled')).toBeDefined()
  })
})
