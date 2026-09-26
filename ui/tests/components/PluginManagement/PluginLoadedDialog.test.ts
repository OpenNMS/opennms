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

const PLUGIN = { karName: 'alec', fileName: 'alec.kar', sha256: '0123', size: 2048, uploadedBy: 'admin', uploadedAt: 1, features: ['alec', 'alec-ui'], bootFile: 'etc/featuresBoot.d/alec.boot', autoStart: true, status: 'staged' as const, pendingRestart: false }
const INSTRUCTIONS = { packages: 'systemctl restart opennms', container: 'docker restart horizon', healthCheck: 'opennms status', note: 'Wait.' }

const mountDialog = (result: any) => mount(PluginLoadedDialog, {
  props: { visible: true, result },
  global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}
})

describe('PluginLoadedDialog.vue', () => {
  it('says the container loads an auto-start plugin within seconds and shows no restart commands', () => {
    const wrapper = mountDialog({ plugin: PLUGIN, restartRequired: false, restartInstructions: INSTRUCTIONS })
    expect(wrapper.find('[data-test="dialog-header"]').text()).toBe('Plugin alec loaded')
    const note = wrapper.find('[data-test="loaded-note-auto"]')
    expect(note.exists()).toBe(true)
    expect(note.text()).toContain('within seconds')
    expect(note.text()).toContain('every later boot')
    expect(wrapper.find('[data-test="loaded-note-restart"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="restart-packages"]').exists()).toBe(false)
    const written = wrapper.find('[data-test="written-list"]').text()
    expect(written).toContain('alec.kar')
    expect(written).toContain('etc/featuresBoot.d/alec.boot')
    expect(written).toContain('alec, alec-ui')
  })

  it('says a Karaf-Feature-Start: false plugin waits for the next restart and shows the commands', () => {
    const wrapper = mountDialog({ plugin: { ...PLUGIN, autoStart: false, pendingRestart: true }, restartRequired: true, restartInstructions: INSTRUCTIONS })
    expect(wrapper.find('[data-test="dialog-header"]').text()).toBe('Plugin alec staged')
    const note = wrapper.find('[data-test="loaded-note-restart"]')
    expect(note.exists()).toBe(true)
    expect(note.text()).toContain('Karaf-Feature-Start: false')
    expect(note.text()).toContain('next restart')
    expect(wrapper.find('[data-test="loaded-note-auto"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="restart-packages"]').text()).toBe('systemctl restart opennms')
  })

  it('closes from the footer button', async () => {
    const wrapper = mountDialog({ plugin: PLUGIN, restartRequired: false, restartInstructions: INSTRUCTIONS })
    await wrapper.find('[data-test="close-button"]').trigger('click')
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })
})
