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

import WsmanEventLogLogDialog from '@/components/ManageWsman/WsmanEventLogLogDialog.vue'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { WsmanEventLogConfig } from '@/types/wsmanAdmin'

const DialogStub = { name: 'Dialog', props: ['visible', 'header', 'modal'], template: '<div v-if="visible"><slot /><slot name="footer" /></div>' }

const config = (): WsmanEventLogConfig => ({
  version: 'v1',
  threads: 4,
  retries: 1,
  targetRefreshInterval: '5m',
  packages: [{
    name: 'windows',
    filter: 'IPADDR != \'0.0.0.0\'',
    logs: [{ name: 'System', enabled: true, interval: 60000, maxRecords: 500, lookback: '1h', levels: 'Error,Warning', includeEventIds: null, excludeEventIds: null, mode: 'wql' }],
    eventMappings: []
  }]
})

const mountDialog = async (original: any = null) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const store = useWsmanAdminStore(pinia)
  vi.mocked(store.saveEventLog).mockResolvedValue({ success: true, message: '' })
  const wrapper = mount(WsmanEventLogLogDialog, {
    props: { visible: false, config: config(), packageName: 'windows', original },
    global: { plugins: [PrimeVue, pinia], stubs: { Dialog: DialogStub }}
  })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return { wrapper, store }
}

describe('WsmanEventLogLogDialog.vue', () => {
  let ctx: { wrapper: VueWrapper<any>, store: ReturnType<typeof useWsmanAdminStore> }

  beforeEach(() => vi.clearAllMocks())

  it('requires a name that is not already listed', async () => {
    ctx = await mountDialog(null)
    expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    await ctx.wrapper.find('[data-test="name-input"]').setValue('system')
    expect(ctx.wrapper.text()).toContain('already listed')
    await ctx.wrapper.find('[data-test="name-input"]').setValue('Application')
    expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
  })

  it('validates the lookback and the Event ID lists', async () => {
    ctx = await mountDialog(null)
    await ctx.wrapper.find('[data-test="name-input"]').setValue('Application')
    await ctx.wrapper.find('[data-test="lookback-input"]').setValue('yesterday')
    expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    await ctx.wrapper.find('[data-test="lookback-input"]').setValue('2d')
    await ctx.wrapper.find('[data-test="include-input"]').setValue('41, x')
    expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    await ctx.wrapper.find('[data-test="include-input"]').setValue('41, 6008')
    expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
  })

  it('appends a new log to the package and closes', async () => {
    ctx = await mountDialog(null)
    await ctx.wrapper.find('[data-test="name-input"]').setValue('Application')
    await ctx.wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const saved = vi.mocked(ctx.store.saveEventLog).mock.calls[0][0]
    expect(saved.packages[0].logs.map(l => l.name)).toEqual(['System', 'Application'])
    expect(saved.packages[0].logs[1]).toMatchObject({ enabled: true, interval: 60000, maxRecords: 500, lookback: '1h', levels: 'Error,Warning', mode: 'wql' })
    expect(ctx.wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('edits an existing log in place and shows a server rejection', async () => {
    ctx = await mountDialog(config().packages[0].logs[0])
    vi.mocked(ctx.store.saveEventLog).mockResolvedValue({ success: false, message: 'The interval of log System must be at least 1000 ms.' })
    await ctx.wrapper.find('[data-test="lookback-input"]').setValue('30m')
    await ctx.wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const saved = vi.mocked(ctx.store.saveEventLog).mock.calls[0][0]
    expect(saved.packages[0].logs).toHaveLength(1)
    expect(saved.packages[0].logs[0].lookback).toBe('30m')
    expect(ctx.wrapper.find('[data-test="dialog-error"]').text()).toContain('at least 1000 ms')
    expect(ctx.wrapper.emitted('update:visible')).toBeFalsy()
  })
})
