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

import { type PluginEntry } from '@/types/pluginManagement'
import PluginRestartDialog from '@/components/PluginManagement/PluginRestartDialog.vue'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/pluginManagementStore')

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><h2 data-test="dialog-header">{{ header }}</h2><slot /><slot name="footer" /></div>'
}

const PLUGIN: PluginEntry = { karName: 'alec', fileName: 'alec.kar', sha256: 'abc', size: 10, uploadedBy: 'admin', uploadedAt: 1, features: ['alec', 'alec-ui'], bootFile: 'alec.boot', autoStart: true, status: 'failed', pendingRestart: false, source: 'upload', managed: true, diagnostics: { alec: 'Unable to resolve' }}

describe('PluginRestartDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountDialog = async (plugin = PLUGIN) => {
    wrapper = mount(PluginRestartDialog, {
      props: { visible: false, plugin },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = { restart: vi.fn().mockResolvedValue({ success: true, message: '', payload: { plugin: { ...PLUGIN, status: 'installed', diagnostics: {}}, startOutcome: { state: 'started', message: '', diagnostics: {}}, restartRequired: false }}) }
    vi.mocked(usePluginManagementStore).mockReturnValue(store)
  })

  it('names the features it restarts and shows Started with a Close button when the restart succeeds', async () => {
    await mountDialog()
    expect(wrapper.find('[data-test="dialog-header"]').text()).toBe('Restart alec?')
    expect(wrapper.find('[data-test="restart-callout"]').text()).toBe('Restarts alec, alec-ui; the plugin is unavailable for a few seconds and comes back in the same mode.')
    expect(wrapper.find('[data-test="restart-button"]').classes()).not.toContain('p-button-danger')
    expect(wrapper.find('[data-test="close-button"]').exists()).toBe(false)
    await wrapper.find('[data-test="restart-button"]').trigger('click')
    await flushPromises()
    expect(store.restart).toHaveBeenCalledWith('alec')
    expect(wrapper.find('[data-test="restart-started"]').text()).toContain('Started')
    expect(wrapper.find('[data-test="restart-started"]').text()).toContain('alec, alec-ui')
    expect(wrapper.find('[data-test="restart-not-started"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="restart-unhealthy"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="restart-callout"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="restart-button"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="cancel-button"]').exists()).toBe(false)
    expect(wrapper.emitted('restarted')?.[0][0]).toMatchObject({ karName: 'alec', status: 'installed' })
    expect(wrapper.emitted('update:visible')).toBeUndefined()
    await wrapper.find('[data-test="close-button"]').trigger('click')
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('shows the failure with its diagnostics and the unload hint when the features do not start', async () => {
    store.restart.mockResolvedValueOnce({ success: true, message: '', payload: {
      plugin: PLUGIN,
      startOutcome: { state: 'failed', message: 'Feature alec did not start.', diagnostics: { alec: 'Unable to resolve org.foo' }},
      restartRequired: true
    }})
    await mountDialog()
    await wrapper.find('[data-test="restart-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="restart-started"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="start-failure-message"]').text()).toBe('Feature alec did not start.')
    expect(wrapper.find('[data-test="start-failure-diagnostics"]').text()).toBe('alec: Unable to resolve org.foo')
    expect(wrapper.find('[data-test="start-failure-hint"]').text()).toContain('unload')
    expect(wrapper.find('[data-test="close-button"]').exists()).toBe(true)
  })

  it('keeps the confirmation and shows the reason when the request itself fails', async () => {
    store.restart.mockResolvedValueOnce({ success: false, message: 'The plugin container is not available; the plugin was not restarted.' })
    await mountDialog()
    await wrapper.find('[data-test="restart-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('not available')
    expect(wrapper.find('[data-test="restart-callout"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="restart-button"]').exists()).toBe(true)
    expect(wrapper.emitted('restarted')).toBeUndefined()
    expect(wrapper.emitted('update:visible')).toBeUndefined()
  })

  it('forgets the previous outcome when reopened, and cancels without restarting', async () => {
    await mountDialog()
    await wrapper.find('[data-test="restart-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="restart-started"]').exists()).toBe(true)
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ visible: true })
    expect(wrapper.find('[data-test="restart-started"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="restart-callout"]').exists()).toBe(true)
    await wrapper.find('[data-test="cancel-button"]').trigger('click')
    expect(store.restart).toHaveBeenCalledTimes(1)
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('shows Started together with the health checks that are not passing yet', async () => {
    store.restart.mockResolvedValueOnce({ success: true, message: '', payload: {
      plugin: { ...PLUGIN, status: 'installed', health: 'unhealthy', healthMessages: ['ALEC :: Driver: Failure (no datasource)'] },
      startOutcome: { state: 'started', message: 'Started alec.', diagnostics: {}, healthy: false, health: [{ description: 'ALEC :: Driver', status: 'Failure', message: 'no datasource' }] },
      restartRequired: false
    }})
    await mountDialog()
    await wrapper.find('[data-test="restart-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="restart-started"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="restart-unhealthy"]').exists()).toBe(true)
    expect(wrapper.findAll('[data-test="health-list"] li').map(li => li.text())).toEqual(['ALEC :: Driver: Failure (no datasource)'])
    expect(wrapper.find('[data-test="restart-failed"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="restart-not-started"]').exists()).toBe(false)
  })
})
