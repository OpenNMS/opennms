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
import { bannerSeverity, computeNodeStatus } from '@/components/Nodes/NodeDetails/nodeStatus'
import { NodeAlarmStatus } from '@/types/index'

const bySeverity = (counts: Record<string, number> = {}) => ({ CRITICAL: 0, MAJOR: 0, MINOR: 0, WARNING: 0, ...counts })

const summary = (overrides: Partial<NodeAlarmStatus> = {}): NodeAlarmStatus => ({
  severity: 'NORMAL',
  unacknowledgedCount: 0,
  unacknowledgedBySeverity: bySeverity(),
  ...overrides
})

// The counting is the server's (NodeRestServiceIT#testAlarmStatus).
describe('computeNodeStatus', () => {
  // Acknowledged alarms are not in the summary: the server leaves the severity NORMAL when all are.
  it('reports no problems for a node without unacknowledged alarms', () => {
    expect(computeNodeStatus(summary())).toEqual({
      severity: 'NORMAL',
      message: 'Node has no unacknowledged alarms.',
      hasProblems: false,
      unackBySeverity: []
    })
  })

  it('keeps the highest unacknowledged severity, and says there are unacknowledged alarms', () => {
    const status = computeNodeStatus(summary({ severity: 'MAJOR', unacknowledgedCount: 3, unacknowledgedBySeverity: bySeverity({ MAJOR: 3 }) }))

    expect(status.severity).toBe('MAJOR')
    expect(status.message).toBe('Node has unacknowledged alarms.')
    expect(status.hasProblems).toBe(true)
  })

  it.each(['CRITICAL', 'MAJOR', 'MINOR', 'WARNING', 'warning'])('says the same for %s', (severity) => {
    expect(computeNodeStatus(summary({ severity })).message).toBe('Node has unacknowledged alarms.')
  })

  it('matches severity names regardless of case', () => {
    expect(computeNodeStatus(summary({ severity: 'Critical' })).severity).toBe('CRITICAL')
  })

  it.each(['INDETERMINATE', 'CLEARED', 'NORMAL'])('treats %s as no problem', (severity) => {
    const status = computeNodeStatus(summary({ severity }))

    expect(status.hasProblems).toBe(false)
    expect(status.message).toBe('Node has no unacknowledged alarms.')
  })

  describe('counts by severity', () => {
    it('lists each severity with unacknowledged alarms, highest first', () => {
      const status = computeNodeStatus(summary({
        severity: 'CRITICAL',
        // Not in order, as a server might send them.
        unacknowledgedBySeverity: { WARNING: 4, MINOR: 0, CRITICAL: 1, MAJOR: 2 }
      }))

      expect(status.unackBySeverity).toEqual([
        { severity: 'CRITICAL', count: 1 },
        { severity: 'MAJOR', count: 2 },
        { severity: 'WARNING', count: 4 }
      ])
    })

    it('matches severity names regardless of case', () => {
      expect(computeNodeStatus(summary({ severity: 'MINOR', unacknowledgedBySeverity: { minor: 2 }})).unackBySeverity)
        .toEqual([{ severity: 'MINOR', count: 2 }])
    })

    it('leaves out a severity that is not a problem', () => {
      expect(computeNodeStatus(summary({ unacknowledgedBySeverity: { NORMAL: 5, CLEARED: 1 }})).unackBySeverity).toEqual([])
    })

    it('has none when the server sends no counts', () => {
      const status = computeNodeStatus({ ...summary({ severity: 'MAJOR' }), unacknowledgedBySeverity: undefined as never })

      expect(status.unackBySeverity).toEqual([])
    })
  })
})

describe('bannerSeverity', () => {
  it.each([
    ['CRITICAL', 'critical'],
    ['Major', 'major'],
    ['minor', 'minor'],
    ['WARNING', 'warning'],
    ['NORMAL', 'normal'],
    ['CLEARED', 'none'],
    ['INDETERMINATE', 'indeterminate'],
    [undefined, 'none']
  ])('%s -> %s', (severity, expected) => {
    expect(bannerSeverity(severity)).toBe(expected)
  })
})
