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
import { SnmpAgentConfig, SnmpBaseConfiguration, SnmpConfig, SnmpDefinition, SnmpProfile } from '@/types/snmpConfig'
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
  Profiles = 2,
  UploadDownload = 3
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
    range: [],
    specific: [],
    ipMatch: [],
    location: DEFAULT_MONITORING_LOCATION,
    profileLabel: ''
  } as SnmpDefinition
}

export const getDefaultSnmpProfile = () => {
  return {
    label: '',
    filter: '',
    readCommunity: '',
    writeCommunity: '',
    encrypted: false
  } as SnmpProfile
}

export const getEmptySnmpConfig = () => {
  return {
    definition: [],
    profiles: {
      profile: []
    }
  } as SnmpConfig
}

export const getDefaultSnmpConfig = () => {
  return {
    definition: [{ ...getDefaultSnmpDefinition(), id: 0 }],
    profiles: {
      profile: []
    }
  } as SnmpConfig
}

export const getMockSnmpConfiguration = () => {
  return {
    definition: [
      {
        ...getDefaultSnmpDefinition(),
        id: 0
      },
      {
        id: 1,
        readCommunity: 'public',
        writeCommunity: 'private',
        encrypted: false,
        range: [
          {
            begin: '10.0.0.0',
            end: '10.0.0.99'
          }
        ],
        specific: [],
        /** Match Octets (as in IPLIKE) */
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
          label: 'My Profile',
          filter: 'ip like 10.0.0.*'
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
    definition: [],
    profiles: {
      profile: []
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

  const resetCurrentDefinition = () => {
    currentDefinition.value = getDefaultSnmpDefinition()
  }

  const resetState = () => {
    isLoading.value = false
    setActiveTab(0)
    resetCurrentDefinition()
    setDefinitionCreateEditMode(SnmpConfigEditMode.Table)
    setSnmpLookupEditMode(SnmpLookupEditMode.Lookup)
    setProfileLabel('')
    setSnmpProfileEditMode(SnmpConfigEditMode.Table)
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

  const saveDefinition = async (config: SnmpAgentConfig, firstIp: string, lastIp?: string): Promise<ValidationResult> => {
    const specific = firstIp && lastIp ? [] : [firstIp]
    const range = firstIp && lastIp ? [{ begin: firstIp, end: lastIp }] : []

    const definition = {
      proxyHost: config.proxyHost,
      maxVarsPerPdu: config.maxVarsPerPdu,
      maxRepetitions: config.maxRepetitions,
      maxRequestSize: config.maxRequestSize,
      version: config.version || DEFAULT_SNMP_VERSION,
      writeCommunity: config.writeCommunity,
      readCommunity: config.readCommunity,
      timeout: config.timeout,
      retry: config.retry,
      port: config.port,
      ttl: config.ttl,
      encrypted: config.encrypted,
      securityName: config.securityName,
      securityLevel: config.securityLevel,
      authPassphrase: config.authPassphrase,
      authProtocol: config.authProtocol,
      engineId: config.engineId,
      contextEngineId: config.contextEngineId,
      contextName: config.contextName,
      privacyPassphrase: config.privacyPassphrase,
      privacyProtocol: config.privacyProtocol,
      enterpriseId: config.enterpriseId,
      range,
      specific,
      ipMatch: [],
      location: config.location,
      profileLabel: config.profileLabel
    } as SnmpDefinition

    const resp = await saveSnmpDefinition(definition)
    return resp
  }

  const removeDefinition = async (ipAddress: string, location: string): Promise<ValidationResult> => {
    const resp = await deleteSnmpDefinition(ipAddress, location)
    return resp
  }

  const saveProfile = async (profile: SnmpProfile): Promise<ValidationResult> => {
    const securityLevel = isValidSnmpSecurityLevel(profile.securityLevel) ? profile.securityLevel : getDefaultSnmpSecurityLevel()

    const dto = {
      ...profile,
      securityLevel,
      location: undefined // remove this as it does not exist in server-side SnmpProfile
    } as SnmpProfile

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
    resetCurrentDefinition,
    resetState,
    setCurrentDefinition,
    setDefinitionCreateEditMode,
    setProfileLabel,
    setSnmpLookupEditMode,
    setSnmpProfileEditMode,
    snmpLookupEditMode,
    snmpProfileEditMode
  }
})
