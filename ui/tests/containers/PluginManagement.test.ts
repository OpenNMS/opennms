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

import PluginManagement from '@/containers/PluginManagement.vue'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showToast } = vi.hoisted(() => ({ showToast: vi.fn() }))
vi.mock('@opennms/onms-ui', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@opennms/onms-ui')>()
  return { ...actual, useOnmsToast: () => ({ showToast, hideAllToasts: vi.fn() }) }
})

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><h2>{{ header }}</h2><slot /><slot name="footer" /></div>'
}

const PLUGIN = { karName: 'alec', fileName: 'alec.kar', sha256: 'abc', size: 10, uploadedBy: 'admin', uploadedAt: 1, features: ['alec'], bootFile: 'alec.boot', autoStart: true, status: 'installed', pendingRestart: false, source: 'upload', managed: true }
const INSTRUCTIONS = { packages: 'systemctl restart opennms', container: 'docker restart horizon', healthCheck: 'opennms status', note: 'Wait.' }
const STATE = { containerAvailable: true, opennmsHome: '/opt/opennms', deployDir: '/opt/opennms/deploy', restartRequired: false, plugins: [PLUGIN] }

const mounted: VueWrapper<any>[] = []

const mountPage = async (state: Record<string, unknown>, stubLoadCard = true) => {
  const wrapper = mount(PluginManagement, {
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true, initialState: { pluginManagementStore: state }})],
      stubs: { BreadCrumbs: true, PluginManagementAbout: true, PluginLoadCard: stubLoadCard, PluginActivityLog: true, Dialog: DialogStub }
    }
  })
  await flushPromises()
  mounted.push(wrapper)
  return wrapper
}

describe('PluginManagement.vue (container)', () => {
  // PrimeVue's TabList positions its ink bar from a timer it never clears
  beforeEach(() => vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] }))
  afterEach(() => {
    vi.runOnlyPendingTimers()
    mounted.splice(0).forEach(w => w.unmount())
    vi.useRealTimers()
    vi.clearAllMocks()
  })

  it('loads everything on mount and renders the title without banners', async () => {
    const wrapper = await mountPage({ state: STATE, restartInstructions: INSTRUCTIONS, log: '' })
    const store = usePluginManagementStore()
    expect(store.load).toHaveBeenCalled()
    expect(store.getRestartInstructions).toHaveBeenCalled()
    expect(store.refreshLog).toHaveBeenCalled()
    expect(wrapper.find('.page-title').text()).toBe('Plugin Management')
    expect(wrapper.find('[data-test="restart-banner"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="container-unavailable"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(false)
    expect(wrapper.findAll('[data-test="unload-plugin"]')).toHaveLength(1)
    expect(wrapper.findComponent({ name: 'PluginManagementAbout' }).exists()).toBe(false)
    expect(wrapper.find('[data-test="temp-files"]').exists()).toBe(false)
  })

  it('opens the load card on the repository tab and reads the catalog', async () => {
    const wrapper = await mountPage({ state: STATE, restartInstructions: INSTRUCTIONS, log: '' }, false)
    const store = usePluginManagementStore()
    expect(store.loadCatalog).toHaveBeenCalled()
    expect(wrapper.find('[data-test="load-tab-repository"]').attributes('aria-selected')).toBe('true')
    expect(wrapper.find('[data-test="load-tab-file"]').attributes('aria-selected')).toBe('false')
    expect(wrapper.find('[data-test="catalog-loading"]').exists()).toBe(true)
  })

  it('reports the temporary download area under the load card', async () => {
    const wrapper = await mountPage({ state: { ...STATE, tempDir: '/opt/opennms/data/tmp/plugins', tempBytes: 3 * 1024 * 1024, tempFiles: 2 }, restartInstructions: INSTRUCTIONS })
    const line = wrapper.find('[data-test="temp-files"]')
    expect(line.text()).toBe('Temporary files: 2 files, 3.0 MB')
    expect(line.attributes('title')).toBe('/opt/opennms/data/tmp/plugins')
    expect((await mountPage({ state: { ...STATE, tempBytes: 0, tempFiles: 0 }, restartInstructions: INSTRUCTIONS })).find('[data-test="temp-files"]').exists()).toBe(false)
    expect((await mountPage({ state: { ...STATE, tempBytes: 10, tempFiles: 1 }, restartInstructions: INSTRUCTIONS })).find('[data-test="temp-files"]').text()).toBe('Temporary files: 1 file, 10 B')
  })

  it('shows the restart banner with the counts and opens the instructions dialog', async () => {
    const plugins = [
      { ...PLUGIN, karName: 'a', status: 'staged', pendingRestart: true },
      { ...PLUGIN, karName: 'b', status: 'staged', pendingRestart: true },
      { ...PLUGIN, karName: 'c', status: 'unloaded', pendingRestart: true },
      { ...PLUGIN, karName: 'd', status: 'unloaded', pendingRestart: false },
      PLUGIN
    ]
    const wrapper = await mountPage({ state: { ...STATE, restartRequired: true, plugins }, restartInstructions: INSTRUCTIONS })
    expect(wrapper.find('[data-test="restart-summary"]').text()).toBe('A restart is required to finish loading or unloading plugins: 2 to load, 1 to unload.')
    await wrapper.find('[data-test="show-restart-instructions"]').trigger('click')
    const dialog = wrapper.find('[data-test="restart-instructions-dialog"]')
    expect(dialog.exists()).toBe(true)
    expect(dialog.find('[data-test="restart-packages"]').text()).toBe('systemctl restart opennms')
    expect(dialog.find('[data-test="restart-container"]').text()).toBe('docker restart horizon')
    expect(dialog.find('[data-test="restart-health-check"]').text()).toBe('opennms status')
    expect(dialog.find('[data-test="restart-note"]').text()).toBe('Wait.')
  })

  it('flags an unavailable container and disables unloading', async () => {
    const wrapper = await mountPage({ state: { ...STATE, containerAvailable: false }, restartInstructions: null })
    expect(wrapper.find('[data-test="container-unavailable"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="unload-plugin"]').attributes('disabled')).toBeDefined()
    expect(wrapper.findComponent({ name: 'PluginLoadCard' }).props('disabled')).toBe(true)
  })

  it('shows the load error and opens the unload dialog for a plugin, toasting after it is unloaded', async () => {
    const wrapper = await mountPage({ state: STATE, loadError: true, restartInstructions: INSTRUCTIONS })
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(true)
    await wrapper.find('[data-test="unload-plugin"]').trigger('click')
    const dialog = wrapper.find('[data-test="plugin-unload-dialog"]')
    expect(dialog.exists()).toBe(true)
    expect(dialog.text()).toContain('Unload alec?')
    ;(wrapper.vm as any).onUnloaded({ ...PLUGIN, status: 'unloaded' })
    expect(showToast).toHaveBeenCalledWith(expect.objectContaining({ message: expect.stringContaining('alec unloaded') }))
  })
})
