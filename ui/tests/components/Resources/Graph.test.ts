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

import { afterEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import Graph from '@/components/Resources/Graph.vue'
import { useGraphStore } from '@/stores/graphStore'

// the chart itself is not under test, and Chart.js does not load under happy-dom
vi.mock('chart.js', () => ({
  Chart: class {
    static register() {}
    static getChart() {
      return undefined
    }
    update() {}
    destroy() {}
  },
  registerables: []
}))
vi.mock('chartjs-plugin-zoom', () => ({ default: {}}))

// a plain object, as the graph collection cell passes one, not a reactive one
const timeFrom = (startTime: number) => ({ startTime, endTime: startTime + 3600, format: 'HH:mm' })

describe('Graph.vue', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders again when the time prop is replaced by a plain object', async () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => {})
    vi.spyOn(console, 'log').mockImplementation(() => {})
    const wrapper = mount(Graph, {
      props: { definition: 'mib2.bits', resourceId: 'node[1].nodeSnmp[]', time: timeFrom(0), label: 'rtr', isSingleGraph: false },
      global: { plugins: [createTestingPinia(), PrimeVue] }
    })
    await flushPromises()
    const store = useGraphStore()
    expect(store.getDefinitionData).toHaveBeenCalledTimes(1)

    await wrapper.setProps({ time: timeFrom(7200) })
    await flushPromises()
    expect(store.getDefinitionData).toHaveBeenCalledTimes(2)
    expect(warn.mock.calls.flat().join(' ')).not.toContain('Invalid watch source')
  })
})
