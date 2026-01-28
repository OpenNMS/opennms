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

import { defineStore } from 'pinia'
import { DEFAULT_MONITORING_LOCATION, DEFAULT_SNMP_MAX_REPETITIONS, DEFAULT_SNMP_MAX_REQUEST_SIZE, DEFAULT_SNMP_MAX_VARS_PER_PDU, DEFAULT_SNMP_PORT, DEFAULT_SNMP_RETRIES, DEFAULT_SNMP_TIMEOUT, DEFAULT_SNMP_TTL, DEFAULT_SNMP_V3_AUTH_PASSPHRASE, DEFAULT_SNMP_V3_AUTH_PROTOCOL, DEFAULT_SNMP_V3_PRIVACY_PASSPHRASE, DEFAULT_SNMP_V3_PRIVACY_PROTOCOL, DEFAULT_SNMP_V3_SECURITY_LEVEL, DEFAULT_SNMP_V3_SECURITY_NAME, DEFAULT_SNMP_VERSION } from '@/lib/constants'
import { getDefaultSnmpSecurityLevel, isValidSnmpSecurityLevel } from '@/lib/snmpValidator'
import { getMonitoringLocations } from '@/services/monitoringLocationService'
import { deleteSnmpDefinition, deleteSnmpProfile, getSnmpConfig, lookupSnmpConfig, saveSnmpDefinition, saveSnmpProfile } from '@/services/snmpConfigService'
import { MonitoringLocation } from '@/types'
import { SnmpAgentConfig, SnmpBaseConfiguration, SnmpConfig, SnmpConfigInfoDto, SnmpDefinition, SnmpProfile, SnmpSaveProfileDto } from '@/types/snmpConfig'
import { ValidationResult } from '@/types/validation'

export enum SnmpLookupEditMode {
  Lookup = 'lookup',
  Edit = 'edit'
}

export enum SnmpConfigEditMode {
  Table = 'table',
  Edit = 'edit',
  Create = 'create'
}

export enum ActiveTabs {
  Lookup = 0,
  Definitions = 1,
  Profiles = 2
}

export const getDefaultSnmpBaseConfiguration = () => {
  return {
    id: 0,
    proxyHost: '',
    maxVarsPerPdu: DEFAULT_SNMP_MAX_VARS_PER_PDU,
    maxRepetitions: DEFAULT_SNMP_MAX_REPETITIONS,
    maxRequestSize: DEFAULT_SNMP_MAX_REQUEST_SIZE,
    version: DEFAULT_SNMP_VERSION,
    writeCommunity: '',
    readCommunity: '',
    timeout: DEFAULT_SNMP_TIMEOUT,
    retry: DEFAULT_SNMP_RETRIES,
    port: DEFAULT_SNMP_PORT,
    ttl: DEFAULT_SNMP_TTL,
    encrypted: false,
    securityName: DEFAULT_SNMP_V3_SECURITY_NAME,
    securityLevel: DEFAULT_SNMP_V3_SECURITY_LEVEL,
    authPassphrase: DEFAULT_SNMP_V3_AUTH_PASSPHRASE,
    authProtocol: DEFAULT_SNMP_V3_AUTH_PROTOCOL,
    engineId: '',
    contextEngineId: '',
    contextName: '',
    privacyPassphrase: DEFAULT_SNMP_V3_PRIVACY_PASSPHRASE,
    privacyProtocol: DEFAULT_SNMP_V3_PRIVACY_PROTOCOL,
    enterpriseId: ''
  } as SnmpBaseConfiguration
}

export const getDefaultSnmpDefinition = () => {
  return {
    readCommunity: 'public',
    writeCommunity: 'private',
    encrypted: false,
    ranges: [],
    specifics: [],
    ipMatches: [],
    location: DEFAULT_MONITORING_LOCATION,
    profileLabel: ''
  } as SnmpDefinition
}

export const getDefaultSnmpProfile = () => {
  return {
    label: '',
    filterExpression: '',
    readCommunity: '',
    writeCommunity: '',
    encrypted: false
  } as SnmpProfile
}

export const getEmptySnmpConfig = () => {
  return {
    definitions: [],
    snmpProfiles: {
      snmpProfiles: []
    }
  } as SnmpConfig
}

export const getDefaultSnmpConfig = () => {
  return {
    definitions: [{ ...getDefaultSnmpDefinition(), id: 0 }],
    snmpProfiles: {
      snmpProfiles: []
    }
  } as SnmpConfig
}

export const getMockSnmpConfiguration = () => {
  return {
    definitions: [
      {
        ...getDefaultSnmpDefinition(),
        id: 0
      },
      {
        id: 1,
        readCommunity: 'public',
        writeCommunity: 'private',
        encrypted: false,
        ranges: [
          {
            begin: '10.0.0.0',
            end: '10.0.0.99'
          }
        ],
        specifics: [],
        /** Match Octets (as in IPLIKE) */
        ipMatches: [],
        location: DEFAULT_MONITORING_LOCATION,
        profileLabel: ''
      }
    ],
    snmpProfiles: {
      snmpProfiles: [
        {
          id: 0,
          readCommunity: 'public',
          writeCommunity: 'private',
          encrypted: false,
          label: 'My Profile',
          filterExpression: 'ip like 10.0.0.*'
        }
      ]
    }
  } as SnmpConfig
}

/**
 * 
 */
export const useSnmpConfigStore = defineStore('useSnmpConfigStore', () => {
  const config = ref<SnmpConfig>({
    definitions: [],
    snmpProfiles: {
      snmpProfiles: []
    }
  })
  const isLoading = ref(false)
  const activeTab = ref(0)

  // current definition being editing or deleted
  const currentDefinition = ref<SnmpDefinition>()
  const definitionCreateEditMode = ref(SnmpConfigEditMode.Table)

  // label of current profile being edited or deleted
  const profileLabel = ref('')
  const snmpProfileEditMode = ref<SnmpConfigEditMode>(SnmpConfigEditMode.Table)

  const monitoringLocations = ref<MonitoringLocation[]>([])
  const snmpLookupEditMode = ref<SnmpLookupEditMode>(SnmpLookupEditMode.Lookup)

  const setActiveTab = (tabIndex: number) => {
    activeTab.value = tabIndex
  }

  const setDefinitionCreateEditMode = (mode: SnmpConfigEditMode) => {
    definitionCreateEditMode.value = mode
  }

  const setSnmpLookupEditMode = (mode: SnmpLookupEditMode) => {
    snmpLookupEditMode.value = mode
  }

  const setSnmpProfileEditMode = (mode: SnmpConfigEditMode) => {
    snmpProfileEditMode.value = mode
  }

  const setCurrentDefinition = (definition: SnmpDefinition) => {
    currentDefinition.value = definition
  }

  const setProfileLabel = (label: string) => {
    profileLabel.value = label
  }

  const populateSnmpConfig = async () => {
    const resp = await getSnmpConfig()

    if (resp) {
      config.value = {
        ...resp
      }
    }
  }

  const fetchMonitoringLocations = async () => {
    const resp = await getMonitoringLocations()

    if (resp) {
      monitoringLocations.value = resp.location
    }
  }

  const lookupIpAddress = async (ipAddress: string, location: string): Promise<SnmpAgentConfig | null> => {
    const resp = await lookupSnmpConfig(ipAddress, location)

    if (!resp) {
      return null
    }

    return resp
  }

  const saveDefinition = async (config: SnmpAgentConfig, firstIp?: string, lastIp?: string): Promise<ValidationResult> => {
    const dto = {
      readCommunity: config.readCommunity,
      version: config.version || DEFAULT_SNMP_VERSION,
      port: config.port,
      retries: config.retry,
      timeout: config.timeout,
      maxVarsPerPdu: config.maxVarsPerPdu,
      maxRepetitions: config.maxRepetitions,
      securityName: config.securityName,
      securityLevel: config.securityLevel,
      authPassPhrase: config.authPassphrase,
      authProtocol: config.authProtocol,
      privPassPhrase: config.privacyPassphrase,
      privProtocol: config.privacyProtocol,
      engineId: config.engineId,
      contextEngineId: config.contextEngineId,
      contextName: config.contextName,
      enterpriseId: config.enterpriseId,
      maxRequestSize: config.maxRequestSize,
      writeCommunity: config.writeCommunity,
      proxyHost: config.proxyHost,
      location: config.location,
      ttl: config.ttl,
      firstIpAddress: firstIp ?? '',
      lastIpAddress: lastIp
    } as SnmpConfigInfoDto

    const resp = await saveSnmpDefinition(dto)

    return resp
  }

  const removeDefinition = async (ipAddress: string, location: string): Promise<ValidationResult> => {
    const resp = await deleteSnmpDefinition(ipAddress, location)

    return resp
  }

  const saveProfile = async (profile: SnmpProfile): Promise<ValidationResult> => {
    const securityLevel = isValidSnmpSecurityLevel(profile.securityLevel) ? profile.securityLevel : getDefaultSnmpSecurityLevel()

    const dto = {
      label: profile.label,
      filterExpression: profile.filterExpression,
      readCommunity: profile.readCommunity ?? undefined,
      writeCommunity: profile.writeCommunity ?? undefined,
      version: profile.version || DEFAULT_SNMP_VERSION,
      port: profile.port ?? undefined,
      retries: profile.retry ?? undefined,
      timeout: profile.timeout ?? undefined,
      maxVarsPerPdu: profile.maxVarsPerPdu ?? undefined,
      maxRepetitions: profile.maxRepetitions ?? undefined,
      securityName: profile.securityName ?? undefined,
      securityLevel,
      authPassPhrase: profile.authPassphrase ?? undefined,
      authProtocol: profile.authProtocol ?? undefined,
      privPassPhrase: profile.privacyPassphrase ?? undefined,
      privProtocol: profile.privacyProtocol ?? undefined,
      engineId: profile.engineId ?? undefined,
      contextEngineId: profile.contextEngineId ?? undefined,
      contextName: profile.contextName ?? undefined,
      enterpriseId: profile.enterpriseId ?? undefined,
      maxRequestSize: profile.maxRequestSize ?? undefined,
      proxyHost: profile.proxyHost ?? undefined,
      ttl: profile.ttl ?? undefined
    } as SnmpSaveProfileDto

    const resp = await saveSnmpProfile(dto)

    return resp
  }

  const deleteProfile = async (label: string): Promise<ValidationResult> => {
    const resp = await deleteSnmpProfile(label)

    return resp
  }

  return {
    activeTab,
    config,
    currentDefinition,
    definitionCreateEditMode,
    deleteProfile,
    fetchMonitoringLocations,
    isLoading,
    lookupIpAddress,
    monitoringLocations,
    populateSnmpConfig,
    profileLabel,
    removeDefinition,
    saveDefinition,
    saveProfile,
    setActiveTab,
    setCurrentDefinition,
    setDefinitionCreateEditMode,
    setProfileLabel,
    setSnmpLookupEditMode,
    setSnmpProfileEditMode,
    snmpLookupEditMode,
    snmpProfileEditMode
  }
})
