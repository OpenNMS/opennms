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

// NOTE: jsdom does not support clientWidth, so the width/resize logic will
// produce width=NaN (debounce fires after mount). Timeline <img> elements
// depend on non-zero ipinterfaces.services arrays, so assertions are limited
// to the availability percentage text and basic card rendering.

import NodeAvailabilityGraph from '@/components/Nodes/NodeAvailabilityGraph.vue'
import { useNodeStore } from '@/stores/nodeStore'
import { NodeAvailability } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const mockNodeId = '101'

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: mockNodeId }})
}))

const seedAvailability: NodeAvailability = {
  availability: 98.76,
  id: 101,
  'service-count': 2,
  'service-down-count': 0,
  ipinterfaces: [
    {
      address: '192.168.1.1',
      availability: 99.5,
      id: 1,
      services: [
        { id: 10, name: 'ICMP', availability: 99.5 }
      ]
    }
  ]
}

describe('NodeAvailabilityGraph.vue', () => {
  let wrapper: VueWrapper<any>
  let nodeStore: ReturnType<typeof useNodeStore>

  beforeEach(() => {
    vi.clearAllMocks()
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
    wrapper = mount(NodeAvailabilityGraph, {
      global: {
        plugins: [pinia, PrimeVue]
      }
    })
    nodeStore = useNodeStore()
    nodeStore.availability = seedAvailability
  })

  afterEach(() => {
    wrapper.unmount()
  })

  it('renders without errors', () => {
    expect(wrapper.exists()).toBe(true)
  })

  it('renders the card element', () => {
    expect(wrapper.find('.card').exists()).toBe(true)
  })

  it('renders availability percentage text from nodeStore.availability', async () => {
    // Re-mount with pre-seeded store so computed picks up the value
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
    const w = mount(NodeAvailabilityGraph, {
      global: {
        plugins: [pinia, PrimeVue]
      }
    })
    const store = useNodeStore()
    store.availability = seedAvailability
    await w.vm.$nextTick()
    // 98.76 rounded to 2 decimal places = 98.76
    expect(w.text()).toContain('98.76%')
    w.unmount()
  })

  it('renders at least one timeline img when ipinterfaces have services', async () => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
    const w = mount(NodeAvailabilityGraph, {
      global: {
        plugins: [pinia, PrimeVue]
      }
    })
    const store = useNodeStore()
    store.availability = seedAvailability
    await w.vm.$nextTick()
    // NOTE: img elements are rendered from v-for over ipinterfaces/services.
    // In jsdom, img src will be set but network requests do not fire.
    const imgs = w.findAll('img')
    expect(imgs.length).toBeGreaterThan(0)
    w.unmount()
  })
})
