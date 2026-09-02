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
import {
  configToInput,
  formToInput,
  isIpAddress,
  isIplikePattern,
  rangeProblem,
  settingsToForm,
  validateSettingsForm
} from '@/components/ManageWsman/wsmanForm'

const SETTINGS = {
  retry: 1, timeout: 30000, username: 'root', hasPassword: true, port: null, maxElements: null,
  ssl: true, strictSsl: false, path: '/wsman', productVendor: null, productVersion: null, gssAuth: null
}

describe('wsmanForm', () => {
  it('round-trips settings through the form, keeping the stored password by default', () => {
    const form = settingsToForm(SETTINGS)
    expect(form.ssl).toBe('true')
    expect(form.strictSsl).toBe('false')
    expect(form.gssAuth).toBe('unset')
    expect(form.password).toBe('')
    const input = formToInput(form)
    expect(input).toMatchObject({ username: 'root', ssl: true, strictSsl: false, gssAuth: null, path: '/wsman', password: null, clearPassword: false })
    expect('hasPassword' in input).toBe(false)
  })

  it('sends a typed password and blanks as null', () => {
    const input = formToInput({ ...settingsToForm(SETTINGS), password: 'new-one', username: '   ', productVendor: ' Dell ' })
    expect(input.password).toBe('new-one')
    expect(input.username).toBeNull()
    expect(input.productVendor).toBe('Dell')
  })

  it('carries every loaded definition with its index so stored passwords survive', () => {
    const input = configToInput({
      defaults: SETTINGS,
      definitions: [
        { ...SETTINGS, ranges: [{ begin: '10.0.0.1', end: '10.0.0.9' }], specifics: [], ipMatches: [] },
        { ...SETTINGS, ranges: [], specifics: ['10.1.1.1'], ipMatches: ['10.2.*.*'] }
      ]
    })
    expect(input.definitions.map(d => d.sourceIndex)).toEqual([0, 1])
    expect(input.definitions.every(d => d.password === null && !d.clearPassword)).toBe(true)
    expect(input.definitions[1].ipMatches).toEqual(['10.2.*.*'])
  })

  it('flags out-of-range settings and a conflicting password request', () => {
    const errors = validateSettingsForm({ ...settingsToForm(SETTINGS), port: 70000, timeout: 0, retry: -1, path: 'wsman x', password: 'x', clearPassword: true })
    expect(Object.keys(errors).sort()).toEqual(['password', 'path', 'port', 'retry', 'timeout'])
    expect(validateSettingsForm(settingsToForm(SETTINGS))).toEqual({})
  })

  it('checks address shapes, range order and IPLIKE patterns', () => {
    expect(isIpAddress('10.0.0.255')).toBe(true)
    expect(isIpAddress('10.0.0.256')).toBe(false)
    expect(isIpAddress('fe80::1')).toBe(true)
    expect(rangeProblem('10.0.0.1', '10.0.0.50')).toBeNull()
    expect(rangeProblem('10.0.0.50', '10.0.0.1')).toContain('before')
    expect(rangeProblem('10.0.0.1', 'fe80::1')).toContain('mix')
    expect(isIplikePattern('10.0.*.*')).toBe(true)
    expect(isIplikePattern('10.0.1-5,9.*')).toBe(true)
    expect(isIplikePattern('10.0.*')).toBe(false)
    expect(isIplikePattern('fe80:*:*:*:*:*:*:*')).toBe(true)
    expect(isIplikePattern('not a pattern')).toBe(false)
  })
})
