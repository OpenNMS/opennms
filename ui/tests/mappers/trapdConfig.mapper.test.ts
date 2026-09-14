import { mapTrapdConfigFromServer, mapUserToServer } from '@/mappers/trapdConfig.mapper'
import { describe, expect, it } from 'vitest'

describe('trapdConfig.mapper', () => {
  describe('mapTrapdConfigFromServer', () => {
    it('preserves the server-assigned id on each SNMPv3 user', () => {
      const serverData = {
        snmpTrapPort: 162,
        snmpTrapAddress: '*',
        newSuspectOnTrap: false,
        snmpv3User: [
          {
            id: 'id-a',
            securityName: 'dupUser',
            securityLevel: 3,
            engineId: null,
            authProtocol: 'SHA',
            authPassphrase: '******',
            privacyProtocol: 'AES',
            privacyPassphrase: '******'
          }
        ]
      }

      const result = mapTrapdConfigFromServer(serverData)

      expect(result.snmpv3User[0].id).toBe('id-a')
      expect(result.snmpv3User[0].securityName).toBe('dupUser')
    })

    it('leaves id undefined when the server omits it', () => {
      const result = mapTrapdConfigFromServer({
        snmpv3User: [{ securityName: 'noId', securityLevel: 1 }]
      })

      expect(result.snmpv3User[0].id).toBeUndefined()
    })
  })

  describe('mapUserToServer', () => {
    it('echoes the id back so an existing user round-trips', () => {
      const user = mapUserToServer({
        id: 'id-b',
        securityName: 'dupUser',
        engineId: null,
        securityLevel: 3,
        authProtocol: 'SHA',
        authPassphrase: '******',
        privacyProtocol: 'AES',
        privacyPassphrase: '******'
      })

      expect(user.id).toBe('id-b')
    })

    it('omits the id for a new user', () => {
      const user = mapUserToServer({
        securityName: 'brandNew',
        engineId: null,
        securityLevel: 1
      })

      expect(user.id).toBeUndefined()
    })
  })
})
