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

import SnmpDataCollectionDefinitionSearch from '@/components/SnmpDataCollection/SnmpDataCollectionDefinitionSearch.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { OnmsAutoComplete, OnmsSelect, OnmsTooltip } from '@opennms/onms-ui'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

const mockFindMibGroupsByName = vi.fn()
const mockFindResourceTypesByName = vi.fn()
const mockGetAllMibGroupNames = vi.fn()
const mockGetAllResourceTypeNames = vi.fn()
vi.mock('@/services/snmpDataCollectionService', () => ({
  findMibGroupsByName: (...args: any[]) => mockFindMibGroupsByName(...args),
  findResourceTypesByName: (...args: any[]) => mockFindResourceTypesByName(...args),
  findSystemDefsByName: vi.fn().mockResolvedValue([]),
  getAllMibGroupNames: (...args: any[]) => mockGetAllMibGroupNames(...args),
  getAllResourceTypeNames: (...args: any[]) => mockGetAllResourceTypeNames(...args),
  getAllSystemDefNames: vi.fn().mockResolvedValue([])
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: vi.fn() })
}))

const mountSearch = () => mount(SnmpDataCollectionDefinitionSearch, {
  global: {
    plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
    directives: { 'onms-tooltip': OnmsTooltip }
  }
})

const search = async (wrapper: ReturnType<typeof mountSearch>, name: string) => {
  await wrapper.find('[data-test="definition-name-input"] input').setValue(name)
  await wrapper.find('[data-test="definition-search-button"]').trigger('click')
  await flushPromises()
}

describe('SnmpDataCollectionDefinitionSearch.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('lists every source that defines the name', async () => {
    mockFindMibGroupsByName.mockResolvedValue([
      { id: 1, name: 'shared', ifType: 'all', enabled: true, collectionSourceId: 10, collectionSourceName: 'MIB2' },
      { id: 2, name: 'shared', ifType: 'all', enabled: false, collectionSourceId: 11, collectionSourceName: '__inline_custom' }
    ])
    const wrapper = mountSearch()

    await search(wrapper, 'shared')

    expect(mockFindMibGroupsByName).toHaveBeenCalledWith('shared')
    const table = wrapper.find('[data-test="definition-results-table"]')
    expect(table.exists()).toBe(true)
    expect(table.text()).toContain('MIB2')
    expect(table.text()).toContain('__inline_custom')
  })

  it('shows a message when no source defines the name', async () => {
    mockFindMibGroupsByName.mockResolvedValue([])
    const wrapper = mountSearch()

    await search(wrapper, 'missing')

    expect(wrapper.find('[data-test="definition-results-table"]').exists()).toBe(false)
    expect(wrapper.text()).toContain('No source defines a MIB group named \'missing\'.')
  })

  it('opens the source on the MIB groups tab, filtered to the name', async () => {
    mockFindMibGroupsByName.mockResolvedValue([
      { id: 1, name: 'shared', ifType: 'all', enabled: true, collectionSourceId: 10, collectionSourceName: 'MIB2' }
    ])
    const wrapper = mountSearch()
    const detailStore = useSnmpDataCollectionDetailStore()

    await search(wrapper, 'shared')
    await wrapper.find('[data-test="view-in-source-button"]').trigger('click')

    expect(detailStore.activeTab).toBe(1)
    expect(detailStore.mibGroupsSearchTerm).toBe('shared')
    expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection Source Detail', params: { id: 10 }})
  })

  it('discards a pending response when the type changes', async () => {
    let resolveMibGroups: (rows: unknown[]) => void = () => {}
    mockFindMibGroupsByName.mockReturnValue(new Promise((resolve) => {
      resolveMibGroups = resolve
    }))
    const wrapper = mountSearch()

    await wrapper.find('[data-test="definition-name-input"] input').setValue('shared')
    await wrapper.find('[data-test="definition-search-button"]').trigger('click')
    wrapper.findComponent(OnmsSelect).vm.$emit('update:modelValue', 'resourcetypes')
    resolveMibGroups([
      { id: 1, name: 'shared', ifType: 'all', enabled: true, collectionSourceId: 10, collectionSourceName: 'MIB2' }
    ])
    await flushPromises()

    expect(wrapper.find('[data-test="definition-results-table"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(false)
  })

  it('shows only the latest search when responses arrive out of order', async () => {
    let resolveFirst: (rows: unknown[]) => void = () => {}
    mockFindMibGroupsByName
      .mockReturnValueOnce(new Promise((resolve) => {
        resolveFirst = resolve
      }))
      .mockResolvedValueOnce([
        { id: 2, name: 'second', ifType: 'all', enabled: true, collectionSourceId: 20, collectionSourceName: 'Second' }
      ])
    const wrapper = mountSearch()

    await wrapper.find('[data-test="definition-name-input"] input').setValue('first')
    await wrapper.find('[data-test="definition-search-button"]').trigger('click')
    await search(wrapper, 'second')
    resolveFirst([
      { id: 1, name: 'first', ifType: 'all', enabled: true, collectionSourceId: 10, collectionSourceName: 'First' }
    ])
    await flushPromises()

    const table = wrapper.find('[data-test="definition-results-table"]')
    expect(table.text()).toContain('Second')
    expect(table.text()).not.toContain('First')
  })

  it('does not suggest the instance values as resource type names', async () => {
    mockGetAllResourceTypeNames.mockResolvedValue(['0', 'ifIndex', 'hrStorageIndex'])
    const wrapper = mountSearch()

    wrapper.findComponent(OnmsSelect).vm.$emit('update:modelValue', 'resourcetypes')
    await flushPromises()
    wrapper.findComponent(OnmsAutoComplete).vm.$emit('complete', '')
    await flushPromises()

    expect(wrapper.findComponent(OnmsAutoComplete).props('suggestions')).toEqual(['hrStorageIndex'])
  })

  it('shows only the latest suggestions when completions finish out of order', async () => {
    let resolveFirst: (names: string[]) => void = () => {}
    mockGetAllMibGroupNames
      .mockReturnValueOnce(new Promise((resolve) => {
        resolveFirst = resolve
      }))
      .mockResolvedValueOnce(['mib2-interfaces', 'ucd-loadavg'])
    const wrapper = mountSearch()
    const autoComplete = wrapper.findComponent(OnmsAutoComplete)

    autoComplete.vm.$emit('complete', 'mib')
    autoComplete.vm.$emit('complete', 'ucd')
    await flushPromises()
    resolveFirst(['mib2-interfaces', 'ucd-loadavg'])
    await flushPromises()

    expect(autoComplete.props('suggestions')).toEqual(['ucd-loadavg'])
  })

  it('clears the suggestions when the type changes', async () => {
    mockGetAllMibGroupNames.mockResolvedValue(['mib2-interfaces'])
    const wrapper = mountSearch()
    const autoComplete = wrapper.findComponent(OnmsAutoComplete)

    autoComplete.vm.$emit('complete', 'mib')
    await flushPromises()
    expect(autoComplete.props('suggestions')).toEqual(['mib2-interfaces'])

    wrapper.findComponent(OnmsSelect).vm.$emit('update:modelValue', 'resourcetypes')
    await flushPromises()

    expect(autoComplete.props('suggestions')).toEqual([])
  })

  it('loads the names again when the tab opens', async () => {
    mockGetAllMibGroupNames
      .mockResolvedValueOnce(['mib2-interfaces'])
      .mockResolvedValueOnce(['mib2-interfaces', 'new-group'])
    const wrapper = mountSearch()
    const pageStore = useSnmpDataCollectionStore()
    const autoComplete = wrapper.findComponent(OnmsAutoComplete)

    autoComplete.vm.$emit('complete', '')
    await flushPromises()
    expect(autoComplete.props('suggestions')).toEqual(['mib2-interfaces'])

    pageStore.activeTab = 1
    await flushPromises()
    pageStore.activeTab = 3
    await flushPromises()
    autoComplete.vm.$emit('complete', '')
    await flushPromises()

    expect(autoComplete.props('suggestions')).toEqual(['mib2-interfaces', 'new-group'])
  })
})
