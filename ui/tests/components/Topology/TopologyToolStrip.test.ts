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
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import TopologyToolStrip from '@/components/Topology/TopologyToolStrip.vue'
import { useTopologyStore } from '@/stores/topologyStore'

const mountStrip = async () => {
  const wrapper = mount(TopologyToolStrip, {
    global: { plugins: [PrimeVue, createTestingPinia({ stubActions: false })] }
  })
  const store = useTopologyStore()
  store.setEditMode(true)
  await wrapper.vm.$nextTick()
  return { wrapper, store }
}

const buttonNamed = (wrapper: ReturnType<typeof mount>, name: string) =>
  wrapper.findAll('button').find(b => !!b.find(`[aria-label="${name}"]`).exists())!

describe('TopologyToolStrip', () => {
  it('shows Select pressed when no drawing mode is on, and turns modes off', async () => {
    const { wrapper, store } = await mountStrip()
    expect(buttonNamed(wrapper, 'Select').attributes('aria-pressed')).toBe('true')

    await buttonNamed(wrapper, 'Draw link').trigger('click')
    expect(store.isLinkDrawMode).toBe(true)
    expect(buttonNamed(wrapper, 'Select').attributes('aria-pressed')).toBe('false')
    expect(buttonNamed(wrapper, 'Draw link').attributes('aria-pressed')).toBe('true')

    await buttonNamed(wrapper, 'Select').trigger('click')
    expect(store.isLinkDrawMode).toBe(false)
    expect(buttonNamed(wrapper, 'Select').attributes('aria-pressed')).toBe('true')
  })

  it('opens the panel on a page, or collapses it when that page is showing', async () => {
    const { wrapper, store } = await mountStrip()
    expect(buttonNamed(wrapper, 'Details').attributes('aria-pressed')).toBe('true')
    await buttonNamed(wrapper, 'Place nodes').trigger('click')
    expect(store.sidePanel).toBe('palette')
    expect(buttonNamed(wrapper, 'Place nodes').attributes('aria-pressed')).toBe('true')
    await buttonNamed(wrapper, 'Place nodes').trigger('click')
    expect(store.sidePanel).toBeNull()
    await buttonNamed(wrapper, 'Details').trigger('click')
    expect(store.sidePanel).toBe('details')
  })

  it('shows only Details outside Edit mode, and a dot for a hidden selection', async () => {
    const { wrapper, store } = await mountStrip()
    store.setEditMode(false)
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[aria-label="Place nodes"]').exists()).toBe(false)
    expect(wrapper.find('[aria-label="Draw link"]').exists()).toBe(false)
    store.setSidePanel(null)
    store.selectOnly('placed-1')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.topology-rail__dot').exists()).toBe(true)
  })

  it('toggles the counts overlay in any mode', async () => {
    const { wrapper, store } = await mountStrip()
    store.setEditMode(false)
    await wrapper.vm.$nextTick()
    const before = store.showCanvasStats
    await buttonNamed(wrapper, 'Counts').trigger('click')
    expect(store.showCanvasStats).toBe(!before)
  })

  it('toggles the box tool and link hints', async () => {
    const { wrapper, store } = await mountStrip()
    await buttonNamed(wrapper, 'Draw box').trigger('click')
    expect(store.isShapeDrawMode).toBe(true)
    const hintsBefore = store.isLinkHintsEnabled
    await buttonNamed(wrapper, 'Link hints').trigger('click')
    expect(store.isLinkHintsEnabled).toBe(!hintsBefore)
  })

  it('offers the background tool only when the view has a background', async () => {
    const { wrapper, store } = await mountStrip()
    expect(wrapper.find('[aria-label="Adjust background"]').exists()).toBe(false)
    store.newView()
    store.setBackground({ type: 'image', ref: 'asset:abc', x: 0, y: 0, width: 100, height: 100 })
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[aria-label="Adjust background"]').exists()).toBe(true)
  })
})
