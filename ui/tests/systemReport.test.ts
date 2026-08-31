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

import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import SystemReport from '@/containers/SystemReport.vue'

const plugins = [
  { name: 'Java', description: 'Java and JVM information' },
  { name: 'OS', description: 'Kernel, OS, and Distribution' }
]
const formatters = [
  { name: 'text', description: 'Human-readable text', extension: 'txt' },
  { name: 'zip', description: 'Compressed file of all resources', extension: 'zip' }
]

const getSystemReportPlugins = vi.fn()
const getSystemReportFormatters = vi.fn()
const generateSystemReport = vi.fn()
vi.mock('@/services', () => ({
  default: {
    getSystemReportPlugins: (...a: unknown[]) => getSystemReportPlugins(...a),
    getSystemReportFormatters: (...a: unknown[]) => getSystemReportFormatters(...a),
    generateSystemReport: (...a: unknown[]) => generateSystemReport(...a)
  }
}))

const downloadFile = vi.fn()
vi.mock('@/composables/useDownload', () => ({ default: () => ({ downloadFile }) }))

const showSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))

// the page is admin-only; tests exercise it as an admin
vi.mock('@/composables/useRole', () => ({ default: () => ({ adminRole: { value: true }}) }))

const okResponse = {
  data: new Blob(['report']),
  headers: { 'content-disposition': 'attachment; filename="opennms-system-report.txt"' }
}

const mountPage = () =>
  mount(SystemReport, {
    global: {
      plugins: [createTestingPinia({ stubActions: false }), PrimeVue],
      stubs: ['router-link', 'BreadCrumbs']
    }
  })

describe('SystemReport', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getSystemReportPlugins.mockResolvedValue([...plugins])
    getSystemReportFormatters.mockResolvedValue([...formatters])
    generateSystemReport.mockResolvedValue({ ...okResponse })
  })

  it('generates with every plugin enabled and the text formatter by default, then downloads', async () => {
    const wrapper = mountPage()
    await flushPromises()

    await wrapper.get('[data-test=generate-btn]').trigger('click')
    await flushPromises()

    // default matches the legacy form's pre-selected 'text', every plugin enabled
    expect(generateSystemReport).toHaveBeenCalledWith({
      formatter: 'text',
      plugins: ['Java', 'OS'],
      output: undefined
    })
    // the response is handed to useDownload as-is (force-blob)
    expect(downloadFile).toHaveBeenCalledWith(expect.objectContaining({ data: expect.any(Blob) }), true)
    // the user gets told generation is under way
    expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ msg: expect.stringMatching(/generating/i) }))
  })

  it('sanitizes the filename to word characters, mirroring the server', async () => {
    const wrapper = mountPage()
    await flushPromises()

    await wrapper.get('[data-test=filename]').setValue('my report.txt')
    await wrapper.get('[data-test=generate-btn]').trigger('click')
    await flushPromises()

    expect(generateSystemReport).toHaveBeenCalledWith(expect.objectContaining({ output: 'myreport.txt' }))
  })

  it('omits output when the filename sanitizes to empty (no empty download name)', async () => {
    const wrapper = mountPage()
    await flushPromises()

    await wrapper.get('[data-test=filename]').setValue('///')
    await wrapper.get('[data-test=generate-btn]').trigger('click')
    await flushPromises()

    expect(generateSystemReport).toHaveBeenCalledWith(expect.objectContaining({ output: undefined }))
  })

  it('reports an error and does not download when generation fails', async () => {
    generateSystemReport.mockResolvedValue(false)
    const wrapper = mountPage()
    await flushPromises()

    await wrapper.get('[data-test=generate-btn]').trigger('click')
    await flushPromises()

    expect(downloadFile).not.toHaveBeenCalled()
    expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true, msg: expect.stringMatching(/could not be generated/i) }))
  })

  it('the All toggle clears every plugin and reselects them', async () => {
    const wrapper = mountPage()
    await flushPromises()
    const vm = wrapper.vm as unknown as { selectedPlugins: string[]; toggleAll: () => void; allSelected: boolean }

    expect(vm.selectedPlugins).toEqual(['Java', 'OS'])
    expect(vm.allSelected).toBe(true)

    vm.toggleAll()
    expect(vm.selectedPlugins).toEqual([])

    vm.toggleAll()
    expect(vm.selectedPlugins).toEqual(['Java', 'OS'])
  })

  it('per-plugin toggle adds and removes a single plugin', async () => {
    const wrapper = mountPage()
    await flushPromises()
    const vm = wrapper.vm as unknown as { selectedPlugins: string[]; togglePlugin: (n: string, c: boolean) => void }

    vm.togglePlugin('Java', false)
    expect(vm.selectedPlugins).toEqual(['OS'])

    vm.togglePlugin('Java', true)
    expect(vm.selectedPlugins).toEqual(['OS', 'Java'])

    // toggling on an already-selected plugin does not duplicate it
    vm.togglePlugin('Java', true)
    expect(vm.selectedPlugins).toEqual(['OS', 'Java'])
  })

  it('surfaces a load error and does not generate when the API fails', async () => {
    getSystemReportPlugins.mockResolvedValue(null)
    const wrapper = mountPage()
    await flushPromises()

    expect(wrapper.get('[data-test=load-error]').text()).toContain('Failed to load')
    await wrapper.get('[data-test=generate-btn]').trigger('click')
    await flushPromises()
    expect(generateSystemReport).not.toHaveBeenCalled()
  })
})
