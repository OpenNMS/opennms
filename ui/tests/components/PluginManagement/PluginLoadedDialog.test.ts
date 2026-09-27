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

import PluginLoadedDialog from '@/components/PluginManagement/PluginLoadedDialog.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><h2 data-test="dialog-header">{{ header }}</h2><slot /><slot name="footer" /></div>'
}

const PLUGIN = { karName: 'alec', fileName: 'alec.kar', sha256: '0123', size: 2048, uploadedBy: 'admin', uploadedAt: 1, features: ['alec', 'alec-ui'], bootFile: 'etc/featuresBoot.d/alec.boot', autoStart: true, status: 'installed' as const, pendingRestart: false, source: 'upload', managed: true }
const INSTRUCTIONS = { packages: 'systemctl restart opennms', container: 'docker restart horizon', healthCheck: 'opennms status', note: 'Wait.' }

const outcome = (state: string, message = '', diagnostics: Record<string, string> = {}) => ({ state, message, diagnostics })

const mountDialog = (result: any) => mount(PluginLoadedDialog, {
  props: { visible: true, result },
  global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}
})

const STATES = ['outcome-started', 'outcome-failed', 'outcome-timeout', 'outcome-unavailable', 'outcome-restart-required']

const onlyCallout = (wrapper: ReturnType<typeof mountDialog>, shown: string) => {
  STATES.forEach(state => expect(wrapper.find(`[data-test="${state}"]`).exists(), state).toBe(state === shown))
}

describe('PluginLoadedDialog.vue', () => {
  it('reports a started plugin with its features and no restart commands', () => {
    const wrapper = mountDialog({ plugin: PLUGIN, startOutcome: outcome('started'), restartRequired: false, restartInstructions: INSTRUCTIONS })
    expect(wrapper.find('[data-test="dialog-header"]').text()).toBe('Plugin alec loaded')
    onlyCallout(wrapper, 'outcome-started')
    expect(wrapper.find('[data-test="outcome-started"]').text()).toBe('Its features started: alec, alec-ui.')
    expect(wrapper.find('[data-test="restart-packages"]').exists()).toBe(false)
    const written = wrapper.find('[data-test="written-list"]').text()
    expect(written).toContain('alec.kar')
    expect(written).toContain('etc/featuresBoot.d/alec.boot')
    expect(written).toContain('alec, alec-ui')
  })

  it('reports a failed start with the message, the diagnostics per feature and the recovery hint', () => {
    const wrapper = mountDialog({
      plugin: { ...PLUGIN, status: 'failed' },
      startOutcome: outcome('failed', 'Feature alec did not start.', { alec: 'Unable to resolve org.foo: missing requirement osgi.wiring.package', 'alec-ui': 'Bundle alec-ui is in state Installed' }),
      restartRequired: true,
      restartInstructions: INSTRUCTIONS
    })
    expect(wrapper.find('[data-test="dialog-header"]').text()).toBe('Plugin alec loaded, but it did not start')
    onlyCallout(wrapper, 'outcome-failed')
    expect(wrapper.find('[data-test="start-failure-message"]').text()).toBe('Feature alec did not start.')
    const diagnostics = wrapper.find('[data-test="start-failure-diagnostics"]').text()
    expect(diagnostics).toContain('alec: Unable to resolve org.foo')
    expect(diagnostics).toContain('alec-ui: Bundle alec-ui is in state Installed')
    expect(wrapper.find('[data-test="start-failure-hint"]').text()).toBe('You can fix the cause and use Restart on the plugin row, or unload it.')
    expect(wrapper.find('[data-test="restart-packages"]').exists()).toBe(false)
  })

  it('says a KAR the container did not pick up in time is staged, without restart commands', () => {
    const wrapper = mountDialog({ plugin: { ...PLUGIN, status: 'staged' }, startOutcome: outcome('timeout'), restartRequired: true, restartInstructions: INSTRUCTIONS })
    expect(wrapper.find('[data-test="dialog-header"]').text()).toBe('Plugin alec staged')
    onlyCallout(wrapper, 'outcome-timeout')
    expect(wrapper.find('[data-test="outcome-timeout"]').text()).toContain('within 30 seconds')
    expect(wrapper.find('[data-test="outcome-timeout"]').text()).toContain('when you use Restart')
    expect(wrapper.find('[data-test="restart-packages"]').exists()).toBe(false)
  })

  it('shows the restart commands when the container was unavailable', () => {
    const wrapper = mountDialog({ plugin: { ...PLUGIN, status: 'staged', pendingRestart: true }, startOutcome: outcome('unavailable', 'The container is not running.'), restartRequired: true, restartInstructions: INSTRUCTIONS })
    expect(wrapper.find('[data-test="dialog-header"]').text()).toBe('Plugin alec staged')
    onlyCallout(wrapper, 'outcome-unavailable')
    expect(wrapper.find('[data-test="outcome-unavailable"]').text()).toContain('The container is not running.')
    expect(wrapper.find('[data-test="restart-packages"]').text()).toBe('systemctl restart opennms')
    expect(wrapper.find('[data-test="restart-container"]').text()).toBe('docker restart horizon')
  })

  it('shows the catalog message and the restart commands for a Karaf-Feature-Start: false plugin', () => {
    const wrapper = mountDialog({ plugin: { ...PLUGIN, autoStart: false, status: 'staged', pendingRestart: true }, startOutcome: outcome('restart-required', 'The manifest sets Karaf-Feature-Start: false.'), restartRequired: true, restartInstructions: INSTRUCTIONS })
    expect(wrapper.find('[data-test="dialog-header"]').text()).toBe('Plugin alec staged')
    onlyCallout(wrapper, 'outcome-restart-required')
    expect(wrapper.find('[data-test="outcome-restart-required"]').text()).toContain('The manifest sets Karaf-Feature-Start: false.')
    expect(wrapper.find('[data-test="outcome-restart-required"]').text()).toContain('listed as staged')
    expect(wrapper.find('[data-test="restart-packages"]').text()).toBe('systemctl restart opennms')
  })

  it('closes from the footer button', async () => {
    const wrapper = mountDialog({ plugin: PLUGIN, startOutcome: outcome('started'), restartRequired: false, restartInstructions: INSTRUCTIONS })
    await wrapper.find('[data-test="close-button"]').trigger('click')
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })
})
