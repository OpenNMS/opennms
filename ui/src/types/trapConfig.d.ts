import { CreateEditMode } from '.'

export interface TrapConfigStoreState {
  isLoading: boolean
  trapdConfig: TrapConfig | null
  SnmpV3Users: SnmpV3User[]
  activeTab: number
  credentialDrawerState: {
    visible: boolean
  }
  createUserDrawerState: {
    visible: boolean
    mode: CreateEditMode
    selectedUserIndex: number
  }
}

export interface TrapConfig {
  snmpTrapAddress: string
  snmpTrapPort: number
  newSuspectOnTrap: boolean
  includeRawMessage: boolean
  threads: number
  queueSize: number
  batchSize: number
  batchInterval: number
  useAddressFromVarbind: boolean
  snmpv3User: SnmpV3User[]
}

export interface SnmpV3User {
  engineId: string | null
  securityName: string
  securityLevel: number
  authProtocol: string | null
  authPassphrase: string | null
  privacyProtocol: string | null
  privacyPassphrase: string | null
}

export interface TrapdConfigurationError {
  port?: string
  bindAddress?: string
  threads?: string
  queueSize?: string
  batchSize?: string
  batchInterval?: string
  snmpv3User?: string
}

export interface SnmpV3UserError {
  engineId?: string
  securityName?: string
  securityLevel?: string
  authProtocol?: string
  authPassphrase?: string
  privacyProtocol?: string
  privacyPassphrase?: string
}
