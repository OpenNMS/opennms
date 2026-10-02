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

import { Alarm } from '@/types'

// The tints NodeDetailsBanner offers. Indeterminate and cleared are never a node's status here --
// they are not problems -- so they have none of their own.
export type BannerSeverity = 'none' | 'normal' | 'warning' | 'minor' | 'major' | 'critical'

/** The banner tint for an OnmsSeverity name; `none` for one that has no tint. */
export const bannerSeverity = (severity: string | undefined): BannerSeverity => {
  const key = (severity ?? '').toLowerCase()

  return (['normal', 'warning', 'minor', 'major', 'critical'] as const).find(s => s === key) ?? 'none'
}

/**
 * A node's at-a-glance status, worked out from its alarms the way the legacy
 * includes/nodeStatus-box.jsp did.
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

// OnmsSeverity ids. Only the order matters here, but it has to be the real order: the JSP compares
// the ids directly.
const SEVERITY_IDS: Record<string, number> = {
  INDETERMINATE: 1,
  CLEARED: 2,
  NORMAL: 3,
  WARNING: 4,
  MINOR: 5,
  MAJOR: 6,
  CRITICAL: 7
}

const NORMAL = SEVERITY_IDS.NORMAL

const severityId = (severity: string | undefined): number => SEVERITY_IDS[(severity ?? '').toUpperCase()] ?? 0

const plural = (count: number, singular: string, pluralForm = `${singular}s`) => `${count} ${count === 1 ? singular : pluralForm}`

export const computeNodeStatus = (alarms: Alarm[]): NodeStatus => {
  let maxSeverity = NORMAL
  let ackCount = 0
  let unackCount = 0
  let nodeDown = false
  let intfDown = 0
  let servDown = 0

  for (const alarm of alarms) {
    const id = severityId(alarm.severity)

    // Indeterminate, cleared and normal alarms are not problems.
    if (id <= NORMAL) {
      continue
    }

    const uei = alarm.uei ?? ''

    if (uei.includes('nodeDown')) {
      nodeDown = true
    }

    if (uei.includes('interfaceDown')) {
      intfDown++
    }

    if (uei.includes('nodeLostService')) {
      servDown++
    }

    const acknowledged = alarm.ackTime != null

    if (acknowledged) {
      ackCount++
    } else {
      unackCount++

      // As in the JSP, an acknowledged problem is counted but does not colour the status: someone
      // has already taken it on.
      if (id > maxSeverity) {
        maxSeverity = id
      }
    }
  }

  const severity = Object.keys(SEVERITY_IDS).find(key => SEVERITY_IDS[key] === maxSeverity) ?? 'NORMAL'
  const hasProblems = maxSeverity > NORMAL

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

  return { severity, message, hasProblems, ackCount, unackCount }
}
