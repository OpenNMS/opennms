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

import { describe, expect, it } from 'vitest'
import { formatInterval, removeLog, removeMapping, toggleLog, upsertLog, upsertMapping } from '@/components/ManageWsman/wsmanEventLogForm'
import { WsmanEventLogConfig } from '@/types/wsmanAdmin'

const config = (): WsmanEventLogConfig => ({
  version: 'v1',
  threads: 4,
  retries: 1,
  targetRefreshInterval: '5m',
  packages: [{
    name: 'windows',
    filter: 'IPADDR != \'0.0.0.0\'',
    logs: [
      { name: 'System', enabled: true, interval: 60000, maxRecords: 500, lookback: '1h', levels: 'Error,Warning', includeEventIds: null, excludeEventIds: null, mode: 'wql' },
      { name: 'Security', enabled: false, interval: 60000, maxRecords: 500, lookback: '15m', levels: 'AuditFailure', includeEventIds: null, excludeEventIds: null, mode: 'wql' }
    ],
    eventMappings: [{ logfile: 'System', source: null, eventId: 6008, uei: 'uei.opennms.org/wsman/eventlog/unexpectedShutdown', severity: 'Major' }]
  }]
})

describe('wsmanEventLogForm', () => {
  it('upserts a log by its original name and appends a new one', () => {
    const original = config()
    const edited = upsertLog(original, 'windows', 'System', { ...original.packages[0].logs[0], interval: 300000 })
    expect(edited.packages[0].logs[0].interval).toBe(300000)
    expect(original.packages[0].logs[0].interval).toBe(60000)
    const added = upsertLog(original, 'windows', null, { ...original.packages[0].logs[0], name: 'Application' })
    expect(added.packages[0].logs.map(l => l.name)).toEqual(['System', 'Security', 'Application'])
  })

  it('removes and toggles a log without touching the source document', () => {
    const original = config()
    expect(removeLog(original, 'windows', 'Security').packages[0].logs.map(l => l.name)).toEqual(['System'])
    expect(toggleLog(original, 'windows', 'Security', true).packages[0].logs[1].enabled).toBe(true)
    expect(original.packages[0].logs).toHaveLength(2)
    expect(original.packages[0].logs[1].enabled).toBe(false)
  })

  it('upserts and removes mappings by index', () => {
    const original = config()
    const mapping = { logfile: null, source: null, eventId: 41, uei: 'uei.opennms.org/x', severity: null }
    expect(upsertMapping(original, 'windows', null, mapping).packages[0].eventMappings).toHaveLength(2)
    expect(upsertMapping(original, 'windows', 0, mapping).packages[0].eventMappings[0].eventId).toBe(41)
    expect(removeMapping(original, 'windows', 0).packages[0].eventMappings).toHaveLength(0)
  })

  it('formats the frequency for the table', () => {
    expect(formatInterval(60000)).toBe('minute')
    expect(formatInterval(300000)).toBe('5 minutes')
    expect(formatInterval(120000)).toBe('2 min')
    expect(formatInterval(45000)).toBe('45 s')
  })
})
