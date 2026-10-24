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

import { describe, expect, it } from 'vitest'
import {
  adhocBuilderRoute,
  adhocStateFromQuery,
  adhocStateTitle,
  adhocStateToQuery
} from '@/components/GraphCollections/adhocCollectionState'
import { decodeAdhocState, encodeAdhocState } from '@/components/AdhocGraphs/utils/adhocUrlState'

describe('adhocCollectionState', () => {
  it('turns the stored string into the query the builder decodes, with repeated keys as lists', () => {
    const query = adhocStateToQuery('nf=rtr&pd=node%5B1%5D.interfaceSnmp%5Beth0%5D%7CifHCInOctets&pd=b%7Cc&n=2&title=Up+links')
    expect(query).toEqual({
      nf: 'rtr',
      pd: ['node[1].interfaceSnmp[eth0]|ifHCInOctets', 'b|c'],
      n: '2',
      title: 'Up links'
    })
    expect(decodeAdhocState(query)?.link.selection.pickedDatasourceKeys).toEqual(['node[1].interfaceSnmp[eth0]|ifHCInOctets', 'b|c'])
  })

  it('drops the window from an encoded link and keeps everything else, round-tripping with the encoder', () => {
    const encoded = encodeAdhocState(
      { series: [], expressions: [], title: 'CPU', verticalLabel: '%', stacked: true, resolution: 400 },
      { startTime: 1_700_000_000, endTime: 1_700_003_600, format: 'minutes', range: { unit: 'hours', amount: 1 }},
      {
        selection: { nodeFilter: 'rtr', resourceFilter: '', datasourceFilter: 'CpuRawUser', pickedNodeIds: [], pickedResourceIds: [], pickedDatasourceKeys: [], excludedDatasourceKeys: [] },
        overrides: {},
        seriesCount: 3
      }
    )
    expect(encoded.range).toBeDefined()
    const state = adhocStateFromQuery(encoded)
    const params = new URLSearchParams(state)
    for (const key of ['range', 'start', 'end', 'fmt']) {
      expect(params.has(key)).toBe(false)
    }
    expect(params.get('nf')).toBe('rtr')
    expect(params.get('title')).toBe('CPU')
    const restored = decodeAdhocState(adhocStateToQuery(state))
    expect(restored?.config).toMatchObject({ title: 'CPU', verticalLabel: '%', stacked: true })
    expect(restored?.link.selection.datasourceFilter).toBe('CpuRawUser')
    expect(restored?.time.range).toBeUndefined()
  })

  it('reads the title the builder stored, or nothing', () => {
    expect(adhocStateTitle('nf=rtr&title=Core+CPU')).toBe('Core CPU')
    expect(adhocStateTitle('nf=rtr')).toBe('')
    expect(adhocStateTitle(null)).toBe('')
  })

  it('opens the builder on the stored graph, on the given window when there is one', () => {
    expect(adhocBuilderRoute('nf=rtr&n=1')).toEqual({ path: '/adhoc-graphs', query: { nf: 'rtr', n: '1' }})
    expect(adhocBuilderRoute('nf=rtr', { startTime: 1_700_000_000, endTime: 1_700_003_600, format: 'minutes' })).toEqual({
      path: '/adhoc-graphs',
      query: { nf: 'rtr', start: '1700000000', end: '1700003600', fmt: 'minutes' }
    })
  })
})
