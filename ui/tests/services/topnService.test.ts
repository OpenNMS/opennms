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
import { SOURCES_PER_REQUEST, clampTopnN, collectSources, invalidateKpiSources, queryTopn, TOPN_KPIS } from '@/services/topnService'
import { rest } from '@/services/axiosInstances'
import { TimeframePreset } from '@/types/dashboard'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn(), post: vi.fn() }
}))

const tf = { preset: TimeframePreset.Last24h, from: null, to: null }

const node = (id: string, label: string, addresses: string[]) => ({
  id, label,
  children: { resource: addresses.map(a => ({ id: `${id}.responseTime[${a}]`, rrdGraphAttributes: { icmp: {}}})) }
})

// two nodes each carrying the ICMP response-time attribute
const resourceTree = { data: { resource: [node('node[1]', 'node-A', ['10.0.0.1']), node('node[2]', 'node-B', ['10.0.0.2'])] }}

describe('queryTopn', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    invalidateKpiSources()
    vi.mocked(rest.get).mockResolvedValue(resourceTree)
  })
  afterEach(() => vi.restoreAllMocks())

  it('attributes each measurement to the source its label encodes, not its position', async () => {
    // backend returns columns in HASH order: s1 (node-B) first, s0 (node-A) second
    vi.mocked(rest.post).mockResolvedValue({
      data: { labels: ['s1', 's0'], columns: [{ values: [200000, 200000] }, { values: [50000, 50000] }] }
    })
    const rows = await queryTopn('response-time', tf, 5, 'desc')
    const byLabel = Object.fromEntries(rows.map(r => [r.label, Math.round(r.value)]))
    expect(byLabel['node-A']).toBe(50)
    expect(byLabel['node-B']).toBe(200)
  })

  it('ranks descending and honors n', async () => {
    vi.mocked(rest.post).mockResolvedValue({ data: { labels: ['s0', 's1'], columns: [{ values: [50000] }, { values: [200000] }] }})
    const rows = await queryTopn('response-time', tf, 1, 'desc')
    expect(rows).toHaveLength(1)
    expect(rows[0].label).toBe('node-B')
  })

  it('asks for a few coarse points per source and only nodes plus their children', async () => {
    vi.mocked(rest.post).mockResolvedValue({ data: { labels: [], columns: [] }})
    await queryTopn('response-time', tf, 5, 'desc')
    expect(vi.mocked(rest.get).mock.calls[0][0]).toBe('/resources?depth=1')
    const payload = vi.mocked(rest.post).mock.calls[0][1] as { step: number; maxrows: number }
    // 24 hours in 24 buckets: one point an hour, well above the 5-minute floor
    expect(payload.step).toBe(3_600_000)
    expect(payload.maxrows).toBeLessThanOrEqual(50)
  })

  it('ranks every candidate, querying large sets in batches', async () => {
    const many = { data: { resource: Array.from({ length: SOURCES_PER_REQUEST + 3 }, (_, i) => node(`node[${i}]`, `node-${i}`, [`10.0.${Math.floor(i / 256)}.${i % 256}`])) }}
    vi.mocked(rest.get).mockResolvedValue(many)
    // each batch answers for the sources it was sent, by their global index label
    vi.mocked(rest.post).mockImplementation(async (_url: string, body: unknown) => {
      const sources = (body as { source: { label: string }[] }).source
      return { data: { labels: sources.map(s => s.label), columns: sources.map(s => ({ values: [Number(s.label.slice(1)) * 1000] })) }}
    })
    const rows = await queryTopn('response-time', tf, 2, 'desc')
    expect(vi.mocked(rest.post)).toHaveBeenCalledTimes(2)
    // the highest values sit past the first batch boundary and are still ranked
    expect(rows.map(r => r.label)).toEqual([`node-${SOURCES_PER_REQUEST + 2}`, `node-${SOURCES_PER_REQUEST + 1}`])
  })

  it('returns empty when there are no sources', async () => {
    vi.mocked(rest.get).mockResolvedValue({ data: { resource: [] }})
    expect(await queryTopn('response-time', tf, 5, 'desc')).toEqual([])
    expect(rest.post).not.toHaveBeenCalled()
  })

  it('lets a failed request surface instead of reading as no data', async () => {
    vi.mocked(rest.post).mockRejectedValue(new Error('503'))
    await expect(queryTopn('response-time', tf, 5, 'desc')).rejects.toThrow('503')
  })

  it('reuses the resource tree across calls', async () => {
    vi.mocked(rest.post).mockResolvedValue({ data: { labels: [], columns: [] }})
    await queryTopn('response-time', tf, 5, 'desc')
    await queryTopn('response-time', tf, 5, 'asc')
    expect(rest.get).toHaveBeenCalledTimes(1)
  })
})

describe('collectSources', () => {
  it('qualifies the label with the address when a node polls more than one interface', () => {
    const tree = { resource: [node('node[7]', 'Router3', ['172.16.50.1', '10.10.10.5']), node('node[8]', 'Switch1', ['172.16.10.1'])] }
    expect(collectSources(tree, TOPN_KPIS[0]).map(s => s.label)).toEqual(['Router3 (172.16.50.1)', 'Router3 (10.10.10.5)', 'Switch1'])
  })
})

describe('clampTopnN', () => {
  it('bounds N the same way on read as on save', () => {
    expect(clampTopnN(undefined)).toBe(5)
    expect(clampTopnN('abc')).toBe(5)
    expect(clampTopnN(0)).toBe(5)
    expect(clampTopnN(-3)).toBe(5)
    expect(clampTopnN(7.9)).toBe(7)
    expect(clampTopnN(500)).toBe(50)
  })
})
