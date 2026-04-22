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

import { TrapConfig, XmlValidationError, XmlValidationResult } from '@/types/trapConfig'
import { ISelectItemType } from '@featherds/select'
import { DEFAULT_TRAPD_BIND_ADDRESS } from './constants'

export const MIN_PORT = 1
export const MAX_PORT = 65535
export const MIN_PASSPHRASE_BYTES = 8
export const TRAPD_XML_NAMESPACE = 'http://xmlns.opennms.org/xsd/config/trapd'

export const passphraseByteLength = (value: string): number =>
  new TextEncoder().encode(value).length

export enum SecurityLevel {
  None = 0,
  NoAuthNoPriv = 1,
  AuthNoPriv = 2,
  AuthPriv = 3
}

export enum AuthProtocol {
  MD5 = 'MD5',
  SHA = 'SHA',
  SHA224 = 'SHA-224',
  SHA256 = 'SHA-256',
  SHA512 = 'SHA-512'
}

export enum PrivacyProtocol {
  DES = 'DES',
  AES = 'AES',
  AES192 = 'AES192',
  AES256 = 'AES256'
}

const VALID_SECURITY_LEVELS = [SecurityLevel.NoAuthNoPriv, SecurityLevel.AuthNoPriv, SecurityLevel.AuthPriv]

export const SECURITY_LEVEL_OPTIONS: ISelectItemType[] = [
  { _text: 'No Auth (1)', _value: String(SecurityLevel.NoAuthNoPriv) },
  { _text: 'Auth Only (2)', _value: String(SecurityLevel.AuthNoPriv) },
  { _text: 'Auth and Privacy (3)', _value: String(SecurityLevel.AuthPriv) }
]

export const AuthProtocols = [
  AuthProtocol.MD5,
  AuthProtocol.SHA,
  AuthProtocol.SHA224,
  AuthProtocol.SHA256,
  AuthProtocol.SHA512
]

export const PrivacyProtocols = [
  PrivacyProtocol.DES,
  PrivacyProtocol.AES,
  PrivacyProtocol.AES192,
  PrivacyProtocol.AES256
]

export const isValidSnmpSecurityLevel = (level: number | undefined): boolean => {
  return level !== undefined && VALID_SECURITY_LEVELS.includes(level)
}

export const isValidIP = (ip: string): boolean => {
  const parts = ip.split('.')
  if (parts.length !== 4) {
    return false
  }
  return parts.every((part) => {
    const num = parseInt(part, 10)
    return !isNaN(num) && num >= 0 && num <= 255
  })
}

export const isValidPort = (port: number | undefined): boolean => {
  return port !== undefined && !isNaN(port) && port >= MIN_PORT && port <= MAX_PORT
}

export const AUTH_PROTOCOL_OPTIONS: ISelectItemType[] = AuthProtocols.map((protocol) => ({
  _text: protocol,
  _value: protocol
}))

export const PRIVACY_PROTOCOL_OPTIONS: ISelectItemType[] = PrivacyProtocols.map((protocol) => ({
  _text: protocol,
  _value: protocol
}))

export const getDefaultTrapdConfig = (): TrapConfig => ({
  snmpTrapAddress: DEFAULT_TRAPD_BIND_ADDRESS,
  snmpTrapPort: 10162,
  newSuspectOnTrap: false,
  includeRawMessage: false,
  threads: 0,
  queueSize: 10000,
  batchSize: 1000,
  batchInterval: 500,
  useAddressFromVarbind: false,
  snmpv3User: []
})

// All valid auth protocol values. Keep these aligned with trapd-configuration.xsd.
const VALID_AUTH_PROTOCOL_VALUES = new Set(AuthProtocols)

// All valid privacy protocol values
const VALID_PRIVACY_PROTOCOL_VALUES = new Set(PrivacyProtocols as string[])

const addError = (errors: XmlValidationError[], field: string, message: string) => errors.push({ field, message })

const validateSnmpV3UserElement = (user: Element, index: number, errors: XmlValidationError[]): void => {
  const prefix = `snmpv3-user[${index}]`

  const securityName = user.getAttribute('security-name')
  if (!securityName || securityName.trim() === '') {
    addError(errors, `${prefix}.security-name`, `${prefix}: security-name is required`)
  }

  const securityLevelAttr = user.getAttribute('security-level')
  let securityLevel: number | undefined
  if (securityLevelAttr !== null && securityLevelAttr.trim() !== '') {
    securityLevel = parseInt(securityLevelAttr, 10)
    if (!isValidSnmpSecurityLevel(securityLevel)) {
      addError(
        errors,
        `${prefix}.security-level`,
        `${prefix}: invalid security-level '${securityLevelAttr}'. Valid values: 1 (NoAuthNoPriv), 2 (AuthNoPriv), 3 (AuthPriv)`
      )
      securityLevel = undefined
    }
  }

  const authProtocol = user.getAttribute('auth-protocol')
  const authPassphrase = user.getAttribute('auth-passphrase')
  const privacyProtocol = user.getAttribute('privacy-protocol')
  const privacyPassphrase = user.getAttribute('privacy-passphrase')

  if (authPassphrase && authPassphrase.trim() !== ''
      && passphraseByteLength(authPassphrase) < MIN_PASSPHRASE_BYTES) {
    addError(
      errors,
      `${prefix}.auth-passphrase`,
      `${prefix}: auth-passphrase must be at least ${MIN_PASSPHRASE_BYTES} bytes`
    )
  }
  if (privacyPassphrase && privacyPassphrase.trim() !== ''
      && passphraseByteLength(privacyPassphrase) < MIN_PASSPHRASE_BYTES) {
    addError(
      errors,
      `${prefix}.privacy-passphrase`,
      `${prefix}: privacy-passphrase must be at least ${MIN_PASSPHRASE_BYTES} bytes`
    )
  }

  if (authProtocol !== null) {
    if (!VALID_AUTH_PROTOCOL_VALUES.has(authProtocol as AuthProtocol)) {
      addError(
        errors,
        `${prefix}.auth-protocol`,
        `${prefix}: invalid auth-protocol '${authProtocol}'. Valid values: ${AuthProtocols.join(', ')}`
      )
    }
    if (!authPassphrase || authPassphrase.trim() === '') {
      addError(errors, `${prefix}.auth-passphrase`, `${prefix}: auth-passphrase is required when auth-protocol is set`)
    }
  }

  if (privacyProtocol !== null) {
    if (!VALID_PRIVACY_PROTOCOL_VALUES.has(privacyProtocol)) {
      addError(
        errors,
        `${prefix}.privacy-protocol`,
        `${prefix}: invalid privacy-protocol '${privacyProtocol}'. Valid values: ${PrivacyProtocols.join(', ')}`
      )
    }
    if (!privacyPassphrase || privacyPassphrase.trim() === '') {
      addError(
        errors,
        `${prefix}.privacy-passphrase`,
        `${prefix}: privacy-passphrase is required when privacy-protocol is set`
      )
    }
    if (authProtocol === null) {
      addError(errors, `${prefix}.auth-protocol`, `${prefix}: auth-protocol is required when privacy-protocol is set`)
    }
  }

  if (securityLevel === SecurityLevel.NoAuthNoPriv) {
    if (authProtocol !== null) {
      addError(
        errors,
        `${prefix}.auth-protocol`,
        `${prefix}: auth-protocol must not be set when security-level is 1 (NoAuthNoPriv)`
      )
    }
    if (authPassphrase !== null) {
      addError(
        errors,
        `${prefix}.auth-passphrase`,
        `${prefix}: auth-passphrase must not be set when security-level is 1 (NoAuthNoPriv)`
      )
    }
    if (privacyProtocol !== null) {
      addError(
        errors,
        `${prefix}.privacy-protocol`,
        `${prefix}: privacy-protocol must not be set when security-level is 1 (NoAuthNoPriv)`
      )
    }
    if (privacyPassphrase !== null) {
      addError(
        errors,
        `${prefix}.privacy-passphrase`,
        `${prefix}: privacy-passphrase must not be set when security-level is 1 (NoAuthNoPriv)`
      )
    }
  }

  if (securityLevel === SecurityLevel.AuthNoPriv) {
    if (authProtocol === null) {
      addError(
        errors,
        `${prefix}.auth-protocol`,
        `${prefix}: auth-protocol is required when security-level is 2 (AuthNoPriv)`
      )
    }
    if (!authPassphrase || authPassphrase.trim() === '') {
      addError(
        errors,
        `${prefix}.auth-passphrase`,
        `${prefix}: auth-passphrase is required when security-level is 2 (AuthNoPriv)`
      )
    }
    if (privacyProtocol !== null) {
      addError(
        errors,
        `${prefix}.privacy-protocol`,
        `${prefix}: privacy-protocol must not be set when security-level is 2 (AuthNoPriv)`
      )
    }
    if (privacyPassphrase !== null) {
      addError(
        errors,
        `${prefix}.privacy-passphrase`,
        `${prefix}: privacy-passphrase must not be set when security-level is 2 (AuthNoPriv)`
      )
    }
  }

  if (securityLevel === SecurityLevel.AuthPriv) {
    if (authProtocol === null) {
      addError(
        errors,
        `${prefix}.auth-protocol`,
        `${prefix}: auth-protocol is required when security-level is 3 (AuthPriv)`
      )
    }
    if (!authPassphrase || authPassphrase.trim() === '') {
      addError(
        errors,
        `${prefix}.auth-passphrase`,
        `${prefix}: auth-passphrase is required when security-level is 3 (AuthPriv)`
      )
    }
    if (privacyProtocol === null) {
      addError(
        errors,
        `${prefix}.privacy-protocol`,
        `${prefix}: privacy-protocol is required when security-level is 3 (AuthPriv)`
      )
    }
    if (!privacyPassphrase || privacyPassphrase.trim() === '') {
      addError(
        errors,
        `${prefix}.privacy-passphrase`,
        `${prefix}: privacy-passphrase is required when security-level is 3 (AuthPriv)`
      )
    }
  }
}

/**
 * Validates a trapd-configuration XML string.
 *
 * Expected structure:
 * <trapd-configuration snmp-trap-address="*|<ipv4>" snmp-trap-port="<1-65535>" new-suspect-on-trap="true|false">
 *   <snmpv3-user security-name="..." security-level="1|2|3" auth-protocol="..." auth-passphrase="..." privacy-protocol="..." privacy-passphrase="..." />
 *   ... (zero or more snmpv3-user elements)
 * </trapd-configuration>
 */
export const validateTrapdXml = (xmlString: string): XmlValidationResult => {
  const errors: XmlValidationError[] = []

  if (!xmlString || xmlString.trim() === '') {
    return { valid: false, errors: [{ field: 'xml', message: 'XML content is empty' }] }
  }

  let doc: Document
  try {
    const parser = new DOMParser()
    doc = parser.parseFromString(xmlString, 'application/xml')
    const parserError = doc.querySelector('parsererror')
    if (parserError) {
      const detail = parserError.textContent?.trim() ?? 'unknown parse error'
      return { valid: false, errors: [{ field: 'xml', message: `XML parse error: ${detail}` }] }
    }
  } catch {
    return { valid: false, errors: [{ field: 'xml', message: 'Failed to parse XML' }] }
  }

  const root = doc.documentElement
  if (root.localName !== 'trapd-configuration') {
    return {
      valid: false,
      errors: [
        {
          field: 'root',
          message: `Root element must be 'trapd-configuration', got '${root.localName}'`
        }
      ]
    }
  }

  const xmlns = root.namespaceURI ?? root.getAttribute('xmlns')
  if (xmlns !== TRAPD_XML_NAMESPACE) {
    addError(errors, 'xmlns', `Invalid xmlns '${xmlns ?? ''}': expected '${TRAPD_XML_NAMESPACE}'`)
  }

  // snmp-trap-address: optional and defaults to '*' in trapd-configuration.xsd.
  const snmpTrapAddress = root.getAttribute('snmp-trap-address')
  if (snmpTrapAddress !== null && snmpTrapAddress !== '*' && !isValidIP(snmpTrapAddress)) {
    addError(
      errors,
      'snmp-trap-address',
      `Invalid snmp-trap-address '${snmpTrapAddress}': must be '*' or a valid IPv4 address`
    )
  }

  // snmp-trap-port: required; must be an integer in [MIN_PORT, MAX_PORT]
  const snmpTrapPortStr = root.getAttribute('snmp-trap-port')
  if (snmpTrapPortStr === null) {
    addError(errors, 'snmp-trap-port', 'snmp-trap-port attribute is required')
  } else {
    const snmpTrapPort = parseInt(snmpTrapPortStr, 10)
    if (!isValidPort(snmpTrapPort)) {
      addError(
        errors,
        'snmp-trap-port',
        `Invalid snmp-trap-port '${snmpTrapPortStr}': must be an integer between ${MIN_PORT} and ${MAX_PORT}`
      )
    }
  }

  // new-suspect-on-trap: optional; must be 'true' or 'false' if present
  const newSuspectOnTrap = root.getAttribute('new-suspect-on-trap')
  if (newSuspectOnTrap !== null && newSuspectOnTrap !== 'true' && newSuspectOnTrap !== 'false') {
    addError(
      errors,
      'new-suspect-on-trap',
      `Invalid new-suspect-on-trap '${newSuspectOnTrap}': must be 'true' or 'false'`
    )
  }

  // snmpv3-user: zero or more child elements
  const snmpv3Users = root.getElementsByTagName('snmpv3-user')
  for (let i = 0; i < snmpv3Users.length; i++) {
    validateSnmpV3UserElement(snmpv3Users[i], i + 1, errors)
  }

  return { valid: errors.length === 0, errors }
}
