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

import { formatInDisplayZone } from '@/lib/displayTimeZone'
import { isValid, parseISO } from 'date-fns'
import { KarCheckLevel, PluginStatus } from '@/types/pluginManagement'
import { type OnmsTagSeverity } from '@opennms/onms-ui'

export const NOT_SET = '—'

export const CONTAINER_UNAVAILABLE = 'The plugin container is not available, so plugins cannot be loaded or unloaded.'

export const formatSize = (bytes: number | null | undefined): string => {
  if (bytes === null || bytes === undefined || !Number.isFinite(bytes) || bytes < 0) {
    return NOT_SET
  }
  if (bytes < 1024) {
    return `${bytes} B`
  }
  const units = ['KB', 'MB', 'GB']
  let value = bytes / 1024
  let unit = 0
  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024
    unit++
  }
  return `${value.toFixed(value >= 100 ? 0 : 1)} ${units[unit]}`
}

export const shortSha = (sha256: string | null | undefined): string => sha256 ? sha256.slice(0, 12) : NOT_SET

// epoch milliseconds, a numeric string, or an ISO-8601 timestamp
export const formatUploadedAt = (value: number | string | null | undefined): string => {
  if (value === null || value === undefined || value === '') {
    return NOT_SET
  }
  const date = typeof value === 'number' ? new Date(value) : /^\d+$/.test(value) ? new Date(Number(value)) : parseISO(value)
  return isValid(date) ? formatInDisplayZone(date) : NOT_SET
}

export const CHECK_TAG: Record<KarCheckLevel, OnmsTagSeverity> = { PASS: 'success', WARN: 'warn', FAIL: 'danger' }

export interface StatusPresentation {
  severity: OnmsTagSeverity
  label: string
  hint: string
  title: string
}

export const STATUS_TAG: Record<PluginStatus, StatusPresentation> = {
  installed: { severity: 'success', label: 'installed', hint: '', title: 'loaded and running' },
  staged: { severity: 'warn', label: 'staged', hint: 'restart required', title: 'written to deploy/; its features start on the next restart' },
  unloaded: { severity: 'secondary', label: 'unloaded', hint: 'restart required', title: 'removed from deploy/; the next restart completes the removal' },
  unmanaged: { severity: 'info', label: 'unmanaged', hint: '', title: 'found in deploy/ but not loaded through this page' },
  unknown: { severity: 'secondary', label: 'unknown', hint: '', title: 'the container could not be reached, so the state of this plugin is not known' }
}

export const statusOf = (status: string): StatusPresentation => STATUS_TAG[status as PluginStatus] ?? { severity: 'secondary', label: status, hint: '', title: '' }
