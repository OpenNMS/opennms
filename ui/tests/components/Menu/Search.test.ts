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

import Search from '@/components/Menu/Search.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useSearchStore } from '@/stores/searchStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it, vi } from 'vitest'

const externalItem = {
  label: 'Chat',
  url: 'https://chat.opennms.org',
  properties: { isExternalLink: 'true', linkTarget: '_blank' }
}

const mountWithResult = async () => {
  const pinia = createTestingPinia({ stubActions: false })
  useMenuStore().mainMenu = { baseHref: '/opennms/' } as any
  const wrapper = mount(Search, { attachTo: document.body, global: { plugins: [pinia, PrimeVue] }})
  // set after mount: Search only rebuilds its flat result list when the results change
  const searchStore = useSearchStore()
  searchStore.searchResultsByContext = [
    { label: 'Action', results: { Menu: { results: [externalItem] }}}
  ] as any

  const input = wrapper.find('input')
  await input.setValue('chat')
  await flushPromises()

  return { wrapper, input }
}

describe('Search external links', () => {
  afterEach(() => {
    vi.restoreAllMocks()
    document.body.innerHTML = ''
  })

  it('opens external result on click without baseHref', async () => {
    const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null)
    const hrefBefore = window.location.href
    const { wrapper } = await mountWithResult()

    await wrapper.find('.search-result-button').trigger('click')

    expect(openSpy).toHaveBeenCalledWith('https://chat.opennms.org', '_blank', 'noopener')
    expect(window.location.href).toBe(hrefBefore)
  })

  it('opens external result on ArrowDown + Enter', async () => {
    const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null)
    const hrefBefore = window.location.href
    const { input } = await mountWithResult()

    await input.trigger('keydown', { key: 'ArrowDown' })
    await flushPromises()
    await input.trigger('keydown', { key: 'Enter' })
    await flushPromises()

    expect(openSpy).toHaveBeenCalledWith('https://chat.opennms.org', '_blank', 'noopener')
    expect(window.location.href).toBe(hrefBefore)
  })
})
