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

import { v2 } from './axiosInstances'

// The status summary endpoints return c3-style [label, count] column pairs,
// e.g. [["Critical", 2], ["Minor", 1]]. Labels are title-case severity/status
// names (Normal/Warning/Minor/Major/Critical).
export interface StatusSummaryEntry {
  label: string
  count: number
}

const fetchSummary = async (path: string): Promise<StatusSummaryEntry[]> => {
  try {
    const resp = await v2.get(path)
    if (resp.status === 204 || !Array.isArray(resp.data)) {
      return []
    }
    return (resp.data as unknown[])
      .filter((row): row is [string, number] => Array.isArray(row) && row.length >= 2)
      .map(row => ({ label: String(row[0]), count: Number(row[1]) }))
  } catch (_err) {
    return []
  }
}

export const getNodesByAlarms = (): Promise<StatusSummaryEntry[]> =>
  fetchSummary('/status/summary/nodes/alarms')

export const getNodesByOutages = (): Promise<StatusSummaryEntry[]> =>
  fetchSummary('/status/summary/nodes/outages')

// Named status lists (business services / applications) — only those with a
// pending problem (severity worse than NORMAL), matching the legacy boxes.
export interface StatusListItem {
  id: number
  name: string
  severity: string
}

const PROBLEM_SEVERITIES = new Set(['WARNING', 'MINOR', 'MAJOR', 'CRITICAL'])

const severityString = (s: unknown): string =>
  (typeof s === 'string' ? s : ((s as any)?.label ?? (s as any)?.name ?? '')).toUpperCase()

const fetchStatusList = async (path: string, key: string): Promise<StatusListItem[]> => {
  try {
    const resp = await v2.get(path, { headers: { Accept: 'application/json' }})
    if (resp.status === 204 || !resp.data) {
      return []
    }
    const raw = resp.data[key]
    const arr = Array.isArray(raw) ? raw : raw ? [raw] : []
    return arr
      .map((x: any) => ({ id: Number(x.id), name: x.name, severity: severityString(x.severity) }))
      .filter((x: StatusListItem) => PROBLEM_SEVERITIES.has(x.severity))
  } catch (_err) {
    return []
  }
}

export const getBusinessServicesStatus = (): Promise<StatusListItem[]> =>
  fetchStatusList('/status/business-services', 'businessservice')

export const getApplicationsStatus = (): Promise<StatusListItem[]> =>
  fetchStatusList('/status/applications', 'application')
