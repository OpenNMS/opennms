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

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import QuickSearchPanel from '@/components/Dashboard/panels/QuickSearchPanel.vue'
import { TimeframePreset } from '@/types/dashboard'

const push = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push })
}))

vi.mock('@/services/dashboardService', () => ({
  getServiceTypes: vi.fn().mockResolvedValue([
    { id: 3, name: 'ICMP' },
    { id: 5, name: 'HTTP' }
  ])
}))

const mountPanel = async () => {
  const wrapper = mount(QuickSearchPanel, {
    props: {
      panelId: 'qs-1',
      options: {},
      filter: { surveillanceCategories: [], ipMatch: null },
      timeframe: { preset: TimeframePreset.Last24h, from: null, to: null },
      refreshTick: 0
    }
  })
  await flushPromises()
  return wrapper
}

// element/nodeList.htm no longer exists; every search must land on the Vue
// nodes page with the query keys it tracks
describe('QuickSearchPanel', () => {
  beforeEach(() => push.mockClear())

  it('routes a node label search with interfaces listed', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('[data-test="quick-search-nodename"] input').setValue('  core-router ')
    await wrapper.find('[data-test="quick-search-nodename"]').trigger('submit')
    expect(push).toHaveBeenCalledWith({ path: '/nodes', query: { nodename: 'core-router', listInterfaces: 'true' }})
  })

  it('routes an IP search as iplike', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('[data-test="quick-search-iplike"] input').setValue('10.0.*.*')
    await wrapper.find('[data-test="quick-search-iplike"]').trigger('submit')
    expect(push).toHaveBeenCalledWith({ path: '/nodes', query: { iplike: '10.0.*.*', listInterfaces: 'false' }})
  })

  it('routes a node id through the nodes page nodeId redirect', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('[data-test="quick-search-nodeId"] input').setValue('42')
    await wrapper.find('[data-test="quick-search-nodeId"]').trigger('submit')
    expect(push).toHaveBeenCalledWith({ path: '/nodes', query: { nodeId: '42', listInterfaces: 'false' }})
  })

  it('searches by service name, not id', async () => {
    const wrapper = await mountPanel()
    const select = wrapper.find('[data-test="quick-search-service"] select')
    expect(select.findAll('option').map(o => o.attributes('value'))).toEqual(['ICMP', 'HTTP'])
    await select.setValue('HTTP')
    await wrapper.find('[data-test="quick-search-service"]').trigger('submit')
    expect(push).toHaveBeenCalledWith({ path: '/nodes', query: { monitoredService: 'HTTP', listInterfaces: 'false' }})
  })

  it('ignores an empty submit', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('[data-test="quick-search-nodename"]').trigger('submit')
    expect(push).not.toHaveBeenCalled()
  })
})
