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
import PluginActivityLog from '@/components/PluginManagement/PluginActivityLog.vue'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it, vi } from 'vitest'

const OnmsCardStub = { name: 'OnmsCard', template: '<div><slot name="title" /><slot name="content" /></div>' }

const LINES = [
  '2026-09-25 11:18:06,596 INFO  action=install user=admin remote=10.0.0.5 kar=alec sha256=661bb88f outcome=ok features=alec autoStart=true warnsAcknowledged=0',
  '2026-09-25 11:18:05,000 WARN  action=install user=admin remote=10.0.0.5 kar=alec sha256=tok outcome=refused failed checks: compatibility',
  '2026-09-25 11:18:04,000 INFO  action=check user=bob remote=10.0.0.6 kar=other sha256=abc outcome=ok file=other.kar',
  '2026-09-25 11:18:03,000 WARN  action=unload user=bob remote=10.0.0.6 kar=null sha256=null outcome=rejected invalid kar name',
  'this line is not in the audit format'
]

const mountLog = (log: string | null | undefined, logLines = 1000) => mount(PluginActivityLog, {
  global: {
    plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true, initialState: { pluginManagementStore: { log, logLines }}})],
    stubs: { OnmsCard: OnmsCardStub }
  }
})

const rows = (wrapper: ReturnType<typeof mountLog>) => wrapper.findAll('tbody tr')

describe('PluginActivityLog.vue', () => {
  afterEach(() => vi.restoreAllMocks())

  it('renders the parsed lines as a table with tags and keeps unparseable lines raw', () => {
    const wrapper = mountLog(LINES.join('\n'))
    const table = wrapper.find('[data-test="activity-log"]')
    expect(table.exists()).toBe(true)
    expect(rows(wrapper)).toHaveLength(5)
    expect(wrapper.findAll('[data-test="log-time"]').map(t => t.text())[0]).toBe('2026-09-25 11:18:06,596')
    const levels = wrapper.findAll('[data-test="log-level"]')
    expect(levels.map(l => l.text())).toEqual(['INFO', 'WARN', 'INFO', 'WARN'])
    expect(levels[0].classes()).toContain('p-tag-secondary')
    expect(levels[1].classes()).toContain('p-tag-warn')
    const outcomes = wrapper.findAll('[data-test="log-outcome"]')
    expect(outcomes.map(o => o.text())).toEqual(['ok', 'refused', 'ok', 'rejected'])
    expect(outcomes[0].classes()).toContain('p-tag-success')
    expect(outcomes[1].classes()).toContain('p-tag-warn')
    expect(wrapper.findAll('[data-test="log-action"]').map(a => a.text())).toEqual(['install', 'install', 'check', 'unload'])
    expect(wrapper.findAll('[data-test="log-user"]').map(u => u.text())).toEqual(['admin', 'admin', 'bob', 'bob'])
    expect(wrapper.findAll('[data-test="log-kar"]').map(k => k.text())).toEqual(['alec', 'alec', 'other', '—'])
    expect(wrapper.findAll('[data-test="log-detail"]')[1].text()).toBe('failed checks: compatibility')
    const raw = wrapper.find('[data-test="log-raw"]')
    expect(raw.text()).toBe('this line is not in the audit format')
    expect(raw.element.closest('td')?.getAttribute('colspan')).toBe('7')
    const failed = rows(wrapper).filter(r => r.classes().includes('log-row--failed'))
    expect(failed).toHaveLength(2)
    expect(rows(wrapper)[4].classes()).toContain('log-row--raw')
  })

  it('filters by search, plugin, action and outcome', async () => {
    const wrapper = mountLog(LINES.join('\n'))
    await wrapper.find('[data-test="log-search"]').setValue('compatibility')
    expect(rows(wrapper)).toHaveLength(1)
    expect(wrapper.find('[data-test="log-outcome"]').text()).toBe('refused')
    await wrapper.find('[data-test="log-search"]').setValue('')
    expect(rows(wrapper)).toHaveLength(5)

    const vm = wrapper.vm as any
    vm.karFilter = 'other'
    await flushPromises()
    expect(rows(wrapper)).toHaveLength(1)
    expect(wrapper.find('[data-test="log-user"]').text()).toBe('bob')
    vm.karFilter = null
    vm.actionFilter = 'install'
    await flushPromises()
    expect(rows(wrapper)).toHaveLength(2)
    vm.outcomeFilter = 'ok'
    await flushPromises()
    expect(rows(wrapper)).toHaveLength(1)
    vm.outcomeFilter = 'error'
    await flushPromises()
    expect(rows(wrapper)).toHaveLength(1)
    expect(wrapper.find('[data-test="log-no-match"]').exists()).toBe(true)
    expect(vm.karOptions).toEqual(['alec', 'other'])
  })

  it('refreshes and changes the number of lines through the store', async () => {
    const wrapper = mountLog(LINES.join('\n'))
    const store = usePluginManagementStore()
    await wrapper.find('[data-test="refresh-log"]').trigger('click')
    await flushPromises()
    expect(store.refreshLog).toHaveBeenCalledTimes(1)
    const select = wrapper.findAllComponents({ name: 'OnmsSelect' }).find(s => s.props('inputId') === 'plugin-log-lines')
    expect(select?.props('options')).toEqual([200, 1000, 5000, 10000])
    expect(select?.props('modelValue')).toBe(1000)
    await (wrapper.vm as any).changeLines(5000)
    expect(store.setLogLines).toHaveBeenCalledWith(5000)
    await (wrapper.vm as any).changeLines(1000)
    expect(store.setLogLines).toHaveBeenCalledTimes(1)
  })

  it('downloads the whole file as plugin-management.log and reports a failed download', async () => {
    const wrapper = mountLog('')
    const store = usePluginManagementStore()
    const createObjectURL = vi.fn((_blob: Blob) => 'blob:log')
    const revokeObjectURL = vi.fn()
    vi.stubGlobal('URL', Object.assign(Object.create(URL), { createObjectURL, revokeObjectURL }))
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
    vi.mocked(store.downloadLog).mockResolvedValueOnce(LINES.join('\n'))
    await wrapper.find('[data-test="download-log"]').trigger('click')
    await flushPromises()
    expect(store.downloadLog).toHaveBeenCalledTimes(1)
    expect(createObjectURL).toHaveBeenCalledTimes(1)
    const blob = createObjectURL.mock.calls[0][0]
    expect(blob).toBeInstanceOf(Blob)
    expect(await blob.text()).toBe(LINES.join('\n'))
    expect(click).toHaveBeenCalledTimes(1)
    expect((click.mock.instances[0] as HTMLAnchorElement).download).toBe('plugin-management.log')
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:log')
    expect(wrapper.find('[data-test="download-error"]').exists()).toBe(false)

    vi.mocked(store.downloadLog).mockResolvedValueOnce(null)
    await wrapper.find('[data-test="download-log"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="download-error"]').exists()).toBe(true)
    expect(click).toHaveBeenCalledTimes(1)
    vi.unstubAllGlobals()
  })

  it('says when the log could not be read, is empty, or is still loading', () => {
    expect(mountLog(null).find('[data-test="log-error"]').text()).toBe('The log could not be read.')
    expect(mountLog('').find('[data-test="log-empty"]').exists()).toBe(true)
    expect(mountLog(undefined).find('[data-test="log-loading"]').exists()).toBe(true)
  })
})
