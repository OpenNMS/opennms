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
import { Alarm } from '@/types'

const UEI = {
  nodeDown: 'uei.opennms.org/nodes/nodeDown',
  interfaceDown: 'uei.opennms.org/nodes/interfaceDown',
  nodeLostService: 'uei.opennms.org/nodes/nodeLostService',
  other: 'uei.opennms.org/nodes/dataCollectionFailed'
}

let nextId = 1
const alarm = (severity: string, uei = UEI.other, acked = false): Alarm => ({
  id: String(nextId++),
  severity,
  uei,
  nodeId: 42,
  nodeLabel: 'n42',
  count: 1,
  lastEventTime: 0,
  logMessage: '',
  ...(acked ? { ackTime: 1700000000000, ackUser: 'admin' } : {})
})

describe('computeNodeStatus', () => {
  it('reports no problems for a node without alarms', () => {
    expect(computeNodeStatus([])).toEqual({
      severity: 'NORMAL',
      message: 'Node has no problems.',
      hasProblems: false,
      ackCount: 0,
      unackCount: 0
    })
  })

  // Severity ids 1-3 are not problems, whatever their UEI says.
  it('ignores indeterminate, cleared and normal alarms entirely', () => {
    const status = computeNodeStatus([
      alarm('INDETERMINATE', UEI.nodeDown),
      alarm('CLEARED', UEI.interfaceDown),
      alarm('NORMAL', UEI.nodeLostService)
    ])

    expect(status.message).toBe('Node has no problems.')
    expect(status.hasProblems).toBe(false)
    expect(status.ackCount + status.unackCount).toBe(0)
  })

  it('names the highest unacknowledged severity', () => {
    const status = computeNodeStatus([alarm('WARNING'), alarm('MAJOR'), alarm('MINOR')])

    expect(status.severity).toBe('MAJOR')
    expect(status.message).toBe('Node has major problems.')
    expect(status.hasProblems).toBe(true)
  })

  it('matches severity names regardless of case', () => {
    expect(computeNodeStatus([alarm('Critical')]).severity).toBe('CRITICAL')
  })

  // Acknowledged means someone has it: it is counted, but does not set the colour.
  it('counts an acknowledged problem without letting it raise the severity', () => {
    const status = computeNodeStatus([alarm('CRITICAL', UEI.other, true), alarm('WARNING')])

    expect(status.severity).toBe('WARNING')
    expect(status.ackCount).toBe(1)
    expect(status.unackCount).toBe(1)
  })

  it('is not a problem when every problem alarm is acknowledged', () => {
    const status = computeNodeStatus([alarm('MAJOR', UEI.other, true)])

    expect(status.severity).toBe('NORMAL')
    expect(status.hasProblems).toBe(false)
    expect(status.ackCount).toBe(1)
  })

  describe('headline', () => {
    it('says the node is down, over any interface or service counts', () => {
      expect(computeNodeStatus([
        alarm('MAJOR', UEI.nodeDown),
        alarm('MINOR', UEI.interfaceDown),
        alarm('MINOR', UEI.nodeLostService)
      ]).message).toBe('Node is currently down.')
    })

    it.each([
      [[UEI.interfaceDown], 'Node has 1 interface down.'],
      [[UEI.interfaceDown, UEI.interfaceDown], 'Node has 2 interfaces down.'],
      [[UEI.nodeLostService], 'Node has 1 service down.'],
      [[UEI.nodeLostService, UEI.nodeLostService, UEI.nodeLostService], 'Node has 3 services down.'],
      [[UEI.interfaceDown, UEI.nodeLostService], 'Node has 1 interface and 1 service down.'],
      [[UEI.interfaceDown, UEI.interfaceDown, UEI.nodeLostService, UEI.nodeLostService], 'Node has 2 interfaces and 2 services down.']
    ])('%j -> %s', (ueis, message) => {
      expect(computeNodeStatus(ueis.map(uei => alarm('MINOR', uei))).message).toBe(message)
    })

    // The JSP counts these whether or not they are acknowledged.
    it('counts acknowledged outages in the headline', () => {
      expect(computeNodeStatus([alarm('MINOR', UEI.nodeLostService, true)]).message).toBe('Node has 1 service down.')
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
