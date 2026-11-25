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
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import UsageStatisticsHeader from '@/components/UsageStatistics/UsageStatisticsHeader.vue'
import { useUsageStatisticsStore } from '@/stores/usageStatisticsStore'

const { api, showSnackBarMock } = vi.hoisted(() => ({
  api: {
    getUsageStatistics: vi.fn(),
    getUsageStatisticsMetadata: vi.fn(),
    getUsageStatisticsStatus: vi.fn(),
    setUsageStatisticsStatus: vi.fn()
  },
  showSnackBarMock: vi.fn()
}))

vi.mock('@/services', () => ({ default: api }))
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

const mountHeader = (enabled: boolean) => {
  const wrapper = mount(UsageStatisticsHeader, {
    global: { plugins: [createTestingPinia({ stubActions: false })] }
  })
  useUsageStatisticsStore().status = { enabled }
  return wrapper
}

describe('UsageStatisticsHeader.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('confirms when sharing is enabled', async () => {
    api.setUsageStatisticsStatus.mockResolvedValue({ status: 200 })
    api.getUsageStatisticsStatus.mockResolvedValue({ enabled: true })
    const wrapper = mountHeader(false)
    await flushPromises()

    await wrapper.get('button.button').trigger('click')
    await flushPromises()

    expect(api.setUsageStatisticsStatus).toHaveBeenCalledWith(true)
    expect(showSnackBarMock).toHaveBeenCalledTimes(1)
    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Usage Statistics Sharing is now enabled. Thank you for helping us improve OpenNMS.'
    })
  })

  it('confirms when sharing is disabled', async () => {
    api.setUsageStatisticsStatus.mockResolvedValue({ status: 202 })
    api.getUsageStatisticsStatus.mockResolvedValue({ enabled: false })
    const wrapper = mountHeader(true)
    await flushPromises()

    await wrapper.get('button.button').trigger('click')
    await flushPromises()

    expect(api.setUsageStatisticsStatus).toHaveBeenCalledWith(false)
    expect(showSnackBarMock).toHaveBeenCalledTimes(1)
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Usage Statistics Sharing is now disabled.' })
  })

  it('shows an error when enabling fails', async () => {
    api.setUsageStatisticsStatus.mockResolvedValue(undefined)
    const wrapper = mountHeader(false)
    await flushPromises()

    await wrapper.get('button.button').trigger('click')
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledTimes(1)
    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Error attempting to enable Usage Statistics Sharing.',
      error: true
    })
  })

  it('shows an error when disabling fails', async () => {
    api.setUsageStatisticsStatus.mockResolvedValue({ status: 500 })
    const wrapper = mountHeader(true)
    await flushPromises()

    await wrapper.get('button.button').trigger('click')
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Error attempting to disable Usage Statistics Sharing.',
      error: true
    })
  })
})
