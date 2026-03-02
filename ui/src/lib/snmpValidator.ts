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

import { isIP } from 'is-ip'
import { SnmpBaseConfiguration, SnmpConfigFormErrors, SnmpProfileFormErrors, SnmpSecurityLevel } from '@/types/snmpConfig'
import { DEFAULT_SNMP_V3_SECURITY_LEVEL } from './constants'

const SNMP_VERSIONS = ['v1', 'v2c', 'v3']
const VALID_SECURITY_LEVELS = [SnmpSecurityLevel.NoAuthNoPriv, SnmpSecurityLevel.AuthNoPriv, SnmpSecurityLevel.AuthPriv]
const MIN_PORT = 1
const MAX_PORT = 65535
const MAX_REQUEST_SIZE_MINIMUM = 484
const IPLIKE_REGEX = /^(([0-9]{1,3}((,|-)[0-9]{1,3})*|\*)\.){3}([0-9]{1,3}((,|-)[0-9]{1,3})*|\*)$/
export const IP_MATCH_ERROR = 'IP Match expression must be in the format of an IPLIKE expression, e.g. 10.0.0.* or 10.0.1-9.*'

export const SecurityLevelSelectionOptions = [
  { _text: 'No Auth (1)', _value: String(SnmpSecurityLevel.NoAuthNoPriv) },
  { _text: 'Auth Only (2)', _value: String(SnmpSecurityLevel.AuthNoPriv) },
  { _text: 'Auth and Privacy (3)', _value: String(SnmpSecurityLevel.AuthPriv) }
]

export const SnmpAuthProtocols = [
  'MD5',
  'SHA',
  'SHA-224',
  'SHA-256',
  'SHA-512'
]

export const SnmpPrivacyProtocols = [
  'DES',
  'AES',
  'AES192',
  'AES256'
]

export const validateSnmpConfiguration = (config: SnmpBaseConfiguration, snmpVersion: string): SnmpConfigFormErrors => {
  const errors: SnmpConfigFormErrors = {}

  if (config.port !== undefined) {
    if (isNaN(config.port) || config.port < MIN_PORT || config.port > MAX_PORT) {
      errors.port = `Port must be a number between ${MIN_PORT} and ${MAX_PORT}`
    }
  }

  if (config.maxRequestSize !== undefined) {
    if (isNaN(config.maxRequestSize) || config.maxRequestSize < MAX_REQUEST_SIZE_MINIMUM) {
      errors.maxRequestSize = `If provided, Max Request Size must be a number greater than or equal to ${MAX_REQUEST_SIZE_MINIMUM}`
    }
  }

  // only check security level for SNMPv3
  if (snmpVersion === 'v3' && config.securityLevel !== undefined) {
    if (isNaN(config.securityLevel) || !VALID_SECURITY_LEVELS.includes(config.securityLevel)) {
      errors.securityLevel = 'Security Level must be one of: 1 (NoAuthNoPriv), 2 (AuthNoPriv), 3 (AuthPriv)'
    }
  }

  if (config.authProtocol !== undefined && config.authProtocol !== '' && !SnmpAuthProtocols.includes(config.authProtocol)) {
    errors.authProtocol = 'Auth Protocol must be one of: ' + SnmpAuthProtocols.join(', ')
  }

  if (config.privacyProtocol !== undefined && config.privacyProtocol !== '' && !SnmpPrivacyProtocols.includes(config.privacyProtocol)) {
    errors.privacyProtocol = 'Privacy Protocol must be one of: ' + SnmpPrivacyProtocols.join(', ')
  }

  // Validate that remaining numeric fields are integers
  const numericFields: string[] = ['timeout', 'retry', 'maxVarsPerPdu', 'maxRepetitions', 'ttl']

  const fieldDisplayNames: Record<string, string> = {
    timeout: 'Timeout',
    retry: 'Retries',
    maxVarsPerPdu: 'Max Vars Per PDU',
    maxRepetitions: 'Max Repetitions',
    ttl: 'TTL'
  }

  numericFields.forEach(field => {
    const value = (config as any)[field]

    if (value !== undefined) {
      const numericValue = Number(value)

      if (isNaN(numericValue) || !Number.isInteger(numericValue) || numericValue < 0) {
        (errors as any)[field] = `${fieldDisplayNames[field]} must be an integer greater than or equal to 0`
      }
    }
  })

  return errors
}

// See opennms-config-jaxb/src/main/resources/xsds/snmp-config.xsd for field definitions
export const validateDefinition = (
  config: SnmpBaseConfiguration,
  snmpVersion: string,
  firstIpAddress: string,
  lastIpAddress: string,
  ipMatch: string
): SnmpConfigFormErrors => {
  const errors: SnmpConfigFormErrors = {}

  if (!snmpVersion) {
    errors.snmpVersion = 'SNMP Version is required'
  }

  if (snmpVersion && !SNMP_VERSIONS.includes(snmpVersion)) {
    errors.snmpVersion = 'SNMP Version must be one of: ' + SNMP_VERSIONS.join(', ')
  }

  const isRange = firstIpAddress && lastIpAddress
  const isSpecific = firstIpAddress && !lastIpAddress
  const isIpMatch = !firstIpAddress && !lastIpAddress && ipMatch
  const isRangeOrSpecific = isRange || isSpecific

  // error if neither range, specific or ipMatch is provided, or if ipMatch is provided along with range or specific
  if ((!isRange && !isSpecific && !isIpMatch) || (isRangeOrSpecific && isIpMatch)) {
    errors.invalidRangeConfig = 'You must specify either a range, specific or IP Match expression'
  }

  if (isRange || isSpecific) {
    if (!isIP(firstIpAddress)) {
      errors.firstIpAddress = 'First IP Address must be a valid IP address'
    }
  }

  if (isRange) {
    if (!isIP(lastIpAddress)) {
      errors.lastIpAddress = 'If provided, last IP Address must be a valid IP address'
    }
  }

  if (isIpMatch) {
    if (!IPLIKE_REGEX.test(ipMatch)) {
      errors.ipMatch = IP_MATCH_ERROR
    }
  }

  const snmpConfigErrors = validateSnmpConfiguration(config, snmpVersion)

  return { ...errors, ...snmpConfigErrors }
}

export const validateProfile = (
  label: string,
  filterExpression: string
): SnmpProfileFormErrors => {
  const errors: SnmpProfileFormErrors = {}

  if (!label) {
    errors.label = 'SNMP Profile label is required'
  }

  if (!filterExpression) {
    errors.filterExpression = 'FilterExpression is required'
  }

  return errors
}

export const isValidSnmpSecurityLevel = (level: number | undefined): boolean => {
  return level !== undefined && VALID_SECURITY_LEVELS.includes(level)
}

export const getDefaultSnmpSecurityLevel = (): number => {
  return DEFAULT_SNMP_V3_SECURITY_LEVEL
}
