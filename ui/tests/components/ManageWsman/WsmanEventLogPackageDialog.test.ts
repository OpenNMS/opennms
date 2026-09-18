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

import WsmanEventLogPackageDialog from '@/components/ManageWsman/WsmanEventLogPackageDialog.vue'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'
import { WsmanEventLogConfig } from '@/types/wsmanAdmin'

const DialogStub = { name: 'Dialog', props: ['visible', 'header', 'modal'], template: '<div v-if="visible"><slot /><slot name="footer" /></div>' }

const config: WsmanEventLogConfig = {
  version: 'v1', threads: 4, retries: 1, targetRefreshInterval: '5m',
  packages: [{ name: 'windows', filter: 'IPADDR != \'0.0.0.0\'', logs: [{ name: 'System', enabled: true, interval: 60000, maxRecords: 500, lookback: '1h', levels: null, includeEventIds: null, excludeEventIds: null, mode: 'wql' }], eventMappings: [] }]
}

const mountDialog = async (original: any) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const store = useWsmanAdminStore(pinia)
  vi.mocked(store.saveEventLog).mockResolvedValue({ success: true, message: '' })
  const wrapper = mount(WsmanEventLogPackageDialog, {
    props: { visible: false, config, original },
    global: { plugins: [PrimeVue, pinia], stubs: { Dialog: DialogStub }}
  })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return { wrapper, store }
}

describe('WsmanEventLogPackageDialog.vue', () => {
  it('requires a unique name and a filter', async () => {
    const { wrapper } = await mountDialog(null)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    await wrapper.find('[data-test="name-input"]').setValue('windows')
    expect(wrapper.text()).toContain('A package with this name exists')
    await wrapper.find('[data-test="name-input"]').setValue('dcs')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
    await wrapper.find('[data-test="filter-input"]').setValue('')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('previews the nodes a filter would read', async () => {
    const { wrapper, store } = await mountDialog(null)
    vi.mocked(store.previewEventLogFilter).mockResolvedValue({ valid: true, readableNodes: 2, matchedNodes: 9, matches: [
      { nodeId: 67, label: 'win-11', ipAddress: '127.0.0.2', location: 'Default' },
      { nodeId: 68, label: 'win-12', ipAddress: '127.0.0.3', location: null }
    ] })
    await wrapper.find('[data-test="filter-input"]').setValue('catincServers')
    await wrapper.find('[data-test="preview-button"]').trigger('click')
    await flushPromises()
    expect(store.previewEventLogFilter).toHaveBeenCalledWith('catincServers')
    expect(wrapper.find('[data-test="preview-text"]').text()).toContain('2 node(s) with the WS-Man service would be read, out of 9')
    expect(wrapper.findAll('[data-test="preview-matches"] li').map(li => li.text())).toEqual(['win-11 (127.0.0.2, Default)', 'win-12 (127.0.0.3)'])

    vi.mocked(store.previewEventLogFilter).mockResolvedValue({ valid: false, error: 'Could not parse', readableNodes: 0, matchedNodes: 0, matches: [] })
    await wrapper.find('[data-test="preview-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="preview-text"]').text()).toBe('Could not parse')
    expect(wrapper.find('[data-test="preview-text"]').classes()).toContain('error')
  })

  it('adds a package with the default logs and edits an existing one in place', async () => {
    const added = await mountDialog(null)
    await added.wrapper.find('[data-test="name-input"]').setValue('dcs')
    await added.wrapper.find('[data-test="filter-input"]').setValue('catincDomain-Controllers')
    await added.wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const saved = vi.mocked(added.store.saveEventLog).mock.calls[0][0]
    expect(saved.packages.map(p => p.name)).toEqual(['windows', 'dcs'])
    expect(saved.packages[1].filter).toBe('catincDomain-Controllers')
    expect(saved.packages[1].logs.map(l => l.name)).toEqual(['System', 'Application'])
    expect(added.wrapper.emitted('update:visible')?.at(-1)).toEqual([false])

    const edited = await mountDialog(config.packages[0])
    await edited.wrapper.find('[data-test="filter-input"]').setValue('IPADDR IPLIKE 10.*.*.*')
    await edited.wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const renamed = vi.mocked(edited.store.saveEventLog).mock.calls[0][0]
    expect(renamed.packages).toHaveLength(1)
    expect(renamed.packages[0].filter).toBe('IPADDR IPLIKE 10.*.*.*')
    expect(renamed.packages[0].logs).toHaveLength(1)
  })
})
