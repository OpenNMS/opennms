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
import { describe, expect, it, vi } from 'vitest'
import AdhocChart from '@/components/AdhocGraphs/AdhocChart.vue'

// the chart itself is not under test, and Chart.js does not load under happy-dom
vi.mock('chart.js', () => ({
  Chart: class {
    static register() {}
    static getChart() {
      return undefined
    }
    update() {}
    destroy() {}
    resetZoom() {}
  },
  registerables: []
}))
vi.mock('chartjs-plugin-zoom', () => ({ default: {}}))

const config = { series: [], expressions: [], title: '', verticalLabel: '', stacked: false, resolution: 400 }
const time = { startTime: 1_700_000_000, endTime: 1_700_003_600, format: 'minutes' }

const mountChart = (props: Record<string, unknown> = {}) => mount(AdhocChart, {
  props: { config, measurements: null, time, loading: false, error: '', ...props },
  global: { plugins: [createTestingPinia(), PrimeVue], stubs: { GraphDataTable: true }}
})

describe('AdhocChart.vue', () => {
  it('draws on the builder page\'s one canvas by default', () => {
    const wrapper = mountChart()
    expect(wrapper.find('canvas#adhoc-graph').exists()).toBe(true)
    expect(wrapper.find('#adhoc-graph-lc').exists()).toBe(true)
  })

  it('takes a canvas id of its own, so several charts can share a page', () => {
    const first = mountChart({ canvasId: 'gc-3-0' })
    const second = mountChart({ canvasId: 'gc-3-1' })
    expect(first.find('canvas#gc-3-0').exists()).toBe(true)
    expect(first.find('#gc-3-0-lc').exists()).toBe(true)
    expect(second.find('canvas#gc-3-1').exists()).toBe(true)
    expect(first.find('canvas#adhoc-graph').exists()).toBe(false)
  })
})
