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

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { listMetricEntities, queryMetricSeries } from '@/services/metricChartService'
import { invalidateKpiSources } from '@/services/topnService'
import { rest } from '@/services/axiosInstances'
import { TimeframePreset } from '@/types/dashboard'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn(), post: vi.fn() }
}))

const tf = { preset: TimeframePreset.Last24h, from: null, to: null }
const tree = { data: { resource: [
  { id: 'node[2]', label: 'zeta', children: { resource: [{ id: 'node[2].responseTime[10.0.0.2]', rrdGraphAttributes: { icmp: {}}}] }},
  { id: 'node[1]', label: 'alpha', children: { resource: [{ id: 'node[1].responseTime[10.0.0.1]', rrdGraphAttributes: { icmp: {}}}] }}
] }}

describe('metricChartService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    invalidateKpiSources()
    vi.mocked(rest.get).mockResolvedValue(tree)
  })
  afterEach(() => vi.restoreAllMocks())

  it('lists entities sorted by label', async () => {
    expect(await listMetricEntities('response-time')).toEqual(['alpha', 'zeta'])
  })

  it('charts the first entity when none is configured, and says which one', async () => {
    vi.mocked(rest.post).mockResolvedValue({ data: { timestamps: [1, 2], columns: [{ values: [1000, 2000] }] }})
    const series = await queryMetricSeries('response-time', '', tf)
    expect(series?.entity).toBe('alpha')
    expect((vi.mocked(rest.post).mock.calls[0][1] as { source: { resourceId: string }[] }).source[0].resourceId).toBe('node[1].responseTime[10.0.0.1]')
  })

  it('keeps collection gaps as nulls so the line breaks there', async () => {
    vi.mocked(rest.post).mockResolvedValue({ data: { timestamps: [1, 2, 3], columns: [{ values: [1000, NaN, 3000] }] }})
    const series = await queryMetricSeries('response-time', 'zeta', tf)
    expect(series?.values).toEqual([1, null, 3])
    expect(series?.unit).toBe('ms')
  })

  it('returns null for an unknown entity or a series with no data', async () => {
    expect(await queryMetricSeries('response-time', 'no-such-node', tf)).toBeNull()
    vi.mocked(rest.post).mockResolvedValue({ data: { timestamps: [1], columns: [{ values: [NaN] }] }})
    expect(await queryMetricSeries('response-time', 'alpha', tf)).toBeNull()
  })

  it('lets a failed request surface instead of reading as no data', async () => {
    vi.mocked(rest.post).mockRejectedValue(new Error('500'))
    await expect(queryMetricSeries('response-time', 'alpha', tf)).rejects.toThrow('500')
  })
})
