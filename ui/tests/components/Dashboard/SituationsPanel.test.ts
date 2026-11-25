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

import SituationsPanel from '@/components/Dashboard/panels/SituationsPanel.vue'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { TimeframePreset } from '@/types/dashboard'

vi.mock('@/services/situationService', async importOriginal => ({
  ...await importOriginal<object>(),
  getPendingSituations: vi.fn().mockResolvedValue([
    { id: 1, severity: 'CRITICAL' },
    { id: 2, severity: 'normal' }
  ])
}))

vi.mock('@/services/dashboardFilterService', async importOriginal => ({
  ...await importOriginal<object>(),
  buildFilterClauses: vi.fn().mockResolvedValue([])
}))

const mountPanel = async (options: Record<string, unknown>) => {
  const wrapper = mount(SituationsPanel, {
    props: { panelId: 'p1', options, filter: {}, timeframe: { preset: TimeframePreset.Last24h, from: null, to: null }, refreshTick: 0 } as never,
    global: { plugins: [createTestingPinia({ stubActions: false })] }
  })
  await flushPromises()
  return wrapper
}

describe('Dashboard SituationsPanel', () => {
  it('tints each row by its severity when shading is on', async () => {
    const wrapper = await mountPanel({ shade: true })
    const rows = wrapper.findAll('li')

    expect(rows[0].classes()).toContain('severity-tint--critical')
    expect(rows[1].classes()).toContain('severity-tint--normal')
  })

  it('adds no tint class when shading is off', async () => {
    const wrapper = await mountPanel({})

    for (const row of wrapper.findAll('li')) {
      expect(row.classes().some(c => c.startsWith('severity-tint--'))).toBe(false)
    }
  })
})
