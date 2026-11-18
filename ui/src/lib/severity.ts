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

// Alarm severities as OpenNMS names them (OnmsSeverity), lowest to highest.
const ONMS_SEVERITIES = ['INDETERMINATE', 'CLEARED', 'NORMAL', 'WARNING', 'MINOR', 'MAJOR', 'CRITICAL']

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

  return ONMS_SEVERITIES.includes(upper) ? upper.charAt(0) + upper.slice(1).toLowerCase() : 'Unknown'
}
