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

import MapSearch from '@/components/Map/MapSearch.vue'
import { useMapStore } from '@/stores/mapStore'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it } from 'vitest'
import { nextTick } from 'vue'

const mountMapSearch = () => mount(MapSearch, {
  attachTo: document.body,
  global: { plugins: [PrimeVue, createTestingPinia({ stubActions: false })] }
})

describe('MapSearch', () => {
  let wrapper: VueWrapper

  afterEach(() => wrapper?.unmount())

  it('renders the leading search icon and the trailing clear control', () => {
    wrapper = mountMapSearch()
    expect(wrapper.find('.map-search__icon').exists()).toBe(true)

    const clear = wrapper.find('.map-search__clear')
    expect(clear.exists()).toBe(true)
    expect(clear.attributes('tabindex')).toBe('0')
    expect(clear.attributes('aria-label')).toBe('Clear search')
  })

  it('clears the selection, the typed text and the map filter', async () => {
    wrapper = mountMapSearch()
    const mapStore = useMapStore()
    const input = wrapper.find('input')

    // a selection made earlier, plus text typed but not yet selected
    await wrapper.findComponent({ name: 'OnmsAutoComplete' }).vm.$emit('update:modelValue', [{ label: 'node1' }])
    await input.setValue('node2')
    expect(mapStore.searchedNodeLabels).toEqual(['node1'])
    expect(wrapper.findAll('.p-autocomplete-chip-item')).toHaveLength(1)

    await wrapper.find('.map-search__clear').trigger('click')
    await nextTick()

    expect(wrapper.findAll('.p-autocomplete-chip-item')).toHaveLength(0)
    expect((input.element as HTMLInputElement).value).toBe('')
    expect(mapStore.searchedNodeLabels).toEqual([])
    expect(mapStore.nodeSearchTerm).toBe('')
  })

  it('clears from the keyboard too', async () => {
    wrapper = mountMapSearch()
    const mapStore = useMapStore()
    await wrapper.findComponent({ name: 'OnmsAutoComplete' }).vm.$emit('update:modelValue', [{ label: 'node1' }])
    expect(mapStore.searchedNodeLabels).toEqual(['node1'])

    await wrapper.find('.map-search__clear').trigger('keydown', { key: 'Enter' })
    expect(mapStore.searchedNodeLabels).toEqual([])
  })
})
