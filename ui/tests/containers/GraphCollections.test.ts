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
import GraphCollections from '@/containers/GraphCollections.vue'
import GraphCollectionView from '@/containers/GraphCollectionView.vue'

// hoisted: the service index pulls in modules that call useSnackbar at import time, before this
// file's own constants would otherwise exist
const {
  getGraphCollections, deleteGraphCollection, createGraphCollection, getResolvedGraphCollection,
  getResolvedSyntheticGraphCollection, getGraphCollectionTimespans, showSnackBar, push, replace, role, query
} = vi.hoisted(() => ({
  getGraphCollections: vi.fn(),
  deleteGraphCollection: vi.fn(),
  createGraphCollection: vi.fn(),
  getResolvedGraphCollection: vi.fn(),
  getResolvedSyntheticGraphCollection: vi.fn(),
  getGraphCollectionTimespans: vi.fn(),
  showSnackBar: vi.fn(),
  push: vi.fn(),
  replace: vi.fn(),
  role: { readOnly: false },
  query: {} as Record<string, string>
}))

// partial: the service index re-exports every function of the module
vi.mock(import('@/services/graphCollectionsService'), async importOriginal => ({
  ...await importOriginal(),
  getGraphCollections: () => getGraphCollections(),
  deleteGraphCollection: (...a: unknown[]) => deleteGraphCollection(...a),
  createGraphCollection: (...a: unknown[]) => createGraphCollection(...a),
  getResolvedGraphCollection: (...a: unknown[]) => getResolvedGraphCollection(...a),
  getResolvedSyntheticGraphCollection: (...a: unknown[]) => getResolvedSyntheticGraphCollection(...a),
  getGraphCollectionTimespans: () => getGraphCollectionTimespans()
}))

vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))
vi.mock('@/composables/useSpinner', () => ({ default: () => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }) }))

vi.mock('@/composables/useRole', () => ({
  default: () => ({
    readOnlyRole: { get value() {
      return role.readOnly
    } },
    rolesAreLoaded: { value: true }
  })
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push, replace }),
  useRoute: () => ({ query })
}))

const collection = (id: number, title: string, graphs = 1) => ({
  id, title, showTimespanButton: id % 2 === 1, showGraphtypeButton: false, graphsPerLine: 0,
  graphs: Array.from({ length: graphs }, (_, i) => ({ title: `g${i}`, resourceId: 'node[1].nodeSnmp[]', graphtype: 'x', timespan: '7_day' }))
})

const mountList = async () => {
  const wrapper = mount(GraphCollections, {
    global: {
      plugins: [createTestingPinia({ stubActions: false }), PrimeVue],
      stubs: {
        'router-link': { props: ['to'], template: '<a :href="to"><slot /></a>' },
        BreadCrumbs: true,
        NodeCollectionPicker: true,
        // the real dialog teleports to <body>, out of the wrapper's reach
        OnmsConfirmationDialog: {
          props: ['visible'],
          emits: ['ok', 'cancel'],
          template: '<div v-if="visible" data-test="confirm"><slot name="content" /><button data-test="confirm-ok" @click="$emit(\'ok\')">OK</button></div>'
        }
      }
    }
  })
  await flushPromises()
  return wrapper
}

describe('GraphCollections.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    role.readOnly = false
    getGraphCollections.mockResolvedValue({
      success: true, message: '', payload: [collection(1, 'Core', 2), collection(2, 'Edge', 0)]
    })
  })

  it('lists every collection with its graph count and a link to its view', async () => {
    const wrapper = await mountList()
    const titles = wrapper.findAll('[data-test="collection-title"]')
    expect(titles.map(t => t.text())).toEqual(['Core', 'Edge'])
    expect(titles[0].attributes('href')).toBe('/graph-collections/1')
    expect(wrapper.findAll('[data-test="collection-graphs"]').map(t => t.text())).toEqual(['2', '0'])
    expect(wrapper.findAll('[data-test="collection-overrides"]').map(t => t.text())).toEqual(['Timespan', 'None'])
  })

  it('hides the write actions from a read-only user', async () => {
    role.readOnly = true
    const wrapper = await mountList()
    expect(wrapper.findAll('[data-test="view-collection"]')).toHaveLength(2)
    expect(wrapper.findAll('[data-test="delete-collection"]')).toHaveLength(0)
    expect(wrapper.findAll('[data-test="duplicate-collection"]')).toHaveLength(0)
    expect(wrapper.findAll('[data-test="edit-collection"]')).toHaveLength(0)
    expect(wrapper.find('[data-test="create-collection"]').exists()).toBe(false)
  })

  it('deletes after confirmation and reloads the list', async () => {
    deleteGraphCollection.mockResolvedValue({ success: true, message: '' })
    const wrapper = await mountList()
    await wrapper.findAll('[data-test="delete-collection"]')[0].trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="confirm"]').text()).toContain('delete the graph collection Core and its 2 graphs')
    await wrapper.find('[data-test="confirm-ok"]').trigger('click')
    await flushPromises()
    expect(deleteGraphCollection).toHaveBeenCalledWith(1)
    expect(getGraphCollections).toHaveBeenCalledTimes(2)
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Deleted graph collection "Core".' })
  })

  it('deletes the collection with id 0, which is a real id', async () => {
    getGraphCollections.mockResolvedValue({ success: true, message: '', payload: [collection(0, 'First')] })
    deleteGraphCollection.mockResolvedValue({ success: true, message: '' })
    const wrapper = await mountList()
    await wrapper.find('[data-test="delete-collection"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="confirm-ok"]').trigger('click')
    await flushPromises()
    expect(deleteGraphCollection).toHaveBeenCalledWith(0)
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Deleted graph collection "First".' })
  })

  it('opens the editor for create, edit and duplicate', async () => {
    const wrapper = await mountList()
    await wrapper.find('[data-test="create-collection"]').trigger('click')
    expect(push).toHaveBeenLastCalledWith('/graph-collections/new')
    await wrapper.findAll('[data-test="edit-collection"]')[1].trigger('click')
    expect(push).toHaveBeenLastCalledWith('/graph-collections/2/edit')
    await wrapper.findAll('[data-test="duplicate-collection"]')[0].trigger('click')
    expect(push).toHaveBeenLastCalledWith({ path: '/graph-collections/new', query: { from: '1' }})
  })

  it('shows the failure and keeps the empty state away when the list cannot be read', async () => {
    getGraphCollections.mockResolvedValue({ success: false, message: 'Failed to load the graph collections.' })
    const wrapper = await mountList()
    expect(wrapper.find('[data-test="load-error"]').text()).toBe('Failed to load the graph collections.')
    expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(false)
  })

  it('reports a refused delete without touching the list', async () => {
    deleteGraphCollection.mockResolvedValue({ success: false, message: 'You do not have permission to change graph collections.' })
    const wrapper = await mountList()
    await wrapper.findAll('[data-test="delete-collection"]')[1].trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="confirm-ok"]').trigger('click')
    await flushPromises()
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'You do not have permission to change graph collections.', error: true })
    expect(getGraphCollections).toHaveBeenCalledTimes(1)
  })
})

const deferred = <T>() => {
  let resolve!: (value: T) => void
  const promise = new Promise<T>((r) => {
    resolve = r
  })
  return { promise, resolve }
}

const resolved = (over: Record<string, unknown> = {}) => ({
  id: 1, type: 'custom', title: 'Core', showTimespanButton: true, showGraphtypeButton: true, graphsPerLine: 2,
  timespan: null, graphtype: null, graphTypes: ['mib2.bits', 'mib2.errors'],
  graphs: [{
    index: 0, kind: 'prefab', title: 'Uplink', timespan: '7_day', graphtype: 'mib2.bits',
    resourceId: 'node[1].nodeSnmp[]', resource: { id: 'node[1].nodeSnmp[]', label: 'rtr', typeName: 'node', typeLabel: 'Node', link: null },
    valid: true, start: 0, end: 3600000
  }],
  ...over
})

const mountView = async (props: Record<string, string>) => {
  const wrapper = mount(GraphCollectionView, {
    props,
    global: {
      plugins: [createTestingPinia({ stubActions: false }), PrimeVue],
      stubs: {
        'router-link': { props: ['to'], template: '<a :href="typeof to === \'string\' ? to : to.path"><slot /></a>' },
        BreadCrumbs: true,
        GraphCollectionGrid: { props: ['collection'], template: '<div data-test="grid" :data-count="collection.graphs.length" />' }
      }
    }
  })
  await flushPromises()
  return wrapper
}

describe('GraphCollectionView.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    for (const key of Object.keys(query)) {
      delete query[key]
    }
    getGraphCollectionTimespans.mockResolvedValue({ success: true, message: '', payload: [{ id: '1_day', label: '1 day' }, { id: '7_day', label: '7 day' }] })
  })

  it('resolves a stored collection with the overrides from the route query', async () => {
    query.timespan = '1_day'
    getResolvedGraphCollection.mockResolvedValue({ success: true, message: '', payload: { collection: resolved({ timespan: '1_day' }), etag: '"e"' }})
    const wrapper = await mountView({ id: '1' })
    expect(getResolvedGraphCollection).toHaveBeenCalledWith(1, { timespan: '1_day', graphtype: null })
    expect(wrapper.find('[data-test="gc-view-title"]').text()).toBe('Core')
    expect(wrapper.find('[data-test="grid"]').attributes('data-count')).toBe('1')
    expect(wrapper.find('[data-test="gc-back"]').exists()).toBe(true)
    // data-test sits on the button inside the link
    expect(wrapper.find('[data-test="gc-customize"]').element.closest('a')?.getAttribute('href')).toBe('/graph-collections/1/edit')
    expect(wrapper.find('[data-test="gc-save-as"]').exists()).toBe(false)
    // the timespan list is only fetched for a collection that offers the override
    expect(getGraphCollectionTimespans).toHaveBeenCalledTimes(1)
  })

  it('puts a chosen override into the route query instead of local state', async () => {
    getResolvedGraphCollection.mockResolvedValue({ success: true, message: '', payload: { collection: resolved(), etag: null }})
    const wrapper = await mountView({ id: '1' })
    const select = wrapper.findComponent({ name: 'OnmsSelect' })
    select.vm.$emit('update:modelValue', '7_day')
    expect(replace).toHaveBeenCalledWith({ query: { timespan: '7_day' }})
    select.vm.$emit('update:modelValue', 'none')
    expect(replace).toHaveBeenLastCalledWith({ query: {}})
  })

  it('builds a node collection on the fly and offers no way back to the stored list', async () => {
    getResolvedSyntheticGraphCollection.mockResolvedValue({
      success: true, message: '', payload: resolved({ id: null, type: 'node', title: 'Node 7', showTimespanButton: false, showGraphtypeButton: false })
    })
    const wrapper = await mountView({ kind: 'node', source: '7' })
    expect(getResolvedSyntheticGraphCollection).toHaveBeenCalledWith('node', '7', { timespan: null, graphtype: null })
    expect(wrapper.find('[data-test="gc-view-title"]').text()).toBe('Node 7')
    expect(wrapper.find('[data-test="gc-back"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="gc-customize"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="gc-save-as"]').exists()).toBe(true)
    expect(getGraphCollectionTimespans).not.toHaveBeenCalled()
  })

  it('shows the collection asked for last when an earlier load answers late', async () => {
    const first = deferred<unknown>()
    const second = deferred<unknown>()
    getResolvedGraphCollection.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise)
    const wrapper = await mountView({ id: '1' })
    await wrapper.setProps({ id: '2' })
    second.resolve({ success: true, message: '', payload: { collection: resolved({ id: 2, title: 'Edge' }), etag: null }})
    await flushPromises()
    first.resolve({ success: true, message: '', payload: { collection: resolved({ id: 1, title: 'Core' }), etag: null }})
    await flushPromises()
    expect(wrapper.find('[data-test="gc-view-title"]').text()).toBe('Edge')
  })

  it('drops the previous collection when a navigation to another one fails', async () => {
    getResolvedGraphCollection.mockResolvedValueOnce({ success: true, message: '', payload: { collection: resolved(), etag: null }})
    const wrapper = await mountView({ id: '1' })
    expect(wrapper.find('[data-test="grid"]').exists()).toBe(true)

    getResolvedGraphCollection.mockResolvedValueOnce({ success: false, message: 'Graph collection 99 was not found.' })
    await wrapper.setProps({ id: '99' })
    await flushPromises()
    expect(wrapper.find('[data-test="load-error"]').text()).toContain('Graph collection 99 was not found.')
    expect(wrapper.find('[data-test="grid"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="gc-view-title"]').text()).toBe('Graph Collection')
  })

  it('says when the collection has no graphs, and shows a load failure with a retry', async () => {
    getResolvedGraphCollection.mockResolvedValueOnce({ success: true, message: '', payload: { collection: resolved({ graphs: [] }), etag: null }})
    const wrapper = await mountView({ id: '1' })
    expect(wrapper.find('[data-test="gc-no-graphs"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="grid"]').exists()).toBe(false)

    getResolvedGraphCollection.mockResolvedValueOnce({ success: false, message: 'Graph collection 9 does not exist.' })
    const failed = await mountView({ id: '9' })
    expect(failed.find('[data-test="load-error"]').text()).toContain('Graph collection 9 does not exist.')
    expect(failed.find('[data-test="gc-view-title"]').text()).toBe('Graph Collection')
    getResolvedGraphCollection.mockResolvedValueOnce({ success: true, message: '', payload: { collection: resolved(), etag: null }})
    await failed.find('[data-test="gc-retry"]').trigger('click')
    await flushPromises()
    expect(failed.find('[data-test="load-error"]').exists()).toBe(false)
  })
})
