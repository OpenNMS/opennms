import { SnmpV3User, TrapConfig } from '@/types/trapConfig'

export const mapTrapdConfigFromServer = (data: any): TrapConfig => {
  return {
    snmpTrapPort: data.snmpTrapPort,
    snmpTrapAddress: data.snmpTrapAddress,
    newSuspectOnTrap: data.newSuspectOnTrap,
    includeRawMessage: data.includeRawMessage,
    threads: data.threads,
    queueSize: data.queueSize,
    batchSize: data.batchSize,
    batchInterval: data.batchInterval,
    useAddressFromVarbind: data.useAddressFromVarbind,
    snmpv3User: (data.snmpv3User || []).map((user: any) => ({
      engineId: user.engineId,
      securityName: user.securityName,
      securityLevel: user.securityLevel,
      authProtocol: user.authProtocol,
      authPassphrase: user.authPassphrase,
      privacyProtocol: user.privacyProtocol,
      privacyPassphrase: user.privacyPassphrase
    } as SnmpV3User))
  }
}

export const mapUserToServer = (payload: any): SnmpV3User => {
  const user = {
    securityName: payload.securityName,
    engineId: payload.engineId,
    securityLevel: payload.securityLevel
  } as SnmpV3User

  if (payload.securityLevel === 1) {
    user.authProtocol = null
    user.authPassphrase = null
    user.privacyProtocol = null
    user.privacyPassphrase = null
  } else if (payload.securityLevel === 2) {
    user.authProtocol = payload.authProtocol
    user.authPassphrase = payload.authPassphrase
    user.privacyProtocol = null
    user.privacyPassphrase = null
  } else if (payload.securityLevel === 3) {
    user.authProtocol = payload.authProtocol
    user.authPassphrase = payload.authPassphrase
    user.privacyProtocol = payload.privacyProtocol
    user.privacyPassphrase = payload.privacyPassphrase
  }

  return user
}
