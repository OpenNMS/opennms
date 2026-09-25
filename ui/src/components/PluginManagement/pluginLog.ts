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

import { type OnmsTagSeverity } from '@opennms/onms-ui'

// One line of plugin-management.log. The server writes
// "<timestamp> <LEVEL> action=… user=… remote=… kar=… sha256=… outcome=… <detail>";
// a line in any other shape is kept raw so nothing is hidden from the table.
export interface PluginLogEntry {
  id: number
  raw: string
  parsed: boolean
  time: string
  level: string
  action: string
  user: string
  remote: string
  kar: string
  sha256: string
  outcome: string
  detail: string
}

export const LOG_ACTIONS = ['check', 'install', 'unload']
export const LOG_OUTCOMES = ['ok', 'rejected', 'refused', 'error']

const LINE = /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}(?:[,.]\d{1,3})?)\s+([A-Z]+)\s+action=(\S+)\s+user=(\S+)\s+remote=(\S+)\s+kar=(\S+)\s+sha256=(\S+)\s+outcome=(\S+)\s*(.*)$/

// the server prints an absent value as the word null
const field = (value: string): string => (value === 'null' ? '' : value)

export const parsePluginLogLine = (line: string, id = 0): PluginLogEntry => {
  const raw = line.replace(/\r$/, '')
  const m = LINE.exec(raw)
  if (!m) {
    return { id, raw, parsed: false, time: '', level: '', action: '', user: '', remote: '', kar: '', sha256: '', outcome: '', detail: '' }
  }
  return {
    id,
    raw,
    parsed: true,
    time: m[1],
    level: m[2],
    action: m[3],
    user: field(m[4]),
    remote: field(m[5]),
    kar: field(m[6]),
    sha256: field(m[7]),
    outcome: m[8],
    detail: m[9].trim()
  }
}

// blank lines are dropped; ids follow the order the server returned
export const parsePluginLog = (text: string): PluginLogEntry[] =>
  text.split('\n').filter(line => line.trim() !== '').map((line, index) => parsePluginLogLine(line, index))

export const levelSeverity = (level: string): OnmsTagSeverity => {
  switch (level.toUpperCase()) {
    case 'WARN': return 'warn'
    case 'ERROR': return 'danger'
    default: return 'secondary'
  }
}

export const outcomeSeverity = (outcome: string): OnmsTagSeverity => {
  switch (outcome.toLowerCase()) {
    case 'ok': return 'success'
    case 'rejected':
    case 'refused': return 'warn'
    case 'error': return 'danger'
    default: return 'secondary'
  }
}

export const isFailedEntry = (entry: PluginLogEntry): boolean => {
  const level = entry.level.toUpperCase()
  const outcome = entry.outcome.toLowerCase()
  return level === 'WARN' || level === 'ERROR' || outcome === 'refused' || outcome === 'error'
}

// case-insensitive match against every column, the raw line included
export const entryMatches = (entry: PluginLogEntry, search: string): boolean => {
  const needle = search.trim().toLowerCase()
  return needle === '' || entry.raw.toLowerCase().includes(needle)
}

export const distinctKars = (entries: PluginLogEntry[]): string[] =>
  Array.from(new Set(entries.map(e => e.kar).filter(k => k !== ''))).sort((a, b) => a.localeCompare(b))
