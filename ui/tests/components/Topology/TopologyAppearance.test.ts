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
import TopologyAppearance from '@/components/Topology/TopologyAppearance.vue'
import { useTopologyStore } from '@/stores/topologyStore'

const mountAppearance = () => {
  const wrapper = mount(TopologyAppearance, {
    global: { plugins: [PrimeVue, createTestingPinia({ stubActions: false })] }
  })
  return { wrapper, store: useTopologyStore() }
}

describe('TopologyAppearance', () => {
  it('shows the current node size and link width', () => {
    const { wrapper, store } = mountAppearance()
    store.setNodeSize(14)
    store.setLinkWidth(5)
    return wrapper.vm.$nextTick().then(() => {
      const values = wrapper.findAll('.topology-appearance__value').map(v => v.text())
      expect(values).toEqual(['14', '5'])
    })
  })

  it('clamps and remembers sizes on the open custom view', () => {
    const { store } = mountAppearance()
    store.newView()
    store.setNodeSize(100)
    store.setLinkWidth(0)
    expect(store.nodeSize).toBe(store.NODE_SIZE_MAX)
    expect(store.linkWidth).toBe(store.LINK_WIDTH_MIN)
    expect(store.currentView?.style).toMatchObject({ nodeSize: store.NODE_SIZE_MAX, linkWidth: store.LINK_WIDTH_MIN })
  })

  it('offers a label placement and remembers it on the open custom view', () => {
    const { wrapper, store } = mountAppearance()
    store.newView()
    expect(wrapper.find('[aria-label="Node label placement"]').exists()).toBe(true)
    store.setLabelPlacement('bottom')
    expect(store.currentView?.style?.labelPlacement).toBe('bottom')
    store.newView()
    expect(store.labelPlacement).toBe('right')
  })

  it('starts a new view from the defaults', () => {
    const { store } = mountAppearance()
    store.newView()
    store.setNodeSize(10)
    store.setLinkWidth(6)
    store.newView()
    expect(store.nodeSize).toBe(20)
    expect(store.linkWidth).toBe(3)
  })
})
