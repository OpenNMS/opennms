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
import { DEFAULT_MONITORING_LOCATION, DEFAULT_SNMP_MAX_REPETITIONS, DEFAULT_SNMP_MAX_REQUEST_SIZE, DEFAULT_SNMP_MAX_VARS_PER_PDU, DEFAULT_SNMP_PORT, DEFAULT_SNMP_READ_COMMUNITY_STRING, DEFAULT_SNMP_RETRIES, DEFAULT_SNMP_TIMEOUT, DEFAULT_SNMP_TTL, DEFAULT_SNMP_V3_AUTH_PASSPHRASE, DEFAULT_SNMP_V3_AUTH_PROTOCOL, DEFAULT_SNMP_V3_PRIVACY_PASSPHRASE, DEFAULT_SNMP_V3_PRIVACY_PROTOCOL, DEFAULT_SNMP_V3_SECURITY_LEVEL, DEFAULT_SNMP_VERSION, DEFAULT_SNMP_WRITE_COMMUNITY_STRING } from '@/lib/constants'
import { getDefaultSnmpSecurityLevel, isValidSnmpSecurityLevel } from '@/lib/snmpValidator'
import { getMonitoringLocations } from '@/services/monitoringLocationService'
import { deleteSnmpDefinition, deleteSnmpProfile, getSnmpConfig, lookupSnmpConfig, saveSnmpDefinition, saveSnmpProfile } from '@/services/snmpConfigService'
import { MonitoringLocation } from '@/types'
import { IpAddressRange, SnmpAgentConfig, SnmpBaseConfiguration, SnmpConfig, SnmpDefinition, SnmpProfile } from '@/types/snmpConfig'
import { ValidationResult } from '@/types/validation'
import { computed, ref } from 'vue'

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
  BrowseDefinitions = 1,
  Advanced = 2
}

export enum AdvancedSubtabs {
  DefaultOverrides = 0,
  Profiles = 1,
  UploadDownload = 2
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
    securityName: '',
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
  const activeAdvancedSubtab = ref(0)

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

  const setActiveAdvancedSubtab = (tabIndex: number) => {
    activeAdvancedSubtab.value = tabIndex
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
    setActiveAdvancedSubtab(0)
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

  const saveDefinition = async (definition: SnmpDefinition): Promise<ValidationResult> => {
    const definitionToSave = {
      ...definition,
      version: definition.version || DEFAULT_SNMP_VERSION
    }

    const resp = await saveSnmpDefinition(definitionToSave)
    return resp
  }

  const removeDefinition = async (ranges: IpAddressRange[] | null, specifics: string[] | null,
    ipMatches: string[] | null, location: string): Promise<ValidationResult> => {
    const resp = await deleteSnmpDefinition(ranges, specifics, ipMatches, location)
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

  const currentDefaults = computed<SnmpBaseConfiguration>(() => ({
    readCommunity: config.value.readCommunity ?? DEFAULT_SNMP_READ_COMMUNITY_STRING,
    writeCommunity: config.value.writeCommunity ?? DEFAULT_SNMP_WRITE_COMMUNITY_STRING,
    timeout: config.value.timeout ?? DEFAULT_SNMP_TIMEOUT,
    retry: config.value.retry ?? DEFAULT_SNMP_RETRIES,
    port: config.value.port ?? DEFAULT_SNMP_PORT,
    maxRequestSize: config.value.maxRequestSize ?? DEFAULT_SNMP_MAX_REQUEST_SIZE,
    maxVarsPerPdu: config.value.maxVarsPerPdu ?? DEFAULT_SNMP_MAX_VARS_PER_PDU,
    maxRepetitions: config.value.maxRepetitions ?? DEFAULT_SNMP_MAX_REPETITIONS,
    ttl: config.value.ttl ?? DEFAULT_SNMP_TTL,
    version: config.value.version ?? DEFAULT_SNMP_VERSION,
    securityName: config.value.securityName ?? '',
    securityLevel: config.value.securityLevel ?? DEFAULT_SNMP_V3_SECURITY_LEVEL,
    authPassphrase: config.value.authPassphrase ?? DEFAULT_SNMP_V3_AUTH_PASSPHRASE,
    authProtocol: config.value.authProtocol ?? DEFAULT_SNMP_V3_AUTH_PROTOCOL,
    privacyPassphrase: config.value.privacyPassphrase ?? DEFAULT_SNMP_V3_PRIVACY_PASSPHRASE,
    privacyProtocol: config.value.privacyProtocol ?? DEFAULT_SNMP_V3_PRIVACY_PROTOCOL,
    proxyHost: config.value.proxyHost,
    engineId: config.value.engineId,
    contextEngineId: config.value.contextEngineId,
    contextName: config.value.contextName,
    enterpriseId: config.value.enterpriseId
  }))

  return {
    activeTab,
    activeAdvancedSubtab,
    config,
    currentDefinition,
    definitionCreateEditMode,
    deleteProfile,
    fetchMonitoringLocations,
    currentDefaults,
    isLoading,
    lookupIpAddress,
    monitoringLocations,
    populateSnmpConfig,
    profileLabel,
    removeDefinition,
    saveDefinition,
    saveProfile,
    setActiveTab,
    setActiveAdvancedSubtab,
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
