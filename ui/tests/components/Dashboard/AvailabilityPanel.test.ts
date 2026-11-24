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

import AvailabilityPanel from '@/components/Dashboard/panels/AvailabilityPanel.vue'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { TimeframePreset } from '@/types/dashboard'

const cat = vi.hoisted(() => (name: string, availabilityClass: string) =>
  ({ name, outageText: '0', availabilityText: `${name}-text`, availability: 1, availabilityClass, outageClass: '' }))

vi.mock('@/services/availabilityService', () => ({
  getAvailability: vi.fn().mockResolvedValue([
    {
      name: 'Categories',
      categories: [cat('A', 'Normal'), cat('B', 'Critical'), cat('C', 'Unknown'), cat('D', '')]
    },
    { name: 'Total', categories: [cat('Total', 'Critical')] }
  ])
}))

const mountPanel = async (options: Record<string, unknown>) => {
  const wrapper = mount(AvailabilityPanel, {
    props: { panelId: 'p1', options, filter: {}, timeframe: { preset: TimeframePreset.Last24h, from: null, to: null }, refreshTick: 0 } as never,
    global: { plugins: [createTestingPinia({ stubActions: false })] }
  })
  await flushPromises()
  return wrapper
}

describe('Dashboard AvailabilityPanel', () => {
  it('gives only Normal/Warning/Critical classes a badge level', async () => {
    const wrapper = await mountPanel({ shade: true })

    expect(wrapper.findAll('[data-test="availability-badge"]').map(b => b.attributes('data-level'))).toEqual(['normal', 'critical'])
    const rows = wrapper.findAll('tbody tr')
    expect(rows[2].text()).toContain('C-text')
    expect(rows[3].text()).toContain('D-text')
    for (const row of [rows[2], rows[3]]) {
      expect(row.classes().some(c => c.startsWith('severity-tint--'))).toBe(false)
    }
  })

  it('tints classed rows when shading is on, and never the total row', async () => {
    const wrapper = await mountPanel({ shade: true })
    const rows = wrapper.findAll('tbody tr')

    expect(rows[0].classes()).toContain('severity-tint--normal')
    expect(rows[1].classes()).toContain('severity-tint--critical')
    expect(rows[4].classes().some(c => c.startsWith('severity-tint--'))).toBe(false)
  })

  it('adds no tint when shading is off', async () => {
    const wrapper = await mountPanel({})

    expect(wrapper.findAll('tr').some(r => r.classes().some(c => c.startsWith('severity-tint--')))).toBe(false)
  })
})
