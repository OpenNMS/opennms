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

import { describe, it, expect, beforeEach, vi } from 'vitest'
import {
  getSnmpConfig,
  lookupSnmpConfig,
  saveSnmpDefinition,
  deleteSnmpDefinition,
  saveSnmpProfile,
  deleteSnmpProfile,
  downloadSnmpConfig,
  uploadSnmpConfig,
  saveSnmpConfigDefaultOverrides
} from '@/services/snmpConfigService'
import { v2 } from '@/services/axiosInstances'
import { SnmpDefinition, SnmpProfile, SnmpBaseConfiguration } from '@/types/snmpConfig'

vi.mock('@/services/axiosInstances', () => ({
  v2: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

describe('snmpConfigService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getSnmpConfig', () => {
    it('should call GET /snmp-config', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: {}})

      await getSnmpConfig()

      expect(v2.get).toHaveBeenCalledWith('/snmp-config')
    })
  })

  describe('lookupSnmpConfig', () => {
    it('should call GET /snmp-config/lookup with ipAddress and location params', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { version: 2 }})

      await lookupSnmpConfig('192.168.1.1', 'Default')

      expect(v2.get).toHaveBeenCalledWith(
        '/snmp-config/lookup?ipAddress=192.168.1.1&location=Default'
      )
    })

    it('should encode special characters in ipAddress and location', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { version: 2 }})

      await lookupSnmpConfig('192.168.1.1/24', 'Location With Spaces')

      expect(v2.get).toHaveBeenCalledWith(
        '/snmp-config/lookup?ipAddress=192.168.1.1%2F24&location=Location%20With%20Spaces'
      )
    })
  })

  describe('saveSnmpDefinition', () => {
    it('should call PUT /snmp-config/definition with definition data', async () => {
      vi.mocked(v2.put).mockResolvedValue({ status: 201 })

      const definition: SnmpDefinition = {
        id: 1,
        readCommunity: 'public',
        location: 'Default',
        version: 'v2c',
        range: [],
        specific: [],
        ipMatch: []
      }

      await saveSnmpDefinition(definition)

      expect(v2.put).toHaveBeenCalledWith('/snmp-config/definition', definition)
    })
  })

  describe('deleteSnmpDefinition', () => {
    it('should call DELETE /snmp-config/definition with ranges param', async () => {
      vi.mocked(v2.delete).mockResolvedValue({ status: 204 })

      const ranges = [{ begin: '192.168.1.1', end: '192.168.1.100' }]

      await deleteSnmpDefinition(ranges, null, null, 'Default')

      expect(v2.delete).toHaveBeenCalledWith(
        '/snmp-config/definition?ranges=192.168.1.1-192.168.1.100&location=Default'
      )
    })

    it('should call DELETE /snmp-config/definition with specifics param', async () => {
      vi.mocked(v2.delete).mockResolvedValue({ status: 204 })

      const specifics = ['192.168.1.1', '192.168.1.2']

      await deleteSnmpDefinition(null, specifics, null, 'Default')

      expect(v2.delete).toHaveBeenCalledWith(
        '/snmp-config/definition?specifics=192.168.1.1%2C192.168.1.2&location=Default'
      )
    })

    it('should call DELETE /snmp-config/definition with both ranges and specifics params', async () => {
      vi.mocked(v2.delete).mockResolvedValue({ status: 204 })

      const ranges = [{ begin: '10.0.0.1', end: '10.0.0.50' }]
      const specifics = ['192.168.1.1']

      await deleteSnmpDefinition(ranges, specifics, null, 'Location1')

      expect(v2.delete).toHaveBeenCalledWith(
        '/snmp-config/definition?ranges=10.0.0.1-10.0.0.50&specifics=192.168.1.1&location=Location1'
      )
    })

    it('should call DELETE /snmp-config/definition with multiple ranges', async () => {
      vi.mocked(v2.delete).mockResolvedValue({ status: 204 })

      const ranges = [
        { begin: '10.0.0.1', end: '10.0.0.50' },
        { begin: '172.16.0.1', end: '172.16.0.100' }
      ]

      await deleteSnmpDefinition(ranges, null, null, 'Default')

      expect(v2.delete).toHaveBeenCalledWith(
        '/snmp-config/definition?ranges=10.0.0.1-10.0.0.50%2C172.16.0.1-172.16.0.100&location=Default'
      )
    })

    it('should call DELETE /snmp-config/definition with multiple ranges and multiple specifics', async () => {
      vi.mocked(v2.delete).mockResolvedValue({ status: 204 })

      const ranges = [
        { begin: '10.0.0.1', end: '10.0.0.50' },
        { begin: '172.16.0.1', end: '172.16.0.100' }
      ]
      const specifics = ['192.168.1.1', '192.168.1.2', '192.168.1.10']

      await deleteSnmpDefinition(ranges, specifics, null, 'Location1')

      expect(v2.delete).toHaveBeenCalledWith(
        '/snmp-config/definition?ranges=10.0.0.1-10.0.0.50%2C172.16.0.1-172.16.0.100&specifics=192.168.1.1%2C192.168.1.2%2C192.168.1.10&location=Location1'
      )
    })
  })

  describe('saveSnmpProfile', () => {
    it('should call POST /snmp-config/profile with profile data', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 204 })

      const profile: SnmpProfile = {
        id: 1,
        label: 'Test Profile',
        readCommunity: 'public',
        filter: 'ip like 10.0.0.*'
      }

      await saveSnmpProfile(profile)

      expect(v2.post).toHaveBeenCalledWith('/snmp-config/profile', profile)
    })
  })

  describe('deleteSnmpProfile', () => {
    it('should call DELETE /snmp-config/profile with label param', async () => {
      vi.mocked(v2.delete).mockResolvedValue({ status: 204 })

      await deleteSnmpProfile('Test Profile')

      expect(v2.delete).toHaveBeenCalledWith('/snmp-config/profile?label=Test%20Profile')
    })

    it('should encode special characters in label', async () => {
      vi.mocked(v2.delete).mockResolvedValue({ status: 204 })

      await deleteSnmpProfile('Profile/With&Special=Chars')

      expect(v2.delete).toHaveBeenCalledWith(
        '/snmp-config/profile?label=Profile%2FWith%26Special%3DChars'
      )
    })
  })

  describe('downloadSnmpConfig', () => {
    it('should call GET /snmp-config/download with format=xml when isXml is true', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: new Blob() })

      await downloadSnmpConfig(true)

      expect(v2.get).toHaveBeenCalledWith(
        '/snmp-config/download?format=xml',
        { responseType: 'blob' }
      )
    })

    it('should call GET /snmp-config/download with format=json when isXml is false', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: new Blob() })

      await downloadSnmpConfig(false)

      expect(v2.get).toHaveBeenCalledWith(
        '/snmp-config/download?format=json',
        { responseType: 'blob' }
      )
    })
  })

  describe('uploadSnmpConfig', () => {
    it('should call POST /snmp-config/upload when isXml is false', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 200 })

      const file = new File(['{}'], 'config.json', { type: 'application/json' })

      await uploadSnmpConfig(file, false)

      expect(v2.post).toHaveBeenCalledWith(
        '/snmp-config/upload',
        expect.any(FormData)
      )

      const formData = vi.mocked(v2.post).mock.calls[0][1] as FormData
      expect(formData.get('upload')).toBe(file)
    })

    it('should call POST /snmp-config/upload/xml when isXml is true', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 200 })

      const file = new File(['<xml/>'], 'config.xml', { type: 'application/xml' })

      await uploadSnmpConfig(file, true)

      expect(v2.post).toHaveBeenCalledWith(
        '/snmp-config/upload/xml',
        expect.any(FormData)
      )

      const formData = vi.mocked(v2.post).mock.calls[0][1] as FormData
      expect(formData.get('upload')).toBe(file)
    })
  })

  describe('saveSnmpConfigDefaultOverrides', () => {
    it('should call POST /snmp-config/defaults with config data and return success on 204 status', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 204 })

      const config: SnmpBaseConfiguration = {
        readCommunity: 'public',
        writeCommunity: 'private',
        version: 'v2c',
        timeout: 3000,
        retry: 3,
        port: 161
      }

      const result = await saveSnmpConfigDefaultOverrides(config)

      expect(v2.post).toHaveBeenCalledWith('/snmp-config/defaults', config)
      expect(result.success).toBe(true)
      expect(result.message).toBe('')
    })

    it('should return failure result with "Invalid SNMP configuration data" message on 400 status', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 400 })

      const config: SnmpBaseConfiguration = {
        readCommunity: 'public',
        version: 'invalid'
      }

      const result = await saveSnmpConfigDefaultOverrides(config)

      expect(v2.post).toHaveBeenCalledWith('/snmp-config/defaults', config)
      expect(result.success).toBe(false)
      expect(result.message).toBe('Invalid SNMP configuration data')
    })

    it('should return failure result with generic message on non-204/400 status', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 500 })

      const config: SnmpBaseConfiguration = {
        readCommunity: 'public',
        version: 'v2c'
      }

      const result = await saveSnmpConfigDefaultOverrides(config)

      expect(v2.post).toHaveBeenCalledWith('/snmp-config/defaults', config)
      expect(result.success).toBe(false)
      expect(result.message).toBe('Failed to save SNMP configuration defaults')
    })

    it('should handle exceptions and return failure result', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(v2.post).mockRejectedValue(new Error('Network error'))

      const config: SnmpBaseConfiguration = {
        readCommunity: 'public',
        version: 'v3',
        securityName: 'admin'
      }

      const result = await saveSnmpConfigDefaultOverrides(config)

      expect(v2.post).toHaveBeenCalledWith('/snmp-config/defaults', config)
      expect(result.success).toBe(false)
      expect(result.message).toBe('Failed to save SNMP configuration defaults')
      expect(consoleErrorSpy).toHaveBeenCalledWith(
        'Error saving SNMP config defaults:',
        expect.any(Error)
      )
      consoleErrorSpy.mockRestore()
    })

    it('should work with SNMPv3 configuration', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 204 })

      const config: SnmpBaseConfiguration = {
        version: 'v3',
        securityName: 'snmpuser',
        securityLevel: 3,
        authPassphrase: 'authpass',
        authProtocol: 'SHA',
        privacyPassphrase: 'privpass',
        privacyProtocol: 'AES128',
        timeout: 5000,
        retry: 2
      }

      const result = await saveSnmpConfigDefaultOverrides(config)

      expect(v2.post).toHaveBeenCalledWith('/snmp-config/defaults', config)
      expect(result.success).toBe(true)
    })

    it('should work with minimal config', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 204 })

      const config: SnmpBaseConfiguration = {
        version: 'v1',
        readCommunity: 'public'
      }

      const result = await saveSnmpConfigDefaultOverrides(config)

      expect(v2.post).toHaveBeenCalledWith('/snmp-config/defaults', config)
      expect(result.success).toBe(true)
    })

    it('should work with advanced options', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 204 })

      const config: SnmpBaseConfiguration = {
        version: 'v2c',
        readCommunity: 'public',
        proxyHost: 'proxy.example.com',
        maxVarsPerPdu: 10,
        maxRepetitions: 2,
        maxRequestSize: 65535,
        port: 1161,
        ttl: 60000
      }

      const result = await saveSnmpConfigDefaultOverrides(config)

      expect(v2.post).toHaveBeenCalledWith('/snmp-config/defaults', config)
      expect(result.success).toBe(true)
    })
  })
})
