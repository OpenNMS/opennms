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

import ManageWsman from '@/containers/ManageWsman.vue'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

const SETTINGS = {
  retry: 1, timeout: 30000, username: 'root', hasPassword: true, port: null, maxElements: null,
  ssl: true, strictSsl: false, path: '/wsman', productVendor: null, productVersion: null, gssAuth: null
}

const CONFIG = { version: 'v1', defaults: SETTINGS, definitions: [] }

const mountPage = async (state: Record<string, unknown>) => {
  const wrapper = mount(ManageWsman, {
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true, initialState: { wsmanAdminStore: state }})],
      stubs: { WsmanHelpPanel: true, WsmanDefaultsCard: true, WsmanDefinitionsTable: true, WsmanDataCollectionPanel: true, WsmanDefaultsDialog: true, WsmanDefinitionDialog: true, WsmanCollectionDialog: true, WsmanSystemDefinitionDialog: true, WsmanGroupDialog: true, OnmsConfirmationDialog: true, BreadCrumbs: true }
    }
  })
  await flushPromises()
  return wrapper
}

describe('ManageWsman.vue (container)', () => {
  it('loads the configuration on mount and renders the page title', async () => {
    const wrapper = await mountPage({ config: CONFIG, loadError: false })
    const store = useWsmanAdminStore()
    expect(store.getConfig).toHaveBeenCalled()
    expect(store.getDataCollection).toHaveBeenCalled()
    expect(wrapper.find('.page-title').text()).toBe('Manage WS-Man')
    expect(wrapper.find('[data-test="tab-definitions"]').text()).toContain('Server Definitions (0)')
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(false)
  })

  it('keeps the tabs on screen when a table action fails, and clears the error on the next success', async () => {
    const wrapper = await mountPage({ config: { ...CONFIG, definitions: [{ ...SETTINGS, ranges: [], specifics: ['10.0.0.1'], ipMatches: [] }, { ...SETTINGS, ranges: [], specifics: ['10.0.0.2'], ipMatches: [] }] }, loadError: false })
    const store = useWsmanAdminStore()
    vi.mocked(store.saveConfig).mockResolvedValueOnce('The WS-Man configuration changed since it was loaded; reload the page and apply the change again.')
    ;(wrapper.vm as any).moveDefinition(0, 1)
    await flushPromises()
    expect(wrapper.find('[data-test="action-error"]').text()).toContain('changed since')
    expect(wrapper.find('[data-test="tab-definitions"]').exists()).toBe(true)
    // the store sends the loaded version along with the reordered definitions
    const input = vi.mocked(store.saveConfig).mock.calls[0][0]
    expect(input.version).toBe('v1')
    expect(input.definitions.map((d: any) => d.sourceIndex)).toEqual([1, 0])

    vi.mocked(store.saveConfig).mockResolvedValueOnce(null)
    ;(wrapper.vm as any).moveDefinition(1, -1)
    await flushPromises()
    expect(wrapper.find('[data-test="action-error"]').exists()).toBe(false)
  })

  it('deletes a data collection object by rewriting only its own file', async () => {
    const dc = {
      rrdRepository: '/rrd', sources: ['wsman-datacollection-config.xml', 'custom.xml'], versions: { 'wsman-datacollection-config.xml': 'r', 'custom.xml': 'c' },
      collections: [], groups: [{ name: 'g1', source: 'custom.xml', resourceType: 'node', resourceUri: 'u', dialect: null, filter: null, attributes: [] }], systemDefinitions: []
    }
    const wrapper = await mountPage({ config: CONFIG, loadError: false, dataCollection: dc, dataCollectionError: false })
    const store = useWsmanAdminStore()
    vi.mocked(store.saveDataCollectionFile).mockResolvedValueOnce(null)
    ;(wrapper.vm as any).askDeleteDataCollection('group', dc.groups[0])
    await (wrapper.vm as any).confirmDeleteDataCollection()
    await flushPromises()
    expect(store.saveDataCollectionFile).toHaveBeenCalledWith('custom.xml', expect.objectContaining({ version: 'c', groups: [] }))
    expect(wrapper.find('[data-test="action-error"]').exists()).toBe(false)
  })

  it('shows an error instead of an empty configuration when the read fails', async () => {
    const wrapper = await mountPage({ config: null, loadError: true })
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="tab-defaults"]').exists()).toBe(false)
  })
})
