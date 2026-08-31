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

vi.mock('@/services/graphCollectionsService', () => ({
  getGraphCollections: vi.fn().mockResolvedValue([
    { id: 7, label: 'Core Routers' },
    { id: 9, label: 'Branch Uplinks' }
  ])
}))

const assign = vi.fn()

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
    assign.mockClear()
    // window.location.assign is not writable in jsdom; replace location wholesale
    vi.stubGlobal('location', { ...window.location, assign })
  })

  it('resolves the typed name to a report id and opens the custom view', async () => {
    // KSC/index.jsp ignores a report parameter entirely — navigating there with
    // raw text (the old behavior) silently did nothing
    const wrapper = await mountPanel()
    await wrapper.find('input').setValue('core routers')
    await wrapper.find('form').trigger('submit')

    expect(assign).toHaveBeenCalledWith('/opennms/KSC/customView.htm?type=custom&report=7')
  })

  it('falls back to a substring match', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('input').setValue('branch')
    await wrapper.find('form').trigger('submit')

    expect(assign).toHaveBeenCalledWith('/opennms/KSC/customView.htm?type=custom&report=9')
  })

  it('shows a hint instead of navigating when nothing matches', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('input').setValue('no-such-collection')
    await wrapper.find('form').trigger('submit')

    expect(assign).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('No graph collection with that name.')
  })

  it('opens the collection list when the input is empty', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('form').trigger('submit')

    expect(assign).toHaveBeenCalledWith('/opennms/KSC/index.jsp')
  })
})
