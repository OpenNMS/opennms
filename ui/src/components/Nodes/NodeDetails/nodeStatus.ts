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

import { severityKey, SeverityKey } from '@/lib/severity'
import { NodeAlarmStatus } from '@/types/index'

// The tints NodeDetailsBanner offers: one per severity look. Indeterminate and cleared are never a
// node's status here -- they are not problems -- so they have none of their own.
export type BannerSeverity = SeverityKey

/** The banner tint for an OnmsSeverity name; `none` for one that has no tint. */
export const bannerSeverity = (severity: string | undefined): BannerSeverity => severityKey(severity)

/** How many of the node's problem alarms are unacknowledged at one severity. */
export interface SeverityCount {
  // An upper-case OnmsSeverity name above Normal.
  severity: string
  count: number
}

/**
 * A node's at-a-glance status: how severe its unacknowledged problem alarms are, and how many
 * there are at each severity.
 */
export interface NodeStatus {
  // Highest severity among the node's unacknowledged problem alarms, as an upper-case
  // OnmsSeverity name; NORMAL when there are none.
  severity: string
  message: string
  // Whether there is anything unacknowledged to act on -- the JSP's maxSeverity > NORMAL.
  hasProblems: boolean
  // The unacknowledged problem alarms at each severity that has any, highest first.
  unackBySeverity: SeverityCount[]
}

// Above Normal, highest first. The server reports the worst unacknowledged one, and NORMAL when
// there is none.
const PROBLEM_SEVERITIES = ['CRITICAL', 'MAJOR', 'MINOR', 'WARNING']

/**
 * The status box's headline and counts from the server's summary of the node's problem alarms
 * (GET /api/v2/nodes/{id}/alarmStatus). The server does the counting. The headline says only
 * whether there are unacknowledged alarms; the counts by severity say the rest.
 */
export const computeNodeStatus = (summary: NodeAlarmStatus): NodeStatus => {
  const severity = (summary.severity ?? 'NORMAL').toUpperCase()
  const hasProblems = PROBLEM_SEVERITIES.includes(severity)

  // Keyed by severity name; matched in any case, and put in order here rather than trusting the
  // order of the server's keys.
  const counts = new Map(Object.entries(summary.unacknowledgedBySeverity ?? {})
    .map(([name, count]) => [name.toUpperCase(), count]))

  const unackBySeverity = PROBLEM_SEVERITIES
    .map(name => ({ severity: name, count: counts.get(name) ?? 0 }))
    .filter(item => item.count > 0)

  return {
    severity: hasProblems ? severity : 'NORMAL',
    message: hasProblems ? 'Node has unacknowledged alarms.' : 'Node has no unacknowledged alarms.',
    hasProblems,
    unackBySeverity
  }
}
