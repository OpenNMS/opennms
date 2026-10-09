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
import { bannerSeverity, computeNodeStatus } from '@/components/Nodes/nodeStatus'
import { NodeAlarmStatus } from '@/types'

const summary = (overrides: Partial<NodeAlarmStatus> = {}): NodeAlarmStatus => ({
  severity: 'NORMAL',
  nodeDown: false,
  interfacesDown: 0,
  servicesDown: 0,
  acknowledgedCount: 0,
  unacknowledgedCount: 0,
  ...overrides
})

// The counting is the server's (NodeRestServiceIT#testAlarmStatus); this is the JSP's wording.
describe('computeNodeStatus', () => {
  it('reports no problems for a node without problem alarms', () => {
    expect(computeNodeStatus(summary())).toEqual({
      severity: 'NORMAL',
      message: 'Node has no problems.',
      hasProblems: false,
      ackCount: 0,
      unackCount: 0
    })
  })

  it('names the highest unacknowledged severity', () => {
    const status = computeNodeStatus(summary({ severity: 'MAJOR', unacknowledgedCount: 3 }))

    expect(status.severity).toBe('MAJOR')
    expect(status.message).toBe('Node has major problems.')
    expect(status.hasProblems).toBe(true)
    expect(status.unackCount).toBe(3)
  })

  it('matches severity names regardless of case', () => {
    expect(computeNodeStatus(summary({ severity: 'Critical' })).severity).toBe('CRITICAL')
  })

  // Acknowledged means someone has it: it is counted, but the server leaves the severity NORMAL.
  it('is not a problem when every problem alarm is acknowledged, but still counts them', () => {
    const status = computeNodeStatus(summary({ acknowledgedCount: 2 }))

    expect(status.severity).toBe('NORMAL')
    expect(status.hasProblems).toBe(false)
    expect(status.ackCount).toBe(2)
  })

  it.each(['INDETERMINATE', 'CLEARED', 'NORMAL'])('treats %s as no problem', (severity) => {
    expect(computeNodeStatus(summary({ severity })).hasProblems).toBe(false)
  })

  describe('headline', () => {
    it('says the node is down, over any interface or service counts', () => {
      expect(computeNodeStatus(summary({ nodeDown: true, interfacesDown: 1, servicesDown: 1 })).message)
        .toBe('Node is currently down.')
    })

    it.each([
      [1, 0, 'Node has 1 interface down.'],
      [2, 0, 'Node has 2 interfaces down.'],
      [0, 1, 'Node has 1 service down.'],
      [0, 3, 'Node has 3 services down.'],
      [1, 1, 'Node has 1 interface and 1 service down.'],
      [2, 2, 'Node has 2 interfaces and 2 services down.']
    ])('%i interfaces, %i services -> %s', (interfacesDown, servicesDown, message) => {
      expect(computeNodeStatus(summary({ severity: 'MINOR', interfacesDown, servicesDown })).message).toBe(message)
    })

    // The JSP counts these whether or not they are acknowledged, so the server does too.
    it('reports outages even when nothing unacknowledged is left', () => {
      expect(computeNodeStatus(summary({ servicesDown: 1, acknowledgedCount: 1 })).message).toBe('Node has 1 service down.')
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
    ['INDETERMINATE', 'none'],
    [undefined, 'none']
  ])('%s -> %s', (severity, expected) => {
    expect(bannerSeverity(severity)).toBe(expected)
  })
})
