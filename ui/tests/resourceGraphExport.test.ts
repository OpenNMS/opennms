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

import { describe, it, expect } from 'vitest'
import { buildGraphCsv, safeFileStem } from '@/components/Resources/utils/graphExport'

describe('safeFileStem', () => {
  it('sanitizes titles to safe filenames', () => {
    expect(safeFileStem('Node-level Performance Data')).toBe('Node-level_Performance_Data')
    expect(safeFileStem('a/b:c*d')).toBe('a_b_c_d')
    expect(safeFileStem('')).toBe('graph')
    expect(safeFileStem('***')).toBe('graph')
  })
})

describe('buildGraphCsv', () => {
  const graphData: any = {
    timestamps: [1700000000000, 1700000060000],
    labels: ['m1', 'm2'],
    columns: [{ values: [1.5, NaN] }, { values: [10, 20] }]
  }
  const converted: any = {
    metrics: [{ name: 'm1' }, { name: 'm2' }],
    printStatements: [
      { metric: 'm1', header: 'Alarms' },
      { metric: 'm2', header: 'Events' }
    ]
  }

  it('emits a header row and one ISO-timestamped row per sample, gaps blank', () => {
    const lines = buildGraphCsv(graphData, converted).split('\r\n')
    expect(lines[0]).toBe('Date/Time,Alarms,Events')
    expect(lines[1]).toBe('2023-11-14T22:13:20.000Z,1.5,10')
    // the NaN gap in m1 becomes an empty cell rather than "NaN"
    expect(lines[2]).toBe('2023-11-14T22:14:20.000Z,,20')
  })

  it('falls back to the metric name when there is no print header, and escapes commas', () => {
    const conv: any = { metrics: [{ name: 'a,b' }], printStatements: [] }
    const gd: any = { timestamps: [1700000000000], labels: ['a,b'], columns: [{ values: [5] }] }
    const lines = buildGraphCsv(gd, conv).split('\r\n')
    expect(lines[0]).toBe('Date/Time,"a,b"')
    expect(lines[1]).toBe('2023-11-14T22:13:20.000Z,5')
  })

  it('omits metrics that have no column in the response instead of emitting blank columns', () => {
    const conv: any = { metrics: [{ name: 'ghost' }, { name: 'real' }], printStatements: [] }
    const gd: any = { timestamps: [1700000000000], labels: ['real'], columns: [{ values: [7] }] }
    const lines = buildGraphCsv(gd, conv).split('\r\n')
    expect(lines[0]).toBe('Date/Time,real')
    expect(lines[1]).toBe('2023-11-14T22:13:20.000Z,7')
  })

  it('neutralizes spreadsheet formula prefixes in header cells', () => {
    const conv: any = { metrics: [{ name: 'm' }], printStatements: [{ metric: 'm', header: '=SUM(A1:A2)' }] }
    const gd: any = { timestamps: [1700000000000], labels: ['m'], columns: [{ values: [1] }] }
    expect(buildGraphCsv(gd, conv).split('\r\n')[0]).toBe('Date/Time,\'=SUM(A1:A2)')
  })

  it('emits a blank time for an invalid timestamp rather than throwing', () => {
    const conv: any = { metrics: [{ name: 'm' }], printStatements: [{ metric: 'm', header: 'M' }] }
    const gd: any = { timestamps: [NaN, 1700000000000], labels: ['m'], columns: [{ values: [1, 2] }] }
    const lines = buildGraphCsv(gd, conv).split('\r\n')
    expect(lines[1]).toBe(',1')
    expect(lines[2]).toBe('2023-11-14T22:13:20.000Z,2')
  })

  it('does not corrupt legitimate negative values (value cells are not formula-guarded)', () => {
    const conv: any = { metrics: [{ name: 'm' }], printStatements: [{ metric: 'm', header: 'M' }] }
    const gd: any = { timestamps: [1700000000000], labels: ['m'], columns: [{ values: [-42] }] }
    expect(buildGraphCsv(gd, conv).split('\r\n')[1]).toBe('2023-11-14T22:13:20.000Z,-42')
  })
})
