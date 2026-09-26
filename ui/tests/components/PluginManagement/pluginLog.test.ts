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
import { distinctKars, entryMatches, isFailedEntry, levelSeverity, outcomeSeverity, parsePluginLog, parsePluginLogLine } from '@/components/PluginManagement/pluginLog'
import { describe, expect, it } from 'vitest'

const OK = '2026-09-25 11:18:06,596 INFO  action=install user=admin remote=10.0.0.5 kar=alec sha256=661bb88f3bd6 outcome=ok features=alec autoStart=false warnsAcknowledged=1'
const REJECTED = '2026-09-25 11:18:06,659 WARN  action=unload user=admin remote=unknown kar=null sha256=null outcome=rejected invalid kar name'
const ERROR = '2026-09-25 11:18:06,672 ERROR action=install user=bob remote=unknown kar=alec sha256=8a67 outcome=error java.nio.file.FileSystemException: boom'
const BARE = '2026-09-25 11:18:07,001 INFO  action=check user=admin remote=unknown kar=alec sha256=abc outcome=ok'

describe('pluginLog', () => {
  it('parses every audit field and blanks the null placeholders', () => {
    expect(parsePluginLogLine(OK, 3)).toEqual({
      id: 3, raw: OK, parsed: true, time: '2026-09-25 11:18:06,596', level: 'INFO', action: 'install', user: 'admin', remote: '10.0.0.5',
      kar: 'alec', sha256: '661bb88f3bd6', outcome: 'ok', detail: 'features=alec autoStart=false warnsAcknowledged=1'
    })
    expect(parsePluginLogLine(REJECTED)).toMatchObject({ level: 'WARN', action: 'unload', kar: '', sha256: '', outcome: 'rejected', detail: 'invalid kar name' })
    expect(parsePluginLogLine(ERROR)).toMatchObject({ level: 'ERROR', user: 'bob', outcome: 'error', detail: 'java.nio.file.FileSystemException: boom' })
    expect(parsePluginLogLine(BARE + '\r')).toMatchObject({ parsed: true, raw: BARE, detail: '' })
  })

  it('keeps a line it cannot parse as raw text', () => {
    const entry = parsePluginLogLine('2026-09-25 11:18:06,596 INFO  something else entirely', 7)
    expect(entry).toMatchObject({ id: 7, parsed: false, raw: '2026-09-25 11:18:06,596 INFO  something else entirely', time: '', level: '', kar: '', outcome: '' })
    expect(parsePluginLogLine('').parsed).toBe(false)
  })

  it('splits the file, drops blank lines and numbers the entries in file order', () => {
    const entries = parsePluginLog(`${OK}\n\n${REJECTED}\nnot a log line\n`)
    expect(entries.map(e => e.id)).toEqual([0, 1, 2])
    expect(entries.map(e => e.parsed)).toEqual([true, true, false])
    expect(parsePluginLog('')).toEqual([])
  })

  it('maps levels and outcomes to tag severities', () => {
    expect(levelSeverity('INFO')).toBe('secondary')
    expect(levelSeverity('WARN')).toBe('warn')
    expect(levelSeverity('ERROR')).toBe('danger')
    expect(levelSeverity('debug')).toBe('secondary')
    expect(outcomeSeverity('ok')).toBe('success')
    expect(outcomeSeverity('rejected')).toBe('warn')
    expect(outcomeSeverity('refused')).toBe('warn')
    expect(outcomeSeverity('error')).toBe('danger')
    expect(outcomeSeverity('')).toBe('secondary')
  })

  it('flags failed entries and searches across the whole line', () => {
    expect(isFailedEntry(parsePluginLogLine(OK))).toBe(false)
    expect(isFailedEntry(parsePluginLogLine(REJECTED))).toBe(true)
    expect(isFailedEntry(parsePluginLogLine(ERROR))).toBe(true)
    expect(isFailedEntry({ ...parsePluginLogLine(OK), level: 'INFO', outcome: 'refused' })).toBe(true)
    expect(isFailedEntry(parsePluginLogLine('garbage'))).toBe(false)
    const ok = parsePluginLogLine(OK)
    expect(entryMatches(ok, '')).toBe(true)
    expect(entryMatches(ok, 'AUTOSTART=FALSE')).toBe(true)
    expect(entryMatches(ok, '10.0.0.5')).toBe(true)
    expect(entryMatches(ok, 'bob')).toBe(false)
  })

  it('lists the distinct plugin names sorted', () => {
    const entries = parsePluginLog([ERROR, REJECTED, OK, OK.replace('kar=alec', 'kar=Zeta'), OK.replace('kar=alec', 'kar=beta')].join('\n'))
    expect(distinctKars(entries)).toEqual(['alec', 'beta', 'Zeta'])
  })
})
