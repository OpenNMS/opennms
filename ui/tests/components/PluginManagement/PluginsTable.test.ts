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

const BASE = { fileName: 'x.kar', sha256: 'abc', size: 1536, uploadedBy: 'admin', uploadedAt: Date.UTC(2026, 8, 25, 12, 0, 0), features: ['f1', 'f2'], bootFile: 'x.boot', autoStart: true, pendingRestart: false }

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
      { ...BASE, karName: 'zeta', status: 'unmanaged' },
      { ...BASE, karName: 'alpha', status: 'installed' },
      { ...BASE, karName: 'mid', status: 'staged', pendingRestart: true },
      { ...BASE, karName: 'omega', status: 'failed' },
      { ...BASE, karName: 'beta', status: 'unloaded', pendingRestart: true },
      { ...BASE, karName: 'auto', status: 'staged', pendingRestart: false }
    ])
    const names = wrapper.findAll('tbody tr').map(r => r.find('td').text())
    expect(names).toEqual(['alpha', 'auto', 'beta', 'mid', 'omega', 'zeta'])
    const tags = wrapper.findAll('[data-test="plugin-status"]')
    expect(tags.map(t => t.attributes('data-status'))).toEqual(['installed', 'staged', 'unloaded', 'staged', 'failed', 'unmanaged'])
    expect(tags.map(t => t.text())).toEqual(['Loaded', 'Load pending restart', 'Unload pending restart', 'Load pending restart', 'Failed to start', 'Not managed here'])
    expect(tags[0].classes()).toContain('p-tag-success')
    expect(tags[1].classes()).toContain('p-tag-warn')
    expect(tags[2].classes()).toContain('p-tag-warn')
    expect(tags[3].classes()).toContain('p-tag-warn')
    expect(tags[4].classes()).toContain('p-tag-danger')
    expect(tags[4].attributes('title')).toContain('see karaf.log')
    expect(tags[5].classes()).toContain('p-tag-info')
    expect(tags[5].attributes('title')).toContain('not loaded through this page')
    expect(wrapper.find('[data-test="plugin-status-hint"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="plugin-features"]').attributes('title')).toBe('f1, f2')
    expect(wrapper.text()).toContain('1.5 KB')
    expect(wrapper.find('[data-test="plugin-uploaded"]').text()).toContain('admin')
    expect(wrapper.find('[data-test="plugin-uploaded"]').text()).toContain('2026')
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
      { ...BASE, karName: 'gamma', status: 'unmanaged' }
    ])
    const buttons = wrapper.findAll('[data-test="unload-plugin"]')
    expect(buttons).toHaveLength(2)
    await buttons[1].trigger('click')
    expect(wrapper.emitted('unload')?.[0][0]).toMatchObject({ karName: 'gamma' })
  })

  it('disables Unload while the container is unavailable', () => {
    const wrapper = mountTable([{ ...BASE, karName: 'alpha', status: 'installed' }], false)
    const button = wrapper.find('[data-test="unload-plugin"]')
    expect(button.attributes('disabled')).toBeDefined()
    expect(button.attributes('title')).toContain('not available')
  })
})
