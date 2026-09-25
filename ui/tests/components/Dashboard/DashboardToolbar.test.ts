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

import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { ref } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const exportDashboardToPdf = vi.fn()
vi.mock('@/components/Dashboard/utils/dashboardExport', () => ({ exportDashboardToPdf: (...args: unknown[]) => exportDashboardToPdf(...args) }))
const showSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))
vi.mock('@/composables/useRole', () => ({ default: () => ({ adminRole: ref(true) }) }))
vi.mock('@/components/Dashboard/DashboardFilterControl.vue', () => ({ default: { name: 'DashboardFilterControl', template: '<span />' }}))

import DashboardToolbar from '@/components/Dashboard/DashboardToolbar.vue'
import { useDashboardStore } from '@/stores/dashboardStore'

const mountToolbar = (editMode = false, name?: string) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const store = useDashboardStore(pinia)
  store.editMode = editMode
  if (name !== undefined) {
    store.layout.name = name
  }
  return mount(DashboardToolbar, { global: { plugins: [PrimeVue, pinia] }})
}

describe('DashboardToolbar.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    document.body.innerHTML = '<div id="dashboard-root"><div class="panel-frame"></div></div>'
  })

  it('shows the dashboard name and falls back to Home for older layouts', () => {
    expect(mountToolbar(false, 'NOC Wall').find('.dashboard-toolbar__title').text()).toBe('NOC Wall')
    expect(mountToolbar(false, '   ').find('.dashboard-toolbar__title').text()).toBe('Home')
  })

  it('exports the dashboard root under its name and reports how many panels went out', async () => {
    exportDashboardToPdf.mockResolvedValue(3)
    const wrapper = mountToolbar(false, 'NOC Wall')
    await wrapper.find('[data-test="export-pdf"]').trigger('click')
    await flushPromises()
    expect(exportDashboardToPdf).toHaveBeenCalledWith(document.getElementById('dashboard-root'), 'NOC Wall', expect.objectContaining({ onProgress: expect.any(Function) }))
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Exported 3 panels to PDF.' })
  })

  it('tells the user when there is nothing to export, and when the export fails', async () => {
    exportDashboardToPdf.mockResolvedValue(0)
    let wrapper = mountToolbar()
    await wrapper.find('[data-test="export-pdf"]').trigger('click')
    await flushPromises()
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'No panels are ready to export yet.' })

    exportDashboardToPdf.mockRejectedValue(new Error('boom'))
    wrapper = mountToolbar()
    await wrapper.find('[data-test="export-pdf"]').trigger('click')
    await flushPromises()
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'The PDF could not be created.', error: true })
  })

  it('shows a progress overlay while the capture runs and removes it when done', async () => {
    let finish: (n: number) => void = () => undefined
    exportDashboardToPdf.mockImplementation((_root: HTMLElement, _name: string, options: { onProgress: (d: number, t: number) => void }) =>
      new Promise<number>((resolve) => {
        options.onProgress(0, 4)
        options.onProgress(2, 4)
        finish = resolve
      }))
    const wrapper = mountToolbar()
    await wrapper.find('[data-test="export-pdf"]').trigger('click')
    await flushPromises()
    const overlay = document.body.querySelector('[data-test="export-overlay"]')
    expect(overlay).not.toBeNull()
    expect(overlay?.textContent).toContain('Generating PDF')
    expect(overlay?.textContent).toContain('Capturing panel 3 of 4')
    expect(wrapper.find('[data-test="export-pdf"]').text()).toContain('Exporting')
    finish(4)
    await flushPromises()
    expect(document.body.querySelector('[data-test="export-overlay"]')).toBeNull()
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Exported 4 panels to PDF.' })
  })

  it('hides the export while the layout is being edited', () => {
    expect(mountToolbar(true).find('[data-test="export-pdf"]').exists()).toBe(false)
  })
})
