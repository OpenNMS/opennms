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
vi.mock('@/services', () => ({
  default: {
    getSystemReportPlugins: (...a: unknown[]) => getSystemReportPlugins(...a),
    getSystemReportFormatters: (...a: unknown[]) => getSystemReportFormatters(...a)
  }
}))

const mountPage = () =>
  mount(SystemReport, {
    global: {
      plugins: [createTestingPinia({ stubActions: false }), PrimeVue],
      stubs: ['router-link', 'BreadCrumbs']
    }
  })

const fieldsOf = (form: HTMLFormElement): Array<[string, string]> =>
  Array.from(form.querySelectorAll('input')).map((i) => [i.name, i.value])

describe('SystemReport', () => {
  let submitted: HTMLFormElement | null

  beforeEach(() => {
    submitted = null
    getSystemReportPlugins.mockResolvedValue([...plugins])
    getSystemReportFormatters.mockResolvedValue([...formatters])
    vi.spyOn(HTMLFormElement.prototype, 'submit').mockImplementation(function (this: HTMLFormElement) {
      submitted = this
    })
  })

  it('generates with every plugin enabled and the zip formatter by default', async () => {
    const wrapper = mountPage()
    await flushPromises()

    await wrapper.get('[data-test=generate-btn]').trigger('click')

    expect(submitted).not.toBeNull()
    expect(submitted!.method).toBe('post')
    expect(submitted!.getAttribute('action')).toContain('admin/support/systemReport.htm')
    const fields = fieldsOf(submitted!)
    expect(fields).toContainEqual(['operation', 'run'])
    expect(fields).toContainEqual(['formatter', 'zip'])
    expect(fields).toContainEqual(['plugins', 'Java'])
    expect(fields).toContainEqual(['plugins', 'OS'])
    // no filename entered -> no output field
    expect(fields.some(([n]) => n === 'output')).toBe(false)
  })

  it('includes the output field when a filename is entered', async () => {
    const wrapper = mountPage()
    await flushPromises()

    await wrapper.get('[data-test=filename]').setValue('my-report')
    await wrapper.get('[data-test=generate-btn]').trigger('click')

    expect(fieldsOf(submitted!)).toContainEqual(['output', 'my-report'])
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

  it('surfaces a load error and does not generate when the API fails', async () => {
    getSystemReportPlugins.mockResolvedValue(null)
    const wrapper = mountPage()
    await flushPromises()

    expect(wrapper.get('[data-test=load-error]').text()).toContain('Failed to load')
    await wrapper.get('[data-test=generate-btn]').trigger('click')
    expect(submitted).toBeNull()
  })
})
