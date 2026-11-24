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

import type { OnmsTagSeverity } from '@opennms/onms-ui'

export type OnmsSeverityName = 'INDETERMINATE' | 'CLEARED' | 'NORMAL' | 'WARNING' | 'MINOR' | 'MAJOR' | 'CRITICAL'

/** Alarm severities as OpenNMS names them (OnmsSeverity), lowest to highest: the position plus 1 is the severity's id. */
export const ONMS_SEVERITIES: readonly OnmsSeverityName[] =
  ['INDETERMINATE', 'CLEARED', 'NORMAL', 'WARNING', 'MINOR', 'MAJOR', 'CRITICAL']

/** The severities that need attention: Warning and above, lowest first. */
export const PROBLEM_SEVERITIES: readonly OnmsSeverityName[] = ['WARNING', 'MINOR', 'MAJOR', 'CRITICAL']

const nameOf = (severity: string | null | undefined): OnmsSeverityName | undefined => {
  const upper = (severity ?? '').toUpperCase()

  return ONMS_SEVERITIES.find(name => name === upper)
}

/**
 * The severities with a look of their own: an icon, a color, a banner tint. Every other severity
 * (indeterminate, cleared, one that is unknown or missing) is `none`.
 */
export type SeverityKey = 'none' | 'normal' | 'warning' | 'minor' | 'major' | 'critical'

const KEYS = ['normal', 'warning', 'minor', 'major', 'critical'] as const

/** The look for a severity name, in any case; `none` for one with no look of its own. */
export const severityKey = (severity: string | null | undefined): SeverityKey => {
  const key = (severity ?? '').toLowerCase()

  return KEYS.find(k => k === key) ?? 'none'
}

/** A severity name for people to read, for example "Major"; "Unknown" for one OpenNMS does not have. */
export const severityName = (severity: string | null | undefined): string => {
  const upper = (severity ?? '').toUpperCase()

  return ONMS_SEVERITIES.some(name => name === upper) ? upper.charAt(0) + upper.slice(1).toLowerCase() : 'Unknown'
}

/** The OnmsSeverity id for a severity name, in any case, for example 6 for MAJOR; undefined for an unknown one. */
export const severityId = (severity: string | null | undefined): number | undefined => {
  const name = nameOf(severity)

  return name === undefined ? undefined : ONMS_SEVERITIES.indexOf(name) + 1
}

/** The OnmsSeverity id (1-7) for a severity name, in any case; 0 for an unknown or missing one. */
export const severityRank = (severity: string | null | undefined): number => severityId(severity) ?? 0

/** The highest of the severities, as an upper-case name; undefined when none is a severity OpenNMS has. */
export const highestSeverity = (severities: Iterable<string | null | undefined>): OnmsSeverityName | undefined => {
  let highest: OnmsSeverityName | undefined

  for (const severity of severities) {
    const name = nameOf(severity)

    if (name !== undefined && (highest === undefined || severityRank(name) > severityRank(highest))) {
      highest = name
    }
  }

  return highest
}

/** Whether the severity is Warning or above. */
export const isProblemSeverity = (severity: string | null | undefined): boolean => severityRank(severity) >= 4

const SEVERITY_TAGS: Record<OnmsSeverityName, OnmsTagSeverity> = {
  CRITICAL: 'danger',
  MAJOR: 'danger',
  MINOR: 'warn',
  WARNING: 'warn',
  NORMAL: 'success',
  CLEARED: 'success',
  INDETERMINATE: 'secondary'
}

/** The tag color for an event, alarm or notification severity (any case); grey for one OpenNMS does not have. */
export const severityTag = (severity: string | null | undefined): OnmsTagSeverity => {
  const name = nameOf(severity)

  return name === undefined ? 'secondary' : SEVERITY_TAGS[name]
}

/** The CSS custom property that holds the severity's color; the cleared one for an unknown or missing severity. */
export const severityColorVar = (severity: string | null | undefined): string =>
  `--onms-${(nameOf(severity) ?? 'CLEARED').toLowerCase()}`

/** The CSS class that tints a panel for the severity; undefined for an unknown or missing one. */
export const severityTintClass = (severity: string | null | undefined): string | undefined => {
  const name = nameOf(severity)

  return name === undefined ? undefined : `severity-tint--${name.toLowerCase()}`
}
