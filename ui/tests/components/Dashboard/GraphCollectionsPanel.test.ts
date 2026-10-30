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
import GraphCollectionsPanel from '@/components/Dashboard/panels/GraphCollectionsPanel.vue'
import { TimeframePreset } from '@/types/dashboard'

const getGraphCollections = vi.fn()
vi.mock('@/services/graphCollectionsService', () => ({
  getGraphCollections: () => getGraphCollections()
}))

const push = vi.fn()
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))

const collection = (id: number, title: string) => ({
  id, title, showTimespanButton: false, showGraphtypeButton: false, graphsPerLine: 0, graphs: []
})

const mountPanel = async () => {
  const wrapper = mount(GraphCollectionsPanel, {
    props: {
      panelId: 'ksc-1',
      options: {},
      filter: { surveillanceCategories: [], ipMatch: null },
      timeframe: { preset: TimeframePreset.Last24h, from: null, to: null },
      refreshTick: 0
    }
  })
  await flushPromises()
  return wrapper
}

describe('GraphCollectionsPanel', () => {
  beforeEach(() => {
    push.mockClear()
    getGraphCollections.mockClear()
    getGraphCollections.mockResolvedValue({
      success: true,
      message: '',
      payload: [collection(7, 'Core Routers'), collection(9, 'Branch Uplinks')]
    })
  })

  it('resolves the typed title to a collection and opens its view', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('input').setValue('core routers')
    await wrapper.find('form').trigger('submit')

    expect(push).toHaveBeenCalledWith('/graph-collections/7')
  })

  it('falls back to a substring match', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('input').setValue('branch')
    await wrapper.find('form').trigger('submit')

    expect(push).toHaveBeenCalledWith('/graph-collections/9')
  })

  it('shows a hint instead of navigating when nothing matches', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('input').setValue('no-such-collection')
    await wrapper.find('form').trigger('submit')

    expect(push).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('No graph collection with that name.')
  })

  it('opens the collection list when the input is empty', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('form').trigger('submit')

    expect(push).toHaveBeenCalledWith('/graph-collections')
  })

  it('reads the list once and leaves the refresh tick alone', async () => {
    const wrapper = await mountPanel()
    await wrapper.setProps({ refreshTick: 1 })
    await flushPromises()
    expect(getGraphCollections).toHaveBeenCalledTimes(1)
  })

  it('shows the load failure instead of an empty list', async () => {
    getGraphCollections.mockResolvedValue({ success: false, message: 'Failed to load the graph collections.' })
    const wrapper = await mountPanel()

    expect(wrapper.find('[data-test="load-error"]').text()).toBe('Failed to load the graph collections.')
  })
})
