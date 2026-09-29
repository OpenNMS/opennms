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

  it('renders the leading search icon, with no clear control until there is something to clear', () => {
    wrapper = mountMapSearch()
    expect(wrapper.find('.map-search__icon').exists()).toBe(true)
    expect(wrapper.find('.map-search__clear').exists()).toBe(false)
    // the slot stays put so showing the button does not resize the panel
    expect(wrapper.find('.map-search__clear-slot').exists()).toBe(true)
  })

  it('shows the clear control once text is typed, and hides it again when it goes', async () => {
    wrapper = mountMapSearch()
    const input = wrapper.find('input')

    await input.setValue('node')
    const clear = wrapper.find('.map-search__clear')
    expect(clear.exists()).toBe(true)
    // a real button, so Enter/Space activation and focus come from the platform
    expect(clear.element.tagName).toBe('BUTTON')
    expect(clear.attributes('type')).toBe('button')
    expect(clear.attributes('aria-label')).toBe('Clear search')

    await input.setValue('')
    expect(wrapper.find('.map-search__clear').exists()).toBe(false)
  })

  it('shows the clear control for a selection with no typed text', async () => {
    wrapper = mountMapSearch()
    expect(wrapper.find('.map-search__clear').exists()).toBe(false)

    await wrapper.findComponent({ name: 'OnmsAutoComplete' }).vm.$emit('update:modelValue', [{ label: 'node1' }])
    expect(wrapper.find('.map-search__clear').exists()).toBe(true)
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
    expect(wrapper.find('.map-search__clear').exists()).toBe(false)
  })

  // PrimeVue empties its inner input itself when a suggestion is taken, without
  // firing `input`, so the tracked query has to be reset off the model change or
  // the control would linger with nothing left to clear.
  it('drops the typed text once it becomes a selection', async () => {
    wrapper = mountMapSearch()
    const autoComplete = wrapper.findComponent({ name: 'OnmsAutoComplete' })
    const input = wrapper.find('input')

    await input.setValue('node1')
    // PrimeVue's own selection handling, reproduced: the model gains the chip
    // and the inner input is emptied without an input event
    await autoComplete.vm.$emit('update:modelValue', [{ label: 'node1' }])
    const inputElement = input.element as HTMLInputElement
    inputElement.value = ''
    await nextTick()

    expect(wrapper.find('.map-search__clear').exists()).toBe(true)

    // removing the chip leaves nothing typed, so the control goes away
    await autoComplete.vm.$emit('update:modelValue', [])
    expect(wrapper.find('.map-search__clear').exists()).toBe(false)
  })
})
