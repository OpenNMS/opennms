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

// OnmsSeverity ordering + display metadata for dashboard panels. Hex colors
// approximate the OpenNMS standard severity palette (used directly by the
// canvas-based Status Overview chart, which can't read the SCSS .severity-*
// classes). Keep highest-severity-first.

export interface SeverityMeta {
  key: string
  label: string
  color: string
  weight: number
}

export const SEVERITIES: SeverityMeta[] = [
  { key: 'CRITICAL', label: 'Critical', color: '#a81d1d', weight: 7 },
  { key: 'MAJOR', label: 'Major', color: '#d4663a', weight: 6 },
  { key: 'MINOR', label: 'Minor', color: '#e89b3b', weight: 5 },
  { key: 'WARNING', label: 'Warning', color: '#f0c419', weight: 4 },
  { key: 'NORMAL', label: 'Normal', color: '#3a7d2c', weight: 3 },
  { key: 'INDETERMINATE', label: 'Indeterminate', color: '#808080', weight: 2 },
  { key: 'CLEARED', label: 'Cleared', color: '#9e9e9e', weight: 1 }
]

// Severities shown in the alarm Status Overview chart (the actionable set).
export const ALARM_CHART_SEVERITIES = ['CRITICAL', 'MAJOR', 'MINOR', 'WARNING', 'INDETERMINATE']

const FALLBACK: SeverityMeta = { key: 'UNKNOWN', label: 'Unknown', color: '#999999', weight: 0 }

export const severityMeta = (key: string): SeverityMeta =>
  SEVERITIES.find((s) => s.key === (key ?? '').toUpperCase()) ?? { ...FALLBACK, key, label: key || 'Unknown' }

export const severityColor = (key: string): string => severityMeta(key).color

export const severityLabel = (key: string): string => severityMeta(key).label

// Highest severity among a set of severity keys (by weight).
export const maxSeverity = (keys: string[]): string =>
  keys.reduce((max, k) => (severityMeta(k).weight > severityMeta(max).weight ? k : max), 'CLEARED')
