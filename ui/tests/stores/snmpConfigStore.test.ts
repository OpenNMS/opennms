import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useSnmpConfigStore, SnmpLookupEditMode, SnmpConfigEditMode, ActiveTabs, AdvancedSubtabs, getDefaultSnmpBaseConfiguration, getDefaultSnmpDefinition, getDefaultSnmpProfile, getEmptySnmpConfig, getDefaultSnmpConfig, getMockSnmpConfiguration } from '@/stores/snmpConfigStore'
import {
  deleteSnmpDefinition,
  deleteSnmpProfile,
  getSnmpConfig,
  lookupSnmpConfig,
  saveSnmpDefinition,
  saveSnmpProfile
} from '@/services/snmpConfigService'
import { getMonitoringLocations } from '@/services/monitoringLocationService'
import { IpAddressRange, SnmpAgentConfig, SnmpConfig, SnmpDefinition, SnmpProfile } from '@/types/snmpConfig'
import { MonitoringLocationApiResponse } from '@/types'
import { createSuccessResponse, createFailureResult } from '@/types/validation'
import {
  DEFAULT_MONITORING_LOCATION,
  DEFAULT_SNMP_MAX_REPETITIONS,
  DEFAULT_SNMP_MAX_REQUEST_SIZE,
  DEFAULT_SNMP_MAX_VARS_PER_PDU,
  DEFAULT_SNMP_PORT,
  DEFAULT_SNMP_READ_COMMUNITY_STRING,
  DEFAULT_SNMP_RETRIES,
  DEFAULT_SNMP_TIMEOUT,
  DEFAULT_SNMP_TTL,
  DEFAULT_SNMP_V3_AUTH_PASSPHRASE,
  DEFAULT_SNMP_V3_AUTH_PROTOCOL,
  DEFAULT_SNMP_V3_PRIVACY_PASSPHRASE,
  DEFAULT_SNMP_V3_PRIVACY_PROTOCOL,
  DEFAULT_SNMP_V3_SECURITY_LEVEL,
  DEFAULT_SNMP_V3_SECURITY_NAME,
  DEFAULT_SNMP_VERSION,
  DEFAULT_SNMP_WRITE_COMMUNITY_STRING
} from '@/lib/constants'

vi.mock('@/services/snmpConfigService', () => ({
  deleteSnmpDefinition: vi.fn(),
  deleteSnmpProfile: vi.fn(),
  getSnmpConfig: vi.fn(),
  lookupSnmpConfig: vi.fn(),
  saveSnmpDefinition: vi.fn(),
  saveSnmpProfile: vi.fn()
}))

vi.mock('@/services/monitoringLocationService', () => ({
  getMonitoringLocations: vi.fn()
}))

describe('useSnmpConfigStore', () => {
  let store: ReturnType<typeof useSnmpConfigStore>

  const mockMonitoringLocations: MonitoringLocationApiResponse = {
    count: 2,
    offset: 0,
    totalCount: 2,
    location: [
      {
        'location-name': 'Default',
        'monitoring-area': 'localhost',
        latitude: 0,
        longitude: 0,
        priority: 100,
        tags: [],
        geolocation: null,
        name: 'Default',
        area: 'localhost'
      },
      {
        'location-name': 'Location1',
        'monitoring-area': 'area1',
        latitude: 10,
        longitude: 20,
        priority: 100,
        tags: [],
        geolocation: null,
        name: 'Location1',
        area: 'area1'
      }
    ] as any
  }

  const mockSnmpConfig: SnmpConfig = {
    definition: [
      {
        id: 1,
        readCommunity: 'public',
        writeCommunity: 'private',
        encrypted: false,
        version: 'v2c',
        range: [
          {
            begin: '10.0.0.0',
            end: '10.0.0.99'
          }
        ],
        specific: [],
        ipMatch: [],
        location: DEFAULT_MONITORING_LOCATION,
        profileLabel: ''
      }
    ],
    profiles: {
      profile: [
        {
          id: 0,
          readCommunity: 'public',
          writeCommunity: 'private',
          encrypted: false,
          label: 'Test Profile',
          filter: 'ip like 10.0.0.*'
        }
      ]
    }
  }

  const mockSnmpAgentConfig: SnmpAgentConfig = {
    location: DEFAULT_MONITORING_LOCATION,
    version: 'v2c',
    port: 161,
    retry: 3,
    timeout: 3000,
    readCommunity: 'public',
    writeCommunity: 'private',
    maxVarsPerPdu: 10,
    maxRepetitions: 2,
    maxRequestSize: 65535,
    securityName: '',
    securityLevel: 1 as any,
    authPassphrase: '',
    authProtocol: '',
    privacyPassphrase: '',
    privacyProtocol: '',
    engineId: '',
    contextEngineId: '',
    contextName: '',
    enterpriseId: '',
    proxyHost: '',
    ttl: 0,
    profileLabel: ''
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useSnmpConfigStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      expect(store.config).toEqual({
        definition: [],
        profiles: {
          profile: []
        }
      })
      expect(store.isLoading).toBe(false)
      expect(store.activeTab).toBe(0)
      expect(store.activeAdvancedSubtab).toBe(0)
      expect(store.currentDefinition).toBeUndefined()
      expect(store.definitionCreateEditMode).toBe(SnmpConfigEditMode.Table)
      expect(store.profileLabel).toBe('')
      expect(store.snmpProfileEditMode).toBe(SnmpConfigEditMode.Table)
      expect(store.monitoringLocations).toEqual([])
      expect(store.snmpLookupEditMode).toBe(SnmpLookupEditMode.Lookup)
    })
  })

  describe('Helper Functions', () => {
    it('should return default SNMP base configuration', () => {
      const defaultConfig = getDefaultSnmpBaseConfiguration()
      expect(defaultConfig.id).toBe(0)
      expect(defaultConfig.version).toBe(DEFAULT_SNMP_VERSION)
      expect(defaultConfig.port).toBe(161)
      expect(defaultConfig.retry).toBe(1)
      expect(defaultConfig.timeout).toBe(3000)
      expect(defaultConfig.encrypted).toBe(false)
    })

    it('should return default SNMP definition', () => {
      const defaultDefinition = getDefaultSnmpDefinition()
      expect(defaultDefinition.readCommunity).toBe('public')
      expect(defaultDefinition.writeCommunity).toBe('private')
      expect(defaultDefinition.encrypted).toBe(false)
      expect(defaultDefinition.range).toEqual([])
      expect(defaultDefinition.specific).toEqual([])
      expect(defaultDefinition.location).toBe(DEFAULT_MONITORING_LOCATION)
    })

    it('should return default SNMP profile', () => {
      const defaultProfile = getDefaultSnmpProfile()
      expect(defaultProfile.label).toBe('')
      expect(defaultProfile.filter).toBe('')
      expect(defaultProfile.readCommunity).toBe('')
      expect(defaultProfile.encrypted).toBe(false)
    })

    it('should return empty SNMP config', () => {
      const emptyConfig = getEmptySnmpConfig()
      expect(emptyConfig.definition).toEqual([])
      expect(emptyConfig.profiles.profile).toEqual([])
    })

    it('should return default SNMP config with one definition', () => {
      const defaultConfig = getDefaultSnmpConfig()
      expect(defaultConfig.definition).toHaveLength(1)
      expect(defaultConfig.definition[0].id).toBe(0)
      expect(defaultConfig.profiles.profile).toEqual([])
    })

    it('should return mock SNMP configuration', () => {
      const mockConfig = getMockSnmpConfiguration()
      expect(mockConfig.definition).toHaveLength(2)
      expect(mockConfig.profiles.profile).toHaveLength(1)
    })
  })

  describe('State Management', () => {
    it('should set active tab', () => {
      store.setActiveTab(ActiveTabs.BrowseDefinitions)
      expect(store.activeTab).toBe(ActiveTabs.BrowseDefinitions)

      store.setActiveTab(ActiveTabs.Advanced)
      expect(store.activeTab).toBe(ActiveTabs.Advanced)

      store.setActiveTab(ActiveTabs.Lookup)
      expect(store.activeTab).toBe(ActiveTabs.Lookup)
    })

    it('should set definition create edit mode', () => {
      store.setDefinitionCreateEditMode(SnmpConfigEditMode.Edit)
      expect(store.definitionCreateEditMode).toBe(SnmpConfigEditMode.Edit)

      store.setDefinitionCreateEditMode(SnmpConfigEditMode.Create)
      expect(store.definitionCreateEditMode).toBe(SnmpConfigEditMode.Create)

      store.setDefinitionCreateEditMode(SnmpConfigEditMode.Table)
      expect(store.definitionCreateEditMode).toBe(SnmpConfigEditMode.Table)
    })

    it('should set SNMP lookup edit mode', () => {
      store.setSnmpLookupEditMode(SnmpLookupEditMode.Edit)
      expect(store.snmpLookupEditMode).toBe(SnmpLookupEditMode.Edit)

      store.setSnmpLookupEditMode(SnmpLookupEditMode.Lookup)
      expect(store.snmpLookupEditMode).toBe(SnmpLookupEditMode.Lookup)
    })

    it('should set SNMP profile edit mode', () => {
      store.setSnmpProfileEditMode(SnmpConfigEditMode.Edit)
      expect(store.snmpProfileEditMode).toBe(SnmpConfigEditMode.Edit)

      store.setSnmpProfileEditMode(SnmpConfigEditMode.Create)
      expect(store.snmpProfileEditMode).toBe(SnmpConfigEditMode.Create)

      store.setSnmpProfileEditMode(SnmpConfigEditMode.Table)
      expect(store.snmpProfileEditMode).toBe(SnmpConfigEditMode.Table)
    })

    it('should set current definition', () => {
      const definition = getDefaultSnmpDefinition()
      store.setCurrentDefinition(definition)
      expect(store.currentDefinition).toEqual(definition)
    })

    it('should reset current definition', () => {
      const customDefinition = {
        ...getDefaultSnmpDefinition(),
        readCommunity: 'custom'
      }
      store.setCurrentDefinition(customDefinition)
      expect(store.currentDefinition?.readCommunity).toBe('custom')

      store.resetCurrentDefinition()
      expect(store.currentDefinition).toEqual(getDefaultSnmpDefinition())
    })

    it('should set profile label', () => {
      store.setProfileLabel('Test Label')
      expect(store.profileLabel).toBe('Test Label')
    })

    it('should set active advanced subtab', () => {
      store.setActiveAdvancedSubtab(AdvancedSubtabs.Profiles)
      expect(store.activeAdvancedSubtab).toBe(AdvancedSubtabs.Profiles)

      store.setActiveAdvancedSubtab(AdvancedSubtabs.UploadDownload)
      expect(store.activeAdvancedSubtab).toBe(AdvancedSubtabs.UploadDownload)

      store.setActiveAdvancedSubtab(AdvancedSubtabs.DefaultOverrides)
      expect(store.activeAdvancedSubtab).toBe(AdvancedSubtabs.DefaultOverrides)
    })

    it('should reset state', () => {
      store.setActiveTab(2)
      store.setActiveAdvancedSubtab(AdvancedSubtabs.UploadDownload)
      store.setDefinitionCreateEditMode(SnmpConfigEditMode.Edit)
      store.setSnmpLookupEditMode(SnmpLookupEditMode.Edit)
      store.setProfileLabel('Test')
      store.setSnmpProfileEditMode(SnmpConfigEditMode.Create)
      store.setCurrentDefinition({ ...getDefaultSnmpDefinition(), readCommunity: 'custom' })

      store.resetState()

      expect(store.isLoading).toBe(false)
      expect(store.activeTab).toBe(0)
      expect(store.activeAdvancedSubtab).toBe(0)
      expect(store.currentDefinition).toEqual(getDefaultSnmpDefinition())
      expect(store.definitionCreateEditMode).toBe(SnmpConfigEditMode.Table)
      expect(store.snmpLookupEditMode).toBe(SnmpLookupEditMode.Lookup)
      expect(store.profileLabel).toBe('')
      expect(store.snmpProfileEditMode).toBe(SnmpConfigEditMode.Table)
    })
  })

  describe('populateSnmpConfig', () => {
    it('should populate SNMP config successfully', async () => {
      vi.mocked(getSnmpConfig).mockResolvedValue(mockSnmpConfig)

      await store.populateSnmpConfig()

      expect(getSnmpConfig).toHaveBeenCalledTimes(1)
      expect(store.config).toEqual(mockSnmpConfig)
    })

    it('should not update state when getSnmpConfig returns false', async () => {
      vi.mocked(getSnmpConfig).mockResolvedValue(false)
      const initialConfig = { ...store.config }

      await store.populateSnmpConfig()

      expect(store.config).toEqual(initialConfig)
    })

    it('should not update state when getSnmpConfig returns null', async () => {
      vi.mocked(getSnmpConfig).mockResolvedValue(null as any)
      const initialConfig = { ...store.config }

      await store.populateSnmpConfig()

      expect(store.config).toEqual(initialConfig)
    })
  })

  describe('fetchMonitoringLocations', () => {
    it('should fetch monitoring locations successfully', async () => {
      vi.mocked(getMonitoringLocations).mockResolvedValue(mockMonitoringLocations)

      await store.fetchMonitoringLocations()

      expect(getMonitoringLocations).toHaveBeenCalledTimes(1)
      expect(store.monitoringLocations).toEqual(mockMonitoringLocations.location)
    })

    it('should not update state when getMonitoringLocations returns false', async () => {
      vi.mocked(getMonitoringLocations).mockResolvedValue(false)
      const initialLocations = [...store.monitoringLocations]

      await store.fetchMonitoringLocations()

      expect(store.monitoringLocations).toEqual(initialLocations)
    })
  })

  describe('lookupIpAddress', () => {
    it('should lookup IP address successfully', async () => {
      vi.mocked(lookupSnmpConfig).mockResolvedValue(mockSnmpAgentConfig)

      const result = await store.lookupIpAddress('10.0.0.1', DEFAULT_MONITORING_LOCATION)

      expect(lookupSnmpConfig).toHaveBeenCalledWith('10.0.0.1', DEFAULT_MONITORING_LOCATION)
      expect(result).toEqual(mockSnmpAgentConfig)
    })

    it('should return null when lookup fails', async () => {
      vi.mocked(lookupSnmpConfig).mockResolvedValue(false)

      const result = await store.lookupIpAddress('10.0.0.1', DEFAULT_MONITORING_LOCATION)

      expect(result).toBeNull()
    })
  })

  describe('saveDefinition', () => {
    it('should save definition with specific IP successfully', async () => {
      const successResponse = createSuccessResponse()
      vi.mocked(saveSnmpDefinition).mockResolvedValue(successResponse)

      const definition = {
        ...mockSnmpAgentConfig,
        range: [] as IpAddressRange[],
        specific: ['10.0.0.1']
      } as SnmpDefinition

      const result = await store.saveDefinition(definition)

      expect(saveSnmpDefinition).toHaveBeenCalledTimes(1)
      const callArg = vi.mocked(saveSnmpDefinition).mock.calls[0][0]
      expect(callArg.specific).toEqual(['10.0.0.1'])
      expect(callArg.range).toEqual([])
      expect(callArg.readCommunity).toBe('public')
      expect(result).toEqual(successResponse)
    })

    it('should save definition with IP range successfully', async () => {
      const successResponse = createSuccessResponse()
      vi.mocked(saveSnmpDefinition).mockResolvedValue(successResponse)

      const definition = {
        ...mockSnmpAgentConfig,
        range: [{ begin: '10.0.0.1', end: '10.0.0.100'}],
        specific: [] as string[]
      } as SnmpDefinition

      const result = await store.saveDefinition(definition)

      expect(saveSnmpDefinition).toHaveBeenCalledTimes(1)
      const callArg = vi.mocked(saveSnmpDefinition).mock.calls[0][0]
      expect(callArg.specific).toEqual([])
      expect(callArg.range).toEqual([{ begin: '10.0.0.1', end: '10.0.0.100' }])
      expect(result).toEqual(successResponse)
    })

    it('should include all SNMP configuration fields in saved definition', async () => {
      const successResponse = createSuccessResponse()
      vi.mocked(saveSnmpDefinition).mockResolvedValue(successResponse)

      const customConfig: SnmpAgentConfig = {
        ...mockSnmpAgentConfig,
        version: 'v3',
        port: 162,
        retry: 5,
        timeout: 5000,
        maxVarsPerPdu: 20,
        maxRepetitions: 5,
        securityName: 'testUser',
        securityLevel: 3 as any,
        authPassphrase: 'authPass',
        authProtocol: 'MD5',
        privacyPassphrase: 'privPass',
        privacyProtocol: 'DES',
        engineId: 'engine123',
        contextEngineId: 'contextEngine123',
        contextName: 'contextTest',
        enterpriseId: 'enterprise123',
        maxRequestSize: 32768,
        proxyHost: 'proxy.example.com',
        ttl: 3600,
        profileLabel: 'CustomProfile'
      }

      const definition = {
        ...customConfig,
        range: [] as IpAddressRange[],
        specific: ['10.0.0.1']
      } as SnmpDefinition

      await store.saveDefinition(definition)

      const callArg = vi.mocked(saveSnmpDefinition).mock.calls[0][0] as any
      expect(callArg.version).toBe('v3')
      expect(callArg.port).toBe(162)
      expect(callArg.retry).toBe(5)
      expect(callArg.timeout).toBe(5000)
      expect(callArg.maxVarsPerPdu).toBe(20)
      expect(callArg.maxRepetitions).toBe(5)
      expect(callArg.securityName).toBe('testUser')
      expect(callArg.securityLevel).toBe(3)
      expect(callArg.authPassphrase).toBe('authPass')
      expect(callArg.authProtocol).toBe('MD5')
      expect(callArg.privacyPassphrase).toBe('privPass')
      expect(callArg.privacyProtocol).toBe('DES')
      expect(callArg.engineId).toBe('engine123')
      expect(callArg.contextEngineId).toBe('contextEngine123')
      expect(callArg.contextName).toBe('contextTest')
      expect(callArg.enterpriseId).toBe('enterprise123')
      expect(callArg.maxRequestSize).toBe(32768)
      expect(callArg.proxyHost).toBe('proxy.example.com')
      expect(callArg.ttl).toBe(3600)
      expect(callArg.profileLabel).toBe('CustomProfile')
    })

    it('should use default version when version is not provided', async () => {
      const successResponse = createSuccessResponse()
      vi.mocked(saveSnmpDefinition).mockResolvedValue(successResponse)

      const configWithoutVersion = { ...mockSnmpAgentConfig, version: undefined }

      const definition = {
        ...configWithoutVersion,
        range: [] as IpAddressRange[],
        specific: ['10.0.0.1']
      } as SnmpDefinition

      await store.saveDefinition(definition)

      const callArg = vi.mocked(saveSnmpDefinition).mock.calls[0][0]
      expect(callArg.version).toBe(DEFAULT_SNMP_VERSION)
    })
  })

  describe('removeDefinition', () => {
    it('should remove definition successfully', async () => {
      const successResponse = createSuccessResponse()
      vi.mocked(deleteSnmpDefinition).mockResolvedValue(successResponse)

      const result = await store.removeDefinition(null, ['10.0.0.1'], null, DEFAULT_MONITORING_LOCATION)

      expect(deleteSnmpDefinition).toHaveBeenCalledWith(null, ['10.0.0.1'], null, DEFAULT_MONITORING_LOCATION)
      expect(result).toEqual(successResponse)
    })

    it('should return failure result when delete fails', async () => {
      const failureResponse = createFailureResult('Failed to delete')
      vi.mocked(deleteSnmpDefinition).mockResolvedValue(failureResponse)

      const result = await store.removeDefinition(null, null, null, DEFAULT_MONITORING_LOCATION)

      expect(deleteSnmpDefinition).toHaveBeenCalledWith(null, null, null, DEFAULT_MONITORING_LOCATION)
      expect(result).toEqual(failureResponse)
      expect(result.success).toBe(false)
    })
  })

  describe('saveProfile', () => {
    it('should save profile successfully', async () => {
      const successResponse = createSuccessResponse()
      vi.mocked(saveSnmpProfile).mockResolvedValue(successResponse)

      const profile: SnmpProfile = {
        label: 'Test Profile',
        filter: 'ip like 10.0.0.*',
        readCommunity: 'public',
        writeCommunity: 'private',
        encrypted: false
      }

      const result = await store.saveProfile(profile)

      expect(saveSnmpProfile).toHaveBeenCalledTimes(1)
      const callArg = vi.mocked(saveSnmpProfile).mock.calls[0][0]
      expect(callArg.label).toBe('Test Profile')
      expect(callArg.filter).toBe('ip like 10.0.0.*')
      expect(callArg.readCommunity).toBe('public')
      expect(callArg.writeCommunity).toBe('private')
      expect(result).toEqual(successResponse)
    })

    it('should save profile with all optional fields', async () => {
      const successResponse = createSuccessResponse()
      vi.mocked(saveSnmpProfile).mockResolvedValue(successResponse)

      const profile: SnmpProfile = {
        label: 'Full Profile',
        filter: 'ip like 10.*',
        readCommunity: 'public',
        writeCommunity: 'private',
        encrypted: false,
        version: 'v3',
        port: 162,
        retry: 4,
        timeout: 4000,
        maxVarsPerPdu: 15,
        maxRepetitions: 3,
        securityName: 'profileUser',
        securityLevel: 2,
        authPassphrase: 'authPass',
        authProtocol: 'SHA',
        privacyPassphrase: 'privPass',
        privacyProtocol: 'AES',
        engineId: 'engine456',
        contextEngineId: 'contextEngine456',
        contextName: 'contextProfile',
        enterpriseId: 'enterprise456',
        maxRequestSize: 16384,
        proxyHost: 'proxy2.example.com',
        ttl: 7200
      }

      await store.saveProfile(profile)

      const callArg = vi.mocked(saveSnmpProfile).mock.calls[0][0]
      expect(callArg.version).toBe('v3')
      expect(callArg.port).toBe(162)
      expect(callArg.retry).toBe(4)
      expect(callArg.timeout).toBe(4000)
      expect(callArg.maxVarsPerPdu).toBe(15)
      expect(callArg.maxRepetitions).toBe(3)
      expect(callArg.securityName).toBe('profileUser')
      expect(callArg.securityLevel).toBe(2)
      expect(callArg.authPassphrase).toBe('authPass')
      expect(callArg.authProtocol).toBe('SHA')
      expect(callArg.privacyPassphrase).toBe('privPass')
      expect(callArg.privacyProtocol).toBe('AES')
      expect(callArg.engineId).toBe('engine456')
      expect(callArg.contextEngineId).toBe('contextEngine456')
      expect(callArg.contextName).toBe('contextProfile')
      expect(callArg.enterpriseId).toBe('enterprise456')
      expect(callArg.maxRequestSize).toBe(16384)
      expect(callArg.proxyHost).toBe('proxy2.example.com')
      expect(callArg.ttl).toBe(7200)
    })

    it('should convert undefined optional fields to undefined in DTO', async () => {
      const successResponse = createSuccessResponse()
      vi.mocked(saveSnmpProfile).mockResolvedValue(successResponse)

      const profile: SnmpProfile = {
        label: 'Minimal Profile',
        filter: 'ip like 172.16.*',
        encrypted: false
      }

      await store.saveProfile(profile)

      const callArg = vi.mocked(saveSnmpProfile).mock.calls[0][0]
      expect(callArg.readCommunity).toBeUndefined()
      expect(callArg.writeCommunity).toBeUndefined()
      expect(callArg.port).toBeUndefined()
      expect(callArg.retry).toBeUndefined()
      expect(callArg.timeout).toBeUndefined()
    })

    it('should handle invalid security level', async () => {
      const successResponse = createSuccessResponse()
      vi.mocked(saveSnmpProfile).mockResolvedValue(successResponse)

      const profile: SnmpProfile = {
        label: 'Profile Invalid Security',
        filter: 'ip like 10.*',
        readCommunity: 'public',
        writeCommunity: 'private',
        encrypted: false,
        securityLevel: 999 as any // invalid number
      }

      await store.saveProfile(profile)

      const callArg = vi.mocked(saveSnmpProfile).mock.calls[0][0]
      // Should use default security level when invalid
      expect(callArg.securityLevel).toBeDefined()
    })
  })

  describe('deleteProfile', () => {
    it('should delete profile successfully', async () => {
      const successResponse = createSuccessResponse()
      vi.mocked(deleteSnmpProfile).mockResolvedValue(successResponse)

      const result = await store.deleteProfile('Test Profile')

      expect(deleteSnmpProfile).toHaveBeenCalledWith('Test Profile')
      expect(result).toEqual(successResponse)
    })

    it('should return failure result when delete fails', async () => {
      const failureResponse = createFailureResult('Profile not found')
      vi.mocked(deleteSnmpProfile).mockResolvedValue(failureResponse)

      const result = await store.deleteProfile('Nonexistent Profile')

      expect(result).toEqual(failureResponse)
      expect(result.success).toBe(false)
    })
  })

  describe('currentDefaults', () => {
    it('should return all SnmpBaseConfiguration fields', () => {
      const defaults = store.currentDefaults
      expect(defaults).toHaveProperty('readCommunity')
      expect(defaults).toHaveProperty('writeCommunity')
      expect(defaults).toHaveProperty('timeout')
      expect(defaults).toHaveProperty('retry')
      expect(defaults).toHaveProperty('port')
      expect(defaults).toHaveProperty('maxRequestSize')
      expect(defaults).toHaveProperty('maxVarsPerPdu')
      expect(defaults).toHaveProperty('maxRepetitions')
      expect(defaults).toHaveProperty('ttl')
      expect(defaults).toHaveProperty('version')
      expect(defaults).toHaveProperty('securityName')
      expect(defaults).toHaveProperty('securityLevel')
      expect(defaults).toHaveProperty('authPassphrase')
      expect(defaults).toHaveProperty('authProtocol')
      expect(defaults).toHaveProperty('privacyPassphrase')
      expect(defaults).toHaveProperty('privacyProtocol')
      expect(defaults).toHaveProperty('proxyHost')
      expect(defaults).toHaveProperty('engineId')
      expect(defaults).toHaveProperty('contextEngineId')
      expect(defaults).toHaveProperty('contextName')
      expect(defaults).toHaveProperty('enterpriseId')
    })

    it('should return hard-coded defaults when config fields are undefined', () => {
      // Initial store.config has no base fields set — all should fall back to constants
      const defaults = store.currentDefaults
      expect(defaults.readCommunity).toBe(DEFAULT_SNMP_READ_COMMUNITY_STRING)
      expect(defaults.writeCommunity).toBe(DEFAULT_SNMP_WRITE_COMMUNITY_STRING)
      expect(defaults.timeout).toBe(DEFAULT_SNMP_TIMEOUT)
      expect(defaults.retry).toBe(DEFAULT_SNMP_RETRIES)
      expect(defaults.port).toBe(DEFAULT_SNMP_PORT)
      expect(defaults.maxRequestSize).toBe(DEFAULT_SNMP_MAX_REQUEST_SIZE)
      expect(defaults.maxVarsPerPdu).toBe(DEFAULT_SNMP_MAX_VARS_PER_PDU)
      expect(defaults.maxRepetitions).toBe(DEFAULT_SNMP_MAX_REPETITIONS)
      expect(defaults.ttl).toBe(DEFAULT_SNMP_TTL)
      expect(defaults.version).toBe(DEFAULT_SNMP_VERSION)
      expect(defaults.securityName).toBe(DEFAULT_SNMP_V3_SECURITY_NAME)
      expect(defaults.securityLevel).toBe(DEFAULT_SNMP_V3_SECURITY_LEVEL)
      expect(defaults.authPassphrase).toBe(DEFAULT_SNMP_V3_AUTH_PASSPHRASE)
      expect(defaults.authProtocol).toBe(DEFAULT_SNMP_V3_AUTH_PROTOCOL)
      expect(defaults.privacyPassphrase).toBe(DEFAULT_SNMP_V3_PRIVACY_PASSPHRASE)
      expect(defaults.privacyProtocol).toBe(DEFAULT_SNMP_V3_PRIVACY_PROTOCOL)
    })

    it('should return overridden values when defined in config', () => {
      store.config.readCommunity = 'customRead'
      store.config.writeCommunity = 'customWrite'
      store.config.timeout = 9000
      store.config.retry = 5
      store.config.port = 162
      store.config.maxRequestSize = 32768
      store.config.maxVarsPerPdu = 20
      store.config.maxRepetitions = 4
      store.config.ttl = 600
      store.config.version = 'v3'
      store.config.securityName = 'myUser'
      store.config.securityLevel = 3
      store.config.authPassphrase = 'myAuthPass'
      store.config.authProtocol = 'SHA'
      store.config.privacyPassphrase = 'myPrivPass'
      store.config.privacyProtocol = 'AES'
      store.config.proxyHost = 'proxy.example.com'
      store.config.engineId = 'myEngineId'
      store.config.contextEngineId = 'myContextEngineId'
      store.config.contextName = 'myContextName'
      store.config.enterpriseId = 'myEnterpriseId'

      const defaults = store.currentDefaults
      expect(defaults.readCommunity).toBe('customRead')
      expect(defaults.writeCommunity).toBe('customWrite')
      expect(defaults.timeout).toBe(9000)
      expect(defaults.retry).toBe(5)
      expect(defaults.port).toBe(162)
      expect(defaults.maxRequestSize).toBe(32768)
      expect(defaults.maxVarsPerPdu).toBe(20)
      expect(defaults.maxRepetitions).toBe(4)
      expect(defaults.ttl).toBe(600)
      expect(defaults.version).toBe('v3')
      expect(defaults.securityName).toBe('myUser')
      expect(defaults.securityLevel).toBe(3)
      expect(defaults.authPassphrase).toBe('myAuthPass')
      expect(defaults.authProtocol).toBe('SHA')
      expect(defaults.privacyPassphrase).toBe('myPrivPass')
      expect(defaults.privacyProtocol).toBe('AES')
      expect(defaults.proxyHost).toBe('proxy.example.com')
      expect(defaults.engineId).toBe('myEngineId')
      expect(defaults.contextEngineId).toBe('myContextEngineId')
      expect(defaults.contextName).toBe('myContextName')
      expect(defaults.enterpriseId).toBe('myEnterpriseId')
    })

    it('should use hard-coded defaults for fields not overridden in config', () => {
      store.config.timeout = 9000

      const defaults = store.currentDefaults
      expect(defaults.timeout).toBe(9000)
      expect(defaults.retry).toBe(DEFAULT_SNMP_RETRIES)
      expect(defaults.port).toBe(DEFAULT_SNMP_PORT)
      expect(defaults.readCommunity).toBe(DEFAULT_SNMP_READ_COMMUNITY_STRING)
    })
  })

  describe('Enums', () => {
    it('should have correct SnmpLookupEditMode values', () => {
      expect(SnmpLookupEditMode.Lookup).toBe('lookup')
      expect(SnmpLookupEditMode.Edit).toBe('edit')
    })

    it('should have correct SnmpConfigEditMode values', () => {
      expect(SnmpConfigEditMode.Table).toBe('table')
      expect(SnmpConfigEditMode.Edit).toBe('edit')
      expect(SnmpConfigEditMode.Create).toBe('create')
    })

    it('should have correct ActiveTabs values', () => {
      expect(ActiveTabs.Lookup).toBe(0)
      expect(ActiveTabs.BrowseDefinitions).toBe(1)
      expect(ActiveTabs.Advanced).toBe(2)
    })
  })
})
