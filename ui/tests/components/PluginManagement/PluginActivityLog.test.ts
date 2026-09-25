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
import { describe, expect, it, vi } from 'vitest'

const OnmsCardStub = { name: 'OnmsCard', template: '<div><slot name="title" /><slot name="content" /></div>' }

const mountLog = (log: string | null | undefined) => mount(PluginActivityLog, {
  global: {
    plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true, initialState: { pluginManagementStore: { log }}})],
    stubs: { OnmsCard: OnmsCardStub }
  }
})

describe('PluginActivityLog.vue', () => {
  it('shows the log text and refreshes it on demand', async () => {
    const wrapper = mountLog('2026-09-25 admin loaded alec.kar')
    expect(wrapper.find('[data-test="activity-log"]').text()).toContain('loaded alec.kar')
    await wrapper.find('[data-test="refresh-log"]').trigger('click')
    await flushPromises()
    expect(usePluginManagementStore().refreshLog).toHaveBeenCalledTimes(1)
  })

  it('says when the log could not be read, is empty, or is still loading', () => {
    expect(mountLog(null).find('[data-test="log-error"]').text()).toBe('The log could not be read.')
    expect(mountLog('').find('[data-test="log-empty"]').exists()).toBe(true)
    expect(mountLog(undefined).find('[data-test="log-loading"]').exists()).toBe(true)
  })
})
