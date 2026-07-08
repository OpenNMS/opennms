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

import { TrapConfig, TrapdValidationError, TrapdValidationResult } from '@/types/trapConfig'
import { ISelectItemType } from '@featherds/select'
import { DEFAULT_TRAPD_BIND_ADDRESS } from './constants'
import { isConvertibleToInteger } from './utils'

export const MIN_PORT = 1
export const MAX_PORT = 65535
export const MIN_PASSPHRASE_CHARACTERS = 8
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

export const AUTH_PROTOCOL_OPTIONS: ISelectItemType[] = AuthProtocols.map(protocol => ({
  _text: protocol,
  _value: protocol
}))

export const PRIVACY_PROTOCOL_OPTIONS: ISelectItemType[] = PrivacyProtocols.map(protocol => ({
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

const addError = (errors: TrapdValidationError[], field: string, message: string) => errors.push({ field, message })

const fieldName = (xmlName: string, jsonName: string, isXml: boolean): string => isXml ? xmlName : jsonName

const validateSnmpTrapAddress = (address: string | null | undefined, isXml: boolean, errors: TrapdValidationError[]): void => {
  const field = fieldName('snmp-trap-address', 'snmpTrapAddress', isXml)
  if (address != null && address !== '*' && !isValidIP(address)) {
    addError(errors, field, `Invalid ${field} '${address}': must be '*' or a valid IPv4 address`)
  }
}

const validateSnmpTrapPort = (value: string | number | null | undefined, isXml: boolean, errors: TrapdValidationError[]): void => {
  const field = fieldName('snmp-trap-port', 'snmpTrapPort', isXml)
  if (value == null || value === '') {
    addError(errors, field, `${field} is required`)
    return
  }

  if (!isConvertibleToInteger(value)) {
    addError(errors, field, `Invalid ${field} '${value}': must be an integer`)
    return
  }

  const port = typeof value === 'string' ? parseInt(value, 10) : value
  if (!isValidPort(port)) {
    addError(errors, field, `Invalid ${field} '${value}': must be an integer between ${MIN_PORT} and ${MAX_PORT}`)
  }
}

const validateNewSuspectOnTrap = (value: string | boolean | null | undefined, isXml: boolean, errors: TrapdValidationError[]): void => {
  if (!isXml) {
    return
  }

  const field = 'new-suspect-on-trap'
  if (value !== null && value !== undefined && value !== 'true' && value !== 'false') {
    addError(errors, field, `Invalid ${field} '${value}': must be 'true' or 'false'`)
  }
}

const validateSnmpV3UserValues = (
  securityName: string | null | undefined,
  securityLevelInput: string | number | null | undefined,
  authProtocol: string | null | undefined,
  authPassphrase: string | null | undefined,
  privacyProtocol: string | null | undefined,
  privacyPassphrase: string | null | undefined,
  index: number,
  isXml: boolean,
  errors: TrapdValidationError[]
): void => {
  const userField = fieldName('snmpv3-user', 'snmpv3User', isXml)
  const prefix = `${userField}[${index}]`
  const snField = fieldName('security-name', 'securityName', isXml)
  const slField = fieldName('security-level', 'securityLevel', isXml)
  const apField = fieldName('auth-protocol', 'authProtocol', isXml)
  const appField = fieldName('auth-passphrase', 'authPassphrase', isXml)
  const ppField = fieldName('privacy-protocol', 'privacyProtocol', isXml)
  const pppField = fieldName('privacy-passphrase', 'privacyPassphrase', isXml)

  if (!securityName || securityName.trim() === '') {
    addError(errors, `${prefix}.${snField}`, `${prefix}: ${snField} is required`)
  }

  let securityLevel: number | undefined

  if (typeof securityLevelInput === 'number') {
    securityLevel = securityLevelInput
    if (!isValidSnmpSecurityLevel(securityLevel)) {
      securityLevel = undefined
    }
  } else if (securityLevelInput != null && securityLevelInput !== '' && isConvertibleToInteger(securityLevelInput)) {
    securityLevel = parseInt(securityLevelInput, 10)
    if (!isValidSnmpSecurityLevel(securityLevel)) {
      securityLevel = undefined
    }
  }

  if (securityLevel === undefined) {
    addError(errors,
      `${prefix}.${slField}`,
      `${prefix}: invalid ${slField} '${securityLevelInput}'. Valid values: 1 (NoAuthNoPriv), 2 (AuthNoPriv), 3 (AuthPriv)`
    )
  }

  // Normalize undefined → null for consistent presence checks
  const authProtocolVal = authProtocol ?? null
  const authPassphraseVal = authPassphrase ?? null
  const privacyProtocolVal = privacyProtocol ?? null
  const privacyPassphraseVal = privacyPassphrase ?? null

  if (authPassphraseVal && authPassphraseVal.trim() !== '' && passphraseByteLength(authPassphraseVal) < MIN_PASSPHRASE_CHARACTERS) {
    addError(errors, `${prefix}.${appField}`, `${prefix}: ${appField} must be at least ${MIN_PASSPHRASE_CHARACTERS} characters`)
  }
  if (privacyPassphraseVal && privacyPassphraseVal.trim() !== '' && passphraseByteLength(privacyPassphraseVal) < MIN_PASSPHRASE_CHARACTERS) {
    addError(errors, `${prefix}.${pppField}`, `${prefix}: ${pppField} must be at least ${MIN_PASSPHRASE_CHARACTERS} characters`)
  }

  if (authProtocolVal !== null) {
    if (!VALID_AUTH_PROTOCOL_VALUES.has(authProtocolVal as AuthProtocol)) {
      addError(
        errors,
        `${prefix}.${apField}`,
        `${prefix}: invalid ${apField} '${authProtocolVal}'. Valid values: ${AuthProtocols.join(', ')}`
      )
    }
    if (!authPassphraseVal || authPassphraseVal.trim() === '') {
      addError(errors, `${prefix}.${appField}`, `${prefix}: ${appField} is required when ${apField} is set`)
    }
  }

  if (privacyProtocolVal !== null) {
    if (!VALID_PRIVACY_PROTOCOL_VALUES.has(privacyProtocolVal)) {
      addError(
        errors,
        `${prefix}.${ppField}`,
        `${prefix}: invalid ${ppField} '${privacyProtocolVal}'. Valid values: ${PrivacyProtocols.join(', ')}`
      )
    }
    if (!privacyPassphraseVal || privacyPassphraseVal.trim() === '') {
      addError(errors, `${prefix}.${pppField}`, `${prefix}: ${pppField} is required when ${ppField} is set`)
    }
    if (authProtocolVal === null) {
      addError(errors, `${prefix}.${apField}`, `${prefix}: ${apField} is required when ${ppField} is set`)
    }
  }

  if (securityLevel === SecurityLevel.NoAuthNoPriv) {
    if (authProtocolVal !== null) {
      addError(errors, `${prefix}.${apField}`, `${prefix}: ${apField} must not be set when ${slField} is 1 (NoAuthNoPriv)`)
    }
    if (authPassphraseVal !== null) {
      addError(errors, `${prefix}.${appField}`, `${prefix}: ${appField} must not be set when ${slField} is 1 (NoAuthNoPriv)`)
    }
    if (privacyProtocolVal !== null) {
      addError(errors, `${prefix}.${ppField}`, `${prefix}: ${ppField} must not be set when ${slField} is 1 (NoAuthNoPriv)`)
    }
    if (privacyPassphraseVal !== null) {
      addError(errors, `${prefix}.${pppField}`, `${prefix}: ${pppField} must not be set when ${slField} is 1 (NoAuthNoPriv)`)
    }
  }

  if (securityLevel === SecurityLevel.AuthNoPriv) {
    if (authProtocolVal === null) {
      addError(errors, `${prefix}.${apField}`, `${prefix}: ${apField} is required when ${slField} is 2 (AuthNoPriv)`)
    }
    if (!authPassphraseVal || authPassphraseVal.trim() === '') {
      addError(errors, `${prefix}.${appField}`, `${prefix}: ${appField} is required when ${slField} is 2 (AuthNoPriv)`)
    }
    if (privacyProtocolVal !== null) {
      addError(errors, `${prefix}.${ppField}`, `${prefix}: ${ppField} must not be set when ${slField} is 2 (AuthNoPriv)`)
    }
    if (privacyPassphraseVal !== null) {
      addError(errors, `${prefix}.${pppField}`, `${prefix}: ${pppField} must not be set when ${slField} is 2 (AuthNoPriv)`)
    }
  }

  if (securityLevel === SecurityLevel.AuthPriv) {
    if (authProtocolVal === null) {
      addError(errors, `${prefix}.${apField}`, `${prefix}: ${apField} is required when ${slField} is 3 (AuthPriv)`)
    }
    if (!authPassphraseVal || authPassphraseVal.trim() === '') {
      addError(errors, `${prefix}.${appField}`, `${prefix}: ${appField} is required when ${slField} is 3 (AuthPriv)`)
    }
    if (privacyProtocolVal === null) {
      addError(errors, `${prefix}.${ppField}`, `${prefix}: ${ppField} is required when ${slField} is 3 (AuthPriv)`)
    }
    if (!privacyPassphraseVal || privacyPassphraseVal.trim() === '') {
      addError(errors, `${prefix}.${pppField}`, `${prefix}: ${pppField} is required when ${slField} is 3 (AuthPriv)`)
    }
  }
}

const validateSnmpV3UserElement = (user: Element, index: number, isXml: boolean, errors: TrapdValidationError[]): void => {
  validateSnmpV3UserValues(
    user.getAttribute('security-name'),
    user.getAttribute('security-level'),
    user.getAttribute('auth-protocol'),
    user.getAttribute('auth-passphrase'),
    user.getAttribute('privacy-protocol'),
    user.getAttribute('privacy-passphrase'),
    index,
    isXml,
    errors
  )
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
export const validateTrapdXml = (xmlString: string): TrapdValidationResult => {
  const errors: TrapdValidationError[] = []

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

  validateSnmpTrapAddress(root.getAttribute('snmp-trap-address'), true, errors)
  validateSnmpTrapPort(root.getAttribute('snmp-trap-port'), true, errors)
  validateNewSuspectOnTrap(root.getAttribute('new-suspect-on-trap'), true, errors)

  // snmpv3-user: zero or more child elements
  const snmpv3Users = root.getElementsByTagName('snmpv3-user')
  for (let i = 0; i < snmpv3Users.length; i++) {
    validateSnmpV3UserElement(snmpv3Users[i], i + 1, true, errors)
  }

  return { valid: errors.length === 0, errors }
}

export const validateTrapdJson = (jsonString: string): TrapdValidationResult => {
  const errors: TrapdValidationError[] = []

  if (!jsonString || jsonString.trim() === '') {
    return { valid: false, errors: [{ field: 'json', message: 'JSON content is empty' }] }
  }

  let parsed: unknown
  try {
    parsed = JSON.parse(jsonString)
  } catch {
    return { valid: false, errors: [{ field: 'json', message: 'Failed to parse JSON' }] }
  }

  if (typeof parsed !== 'object' || parsed === null || Array.isArray(parsed)) {
    return { valid: false, errors: [{ field: 'json', message: 'JSON must be an object' }] }
  }

  const raw = parsed as Record<string, unknown>
  const config = {
    ...raw,
    includeRawMessage: raw['includeRawMessage'] ?? false,
    newSuspectOnTrap: raw['newSuspectOnTrap'] ?? false,
    useAddressFromVarbind: raw['useAddressFromVarbind'] ?? false
  } as TrapConfig

  validateSnmpTrapAddress(config.snmpTrapAddress, false, errors)
  validateSnmpTrapPort(config.snmpTrapPort, false, errors)
  validateNewSuspectOnTrap(config.newSuspectOnTrap, false, errors)

  if (Array.isArray(config.snmpv3User)) {
    config.snmpv3User.forEach((user, i) => {
      validateSnmpV3UserValues(
        user.securityName,
        user.securityLevel,
        user.authProtocol,
        user.authPassphrase,
        user.privacyProtocol,
        user.privacyPassphrase,
        i + 1,
        false,
        errors
      )
    })
  }

  return { valid: errors.length === 0, errors }
}
