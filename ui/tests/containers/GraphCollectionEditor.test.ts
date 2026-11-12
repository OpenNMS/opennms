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

import { flushPromises, mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import GraphCollectionEditor from '@/containers/GraphCollectionEditor.vue'

const {
  getGraphCollection, getResolvedGraphCollection, getResolvedSyntheticGraphCollection, getGraphCollectionTimespans,
  createGraphCollection, updateGraphCollection, showSnackBar, push, query, leaveGuards
} = vi.hoisted(() => ({
  getGraphCollection: vi.fn(),
  getResolvedGraphCollection: vi.fn(),
  getResolvedSyntheticGraphCollection: vi.fn(),
  getGraphCollectionTimespans: vi.fn(),
  createGraphCollection: vi.fn(),
  updateGraphCollection: vi.fn(),
  showSnackBar: vi.fn(),
  push: vi.fn(),
  query: {} as Record<string, string>,
  leaveGuards: [] as Array<() => unknown>
}))

vi.mock(import('@/services/graphCollectionsService'), async importOriginal => ({
  ...await importOriginal(),
  getGraphCollection: (...a: unknown[]) => getGraphCollection(...a),
  getResolvedGraphCollection: (...a: unknown[]) => getResolvedGraphCollection(...a),
  getResolvedSyntheticGraphCollection: (...a: unknown[]) => getResolvedSyntheticGraphCollection(...a),
  getGraphCollectionTimespans: () => getGraphCollectionTimespans(),
  createGraphCollection: (...a: unknown[]) => createGraphCollection(...a),
  updateGraphCollection: (...a: unknown[]) => updateGraphCollection(...a)
}))
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))
vi.mock('@/composables/useSpinner', () => ({ default: () => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }) }))
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push,
    // the editor guards every navigation on the router itself, see the component
    beforeEach: (guard: () => unknown) => {
      leaveGuards.push(guard)
      return () => leaveGuards.splice(leaveGuards.indexOf(guard), 1)
    }
  }),
  useRoute: () => ({ query })
}))

const stored = {
  id: 4, title: 'Core', showTimespanButton: true, showGraphtypeButton: false, graphsPerLine: 2,
  graphs: [
    { kind: 'prefab', title: 'Uplink', resourceId: 'node[1].interfaceSnmp[ge-0_0_0]', graphtype: 'mib2.bits', timespan: '7_day', extlink: null },
    { kind: 'prefab', title: 'Gone', resourceId: 'node[9].nodeSnmp[]', graphtype: 'x', timespan: 'fortnight', extlink: null }
  ]
}
const resolved = {
  collection: {
    id: 4, type: 'custom', title: 'Core', showTimespanButton: true, showGraphtypeButton: false, graphsPerLine: 2, graphTypes: [],
    graphs: [
      { index: 0, valid: true, resource: { id: 'node[1].interfaceSnmp[ge-0_0_0]', label: 'ge-0/0/0', typeLabel: 'SNMP Interface Data', parent: { label: 'rtr-01' }}},
      { index: 1, valid: false, error: 'Graph 2: unknown timespan \'fortnight\'.', resource: null }
    ]
  },
  etag: null
}

const PickerStub = {
  props: ['visible', 'graph', 'timespans'],
  emits: ['update:visible', 'save'],
  template: '<div v-if="visible" data-test="picker-stub" :data-editing="graph ? graph.key : \'\'" />'
}

const AdhocDetailsStub = {
  props: ['visible', 'graph', 'timespans'],
  emits: ['update:visible', 'save'],
  template: '<div v-if="visible" data-test="adhoc-details-stub" />'
}

const mountEditor = async (props: Record<string, string> = {}) => {
  const wrapper = mount(GraphCollectionEditor, {
    props,
    global: {
      plugins: [createTestingPinia({ stubActions: false }), PrimeVue],
      stubs: {
        BreadCrumbs: true,
        GraphPickerDialog: PickerStub,
        AdhocGraphDetailsDialog: AdhocDetailsStub,
        OnmsDialog: { props: ['visible'], template: '<div v-if="visible"><slot /></div>' },
        OnmsConfirmationDialog: {
          props: ['visible'],
          emits: ['ok', 'cancel'],
          template: '<div v-if="visible" data-test="leave-confirm"><slot name="content" /><button data-test="leave-ok" @click="$emit(\'ok\')">OK</button></div>'
        }
      }
    }
  })
  await flushPromises()
  return wrapper
}

const rows = (wrapper: ReturnType<typeof mount>) => wrapper.findAll('[data-test="graph-title"]').map(t => t.text())

describe('GraphCollectionEditor.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    leaveGuards.length = 0
    for (const key of Object.keys(query)) {
      delete query[key]
    }
    getGraphCollectionTimespans.mockResolvedValue({ success: true, message: '', payload: [{ id: '1_day', label: '1 day' }, { id: '7_day', label: '7 day' }] })
    getGraphCollection.mockResolvedValue({ success: true, message: '', payload: { collection: stored, etag: '"v1"' }})
    getResolvedGraphCollection.mockResolvedValue({ success: true, message: '', payload: resolved })
  })

  it('loads a stored collection with the resolved labels and problems beside each graph', async () => {
    const wrapper = await mountEditor({ id: '4' })
    expect(wrapper.find('[data-test="editor-title"]').text()).toBe('Edit Core')
    expect((wrapper.find('[data-test="title"]').element as HTMLInputElement).value).toBe('Core')
    expect(rows(wrapper)).toEqual(['Uplink', 'Gone'])
    expect(wrapper.findAll('[data-test="graph-resource"]')[0].text()).toBe('rtr-01 / ge-0/0/0')
    expect(wrapper.findAll('[data-test="graph-resource"]')[1].text()).toBe('node[9].nodeSnmp[]')
    expect(wrapper.find('[data-test="graph-problem"]').text()).toContain('unknown timespan')
  })

  it('reorders, removes and adds graphs, then saves the whole list with If-Match', async () => {
    updateGraphCollection.mockResolvedValue({ success: true, message: '', payload: { collection: { ...stored }, etag: '"v2"' }})
    const wrapper = await mountEditor({ id: '4' })

    await wrapper.findAll('[data-test="move-down"]')[0].trigger('click')
    expect(rows(wrapper)).toEqual(['Gone', 'Uplink'])
    await wrapper.findAll('[data-test="remove-graph"]')[0].trigger('click')
    expect(rows(wrapper)).toEqual(['Uplink'])

    await wrapper.find('[data-test="add-graph"]').trigger('click')
    const picker = wrapper.findComponent(PickerStub)
    expect(picker.props('graph')).toBeNull()
    picker.vm.$emit('save', { key: 0, kind: 'prefab', title: 'Errors', resourceId: 'node[1].interfaceSnmp[ge-0_0_0]', graphtype: 'mib2.errors', timespan: '1_day', valid: true })
    await flushPromises()
    expect(rows(wrapper)).toEqual(['Uplink', 'Errors'])

    await wrapper.find('[data-test="title"]').setValue('Core (edited)')
    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()

    expect(updateGraphCollection).toHaveBeenCalledWith(4, {
      title: 'Core (edited)', showTimespanButton: true, showGraphtypeButton: false, graphsPerLine: 2,
      graphs: [
        { kind: 'prefab', title: 'Uplink', resourceId: 'node[1].interfaceSnmp[ge-0_0_0]', graphtype: 'mib2.bits', timespan: '7_day', adhocState: null, extlink: null, nodeId: null, nodeSource: null, domain: null, interfaceId: null },
        { kind: 'prefab', title: 'Errors', resourceId: 'node[1].interfaceSnmp[ge-0_0_0]', graphtype: 'mib2.errors', timespan: '1_day', adhocState: null, extlink: null, nodeId: null, nodeSource: null, domain: null, interfaceId: null }
      ]
    }, '"v1"')
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Saved "Core (edited)".' })
    expect(push).toHaveBeenCalledWith('/graph-collections/4')
  })

  it('edits one graph in place through the picker', async () => {
    const wrapper = await mountEditor({ id: '4' })
    await wrapper.findAll('[data-test="edit-graph"]')[1].trigger('click')
    const picker = wrapper.findComponent(PickerStub)
    const editing = picker.props('graph')
    expect(editing.title).toBe('Gone')
    picker.vm.$emit('save', { ...editing, timespan: '1_day', valid: true, error: null })
    await flushPromises()
    expect(rows(wrapper)).toEqual(['Uplink', 'Gone'])
    expect(wrapper.find('[data-test="graph-problem"]').exists()).toBe(false)
  })

  it('edits an ad-hoc entry\'s title and timespan in its own dialog and stores only its builder state', async () => {
    const state = 'nf=rtr&df=CpuRawUser&n=3&title=CPU'
    const adhoc = { kind: 'adhoc', title: '', resourceId: null, graphtype: null, timespan: '1_day', adhocState: state, extlink: null }
    getGraphCollection.mockResolvedValue({ success: true, message: '', payload: { collection: { ...stored, graphs: [stored.graphs[0], adhoc] }, etag: '"v1"' }})
    getResolvedGraphCollection.mockResolvedValue({ success: true, message: '', payload: { ...resolved, collection: {
      ...resolved.collection, graphs: [resolved.collection.graphs[0], { index: 1, kind: 'adhoc', valid: true, adhocState: state, resource: null }]
    }}})
    updateGraphCollection.mockResolvedValue({ success: true, message: '', payload: { collection: stored, etag: '"v2"' }})
    const wrapper = await mountEditor({ id: '4' })

    // the row names the graph the builder titled it, and says what it is instead of a graph type
    expect(wrapper.findAll('[data-test="graph-resource"]')[1].text()).toBe('CPU')
    expect(wrapper.text()).toContain('Ad-hoc')
    expect(wrapper.find('[data-test="graph-problem"]').exists()).toBe(false)

    await wrapper.findAll('[data-test="edit-graph"]')[1].trigger('click')
    expect(wrapper.findComponent(PickerStub).props('visible')).toBe(false)
    const details = wrapper.findComponent(AdhocDetailsStub)
    expect(details.props('visible')).toBe(true)
    expect(details.props('graph').adhocState).toBe(state)
    details.vm.$emit('save', { ...details.props('graph'), title: 'Core CPU', timespan: '7_day' })
    await flushPromises()
    expect(rows(wrapper)).toEqual(['Uplink', 'Core CPU'])

    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    expect(updateGraphCollection.mock.calls[0][1].graphs[1]).toEqual({
      kind: 'adhoc', title: 'Core CPU', resourceId: null, graphtype: null, timespan: '7_day', adhocState: state, extlink: null,
      nodeId: null, nodeSource: null, domain: null, interfaceId: null
    })
  })

  it('creates a new collection and requires a title', async () => {
    createGraphCollection.mockResolvedValue({ success: true, message: '', payload: { collection: { ...stored, id: 11, graphs: [] }, etag: '"n"' }})
    const wrapper = await mountEditor()
    expect(wrapper.find('[data-test="editor-title"]').text()).toBe('New Graph Collection')
    expect(wrapper.find('[data-test="no-graphs"]').exists()).toBe(true)
    expect(getGraphCollection).not.toHaveBeenCalled()

    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    expect(createGraphCollection).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('A title is required.')

    await wrapper.find('[data-test="title"]').setValue('Fresh')
    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    expect(createGraphCollection).toHaveBeenCalledWith({ title: 'Fresh', showTimespanButton: false, showGraphtypeButton: false, graphsPerLine: 0, graphs: [] })
    expect(push).toHaveBeenCalledWith('/graph-collections/11')
  })

  it('starts a copy from a stored collection without its id or ETag', async () => {
    query.from = '4'
    createGraphCollection.mockResolvedValue({ success: true, message: '', payload: { collection: { ...stored, id: 12 }, etag: null }})
    const wrapper = await mountEditor()
    expect((wrapper.find('[data-test="title"]').element as HTMLInputElement).value).toBe('Copy of Core')
    expect(rows(wrapper)).toEqual(['Uplink', 'Gone'])
    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    expect(createGraphCollection).toHaveBeenCalledWith(expect.objectContaining({ title: 'Copy of Core' }))
    expect(updateGraphCollection).not.toHaveBeenCalled()
  })

  it('starts a new collection from an on-the-fly node collection', async () => {
    query.fromNode = '7'
    getResolvedSyntheticGraphCollection.mockResolvedValue({
      success: true, message: '',
      payload: {
        id: null, type: 'node', title: 'Node Report for Node Number 7', showTimespanButton: true, showGraphtypeButton: true, graphsPerLine: 1, graphTypes: [],
        graphs: [{ index: 0, kind: 'prefab', title: 'eth0', timespan: '7_day', graphtype: 'mib2.bits', resourceId: 'node[7].interfaceSnmp[eth0]', resource: { id: 'node[7].interfaceSnmp[eth0]', label: 'eth0', typeLabel: 'SNMP Interface Data' }, valid: true, start: 0, end: 1 }]
      }
    })
    createGraphCollection.mockResolvedValue({ success: true, message: '', payload: { collection: { ...stored, id: 13 }, etag: null }})
    const wrapper = await mountEditor()
    expect(getResolvedSyntheticGraphCollection).toHaveBeenCalledWith('node', '7')
    expect((wrapper.find('[data-test="title"]').element as HTMLInputElement).value).toBe('Node Report for Node Number 7')
    expect(rows(wrapper)).toEqual(['eth0'])

    // the on-the-fly collection offers both overrides, and a copy of it keeps them
    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    expect(createGraphCollection).toHaveBeenCalledWith(expect.objectContaining({
      title: 'Node Report for Node Number 7', showTimespanButton: true, showGraphtypeButton: true, graphsPerLine: 0
    }))
  })

  it('disables saving and goes back to the list when the collection cannot be loaded', async () => {
    getGraphCollection.mockResolvedValue({ success: false, message: 'Graph collection 4 was not found.' })
    const wrapper = await mountEditor({ id: '4' })
    expect(wrapper.find('[data-test="load-error"]').text()).toBe('Graph collection 4 was not found.')
    expect(wrapper.find('[data-test="title"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="save"]').attributes('disabled')).toBeDefined()
    await wrapper.find('[data-test="cancel"]').trigger('click')
    expect(push).toHaveBeenLastCalledWith('/graph-collections')
  })

  it('lists every server-side problem and keeps the form', async () => {
    updateGraphCollection.mockResolvedValue({
      success: false, status: 400, message: 'a\nb', errors: ['title must be at most 255 characters.', 'Graph 2: unknown timespan \'fortnight\'.']
    })
    const wrapper = await mountEditor({ id: '4' })
    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    const problems = wrapper.find('[data-test="problems"]')
    expect(problems.findAll('p').map(p => p.text())).toEqual(['title must be at most 255 characters.', 'Graph 2: unknown timespan \'fortnight\'.'])
    expect(push).not.toHaveBeenCalled()
    expect(rows(wrapper)).toEqual(['Uplink', 'Gone'])
  })

  it('offers overwrite or reload when the collection changed underneath', async () => {
    updateGraphCollection
      .mockResolvedValueOnce({ success: false, status: 412, message: 'Graph collection 4 has changed since it was read; fetch it again before writing.' })
      .mockResolvedValueOnce({ success: true, message: '', payload: { collection: stored, etag: '"v3"' }})
    const wrapper = await mountEditor({ id: '4' })
    await wrapper.find('[data-test="title"]').setValue('Mine')
    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="stale-dialog"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="problems"]').exists()).toBe(false)

    await wrapper.find('[data-test="stale-overwrite"]').trigger('click')
    await flushPromises()
    expect(updateGraphCollection).toHaveBeenLastCalledWith(4, expect.objectContaining({ title: 'Mine' }), null)
    expect(push).toHaveBeenCalledWith('/graph-collections/4')
  })

  it('reloads the server version on request and drops the local edits', async () => {
    updateGraphCollection.mockResolvedValueOnce({ success: false, status: 412, message: 'changed' })
    const wrapper = await mountEditor({ id: '4' })
    await wrapper.find('[data-test="title"]').setValue('Mine')
    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="stale-reload"]').trigger('click')
    await flushPromises()
    expect(getGraphCollection).toHaveBeenCalledTimes(2)
    expect((wrapper.find('[data-test="title"]').element as HTMLInputElement).value).toBe('Core')
    expect(wrapper.find('[data-test="stale-dialog"]').exists()).toBe(false)
  })

  it('asks before leaving with unsaved changes, and not otherwise', async () => {
    const wrapper = await mountEditor({ id: '4' })
    expect(leaveGuards).toHaveLength(1)
    expect(leaveGuards[0]()).toBe(true)

    await wrapper.find('[data-test="title"]').setValue('Changed')
    const pending = leaveGuards[0]() as Promise<boolean>
    await flushPromises()
    expect(wrapper.find('[data-test="leave-confirm"]').exists()).toBe(true)
    await wrapper.find('[data-test="leave-ok"]').trigger('click')
    expect(await pending).toBe(true)
  })

  it('keeps its guard on the router only while mounted', async () => {
    const wrapper = await mountEditor({ id: '4' })
    expect(leaveGuards).toHaveLength(1)
    wrapper.unmount()
    expect(leaveGuards).toHaveLength(0)
  })

  it('asks for the timespans and the collection at the same time', async () => {
    getGraphCollectionTimespans.mockReturnValue(new Promise(() => {}))
    await mountEditor({ id: '4' })
    expect(getGraphCollection).toHaveBeenCalledWith(4)
    expect(getResolvedGraphCollection).toHaveBeenCalledWith(4)
  })

  it('lets the navigation after a successful save through the leave guard', async () => {
    updateGraphCollection.mockResolvedValue({ success: true, message: '', payload: { collection: stored, etag: '"v2"' }})
    const wrapper = await mountEditor({ id: '4' })
    await wrapper.find('[data-test="title"]').setValue('Changed')
    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    expect(push).toHaveBeenCalledWith('/graph-collections/4')
    expect(leaveGuards[0]()).toBe(true)
  })

  it('reloads when the route moves from editing one collection to creating another', async () => {
    const wrapper = await mountEditor({ id: '4' })
    expect(rows(wrapper)).toEqual(['Uplink', 'Gone'])
    await wrapper.setProps({ id: undefined })
    await flushPromises()
    expect(wrapper.find('[data-test="editor-title"]').text()).toBe('New Graph Collection')
    expect((wrapper.find('[data-test="title"]').element as HTMLInputElement).value).toBe('')
    expect(rows(wrapper)).toEqual([])
  })

  it('goes back to the view when editing and to the list when creating', async () => {
    const wrapper = await mountEditor({ id: '4' })
    await wrapper.find('[data-test="cancel"]').trigger('click')
    expect(push).toHaveBeenLastCalledWith('/graph-collections/4')
    const fresh = await mountEditor()
    await fresh.find('[data-test="cancel"]').trigger('click')
    expect(push).toHaveBeenLastCalledWith('/graph-collections')
  })
})
