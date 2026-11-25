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

import StatusOverviewPanel from '@/components/Dashboard/panels/StatusOverviewPanel.vue'
import { useAppStore } from '@/stores/appStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { TimeframePreset } from '@/types/dashboard'

const charts = vi.hoisted(() => [] as Array<{ colors: () => string[] }>)

vi.mock('chart.js/auto', () => ({
  default: class {
    data: { datasets: Array<{ backgroundColor: string[] }> }
    options: Record<string, unknown>
    update = vi.fn()
    destroy = vi.fn()
    resize = vi.fn()
    constructor(_canvas: unknown, config: { data: { datasets: Array<{ backgroundColor: string[] }> }, options: Record<string, unknown> }) {
      this.data = config.data
      this.options = config.options
      charts.push({ colors: () => this.data.datasets[0].backgroundColor })
    }
  }
}))

vi.mock('@/services/statusService', async importOriginal => ({
  ...await importOriginal<object>(),
  getNodesByAlarms: vi.fn().mockResolvedValue([{ label: 'Major', count: 2 }, { label: 'Critical', count: 1 }]),
  getNodesByOutages: vi.fn().mockResolvedValue([{ label: 'Major', count: 2 }, { label: 'Critical', count: 1 }])
}))

afterEach(() => {
  document.documentElement.style.removeProperty('--onms-major')
  document.documentElement.style.removeProperty('--onms-critical')
  charts.length = 0
})

describe('Dashboard StatusOverviewPanel', () => {
  it('colors the slices from the severity tokens and recolors them on a theme change', async () => {
    document.documentElement.style.setProperty('--onms-major', '#e35302')
    document.documentElement.style.setProperty('--onms-critical', '#a5021f')
    const pinia = createTestingPinia({ stubActions: false })
    const wrapper = mount(StatusOverviewPanel, {
      props: { panelId: 'p1', options: {}, filter: {}, timeframe: { preset: TimeframePreset.Last24h, from: null, to: null }, refreshTick: 0 } as never,
      global: { plugins: [pinia] }
    })
    await flushPromises()

    expect(charts).toHaveLength(2)
    expect(charts[0].colors()).toEqual(['#e35302', '#a5021f'])

    document.documentElement.style.setProperty('--onms-critical', '#ef4444')
    useAppStore().setTheme('open-dark')
    await flushPromises()

    expect(charts[0].colors()).toContain('#ef4444')
    expect(charts[1].colors()).toContain('#ef4444')
    wrapper.unmount()
  })
})
