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
import {
  DEFAULT_NODE_COLOR,
  severityColor,
  highestSeverity,
  aggregateNodeSeverities
} from '@/components/Topology/severity'

describe('topology severity utilities', () => {
  describe('severityColor', () => {
    it('maps known severities to distinct colors (case-insensitive)', () => {
      expect(severityColor('CRITICAL')).toBe('#c9292c')
      expect(severityColor('major')).toBe('#ff8d00')
      expect(severityColor('Normal')).toBe('#3bb44a')
    })

    it('falls back to the default node color for missing/unknown severities', () => {
      expect(severityColor(undefined)).toBe(DEFAULT_NODE_COLOR)
      expect(severityColor('')).toBe(DEFAULT_NODE_COLOR)
      expect(severityColor('BOGUS')).toBe(DEFAULT_NODE_COLOR)
    })
  })

  describe('highestSeverity', () => {
    it('returns the most severe by OpenNMS rank', () => {
      expect(highestSeverity(['NORMAL', 'MAJOR', 'MINOR'])).toBe('MAJOR')
      expect(highestSeverity(['WARNING', 'CRITICAL', 'NORMAL'])).toBe('CRITICAL')
    })

    it('ignores empty entries and returns undefined for none', () => {
      expect(highestSeverity([undefined, '', undefined])).toBeUndefined()
      expect(highestSeverity([])).toBeUndefined()
    })
  })

  describe('aggregateNodeSeverities', () => {
    it('rolls alarms up to the highest severity per node id', () => {
      const result = aggregateNodeSeverities([
        { nodeId: 1, severity: 'WARNING' },
        { nodeId: 1, severity: 'MAJOR' },
        { nodeId: 1, severity: 'MINOR' },
        { nodeId: 2, severity: 'NORMAL' }
      ])
      expect(result).toEqual({ 1: 'MAJOR', 2: 'NORMAL' })
    })

    it('skips alarms with no node id or no severity', () => {
      const result = aggregateNodeSeverities([
        { severity: 'CRITICAL' },
        { nodeId: 3 },
        { nodeId: 4, severity: 'MINOR' }
      ])
      expect(result).toEqual({ 4: 'MINOR' })
    })
  })
})

// A cleared alarm is history, not a state. Its level is 0 and the first alarm
// seen for a node was taken unconditionally, so a node whose only alarms were
// cleared painted grey instead of keeping its default color.
describe('aggregateNodeSeverities and cleared alarms', () => {
  it('ignores a cleared alarm entirely', () => {
    expect(aggregateNodeSeverities([{ nodeId: 7, severity: 'CLEARED' }])).toEqual({})
  })

  it('is case-insensitive about it', () => {
    expect(aggregateNodeSeverities([{ nodeId: 7, severity: 'Cleared' }])).toEqual({})
  })

  it('still reports a real severity alongside a cleared one', () => {
    expect(aggregateNodeSeverities([
      { nodeId: 7, severity: 'CLEARED' },
      { nodeId: 7, severity: 'MINOR' }
    ])).toEqual({ 7: 'MINOR' })
  })
})
