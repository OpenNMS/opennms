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

import { describe, it, expect } from 'vitest'
import { withDefaultHints } from '@/lib/snmpConfigHelpers'
import { SnmpBaseConfiguration, SnmpFieldInfo } from '@/types/snmpConfig'

const defaults: SnmpBaseConfiguration = {
  timeout: 3000,
  retry: 1,
  port: 161,
  readCommunity: 'public',
  writeCommunity: 'private',
  authPassphrase: '0p3nNMSv3',
  privacyPassphrase: '0p3nNMSv3'
}

const field = (overrides: Partial<SnmpFieldInfo>): SnmpFieldInfo => ({
  key: 'timeout',
  label: 'Timeout',
  dataTest: 'test-timeout',
  ...overrides
})

describe('withDefaultHints', () => {
  it('appends default value to existing hint', () => {
    const result = withDefaultHints([
      field({ key: 'timeout', hint: 'Timeout in milliseconds' })
    ], defaults)
    expect(result[0].hint).toBe('Timeout in milliseconds. Current default value is: \'3000\'.')
  })

  it('sets hint when field has no existing hint', () => {
    const result = withDefaultHints([
      field({ key: 'port', hint: undefined })
    ], defaults)
    expect(result[0].hint).toBe('Current default value is: \'161\'.')
  })

  it('does not modify hint when default value is undefined', () => {
    const result = withDefaultHints([
      field({ key: 'proxyHost', hint: 'Proxy host' })
    ], defaults)
    expect(result[0].hint).toBe('Proxy host')
  })

  it('does not modify hint when default value is null', () => {
    const nullDefaults = { ...defaults, timeout: null as unknown as number }
    const result = withDefaultHints([
      field({ key: 'timeout', hint: 'Timeout in milliseconds' })
    ], nullDefaults)
    expect(result[0].hint).toBe('Timeout in milliseconds')
  })

  it('does not modify hint when default value is empty string', () => {
    const emptyDefaults = { ...defaults, readCommunity: '' }
    const result = withDefaultHints([
      field({ key: 'readCommunity', hint: 'Read community string' })
    ], emptyDefaults)
    expect(result[0].hint).toBe('Read community string')
  })

  it('does not append hint when skipDefaultHint is true', () => {
    const result = withDefaultHints([
      field({ key: 'authPassphrase', hint: 'Authentication passphrase', skipDefaultHint: true })
    ], defaults)
    expect(result[0].hint).toBe('Authentication passphrase')
  })

  it('suppresses hint entirely when skipDefaultHint is true and no existing hint', () => {
    const result = withDefaultHints([
      field({ key: 'privacyPassphrase', hint: undefined, skipDefaultHint: true })
    ], defaults)
    expect(result[0].hint).toBeUndefined()
  })

  it('does not expose passphrase values in hints', () => {
    const result = withDefaultHints([
      field({ key: 'authPassphrase', hint: 'Authentication passphrase', skipDefaultHint: true }),
      field({ key: 'privacyPassphrase', hint: 'Privacy passphrase', skipDefaultHint: true })
    ], defaults)
    expect(result[0].hint).not.toContain('0p3nNMSv3')
    expect(result[1].hint).not.toContain('0p3nNMSv3')
  })

  it('returns original field reference when no modification is needed', () => {
    const original = field({ key: 'proxyHost', hint: 'Proxy host' })
    const result = withDefaultHints([original], defaults)
    expect(result[0]).toBe(original)
  })

  it('returns a new object when hint is modified', () => {
    const original = field({ key: 'timeout', hint: 'Timeout in milliseconds' })
    const result = withDefaultHints([original], defaults)
    expect(result[0]).not.toBe(original)
  })

  it('processes multiple fields correctly', () => {
    const result = withDefaultHints([
      field({ key: 'timeout', hint: 'Timeout in milliseconds' }),
      field({ key: 'retry', hint: 'Number of retries' }),
      field({ key: 'authPassphrase', hint: 'Authentication passphrase', skipDefaultHint: true })
    ], defaults)
    expect(result[0].hint).toBe('Timeout in milliseconds. Current default value is: \'3000\'.')
    expect(result[1].hint).toBe('Number of retries. Current default value is: \'1\'.')
    expect(result[2].hint).toBe('Authentication passphrase')
  })
})
