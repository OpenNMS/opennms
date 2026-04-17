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

import { ISelectItemType } from '@featherds/select'

export enum SnmpSecurityLevel {
  None = 0,
  NoAuthNoPriv = 1,
  AuthNoPriv = 2,
  AuthPriv = 3
}

export interface SnmpBaseConfiguration {
  // for UI use. For now it is the 0-based index into the array of definitions or profiles
  id?: number

  /** The proxy host to use when communicating with this agent */
  proxyHost?: string

  /** Number of variables to send per SNMP request. */
  maxVarsPerPdu?: number

  /** Number of repetitions to send per get-bulk request. */
  maxRepetitions?: number

  /**
   * (SNMP4J specific) Specifies the maximum number of bytes that may be
   * encoded into an individual SNMP PDU request by Collectd. Provides a
   * means to limit the size of outgoing PDU requests. Default is 65535,
   * must be at least 484.
   */
  maxRequestSize?: number

  /**
   * If set, forces SNMP data collection to the specified version.
   * May be a numeric version, e.g. 1, 2, 3, or a string version, e.g. 'v1', 'v2c', 'v3'.
   * If not set, the server will attempt to auto-detect the SNMP version during collection.
   */
  version?: string | number

  /** Default write community string */
  writeCommunity?: string

  /** Default read community string */
  readCommunity?: string

  /** Default timeout (in milliseconds) */
  timeout?: number

  /** Default number of retries */
  retry?: number

  /** If set, overrides UDP port 161 as the port where SNMP GET/GETNEXT/GETBULK requests are sent.  */
  port?: number

  ttl?: number
  encrypted?: boolean

  // The following are SNMPv3 only
  securityName?: string
  securityLevel?: number
  authPassphrase?: string
  authProtocol?: string
  engineId?: string
  contextEngineId?: string
  contextName?: string
  privacyPassphrase?: string
  privacyProtocol?: string
  enterpriseId?: string
}

export interface IpAddressRange {
  begin: string
  end: string
}

/**
 * Provides a mechanism for associating one or more specific IP addresses
 * and/or IP address ranges with a set of SNMP parms which will be used in
 * place of the default values during SNMP data collection.
 */
export interface SnmpDefinition extends SnmpBaseConfiguration {
  /** IP address range to which this definition applies. */
  range: IpAddressRange[]

  /** Specific IP address to which this definition applies. */
  specific: string[]

  /** Match Octets (as in IPLIKE) */
  ipMatch: string[]

  location?: string
  profileLabel?: string
}

export interface SnmpProfile extends SnmpBaseConfiguration {
  label: string
  filter: string
}

export interface SnmpConfig extends SnmpBaseConfiguration {
  definition: SnmpDefinition[]
  profiles: {
    profile: SnmpProfile[]
  }
}

export interface SnmpAgentConfig extends SnmpBaseConfiguration {
  /** The IP address that this config applies to. */
  address?: string

  /** The monitoring location that this config applies to. */
  location?: string

  isDefault?: boolean
  proxyFor?: string
  profileLabel?: string
}

export interface SnmpConfigFormErrors {
  snmpVersion?: string
  invalidRangeConfig?: string
  mixingRangeWithIpMatch?: string
  duplicateRangeItem?: string
  firstIpAddress?: string
  lastIpAddress?: string
  ipMatch?: string
  port?: string
  maxRequestSize?: string
  securityLevel?: string
  authProtocol?: string
  privacyProtocol?: string
  timeout?: string
  retry?: string
  maxVarsPerPdu?: string
  maxRepetitions?: string
  ttl?: string
  readCommunity?: string
  writeCommunity?: string
  securityName?: string
  authPassphrase?: string
  privacyPassphrase?: string
}

export interface SnmpProfileFormErrors extends SnmpConfigFormErrors {
  label?: string
  filterExpression?: string
}

// Used for defining the fields for the SnmpConfigPairedFieldInputs component
export interface SnmpFieldInfo {
  // The key for the field, used to bind to the config object
  key: string

  label: string
  hint?: string
  dataTest: string

  // if true, the field will display an SCV input icon
  scvEnabled?: boolean

  // true if the underlying value is numeric
  isNumeric?: boolean

  // true if the field is a select dropdown
  isSelect?: boolean

  // options for the select dropdown. _text is display label, _value is the underlying value.
  // _value should be a string even if isNumeric is true
  selectOptions?: ISelectItemType[]

  // if true, just display the 'hint', do not display the default value.
  // this is useful for fields that might display sensitive information, such as passwords or passphrases.
  skipDefaultHint?: boolean
}
