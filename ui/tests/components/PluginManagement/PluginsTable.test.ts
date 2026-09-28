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

import PluginsTable from '@/components/PluginManagement/PluginsTable.vue'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

const OnmsCardStub = { name: 'OnmsCard', template: '<div><slot name="title" /><slot name="content" /></div>' }

const BASE = { fileName: 'x.kar', sha256: 'abc', size: 1536, uploadedBy: 'admin', uploadedAt: Date.UTC(2026, 8, 25, 12, 0, 0), features: ['f1', 'f2'], bootFile: 'x.boot', autoStart: true, pendingRestart: false, source: 'upload', managed: true }

const mountTable = (plugins: any[], containerAvailable = true) => mount(PluginsTable, {
  props: { plugins, containerAvailable },
  global: { plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })], stubs: { OnmsCard: OnmsCardStub }}
})

describe('PluginsTable.vue', () => {
  it('shows the empty text when nothing is loaded', () => {
    expect(mountTable([]).find('[data-test="no-plugins"]').exists()).toBe(true)
  })

  it('sorts by KAR name and maps every status to a tag', () => {
    const wrapper = mountTable([
      { ...BASE, karName: 'zeta', status: 'unknown' },
      { ...BASE, karName: 'alpha', status: 'installed' },
      { ...BASE, karName: 'mid', status: 'staged', pendingRestart: true },
      { ...BASE, karName: 'omega', status: 'failed' },
      { ...BASE, karName: 'beta', status: 'unloaded', pendingRestart: true },
      { ...BASE, karName: 'auto', status: 'staged', pendingRestart: false }
    ])
    const names = wrapper.findAll('tbody tr').map(r => r.find('td').text())
    expect(names).toEqual(['alpha', 'auto', 'beta', 'mid', 'omega', 'zeta'])
    const tags = wrapper.findAll('[data-test="plugin-status"]')
    expect(tags.map(t => t.attributes('data-status'))).toEqual(['installed', 'staged', 'unloaded', 'staged', 'failed', 'unknown'])
    expect(tags.map(t => t.text())).toEqual(['Loaded', 'Load pending restart', 'Unload pending restart', 'Load pending restart', 'Failed to start', 'Unknown'])
    expect(tags[0].classes()).toContain('p-tag-success')
    expect(tags[1].classes()).toContain('p-tag-warn')
    expect(tags[2].classes()).toContain('p-tag-warn')
    expect(tags[3].classes()).toContain('p-tag-warn')
    expect(tags[4].classes()).toContain('p-tag-danger')
    expect(tags[4].attributes('title')).toContain('see karaf.log')
    expect(tags[5].classes()).toContain('p-tag-secondary')
    expect(wrapper.find('[data-test="plugin-status-hint"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="plugin-features"]').attributes('title')).toBe('f1, f2')
    expect(wrapper.text()).toContain('1.5 KB')
    expect(wrapper.find('[data-test="plugin-uploaded"]').text()).toContain('admin')
    expect(wrapper.find('[data-test="plugin-uploaded"]').text()).toContain('2026')
  })

  it('shows where each plugin came from, with the raw value as the title', () => {
    const wrapper = mountTable([
      { ...BASE, karName: 'alpha', status: 'installed', source: 'github:OpenNMS-Plugins/alec@v3.0.4' },
      { ...BASE, karName: 'beta', status: 'installed', source: 'upload' },
      { ...BASE, karName: 'gamma', status: 'installed', source: null }
    ])
    const cells = wrapper.findAll('[data-test="plugin-source"]')
    expect(cells.map(c => c.text())).toEqual(['OpenNMS-Plugins/alec@v3.0.4', 'Uploaded file', 'Uploaded file'])
    expect(cells[0].attributes('title')).toBe('github:OpenNMS-Plugins/alec@v3.0.4')
    expect(wrapper.findAll('thead th').map(h => h.text())).toContain('Source')
  })

  it('shows a plugin loaded by hand with its features, their state and the real status', () => {
    const wrapper = mountTable([
      { ...BASE, karName: 'alec', status: 'installed', source: 'manual', managed: false, uploadedBy: null, uploadedAt: null, fileName: null, size: 0,
        features: ['alec-opennms-standalone'], featureStates: { 'alec-opennms-standalone': 'Started' }, bootFile: 'etc/featuresBoot.d/alec.boot' }
    ])
    const source = wrapper.find('[data-test="plugin-source"]')
    expect(source.text()).toBe('Loaded by hand')
    expect(source.classes()).toContain('p-tag-info')
    expect(source.attributes('title')).toBe('installed outside this page; features from its boot file or the KAR itself')
    expect(wrapper.find('[data-test="plugin-features"]').text()).toBe('alec-opennms-standalone')
    expect(wrapper.find('[data-test="plugin-features"]').attributes('title')).toBe('alec-opennms-standalone (Started)')
    expect(wrapper.find('[data-test="plugin-status"]').text()).toBe('Loaded')
    expect(wrapper.find('[data-test="plugin-uploaded"]').text()).toContain('—')
    expect(wrapper.findAll('[data-test="unload-plugin"]')).toHaveLength(1)
  })

  it('shows an unloaded plugin that no longer waits for a restart as Unloaded', () => {
    const tag = mountTable([{ ...BASE, karName: 'beta', status: 'unloaded', pendingRestart: false }]).find('[data-test="plugin-status"]')
    expect(tag.text()).toBe('Unloaded')
    expect(tag.classes()).toContain('p-tag-secondary')
  })

  it('offers Unload except for unloaded plugins, and emits the entry', async () => {
    const wrapper = mountTable([
      { ...BASE, karName: 'alpha', status: 'installed' },
      { ...BASE, karName: 'beta', status: 'unloaded' },
      { ...BASE, karName: 'gamma', status: 'installed', source: 'manual', managed: false }
    ])
    const buttons = wrapper.findAll('[data-test="unload-plugin"]')
    expect(buttons).toHaveLength(2)
    await buttons[1].trigger('click')
    expect(wrapper.emitted('unload')?.[0][0]).toMatchObject({ karName: 'gamma' })
  })

  it('disables Unload and Restart while the container is unavailable', () => {
    const wrapper = mountTable([{ ...BASE, karName: 'alpha', status: 'installed' }], false)
    const button = wrapper.find('[data-test="unload-plugin"]')
    expect(button.attributes('disabled')).toBeDefined()
    expect(button.attributes('title')).toContain('not available')
    const restart = wrapper.find('[data-test="restart-plugin"]')
    expect(restart.attributes('disabled')).toBeDefined()
    expect(restart.attributes('title')).toContain('not available')
  })

  it('offers Restart for loaded and failed plugins only, and emits the entry', async () => {
    const wrapper = mountTable([
      { ...BASE, karName: 'alpha', status: 'installed' },
      { ...BASE, karName: 'beta', status: 'failed' },
      { ...BASE, karName: 'gamma', status: 'staged', pendingRestart: true },
      { ...BASE, karName: 'delta', status: 'unknown' },
      { ...BASE, karName: 'omega', status: 'unloaded', pendingRestart: true }
    ])
    const buttons = wrapper.findAll('[data-test="restart-plugin"]')
    expect(buttons).toHaveLength(4)
    expect(buttons.map(b => b.attributes('disabled') !== undefined)).toEqual([false, false, true, true])
    expect(buttons[0].attributes('title')).toBe('Restart alpha')
    expect(buttons[1].attributes('title')).toBe('Restart beta')
    expect(buttons[2].attributes('title')).toBe('Restart is available once the plugin is loaded')
    expect(buttons[0].classes()).not.toContain('p-button-danger')
    await buttons[1].trigger('click')
    expect(wrapper.emitted('restart')?.[0][0]).toMatchObject({ karName: 'beta' })
    expect(wrapper.emitted('unload')).toBeUndefined()
  })

  it('puts the first diagnostic on the Failed to start tag and every diagnostic on the features title', () => {
    const wrapper = mountTable([
      { ...BASE, karName: 'alec', status: 'failed', featureStates: { f1: 'Started', f2: 'Installed' },
        diagnostics: { f2: 'Unable to resolve org.foo\n  missing requirement osgi.wiring.package', f1: '' }}
    ])
    expect(wrapper.find('[data-test="plugin-status"]').attributes('title')).toBe('f2: Unable to resolve org.foo')
    const features = wrapper.find('[data-test="plugin-features"]')
    expect(features.attributes('title')).toBe('f1 (Started), f2 (Installed)\nf2: Unable to resolve org.foo\n  missing requirement osgi.wiring.package\nf1: ')
    expect(features.attributes('data-diagnostics')).toBe('2')
    const plain = mountTable([{ ...BASE, karName: 'x', status: 'failed', diagnostics: {}}])
    expect(plain.find('[data-test="plugin-status"]').attributes('title')).toContain('see karaf.log')
    expect(plain.find('[data-test="plugin-features"]').attributes('data-diagnostics')).toBeUndefined()
  })

  it('shows a loaded plugin whose health checks fail as Loaded, health failing with the first message as title', () => {
    const wrapper = mountTable([
      { ...BASE, karName: 'alec', status: 'installed', health: 'unhealthy', healthMessages: ['ALEC :: Driver: Failure (no datasource)', 'Bundle x: Starting'] },
      { ...BASE, karName: 'fine', status: 'installed', health: 'healthy', healthMessages: [] }
    ])
    const tags = wrapper.findAll('[data-test="plugin-status"]')
    expect(tags.map(t => t.text())).toEqual(['Loaded, health failing', 'Loaded'])
    expect(tags[0].classes()).toContain('p-tag-warn')
    expect(tags[0].attributes('title')).toBe('ALEC :: Driver: Failure (no datasource)')
    expect(tags[0].attributes('data-health')).toBe('unhealthy')
    expect(tags[1].classes()).toContain('p-tag-success')
    expect(wrapper.findAll('[data-test="plugin-features"]')[0].attributes('title')).toContain('Bundle x: Starting')
    expect(wrapper.findAll('[data-test="restart-plugin"]')[0].attributes('disabled')).toBeUndefined()
  })
})
