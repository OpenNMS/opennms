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

import { ISelectItemType } from '@/types'
import { describe, expect, it } from 'vitest'
import {
  AUTH_PROTOCOL_OPTIONS,
  MIN_PASSPHRASE_CHARACTERS,
  PRIVACY_PROTOCOL_OPTIONS,
  SECURITY_LEVEL_OPTIONS,
  validateSnmpV3UserForm
} from '@/lib/trapdValidator'

const createEmptySelectItem = (): ISelectItemType => (undefined as unknown as ISelectItemType)

// Minimal valid level-2 call: name + level + protocol + long-enough passphrase
const validAuthNoPriv = () =>
  validateSnmpV3UserForm('user', SECURITY_LEVEL_OPTIONS[1], AUTH_PROTOCOL_OPTIONS[0], 'longenough', createEmptySelectItem(), '')

// Minimal valid level-3 call: name + level + auth + privacy credentials
const validAuthPriv = () =>
  validateSnmpV3UserForm('user', SECURITY_LEVEL_OPTIONS[2], AUTH_PROTOCOL_OPTIONS[0], 'longenough', PRIVACY_PROTOCOL_OPTIONS[0], 'longenough')

describe('validateSnmpV3UserForm – security level cross-field rules', () => {
  it('reports error when level 1 has auth credentials', () => {
    const errors = validateSnmpV3UserForm(
      'new-user',
      SECURITY_LEVEL_OPTIONS[0],
      AUTH_PROTOCOL_OPTIONS[0],
      '',
      createEmptySelectItem(),
      ''
    )

    expect(errors.securityLevel).toBe('Security level 1 does not allow auth or privacy credentials')
  })

  it('reports error when level 1 has privacy credentials', () => {
    const errors = validateSnmpV3UserForm(
      'new-user',
      SECURITY_LEVEL_OPTIONS[0],
      createEmptySelectItem(),
      '',
      PRIVACY_PROTOCOL_OPTIONS[0],
      ''
    )

    expect(errors.securityLevel).toBe('Security level 1 does not allow auth or privacy credentials')
  })

  it('reports error when level 2 has privacy credentials', () => {
    const errors = validateSnmpV3UserForm(
      'new-user',
      SECURITY_LEVEL_OPTIONS[1],
      createEmptySelectItem(),
      '',
      PRIVACY_PROTOCOL_OPTIONS[0],
      ''
    )

    expect(errors.privacyProtocol).toBe('Security level 2 does not allow privacy credentials')
  })
})

describe('validateSnmpV3UserForm – auth-only security level (level 2)', () => {
  it('reports auth protocol but no authPassphrase errors when both are missing', () => {
    const errors = validateSnmpV3UserForm(
      'auth-only-user',
      SECURITY_LEVEL_OPTIONS[1],
      createEmptySelectItem(),
      '',
      createEmptySelectItem(),
      ''
    )

    expect(errors.authProtocol).toBeDefined()
    expect(errors.authPassphrase).toBeUndefined()
  })

  it('reports specific passphrase error when passphrase is set but protocol is cleared', () => {
    const errors = validateSnmpV3UserForm(
      'auth-user',
      SECURITY_LEVEL_OPTIONS[1],
      createEmptySelectItem(),
      'some-passphrase',
      createEmptySelectItem(),
      ''
    )

    expect(errors.authProtocol).toBe('Auth Protocol is required for selected security level')
    expect(errors.authPassphrase).toBe('Auth Passphrase requires an Auth Protocol to be selected')
  })

  it('reports generic protocol error but no authPassphrase error when protocol and passphrase are both missing', () => {
    const errors = validateSnmpV3UserForm(
      'auth-user',
      SECURITY_LEVEL_OPTIONS[1],
      createEmptySelectItem(),
      '',
      createEmptySelectItem(),
      ''
    )

    expect(errors.authProtocol).toBe('Auth Protocol is required for selected security level')
    expect(errors.authPassphrase).toBeUndefined()
  })

  it('accepts a valid level-2 configuration with no errors', () => {
    expect(validAuthNoPriv()).toEqual({})
  })
})

describe('validateSnmpV3UserForm – auth-priv security level (level 3)', () => {
  it('reports privacy protocol but no privacy passphrase errors when both are missing', () => {
    const errors = validateSnmpV3UserForm(
      'auth-priv-user',
      SECURITY_LEVEL_OPTIONS[2],
      AUTH_PROTOCOL_OPTIONS[0],
      'auth-secret',
      createEmptySelectItem(),
      ''
    )

    expect(errors.privacyProtocol).toBeDefined()
    expect(errors.privacyPassphrase).toBeUndefined()
  })

  it('reports specific privacy passphrase error when passphrase is set but protocol is cleared', () => {
    const errors = validateSnmpV3UserForm(
      'priv-user',
      SECURITY_LEVEL_OPTIONS[2],
      AUTH_PROTOCOL_OPTIONS[0],
      'auth-secret',
      createEmptySelectItem(),
      'privacy-secret'
    )

    expect(errors.privacyProtocol).toBe('Privacy Protocol is required for selected security level')
    expect(errors.privacyPassphrase).toBe('Privacy Passphrase requires a Privacy Protocol to be selected')
  })

  it('reports generic protocol but no privacy passphrase error when both protocol and passphrase are missing', () => {
    const errors = validateSnmpV3UserForm(
      'priv-user',
      SECURITY_LEVEL_OPTIONS[2],
      AUTH_PROTOCOL_OPTIONS[0],
      'auth-secret',
      createEmptySelectItem(),
      ''
    )

    expect(errors.privacyProtocol).toBe('Privacy Protocol is required for selected security level')
    expect(errors.privacyPassphrase).toBeUndefined()
  })

  it('accepts a valid level-3 configuration with no errors', () => {
    expect(validAuthPriv()).toEqual({})
  })
})

describe('validateSnmpV3UserForm – passphrase length and SCV', () => {
  it('reports error when auth passphrase is too short', () => {
    const errors = validateSnmpV3UserForm(
      'user',
      SECURITY_LEVEL_OPTIONS[1],
      AUTH_PROTOCOL_OPTIONS[0],
      'short',
      createEmptySelectItem(),
      ''
    )

    expect(errors.authPassphrase).toBe(`Auth Passphrase must be at least ${MIN_PASSPHRASE_CHARACTERS} characters`)
  })

  it('reports error when privacy passphrase is too short', () => {
    const errors = validateSnmpV3UserForm(
      'user',
      SECURITY_LEVEL_OPTIONS[2],
      AUTH_PROTOCOL_OPTIONS[0],
      'longenough',
      PRIVACY_PROTOCOL_OPTIONS[0],
      'short'
    )

    expect(errors.privacyPassphrase).toBe(`Privacy Passphrase must be at least ${MIN_PASSPHRASE_CHARACTERS} characters`)
  })

  it('accepts a masked auth passphrase without length validation', () => {
    const errors = validateSnmpV3UserForm(
      'user',
      SECURITY_LEVEL_OPTIONS[1],
      AUTH_PROTOCOL_OPTIONS[0],
      '******',
      createEmptySelectItem(),
      ''
    )

    expect(errors.authPassphrase).toBeUndefined()
  })

  it('accepts a masked privacy passphrase without length validation', () => {
    const errors = validateSnmpV3UserForm(
      'user',
      SECURITY_LEVEL_OPTIONS[2],
      AUTH_PROTOCOL_OPTIONS[0],
      'longenough',
      PRIVACY_PROTOCOL_OPTIONS[0],
      '******'
    )

    expect(errors.privacyPassphrase).toBeUndefined()
  })

  it('reports error when auth passphrase starts with \'*\' but is not the masked value', () => {
    const errors = validateSnmpV3UserForm(
      'user',
      SECURITY_LEVEL_OPTIONS[1],
      AUTH_PROTOCOL_OPTIONS[0],
      '*notmasked',
      createEmptySelectItem(),
      ''
    )

    expect(errors.authPassphrase).toBe('Auth Passphrase should not start with a \'*\' character.')
  })

  it('reports error when auth passphrase is a single \'*\'', () => {
    const errors = validateSnmpV3UserForm(
      'user',
      SECURITY_LEVEL_OPTIONS[1],
      AUTH_PROTOCOL_OPTIONS[0],
      '*',
      createEmptySelectItem(),
      ''
    )

    expect(errors.authPassphrase).toBe('Auth Passphrase should not start with a \'*\' character.')
  })

  it('reports error when privacy passphrase starts with \'*\' but is not the masked value', () => {
    const errors = validateSnmpV3UserForm(
      'user',
      SECURITY_LEVEL_OPTIONS[2],
      AUTH_PROTOCOL_OPTIONS[0],
      'longenough',
      PRIVACY_PROTOCOL_OPTIONS[0],
      '*notmasked'
    )

    expect(errors.privacyPassphrase).toBe('Privacy Passphrase should not start with a \'*\' character.')
  })

  it('reports error for invalid SCV expression in auth passphrase', () => {
    const errors = validateSnmpV3UserForm(
      'user',
      SECURITY_LEVEL_OPTIONS[1],
      AUTH_PROTOCOL_OPTIONS[0],
      '${scv:bad',
      createEmptySelectItem(),
      ''
    )

    expect(errors.authPassphrase).toBe('Invalid SCV expression')
  })

  it('reports error for invalid SCV expression in privacy passphrase', () => {
    const errors = validateSnmpV3UserForm(
      'user',
      SECURITY_LEVEL_OPTIONS[2],
      AUTH_PROTOCOL_OPTIONS[0],
      'longenough',
      PRIVACY_PROTOCOL_OPTIONS[0],
      '${scv:bad'
    )

    expect(errors.privacyPassphrase).toBe('Invalid SCV expression')
  })

  it('accepts a valid SCV expression in auth passphrase', () => {
    const errors = validateSnmpV3UserForm(
      'user',
      SECURITY_LEVEL_OPTIONS[1],
      AUTH_PROTOCOL_OPTIONS[0],
      '${scv:vault:auth-key}',
      createEmptySelectItem(),
      ''
    )

    expect(errors.authPassphrase).toBeUndefined()
  })
})
