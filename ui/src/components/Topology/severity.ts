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

import { numericSeverityLevel } from '@/components/Map/utils'

/**
 * Node color when no alarm status applies (no severity, or status hasn't
 * been fetched). Matches the default placed-node color on the canvas.
 */
export const DEFAULT_NODE_COLOR = '#1f5fb0'

/**
 * OpenNMS severity -> canvas node color. WebGL nodes need a concrete hex
 * (the geomap colors its markers via CSS classes instead), so this is the
 * topology canvas's own mapping, keyed by upper-cased severity name.
 */
const SEVERITY_COLORS: Record<string, string> = {
  CRITICAL: '#c9292c',
  MAJOR: '#ff8d00',
  MINOR: '#ffcc00',
  WARNING: '#fff200',
  NORMAL: '#3bb44a',
  INDETERMINATE: '#999999',
  CLEARED: '#bdbdbd'
}

/** Color for a single severity, falling back to the default node color. */
export const severityColor = (severity?: string): string => {
  if (!severity) {
    return DEFAULT_NODE_COLOR
  }
  return SEVERITY_COLORS[severity.toUpperCase()] ?? DEFAULT_NODE_COLOR
}

/**
 * The most severe of a list of severity names (by OpenNMS severity rank),
 * or undefined for an empty list. Used to roll a node's alarms up to a
 * single status color.
 */
export const highestSeverity = (severities: Array<string | undefined>): string | undefined => {
  let best: string | undefined
  let bestLevel = -1
  for (const s of severities) {
    if (!s) {
      continue
    }
    const level = numericSeverityLevel(s)
    if (level > bestLevel) {
      bestLevel = level
      best = s
    }
  }
  return best
}

/** Minimal alarm shape needed to roll severities up per node. */
export interface NodeSeverityAlarm {
  nodeId?: number
  severity?: string
}

/**
 * Reduce a flat list of alarms to a map of node id -> highest severity.
 * Alarms without a node id are ignored. Pure (no I/O) so it can be tested
 * directly and reused regardless of how the alarms were fetched.
 */
export const aggregateNodeSeverities = (
  alarms: NodeSeverityAlarm[]
): Record<number, string> => {
  const byNode: Record<number, string> = {}
  for (const alarm of alarms) {
    if (alarm.nodeId == null || !alarm.severity) {
      continue
    }
    const current = byNode[alarm.nodeId]
    if (!current || numericSeverityLevel(alarm.severity) > numericSeverityLevel(current)) {
      byNode[alarm.nodeId] = alarm.severity
    }
  }
  return byNode
}
