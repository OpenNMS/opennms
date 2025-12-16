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

  /** If set, forces SNMP data collection to the specified version. */
  version?: string

  /** Default write community string */
  writeCommunity: string

  /** Default read community string */
  readCommunity: string

  /** Default timeout (in milliseconds) */
  timeout?: number

  /** Default number of retries */
  retry?: number

  /** If set, overrides UDP port 161 as the port where SNMP GET/GETNEXT/GETBULK requests are sent.  */
  port?: number

  ttl?: number
  encrypted: boolean

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
  ranges: IpAddressRange[]

  /** Specific IP address to which this definition applies. */
  specifics: string[]

  /** Match Octets (as in IPLIKE) */
  ipMatches: string[]

  location: string
  profileLabel: string
}

export interface SnmpProfile extends SnmpBaseConfiguration {
  label: string
  filterExpression: string
}

export interface SnmpConfig {
  definitions: SnmpDefinition[]
  profiles: SnmpProfile[]
}

export type SnmpConfigStoreState = {
  config: SnmpConfig
  isLoading: boolean
  activeTab: number
  createEditMode: CreateEditMode
  definitionId: number
  profileId: number
}

export type SnmpDefinitionFormErrors = {
  snmpVersion?: string
  firstIpAddress?: string
  secondIpAddress?: string
}

export type SnmpProfileFormErrors = {
  label?: string
  filterExpression?: string
}
