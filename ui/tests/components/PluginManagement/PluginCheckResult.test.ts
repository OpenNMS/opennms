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

import PluginCheckResult from '@/components/PluginManagement/PluginCheckResult.vue'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/pluginManagementStore')

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><h2>{{ header }}</h2><slot /><slot name="footer" /></div>'
}

const INSPECTION = {
  karName: 'alec', size: 2048, sha256: '0123456789abcdef0123', uploadToken: 'tok',
  manifest: {},
  features: [
    { name: 'alec', version: '', description: null, topLevel: true, dependencies: [] },
    { name: 'alec-ui', version: '3.0.0', description: 'The web console', topLevel: true, dependencies: [] },
    { name: 'alec-api', version: '3.0.0', description: null, topLevel: false, dependencies: [] }
  ],
  bundles: [],
  checks: [{ id: 'structure', level: 'PASS', message: 'ok' }],
  suggestedFeatures: ['alec']
}
const PLUGIN = { karName: 'alec', fileName: 'alec.kar', sha256: '0123', size: 2048, uploadedBy: 'admin', uploadedAt: 1, features: ['alec'], bootFile: 'alec.boot', autoStart: true, status: 'staged', pendingRestart: true, source: 'github:OpenNMS-Plugins/alec@v3.0.4', managed: true }
const INSTRUCTIONS = { packages: 'systemctl restart opennms', container: 'docker restart horizon', healthCheck: 'opennms status', note: 'Wait.' }

describe('PluginCheckResult.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  beforeEach(() => {
    vi.clearAllMocks()
    store = { install: vi.fn() }
    vi.mocked(usePluginManagementStore).mockReturnValue(store)
  })

  afterEach(() => wrapper?.unmount())

  const mountResult = (inspection: any, disabled = false, source = 'Uploaded alec.kar (2.0 KB)') => {
    wrapper = mount(PluginCheckResult, {
      props: { inspection, source, disabled },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}
    })
  }

  it('renders nothing but the dialog host without an inspection', () => {
    mountResult(null)
    expect(wrapper.find('[data-test="inspection-summary"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="load-plugin"]').exists()).toBe(false)
  })

  it('shows the source line, the summary and the checks, with unset values marked', () => {
    mountResult(INSPECTION)
    expect(wrapper.find('[data-test="summary-source"]').text()).toBe('Uploaded alec.kar (2.0 KB)')
    expect(wrapper.find('[data-test="summary-features"]').text()).toBe('alec, alec-ui 3.0.0, alec-api 3.0.0')
    expect(wrapper.find('[data-test="summary-feature-start"]').text()).toBe('—')
    expect(wrapper.find('[data-test="summary-bundles"]').text()).toBe('0')
    expect(wrapper.findAll('[data-test="check-level"]')).toHaveLength(1)
    expect(wrapper.find('[data-test="load-plugin"]').attributes('disabled')).toBeUndefined()
  })

  it('blocks the load on a failure and asks for an acknowledgement on a warning, resetting it for the next inspection', async () => {
    mountResult({ ...INSPECTION, checks: [{ id: 'java', level: 'WARN', message: 'old' }] })
    const load = () => wrapper.find('[data-test="load-plugin"]')
    expect(load().attributes('disabled')).toBeDefined()
    expect(load().attributes('title')).toBe('Acknowledge the warnings first')
    await wrapper.find('[data-test="ack-warnings"] input[type="checkbox"]').setValue(true)
    expect(load().attributes('disabled')).toBeUndefined()
    await wrapper.setProps({ inspection: { ...INSPECTION, uploadToken: 'tok2', checks: [{ id: 'java', level: 'WARN', message: 'old' }] }})
    expect(load().attributes('disabled')).toBeDefined()
    await wrapper.setProps({ inspection: { ...INSPECTION, checks: [{ id: 'duplicate', level: 'FAIL', message: 'loaded' }, { id: 'java', level: 'WARN', message: 'old' }] }})
    expect(wrapper.find('[data-test="fail-note"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="ack-warnings"]').exists()).toBe(false)
    expect(load().attributes('title')).toBe('A check failed')
  })

  it('installs with the token, opens the dialog and emits loaded', async () => {
    mountResult(INSPECTION)
    store.install.mockResolvedValueOnce({ success: true, message: '', payload: { plugin: PLUGIN, restartRequired: true, restartInstructions: INSTRUCTIONS }})
    await wrapper.find('[data-test="load-plugin"]').trigger('click')
    await flushPromises()
    expect(store.install).toHaveBeenCalledWith({ uploadToken: 'tok', karName: 'alec', acknowledgeWarnings: false, features: ['alec'] })
    expect(wrapper.emitted('loaded')?.[0][0]).toMatchObject({ plugin: { karName: 'alec' }, restartRequired: true })
    const dialog = wrapper.find('[data-test="plugin-loaded-dialog"]')
    expect(dialog.text()).toContain('Plugin alec staged')
    expect(dialog.find('[data-test="restart-packages"]').text()).toBe(INSTRUCTIONS.packages)
    await wrapper.setProps({ inspection: null })
    expect(wrapper.find('[data-test="plugin-loaded-dialog"]').exists()).toBe(true)
  })

  it('shows a failed install inline and keeps the inspection', async () => {
    mountResult(INSPECTION)
    store.install.mockResolvedValueOnce({ success: false, message: 'The plugin container is not available; the plugin was not loaded.' })
    await wrapper.find('[data-test="load-plugin"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="load-error"]').text()).toContain('not available')
    expect(wrapper.emitted('loaded')).toBeUndefined()
    expect(wrapper.find('[data-test="inspection-summary"]').exists()).toBe(true)
    await wrapper.setProps({ inspection: { ...INSPECTION, uploadToken: 'tok2' }})
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(false)
  })

  it('offers the top-level features as checkboxes, pre-ticked from the suggestion', () => {
    mountResult(INSPECTION)
    const rows = wrapper.findAll('[data-test="feature-row"]')
    expect(rows.map(r => r.find('[data-test="feature-name"]').text())).toEqual(['alec', 'alec-ui'])
    expect(rows[1].find('[data-test="feature-description"]').text()).toBe('The web console')
    expect(rows[0].find('[data-test="feature-description"]').exists()).toBe(false)
    const boxes = wrapper.findAll('[data-test="feature-checkbox"] input[type="checkbox"]')
    expect(boxes.map(b => (b.element as HTMLInputElement).checked)).toEqual([true, false])
    expect(wrapper.find('[data-test="feature-hint"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="feature-hint-suggested"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="load-plugin"]').attributes('disabled')).toBeUndefined()
  })

  it('keeps Load disabled until a feature is ticked when nothing is suggested, then sends the choice in declared order', async () => {
    mountResult({ ...INSPECTION, suggestedFeatures: [] })
    const load = () => wrapper.find('[data-test="load-plugin"]')
    expect(wrapper.find('[data-test="feature-hint"]').text()).toContain('tick the feature that matches this server')
    expect(load().attributes('disabled')).toBeDefined()
    expect(load().attributes('title')).toBe('Choose at least one feature to start')
    const boxes = wrapper.findAll('[data-test="feature-checkbox"] input[type="checkbox"]')
    await boxes[1].setValue(true)
    expect(load().attributes('disabled')).toBeUndefined()
    await boxes[0].setValue(true)
    store.install.mockResolvedValueOnce({ success: true, message: '', payload: { plugin: { ...PLUGIN, features: ['alec', 'alec-ui'] }, restartRequired: false, restartInstructions: INSTRUCTIONS }})
    await load().trigger('click')
    await flushPromises()
    expect(store.install).toHaveBeenCalledWith({ uploadToken: 'tok', karName: 'alec', acknowledgeWarnings: false, features: ['alec', 'alec-ui'] })
    expect(wrapper.find('[data-test="plugin-loaded-dialog"] [data-test="written-list"]').text()).toContain('alec, alec-ui')
  })

  it('unticking every suggested feature disables Load and a new inspection resets the ticks', async () => {
    mountResult(INSPECTION)
    const load = () => wrapper.find('[data-test="load-plugin"]')
    await wrapper.findAll('[data-test="feature-checkbox"] input[type="checkbox"]')[0].setValue(false)
    expect(load().attributes('disabled')).toBeDefined()
    await wrapper.setProps({ inspection: { ...INSPECTION, uploadToken: 'tok2', suggestedFeatures: ['alec-ui'] }})
    const boxes = wrapper.findAll('[data-test="feature-checkbox"] input[type="checkbox"]')
    expect(boxes.map(b => (b.element as HTMLInputElement).checked)).toEqual([false, true])
    expect(load().attributes('disabled')).toBeUndefined()
  })

  it('hides the feature choice when a check failed', () => {
    mountResult({ ...INSPECTION, checks: [{ id: 'features-declared', level: 'FAIL', message: 'stub' }] })
    expect(wrapper.find('[data-test="feature-selection"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="load-plugin"]').attributes('title')).toBe('A check failed')
  })

  it('disables Load while the container is unavailable', () => {
    mountResult(INSPECTION, true)
    const load = wrapper.find('[data-test="load-plugin"]')
    expect(load.attributes('disabled')).toBeDefined()
    expect(load.attributes('title')).toContain('not available')
  })
})
