import { describe, it, expect } from 'vitest'
import { IP_MATCH_ERROR, checkForDuplicateDefinitionItems, validateDefinition, validateProfile, validateSnmpConfiguration } from '@/lib/snmpValidator'
import { IpAddressRange, SnmpBaseConfiguration, SnmpDefinition } from '@/types/snmpConfig'

describe('snmpValidator', () => {
  describe('validateDefinition', () => {
    const validConfig: SnmpBaseConfiguration = {
      port: 161,
      maxRequestSize: 65535,
      securityLevel: 0,
      authProtocol: 'MD5',
      privacyProtocol: 'DES'
    }

    describe('SNMP Version validation', () => {
      it('should return error when SNMP version is missing', () => {
        const errors = validateDefinition(validConfig, '', '192.168.1.1', '', '', false)
        expect(errors.snmpVersion).toBe('SNMP Version is required')
      })

      it('should return error when SNMP version is invalid', () => {
        const errors = validateDefinition(validConfig, 'v4', '192.168.1.1', '', '', false)
        expect(errors.snmpVersion).toBe('SNMP Version must be one of: v1, v2c, v3')
      })

      it('should pass validation with valid SNMP versions', () => {
        const versions = ['v1', 'v2c', 'v3']
        versions.forEach((version) => {
          const errors = validateDefinition(validConfig, version, '192.168.1.1', '', '', false)
          expect(errors.snmpVersion).toBeUndefined()
        })
      })
    })

    describe('IP Address validation', () => {
      it('should not return invalidRangeConfig error when no range, specific, or ipMatch is provided', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '', '', false)
        expect(errors.invalidRangeConfig).toBeUndefined()
      })

      it('should not return invalidRangeConfig when a specific IP is provided', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', '', '', false)
        expect(errors.invalidRangeConfig).toBeUndefined()
      })

      it('should not return invalidRangeConfig when a range is provided', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', '192.168.1.255', '', false)
        expect(errors.invalidRangeConfig).toBeUndefined()
      })

      it('should not return invalidRangeConfig when an ipMatch is provided', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '', '192.168.1.*', false)
        expect(errors.invalidRangeConfig).toBeUndefined()
      })

      it('should return error when first IP address is invalid (specific)', () => {
        const errors = validateDefinition(validConfig, 'v2c', 'invalid-ip', '', '', false)
        expect(errors.firstIpAddress).toBe('First IP Address must be a valid IP address')
      })

      it('should return error when first IP address is invalid (range)', () => {
        const errors = validateDefinition(validConfig, 'v2c', 'invalid-ip', '192.168.1.255', '', false)
        expect(errors.firstIpAddress).toBe('First IP Address must be a valid IP address')
      })

      it('should pass validation with valid IPv4 specific address', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', '', '', false)
        expect(errors.firstIpAddress).toBeUndefined()
      })

      it('should pass validation with valid IPv6 specific address', () => {
        const errors = validateDefinition(validConfig, 'v2c', '2001:0db8:85a3:0000:0000:8a2e:0370:7334', '', '', false)
        expect(errors.firstIpAddress).toBeUndefined()
      })

      it('should return error when last IP address is invalid in a range', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', 'invalid-ip', '', false)
        expect(errors.lastIpAddress).toBe('If provided, last IP Address must be a valid IP address')
      })

      it('should not validate last IP address when it is empty (specific mode)', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', '', '', false)
        expect(errors.lastIpAddress).toBeUndefined()
      })

      it('should return error when last IP address is provided but first IP address is empty', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '192.168.1.255', '', false)
        expect(errors.firstIpAddress).toBeUndefined()
        expect(errors.lastIpAddress).toBe('Last IP Address cannot be provided without a first IP address')
      })

      it('should pass validation with valid range', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', '192.168.1.255', '', false)
        expect(errors.firstIpAddress).toBeUndefined()
        expect(errors.lastIpAddress).toBeUndefined()
      })

      it('should not validate IP addresses when only ipMatch is provided', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '', '192.168.1.*', false)
        expect(errors.firstIpAddress).toBeUndefined()
        expect(errors.lastIpAddress).toBeUndefined()
      })

      it('should return error when IP Match is provided but first and last IP addresses are not empty', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.254', '192.168.1.255', '192.168.1.*', false)
        expect(errors.firstIpAddress).toBeUndefined()
        expect(errors.lastIpAddress).toBeUndefined()
        expect(errors.ipMatch).toBeUndefined()
        expect(errors.invalidRangeConfig).toBe('You cannot specify an IP Match expression along with a range or specific IP address')
      })

      it('should return error when IP Match is provided but first IP address is not empty', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.254', '', '192.168.1.*', false)
        expect(errors.firstIpAddress).toBeUndefined()
        expect(errors.lastIpAddress).toBeUndefined()
        expect(errors.ipMatch).toBeUndefined()
        expect(errors.invalidRangeConfig).toBe('You cannot specify an IP Match expression along with a range or specific IP address')
      })

      it('should return error when IP Match is provided but first IP address is empty but last IP address is provided', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '192.168.1.255', '192.168.1.*', false)
        expect(errors.firstIpAddress).toBeUndefined()
        expect(errors.lastIpAddress).toBe('Last IP Address cannot be provided without a first IP address')
        expect(errors.ipMatch).toBeUndefined()
        expect(errors.invalidRangeConfig).toBe('You cannot specify an IP Match expression along with a range or specific IP address')
      })

      it('should return error for invalid IP match expressions', () => {
        const invalidIplikeExpressions = ['invalid-ip', 'iplike 192.168.*.*', '192.168.*']

        invalidIplikeExpressions.forEach((expr) => {
          const errors = validateDefinition(validConfig, 'v2c', '', '', expr, false)
          expect(errors.ipMatch).toBe(IP_MATCH_ERROR)
        })
      })
    })

    describe('all IPs are empty', () => {
      it('should not set invalidRangeConfig when all IPs are empty', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '', '', false)
        expect(errors.invalidRangeConfig).toBeUndefined()
      })

      it('should not set firstIpAddress when all IPs are empty', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '', '', false)
        expect(errors.firstIpAddress).toBeUndefined()
      })

      it('should not set lastIpAddress when all IPs are empty', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '', '', false)
        expect(errors.lastIpAddress).toBeUndefined()
      })

      it('should not set ipMatch when all IPs are empty', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '', '', false)
        expect(errors.ipMatch).toBeUndefined()
      })
    })

    describe('isSaving: true', () => {
      it('should not set firstIpAddress when first IP is invalid when isSaving mode is true', () => {
        const errors = validateDefinition(validConfig, 'v2c', 'invalid-ip', '', '', true)
        expect(errors.firstIpAddress).toBeUndefined()
      })

      it('should not set lastIpAddress when last IP is invalid when isSaving mode is true', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', 'invalid-ip', '', true)
        expect(errors.lastIpAddress).toBeUndefined()
      })

      it('should not set lastIpAddress when last IP is provided without a first IP when isSaving mode is true', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '192.168.1.255', '', true)
        expect(errors.lastIpAddress).toBeUndefined()
      })

      it('should not set ipMatch when IP match expression is invalid when isSaving mode is true', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '', 'invalid-ip', true)
        expect(errors.ipMatch).toBeUndefined()
      })

      it('should not set invalidRangeConfig when both IPs and ipMatch are provided when isSaving mode is true', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', '192.168.1.255', '192.168.1.*', true)
        expect(errors.invalidRangeConfig).toBeUndefined()
      })
    })

    describe.each([false, true])('Port validation (isSaving: %s)', (isSaving) => {
      it('should return error when port is less than 1', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, port: 0 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.port).toBe('Port must be a number between 1 and 65535')
      })

      it('should return error when port is a negative number', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, port: -1 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.port).toBe('Port must be a number between 1 and 65535')
      })

      it('should return error when port is greater than 65535', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, port: 65536 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.port).toBe('Port must be a number between 1 and 65535')
      })

      it('should return error when port is not a number', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, port: NaN }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.port).toBe('Port must be a number between 1 and 65535')
      })

      it('should pass validation with valid port', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, port: 161 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.port).toBeUndefined()
      })
    })

    describe.each([false, true])('Max Request Size validation (isSaving: %s)', (isSaving) => {
      it('should return error when maxRequestSize is less than 484', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, maxRequestSize: 483 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.maxRequestSize).toBe('If provided, Max Request Size must be a number greater than or equal to 484')
      })

      it('should pass validation when maxRequestSize is 484', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, maxRequestSize: 484 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.maxRequestSize).toBeUndefined()
      })

      it('should pass validation with valid maxRequestSize', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, maxRequestSize: 65535 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.maxRequestSize).toBeUndefined()
      })
    })

    describe.each([false, true])('Security Level validation (isSaving: %s)', (isSaving) => {
      it('should ignore security level when SNMP version is not v3', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, securityLevel: 5 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.securityLevel).toBeUndefined()
      })

      it('should return error when security level is invalid', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, securityLevel: 5 }
        const errors = validateDefinition(config, 'v3', '192.168.1.1', '', '', isSaving)
        expect(errors.securityLevel).toBe('Security Level must be one of: 1 (NoAuthNoPriv), 2 (AuthNoPriv), 3 (AuthPriv)')
      })

      it('should pass validation with valid security levels', () => {
        const validLevels = [1, 2, 3]
        validLevels.forEach((level) => {
          const config: SnmpBaseConfiguration = { ...validConfig, securityLevel: level }
          const errors = validateDefinition(config, 'v3', '192.168.1.1', '', '', isSaving)
          expect(errors.securityLevel).toBeUndefined()
        })
      })
    })

    describe.each([false, true])('Auth Protocol validation (isSaving: %s)', (isSaving) => {
      it('should return error when auth protocol is invalid', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, authProtocol: 'INVALID' }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.authProtocol).toBe('Auth Protocol must be one of: MD5, SHA, SHA-224, SHA-256, SHA-512')
      })

      it('should pass validation with valid auth protocols', () => {
        const validProtocols = ['MD5', 'SHA', 'SHA-224', 'SHA-256', 'SHA-512']
        validProtocols.forEach((protocol) => {
          const config: SnmpBaseConfiguration = { ...validConfig, authProtocol: protocol }
          const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
          expect(errors.authProtocol).toBeUndefined()
        })
      })

      it('should pass validation when auth protocol is empty string', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, authProtocol: '' }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.authProtocol).toBeUndefined()
      })
    })

    describe.each([false, true])('Privacy Protocol validation (isSaving: %s)', (isSaving) => {
      it('should return error when privacy protocol is invalid', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, privacyProtocol: 'INVALID' }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.privacyProtocol).toBe('Privacy Protocol must be one of: DES, AES, AES192, AES256')
      })

      it('should pass validation with valid privacy protocols', () => {
        const validProtocols = ['DES', 'AES', 'AES192', 'AES256']
        validProtocols.forEach((protocol) => {
          const config: SnmpBaseConfiguration = { ...validConfig, privacyProtocol: protocol }
          const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
          expect(errors.privacyProtocol).toBeUndefined()
        })
      })

      it('should pass validation when privacy protocol is empty string', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, privacyProtocol: '' }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.privacyProtocol).toBeUndefined()
      })
    })

    describe.each([false, true])('Numeric fields validation (isSaving: %s)', (isSaving) => {
      it('should return error when timeout is not an integer', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, timeout: 3.5 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.timeout).toBe('Timeout must be an integer greater than or equal to 0')
      })

      it('should return error when retry is not an integer', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, retry: 2.7 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.retry).toBe('Retries must be an integer greater than or equal to 0')
      })

      it('should return error when maxVarsPerPdu is not an integer', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, maxVarsPerPdu: 10.5 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.maxVarsPerPdu).toBe('Max Vars Per PDU must be an integer greater than or equal to 0')
      })

      it('should return error when maxRepetitions is not an integer', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, maxRepetitions: 5.3 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.maxRepetitions).toBe('Max Repetitions must be an integer greater than or equal to 0')
      })

      it('should pass validation with valid integer values', () => {
        const config: SnmpBaseConfiguration = {
          ...validConfig,
          timeout: 3000,
          retry: 3,
          maxVarsPerPdu: 10,
          maxRepetitions: 2
        }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '', isSaving)
        expect(errors.timeout).toBeUndefined()
        expect(errors.retry).toBeUndefined()
        expect(errors.maxVarsPerPdu).toBeUndefined()
        expect(errors.maxRepetitions).toBeUndefined()
      })
    })

    describe('Empty validation', () => {
      it('should return an error for an empty configuration', () => {
        const config: SnmpBaseConfiguration = {}
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '192.168.1.255', '', false)
        expect(Object.keys(errors)).toHaveLength(0)
      })
    })

    describe.each([false, true])('SCV expression validation (isSaving: %s)', (isSaving) => {
      const scvEnabledKeys = new Set(['readCommunity'])
      const ip = '192.168.1.1'

      it('should pass when readCommunity is a valid SCV expression', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:my-key}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should pass when readCommunity is a valid two-subkey SCV expression', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:alias:key}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should return error when readCommunity starts with ${ but is not a valid SCV expression', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:key-}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBe('Invalid SCV expression')
      })

      it('should return error when readCommunity is a malformed SCV prefix with no key', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBe('Invalid SCV expression')
      })

      it('should pass when readCommunity is a valid SCV expression with underscores', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:_alias_:my_key}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should pass when readCommunity is a valid SCV expression with dots', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:group.name:item.key}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should not validate readCommunity as SCV when it does not start with ${', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: 'public' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should not validate readCommunity as SCV when scvEnabledKeys is empty', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:key-}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, new Set())
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should pass when readCommunity is a valid SCV expression with a default value', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:my-key|my-default}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should pass when readCommunity is a valid two-subkey SCV expression with a default value', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:alias:key|my-default}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should pass when readCommunity SCV default value contains special characters', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:key|p@$$w0rd!}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should return error when readCommunity SCV has a pipe but empty default value', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:key|}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBe('Invalid SCV expression')
      })

      it('should return error when readCommunity SCV default value contains a pipe character', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, readCommunity: '${scv:key|val|extra}' }
        const errors = validateDefinition(config, 'v2c', ip, '', '', isSaving, scvEnabledKeys)
        expect(errors.readCommunity).toBe('Invalid SCV expression')
      })
    })

    describe('Complete validation', () => {
      it('should return no errors for completely valid configuration', () => {
        const config: SnmpBaseConfiguration = {
          port: 161,
          maxRequestSize: 65535,
          securityLevel: 1,
          authProtocol: 'SHA',
          privacyProtocol: 'AES',
          timeout: 3000,
          retry: 3,
          maxVarsPerPdu: 10,
          maxRepetitions: 2
        }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '192.168.1.255', '', false, new Set())
        expect(Object.keys(errors)).toHaveLength(0)
      })
    })
  })

  describe('validateSnmpConfiguration', () => {
    const validConfig: SnmpBaseConfiguration = {
      port: 161,
      maxRequestSize: 65535,
      securityLevel: 0,
      authProtocol: 'MD5',
      privacyProtocol: 'DES'
    }

    it('should return no errors for a valid configuration', () => {
      const errors = validateSnmpConfiguration(validConfig, 'v2c')
      expect(Object.keys(errors)).toHaveLength(0)
    })

    it('should return no errors for an empty configuration', () => {
      const errors = validateSnmpConfiguration({}, 'v2c')
      expect(Object.keys(errors)).toHaveLength(0)
    })

    describe('Port validation', () => {
      it('should return error when port is 0', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, port: 0 }, 'v2c')
        expect(errors.port).toBe('Port must be a number between 1 and 65535')
      })

      it('should return error when port exceeds 65535', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, port: 65536 }, 'v2c')
        expect(errors.port).toBe('Port must be a number between 1 and 65535')
      })

      it('should return error when port is NaN', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, port: NaN }, 'v2c')
        expect(errors.port).toBe('Port must be a number between 1 and 65535')
      })

      it('should pass when port is undefined', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, port: undefined }
        const errors = validateSnmpConfiguration(config, 'v2c')
        expect(errors.port).toBeUndefined()
      })
    })

    describe('Max Request Size validation', () => {
      it('should return error when maxRequestSize is below 484', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, maxRequestSize: 483 }, 'v2c')
        expect(errors.maxRequestSize).toBe('If provided, Max Request Size must be a number greater than or equal to 484')
      })

      it('should pass when maxRequestSize is exactly 484', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, maxRequestSize: 484 }, 'v2c')
        expect(errors.maxRequestSize).toBeUndefined()
      })

      it('should pass when maxRequestSize is undefined', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, maxRequestSize: undefined }
        const errors = validateSnmpConfiguration(config, 'v2c')
        expect(errors.maxRequestSize).toBeUndefined()
      })
    })

    describe('Security Level validation', () => {
      it('should return error when securityLevel is invalid for v3', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, securityLevel: 5 }, 'v3')
        expect(errors.securityLevel).toBe('Security Level must be one of: 1 (NoAuthNoPriv), 2 (AuthNoPriv), 3 (AuthPriv)')
      })

      it('should not validate securityLevel for non-v3 versions', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, securityLevel: 5 }, 'v2c')
        expect(errors.securityLevel).toBeUndefined()
      })

      it('should pass with valid security levels for v3', () => {
        [1, 2, 3].forEach((level) => {
          const errors = validateSnmpConfiguration({ ...validConfig, securityLevel: level }, 'v3')
          expect(errors.securityLevel).toBeUndefined()
        })
      })
    })

    describe('Auth Protocol validation', () => {
      it('should return error for an invalid auth protocol', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, authProtocol: 'INVALID' }, 'v2c')
        expect(errors.authProtocol).toBe('Auth Protocol must be one of: MD5, SHA, SHA-224, SHA-256, SHA-512')
      })

      it('should pass when auth protocol is empty string', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, authProtocol: '' }, 'v2c')
        expect(errors.authProtocol).toBeUndefined()
      })

      it('should pass with all valid auth protocols', () => {
        ['MD5', 'SHA', 'SHA-224', 'SHA-256', 'SHA-512'].forEach((protocol) => {
          const errors = validateSnmpConfiguration({ ...validConfig, authProtocol: protocol }, 'v2c')
          expect(errors.authProtocol).toBeUndefined()
        })
      })
    })

    describe('Privacy Protocol validation', () => {
      it('should return error for an invalid privacy protocol', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, privacyProtocol: 'INVALID' }, 'v2c')
        expect(errors.privacyProtocol).toBe('Privacy Protocol must be one of: DES, AES, AES192, AES256')
      })

      it('should pass when privacy protocol is empty string', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, privacyProtocol: '' }, 'v2c')
        expect(errors.privacyProtocol).toBeUndefined()
      })

      it('should pass with all valid privacy protocols', () => {
        ['DES', 'AES', 'AES192', 'AES256'].forEach((protocol) => {
          const errors = validateSnmpConfiguration({ ...validConfig, privacyProtocol: protocol }, 'v2c')
          expect(errors.privacyProtocol).toBeUndefined()
        })
      })
    })

    describe('Numeric fields validation', () => {
      it('should return error when timeout is a float', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, timeout: 3.5 }, 'v2c')
        expect(errors.timeout).toBe('Timeout must be an integer greater than or equal to 0')
      })

      it('should return error when retry is a float', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, retry: 2.7 }, 'v2c')
        expect(errors.retry).toBe('Retries must be an integer greater than or equal to 0')
      })

      it('should return error when maxVarsPerPdu is a float', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, maxVarsPerPdu: 10.5 }, 'v2c')
        expect(errors.maxVarsPerPdu).toBe('Max Vars Per PDU must be an integer greater than or equal to 0')
      })

      it('should return error when maxRepetitions is a float', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, maxRepetitions: 5.3 }, 'v2c')
        expect(errors.maxRepetitions).toBe('Max Repetitions must be an integer greater than or equal to 0')
      })

      it('should return error when ttl is a float', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, ttl: 1.5 }, 'v2c')
        expect(errors.ttl).toBe('TTL must be an integer greater than or equal to 0')
      })

      it('should return error when a numeric field is negative', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, timeout: -1 }, 'v2c')
        expect(errors.timeout).toBe('Timeout must be an integer greater than or equal to 0')
      })

      it('should pass when numeric fields are valid integers', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, timeout: 3000, retry: 3, maxVarsPerPdu: 10, maxRepetitions: 2, ttl: 60 }
        const errors = validateSnmpConfiguration(config, 'v2c')
        expect(errors.timeout).toBeUndefined()
        expect(errors.retry).toBeUndefined()
        expect(errors.maxVarsPerPdu).toBeUndefined()
        expect(errors.maxRepetitions).toBeUndefined()
        expect(errors.ttl).toBeUndefined()

      })

      it('should pass when numeric fields are zero', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, timeout: 0, retry: 0 }
        const errors = validateSnmpConfiguration(config, 'v2c')
        expect(errors.timeout).toBeUndefined()
        expect(errors.retry).toBeUndefined()
      })
    })

    describe('SCV expression validation', () => {
      const scvEnabledKeys = new Set(['readCommunity'])

      it('should pass when readCommunity is a valid SCV expression', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, readCommunity: '${scv:my-key}' }, 'v2c', scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should pass when readCommunity is a valid SCV expression with a default value', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, readCommunity: '${scv:my-key|my-default}' }, 'v2c', scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should pass when readCommunity is a valid two-subkey SCV expression with a default value', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, readCommunity: '${scv:alias:key|my-default}' }, 'v2c', scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should return error when readCommunity is a malformed SCV expression', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, readCommunity: '${scv:key-}' }, 'v2c', scvEnabledKeys)
        expect(errors.readCommunity).toBe('Invalid SCV expression')
      })

      it('should return error when readCommunity SCV has a pipe but empty default value', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, readCommunity: '${scv:key|}' }, 'v2c', scvEnabledKeys)
        expect(errors.readCommunity).toBe('Invalid SCV expression')
      })

      it('should return error when readCommunity SCV default value contains a pipe character', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, readCommunity: '${scv:key|val|extra}' }, 'v2c', scvEnabledKeys)
        expect(errors.readCommunity).toBe('Invalid SCV expression')
      })

      it('should not validate readCommunity as SCV when it does not start with ${', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, readCommunity: 'public' }, 'v2c', scvEnabledKeys)
        expect(errors.readCommunity).toBeUndefined()
      })

      it('should not validate readCommunity as SCV when scvEnabledKeys is empty', () => {
        const errors = validateSnmpConfiguration({ ...validConfig, readCommunity: '${scv:key-}' }, 'v2c')
        expect(errors.readCommunity).toBeUndefined()
      })
    })
  })

  describe('validateProfile', () => {
    it('should return error when label is missing', () => {
      const errors = validateProfile('', 'some-expression')
      expect(errors.label).toBe('SNMP Profile label is required')
    })

    it('should return error when filterExpression is missing', () => {
      const errors = validateProfile('My Profile', '')
      expect(errors.filterExpression).toBe('FilterExpression is required')
    })

    it('should return both errors when both fields are missing', () => {
      const errors = validateProfile('', '')
      expect(errors.label).toBe('SNMP Profile label is required')
      expect(errors.filterExpression).toBe('FilterExpression is required')
    })

    it('should return no errors when both fields are provided', () => {
      const errors = validateProfile('My Profile', 'IPADDR != \'0.0.0.0\'')
      expect(Object.keys(errors)).toHaveLength(0)
    })
  })

  describe('checkForDuplicateDefinitionItems', () => {
    const emptyDefinition: SnmpDefinition = { range: [], specific: [], ipMatch: [] }

    describe('no new items provided', () => {
      it('should return null when definition is empty and no new items are provided', () => {
        expect(checkForDuplicateDefinitionItems(emptyDefinition)).toBeNull()
      })

      it('should return null when definition has items but no new items are provided', () => {
        const definition: SnmpDefinition = {
          range: [{ begin: '10.0.0.1', end: '10.0.0.255' }],
          specific: ['192.168.1.1'],
          ipMatch: ['10.0.0.*']
        }
        expect(checkForDuplicateDefinitionItems(definition)).toBeNull()
      })
    })

    describe('range duplicate detection', () => {
      const existingRange: IpAddressRange = { begin: '10.0.0.1', end: '10.0.0.255' }
      const definition: SnmpDefinition = { range: [existingRange], specific: [], ipMatch: [] }

      it('should return error when newRange is a duplicate', () => {
        expect(checkForDuplicateDefinitionItems(definition, { begin: '10.0.0.1', end: '10.0.0.255' }))
          .toBe('Duplicate range: 10.0.0.1 - 10.0.0.255')
      })

      it('should return null when newRange has a different begin', () => {
        expect(checkForDuplicateDefinitionItems(definition, { begin: '10.0.0.2', end: '10.0.0.255' })).toBeNull()
      })

      it('should return null when newRange has a different end', () => {
        expect(checkForDuplicateDefinitionItems(definition, { begin: '10.0.0.1', end: '10.0.0.254' })).toBeNull()
      })

      it('should return null when definition has no ranges', () => {
        expect(checkForDuplicateDefinitionItems(emptyDefinition, { begin: '10.0.0.1', end: '10.0.0.255' })).toBeNull()
      })
    })

    describe('specific duplicate detection', () => {
      const definition: SnmpDefinition = { range: [], specific: ['192.168.1.1'], ipMatch: [] }

      it('should return error when newSpecific is a duplicate', () => {
        expect(checkForDuplicateDefinitionItems(definition, undefined, '192.168.1.1'))
          .toBe('Duplicate specific IP address: 192.168.1.1')
      })

      it('should return null when newSpecific is not in existing specifics', () => {
        expect(checkForDuplicateDefinitionItems(definition, undefined, '192.168.1.2')).toBeNull()
      })

      it('should return null when definition has no specifics', () => {
        expect(checkForDuplicateDefinitionItems(emptyDefinition, undefined, '192.168.1.1')).toBeNull()
      })
    })

    describe('ipMatch duplicate detection', () => {
      const definition: SnmpDefinition = { range: [], specific: [], ipMatch: ['10.0.0.*'] }

      it('should return error when newIpMatch is a duplicate', () => {
        expect(checkForDuplicateDefinitionItems(definition, undefined, undefined, '10.0.0.*'))
          .toBe('Duplicate IP Match expression: 10.0.0.*')
      })

      it('should return null when newIpMatch is not in existing ipMatches', () => {
        expect(checkForDuplicateDefinitionItems(definition, undefined, undefined, '10.0.1.*')).toBeNull()
      })

      it('should return null when definition has no ipMatches', () => {
        expect(checkForDuplicateDefinitionItems(emptyDefinition, undefined, undefined, '10.0.0.*')).toBeNull()
      })
    })
  })
})
