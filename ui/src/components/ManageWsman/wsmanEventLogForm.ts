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

import { WsmanEventLogConfig, WsmanEventLogLog, WsmanEventLogMapping, WsmanEventLogPackage } from '@/types/wsmanAdmin'

export const LEVEL_OPTIONS = ['Error', 'Warning', 'Information', 'AuditSuccess', 'AuditFailure']

export const MODE_OPTIONS = [
  { label: 'WQL (Win32_NTLogEvent, classic logs)', value: 'wql' },
  { label: 'Shell (Get-WinEvent, any log)', value: 'shell' }
]

export const SEVERITY_OPTIONS = ['Critical', 'Major', 'Minor', 'Warning', 'Normal', 'Cleared', 'Indeterminate']

export const INTERVAL_OPTIONS = [
  { label: 'Every 30 seconds', value: 30_000 },
  { label: 'Every minute', value: 60_000 },
  { label: 'Every 5 minutes', value: 300_000 },
  { label: 'Every 15 minutes', value: 900_000 },
  { label: 'Every hour', value: 3_600_000 }
]

export const DURATION = /^\s*\d+\s*[smhd]?\s*$/i

export const defaultLog = (): WsmanEventLogLog => ({
  name: '',
  enabled: true,
  interval: 60_000,
  maxRecords: 500,
  lookback: '1h',
  levels: 'Error,Warning',
  includeEventIds: null,
  excludeEventIds: null,
  mode: 'wql'
})

export const defaultMapping = (): WsmanEventLogMapping => ({
  logfile: null,
  source: null,
  eventId: 0,
  uei: 'uei.opennms.org/wsman/eventlog/',
  severity: null
})

export const splitCsv = (value: string | null | undefined): string[] =>
  (value ?? '').split(',').map(v => v.trim()).filter(Boolean)

export const joinCsv = (values: string[]): string | null => (values.length ? values.join(',') : null)

export const formatInterval = (ms: number): string => {
  const option = INTERVAL_OPTIONS.find(o => o.value === ms)
  if (option) {
    return option.label.replace('Every ', '')
  }
  return ms % 60_000 === 0 ? `${ms / 60_000} min` : `${ms / 1000} s`
}

// A deep copy the dialogs can edit without touching the store's document.
export const cloneConfig = (config: WsmanEventLogConfig): WsmanEventLogConfig => JSON.parse(JSON.stringify(config))

// Replaces (or appends) one log in a package, keyed by the original name.
export const upsertLog = (config: WsmanEventLogConfig, packageName: string, originalName: string | null, log: WsmanEventLogLog): WsmanEventLogConfig => {
  const next = cloneConfig(config)
  const pkg = next.packages.find(p => p.name === packageName)
  if (!pkg) {
    return next
  }
  const index = originalName === null ? -1 : pkg.logs.findIndex(l => l.name === originalName)
  if (index < 0) {
    pkg.logs.push(log)
  } else {
    pkg.logs[index] = log
  }
  return next
}

export const removeLog = (config: WsmanEventLogConfig, packageName: string, name: string): WsmanEventLogConfig => {
  const next = cloneConfig(config)
  const pkg = next.packages.find(p => p.name === packageName)
  if (pkg) {
    pkg.logs = pkg.logs.filter(l => l.name !== name)
  }
  return next
}

export const upsertMapping = (config: WsmanEventLogConfig, packageName: string, originalIndex: number | null, mapping: WsmanEventLogMapping): WsmanEventLogConfig => {
  const next = cloneConfig(config)
  const pkg = next.packages.find(p => p.name === packageName)
  if (!pkg) {
    return next
  }
  if (originalIndex === null || originalIndex < 0 || originalIndex >= pkg.eventMappings.length) {
    pkg.eventMappings.push(mapping)
  } else {
    pkg.eventMappings[originalIndex] = mapping
  }
  return next
}

export const removeMapping = (config: WsmanEventLogConfig, packageName: string, index: number): WsmanEventLogConfig => {
  const next = cloneConfig(config)
  const pkg = next.packages.find(p => p.name === packageName)
  if (pkg) {
    pkg.eventMappings.splice(index, 1)
  }
  return next
}

// Flips one log's enabled flag; the toggle in the table saves straight away.
export const toggleLog = (config: WsmanEventLogConfig, packageName: string, name: string, enabled: boolean): WsmanEventLogConfig => {
  const next = cloneConfig(config)
  const log = next.packages.find(p => p.name === packageName)?.logs.find(l => l.name === name)
  if (log) {
    log.enabled = enabled
  }
  return next
}

export const DEFAULT_FILTER = 'IPADDR != \'0.0.0.0\''

// A new package starts with the two logs every Windows host has, so it reads something at once.
export const defaultPackage = (): WsmanEventLogPackage => ({
  name: '',
  filter: DEFAULT_FILTER,
  logs: [
    { ...defaultLog(), name: 'System' },
    { ...defaultLog(), name: 'Application' }
  ],
  eventMappings: []
})

// Replaces (or appends) a package, keyed by its original name; logs and mappings travel with it.
export const upsertPackage = (config: WsmanEventLogConfig, originalName: string | null, pkg: WsmanEventLogPackage): WsmanEventLogConfig => {
  const next = cloneConfig(config)
  const index = originalName === null ? -1 : next.packages.findIndex(p => p.name === originalName)
  if (index < 0) {
    next.packages.push(pkg)
  } else {
    next.packages[index] = { ...next.packages[index], name: pkg.name, filter: pkg.filter }
  }
  return next
}

export const removePackage = (config: WsmanEventLogConfig, name: string): WsmanEventLogConfig => {
  const next = cloneConfig(config)
  next.packages = next.packages.filter(p => p.name !== name)
  return next
}

export const formatWhen = (ms: number | null | undefined): string => (ms ? new Date(ms).toLocaleString() : 'never')
