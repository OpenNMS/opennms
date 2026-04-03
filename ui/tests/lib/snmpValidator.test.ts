import { describe, it, expect } from 'vitest'
import { IP_MATCH_ERROR, validateDefinition, validateProfile } from '@/lib/snmpValidator'
import { SnmpBaseConfiguration } from '@/types/snmpConfig'

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
        const errors = validateDefinition(validConfig, '', '192.168.1.1', '', '')
        expect(errors.snmpVersion).toBe('SNMP Version is required')
      })

      it('should return error when SNMP version is invalid', () => {
        const errors = validateDefinition(validConfig, 'v4', '192.168.1.1', '', '')
        expect(errors.snmpVersion).toBe('SNMP Version must be one of: v1, v2c, v3')
      })

      it('should pass validation with valid SNMP versions', () => {
        const versions = ['v1', 'v2c', 'v3']
        versions.forEach(version => {
          const errors = validateDefinition(validConfig, version, '192.168.1.1', '', '')
          expect(errors.snmpVersion).toBeUndefined()
        })
      })
    })

    describe('IP Address validation', () => {
      it('should return invalidRangeConfig error when no range, specific, or ipMatch is provided', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '', '')
        expect(errors.invalidRangeConfig).toBe('You must specify either a range, specific or IP Match expression')
      })

      it('should not return invalidRangeConfig when a specific IP is provided', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', '', '')
        expect(errors.invalidRangeConfig).toBeUndefined()
      })

      it('should not return invalidRangeConfig when a range is provided', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', '192.168.1.255', '')
        expect(errors.invalidRangeConfig).toBeUndefined()
      })

      it('should not return invalidRangeConfig when an ipMatch is provided', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '', '192.168.1.*')
        expect(errors.invalidRangeConfig).toBeUndefined()
      })

      it('should return error when first IP address is invalid (specific)', () => {
        const errors = validateDefinition(validConfig, 'v2c', 'invalid-ip', '', '')
        expect(errors.firstIpAddress).toBe('First IP Address must be a valid IP address')
      })

      it('should return error when first IP address is invalid (range)', () => {
        const errors = validateDefinition(validConfig, 'v2c', 'invalid-ip', '192.168.1.255', '')
        expect(errors.firstIpAddress).toBe('First IP Address must be a valid IP address')
      })

      it('should pass validation with valid IPv4 specific address', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', '', '')
        expect(errors.firstIpAddress).toBeUndefined()
      })

      it('should pass validation with valid IPv6 specific address', () => {
        const errors = validateDefinition(validConfig, 'v2c', '2001:0db8:85a3:0000:0000:8a2e:0370:7334', '', '')
        expect(errors.firstIpAddress).toBeUndefined()
      })

      it('should return error when last IP address is invalid in a range', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', 'invalid-ip', '')
        expect(errors.lastIpAddress).toBe('If provided, last IP Address must be a valid IP address')
      })

      it('should not validate last IP address when it is empty (specific mode)', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', '', '')
        expect(errors.lastIpAddress).toBeUndefined()
      })

      it('should pass validation with valid range', () => {
        const errors = validateDefinition(validConfig, 'v2c', '192.168.1.1', '192.168.1.255', '')
        expect(errors.firstIpAddress).toBeUndefined()
        expect(errors.lastIpAddress).toBeUndefined()
      })

      it('should not validate IP addresses when only ipMatch is provided', () => {
        const errors = validateDefinition(validConfig, 'v2c', '', '', '192.168.1.*')
        expect(errors.firstIpAddress).toBeUndefined()
        expect(errors.lastIpAddress).toBeUndefined()
      })

      it('should return error for invalid IP match expressions', () => {
        const invalidIplikeExpressions = ['invalid-ip', 'iplike 192.168.*.*', '192.168.*']

        invalidIplikeExpressions.forEach(expr => {
          const errors = validateDefinition(validConfig, 'v2c', '', '', expr)
          expect(errors.ipMatch).toBe(IP_MATCH_ERROR)
        })
      })
    })

    describe('Port validation', () => {
      it('should return error when port is less than 1', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, port: 0 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.port).toBe('Port must be a number between 1 and 65535')
      })

      it('should return error when port is a negative number', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, port: -1 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.port).toBe('Port must be a number between 1 and 65535')
      })

      it('should return error when port is greater than 65535', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, port: 65536 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.port).toBe('Port must be a number between 1 and 65535')
      })

      it('should return error when port is not a number', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, port: NaN }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.port).toBe('Port must be a number between 1 and 65535')
      })

      it('should pass validation with valid port', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, port: 161 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.port).toBeUndefined()
      })
    })

    describe('Max Request Size validation', () => {
      it('should return error when maxRequestSize is less than 484', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, maxRequestSize: 483 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.maxRequestSize).toBe('If provided, Max Request Size must be a number greater than or equal to 484')
      })

      it('should pass validation when maxRequestSize is 484', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, maxRequestSize: 484 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.maxRequestSize).toBeUndefined()
      })

      it('should pass validation with valid maxRequestSize', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, maxRequestSize: 65535 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.maxRequestSize).toBeUndefined()
      })
    })

    describe('Security Level validation', () => {
      it('should ignore security level when SNMP version is not v3', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, securityLevel: 5 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.securityLevel).toBeUndefined()
      })

      it('should return error when security level is invalid', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, securityLevel: 5 }
        const errors = validateDefinition(config, 'v3', '192.168.1.1', '', '')
        expect(errors.securityLevel).toBe('Security Level must be one of: 1 (NoAuthNoPriv), 2 (AuthNoPriv), 3 (AuthPriv)')
      })

      it('should pass validation with valid security levels', () => {
        const validLevels = [1, 2, 3]
        validLevels.forEach(level => {
          const config: SnmpBaseConfiguration = { ...validConfig, securityLevel: level }
          const errors = validateDefinition(config, 'v3', '192.168.1.1', '', '')
          expect(errors.securityLevel).toBeUndefined()
        })
      })
    })

    describe('Auth Protocol validation', () => {
      it('should return error when auth protocol is invalid', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, authProtocol: 'INVALID' }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.authProtocol).toBe('Auth Protocol must be one of: MD5, SHA, SHA-224, SHA-256, SHA-512')
      })

      it('should pass validation with valid auth protocols', () => {
        const validProtocols = ['MD5', 'SHA', 'SHA-224', 'SHA-256', 'SHA-512']
        validProtocols.forEach(protocol => {
          const config: SnmpBaseConfiguration = { ...validConfig, authProtocol: protocol }
          const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
          expect(errors.authProtocol).toBeUndefined()
        })
      })

      it('should pass validation when auth protocol is empty string', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, authProtocol: '' }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.authProtocol).toBeUndefined()
      })
    })

    describe('Privacy Protocol validation', () => {
      it('should return error when privacy protocol is invalid', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, privacyProtocol: 'INVALID' }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.privacyProtocol).toBe('Privacy Protocol must be one of: DES, AES, AES192, AES256')
      })

      it('should pass validation with valid privacy protocols', () => {
        const validProtocols = ['DES', 'AES', 'AES192', 'AES256']
        validProtocols.forEach(protocol => {
          const config: SnmpBaseConfiguration = { ...validConfig, privacyProtocol: protocol }
          const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
          expect(errors.privacyProtocol).toBeUndefined()
        })
      })

      it('should pass validation when privacy protocol is empty string', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, privacyProtocol: '' }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.privacyProtocol).toBeUndefined()
      })
    })

    describe('Numeric fields validation', () => {
      it('should return error when timeout is not an integer', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, timeout: 3.5 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.timeout).toBe('Timeout must be an integer greater than or equal to 0')
      })

      it('should return error when retry is not an integer', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, retry: 2.7 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.retry).toBe('Retries must be an integer greater than or equal to 0')
      })

      it('should return error when maxVarsPerPdu is not an integer', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, maxVarsPerPdu: 10.5 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.maxVarsPerPdu).toBe('Max Vars Per PDU must be an integer greater than or equal to 0')
      })

      it('should return error when maxRepetitions is not an integer', () => {
        const config: SnmpBaseConfiguration = { ...validConfig, maxRepetitions: 5.3 }
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
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
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '', '')
        expect(errors.timeout).toBeUndefined()
        expect(errors.retry).toBeUndefined()
        expect(errors.maxVarsPerPdu).toBeUndefined()
        expect(errors.maxRepetitions).toBeUndefined()
      })
    })

    describe('Empty validation', () => {
      it('should return an error for an empty configuration', () => {
        const config: SnmpBaseConfiguration = {}
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '192.168.1.255', '')
        expect(Object.keys(errors)).toHaveLength(0)
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
        const errors = validateDefinition(config, 'v2c', '192.168.1.1', '192.168.1.255', '')
        expect(Object.keys(errors)).toHaveLength(0)
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
})
