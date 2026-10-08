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

import { NodeAlarmStatus } from '@/types'

// The tints NodeDetailsBanner offers. Indeterminate and cleared are never a node's status here --
// they are not problems -- so they have none of their own.
export type BannerSeverity = 'none' | 'normal' | 'warning' | 'minor' | 'major' | 'critical'

/** The banner tint for an OnmsSeverity name; `none` for one that has no tint. */
export const bannerSeverity = (severity: string | undefined): BannerSeverity => {
  const key = (severity ?? '').toLowerCase()

  return (['normal', 'warning', 'minor', 'major', 'critical'] as const).find(s => s === key) ?? 'none'
}

/**
 * A node's at-a-glance status, as the legacy includes/nodeStatus-box.jsp showed it.
 */
export interface NodeStatus {
  // Highest severity among the node's unacknowledged problem alarms, as an upper-case
  // OnmsSeverity name; NORMAL when there are none.
  severity: string
  // Headline: down, how many interfaces/services are down, or how bad its problems are.
  message: string
  // Whether there is anything unacknowledged to act on -- the JSP's maxSeverity > NORMAL, which
  // is also what decides whether the acknowledged/unacknowledged counts are shown.
  hasProblems: boolean
  ackCount: number
  unackCount: number
}

// Above Normal. The server reports the worst unacknowledged one, and NORMAL when there is none.
const PROBLEM_SEVERITIES = ['WARNING', 'MINOR', 'MAJOR', 'CRITICAL']

const plural = (count: number, singular: string, pluralForm = `${singular}s`) => `${count} ${count === 1 ? singular : pluralForm}`

/**
 * The status box's headline and counts from the server's summary of the node's problem alarms
 * (GET /api/v2/nodes/{id}/alarmStatus). The server does the counting, as the JSP did over every
 * alarm the node had; this keeps the JSP's wording.
 */
export const computeNodeStatus = (summary: NodeAlarmStatus): NodeStatus => {
  const severity = (summary.severity ?? 'NORMAL').toUpperCase()
  const hasProblems = PROBLEM_SEVERITIES.includes(severity)
  const { nodeDown, interfacesDown: intfDown, servicesDown: servDown } = summary

  let message = `Node has ${hasProblems ? severity.toLowerCase() : 'no'} problems.`

  if (nodeDown) {
    message = 'Node is currently down.'
  } else if (intfDown > 0 && servDown > 0) {
    message = `Node has ${plural(intfDown, 'interface')} and ${plural(servDown, 'service')} down.`
  } else if (intfDown > 0) {
    message = `Node has ${plural(intfDown, 'interface')} down.`
  } else if (servDown > 0) {
    message = `Node has ${plural(servDown, 'service')} down.`
  }

  return {
    severity: hasProblems ? severity : 'NORMAL',
    message,
    hasProblems,
    ackCount: summary.acknowledgedCount,
    unackCount: summary.unacknowledgedCount
  }
}
