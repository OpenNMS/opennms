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

import { v2 } from './axiosInstances'

import {
  IpAddressRange,
  SnmpAgentConfig,
  SnmpBaseConfiguration,
  SnmpConfig,
  SnmpDefinition,
  SnmpProfile
} from '@/types/snmpConfig'
import { isNumber, isString } from '@/lib/utils'
import { createFailureResult, createSuccessResponse, ValidationResult } from '@/types/validation'
import { DEFAULT_MONITORING_LOCATION } from '@/lib/constants'

const endpoint = '/snmp-config'

const convertSnmpVersionToString = (version: string | number): string => {
  if (isString(version)) {
    return version as string
  }

  if (isNumber(version)) {
    switch (version) {
      case 1:
        return 'v1'
      case 3:
        return 'v3'
      case 2:
      default:
        return 'v2c'
    }
  }

  return String(version)
}

const convertSnmpVersionToNumber = (version: string | number): number => {
  if (isNumber(version)) {
    return version as number
  }

  if (isString(version)) {
    switch (version) {
      case 'v1':
        return 1
      case 'v3':
        return 3
      case 'v2c':
      default:
        return 2
    }
  }

  return 2
}

const getSnmpConfig = async (): Promise<SnmpConfig | false> => {
  try {
    const resp = await v2.get(endpoint)

    // no content from server
    if (resp.status === 204) {
      return {
        definition: [] as SnmpDefinition[],
        profiles: {
          profile: [] as SnmpProfile[]
        }
      }
    }

    return resp.data as SnmpConfig
  } catch (_err) {
    return false
  }
}

const lookupSnmpConfig = async (ipAddress: string, location: string): Promise<SnmpAgentConfig | false> => {
  const fullEndpoint = `${endpoint}/lookup?ipAddress=${encodeURIComponent(ipAddress)}&location=${encodeURIComponent(location ?? DEFAULT_MONITORING_LOCATION)}`

  try {
    const resp = await v2.get(fullEndpoint)

    if (resp.status !== 200) {
      return false
    }

    // The Lookup API returns the SNMP version as a number, but the UI expects a string, so we need to convert it here.
    const data = {
      ...resp.data,
      location: location ?? DEFAULT_MONITORING_LOCATION,
      version: convertSnmpVersionToString(resp.data.version)
    } as SnmpAgentConfig

    return data
  } catch (_err) {
    return false
  }
}

const saveSnmpConfigDefaultOverrides = async (config: SnmpBaseConfiguration): Promise<ValidationResult> => {
  const fullEndpoint = `${endpoint}/defaults`

  try {
    const resp = await v2.post(fullEndpoint, config)

    if (resp.status === 204) {
      return createSuccessResponse()
    } else if (resp.status === 400) {
      return createFailureResult('Invalid SNMP configuration data')
    } else {
      return createFailureResult('Failed to save SNMP configuration defaults')
    }
  } catch (_err) {
    console.error('Error saving SNMP config defaults:', _err)
    return createFailureResult('Failed to save SNMP configuration defaults')
  }
}

const saveSnmpDefinition = async (definition: SnmpDefinition): Promise<ValidationResult> => {
  const fullEndpoint = `${endpoint}/definition`

  try {
    const resp = await v2.put(fullEndpoint, definition)

    if (resp.status === 201) {
      return createSuccessResponse()
    } else if (resp.status === 400) {
      return createFailureResult('Invalid SNMP configuration data')
    } else {
      return createFailureResult('Failed to save SNMP configuration')
    }
  } catch (_err) {
    console.error('Error saving SNMP definition:', _err)
    return createFailureResult('Failed to save SNMP configuration')
  }
}

const deleteSnmpDefinition = async (ranges: IpAddressRange[] | null, specifics: string[] | null,
  ipMatches: string[] | null, location: string): Promise<ValidationResult> => {
  let rangeItem = ''
  let specificItem = ''
  let ipMatchItem = ''

  if (ranges && ranges.length > 0) {
    const rangeParam = `${ranges.map(range => `${range.begin}-${range.end}`).join(',')}`
    rangeItem = `ranges=${encodeURIComponent(rangeParam)}`
  }

  if (specifics && specifics.length > 0) {
    const specificParam = `${encodeURIComponent(specifics.join(','))}`
    const hasRange = rangeItem.length > 0
    specificItem = `${hasRange ? '&' : ''}specifics=${specificParam}`
  }

  if (ipMatches && ipMatches.length > 0) {
    const ipMatchParam = `${encodeURIComponent(ipMatches.join(','))}`
    const hasRangeOrSpecific = rangeItem.length > 0 || specificItem.length > 0
    ipMatchItem = `${hasRangeOrSpecific ? '&' : ''}ipmatches=${ipMatchParam}`
  }

  if (!rangeItem && !specificItem && !ipMatchItem) {
    return createFailureResult('At least one of IP address range, specific IP address, or IP match must be provided')
  }

  const fullEndpoint = `${endpoint}/definition?${rangeItem}${specificItem}${ipMatchItem}&location=${encodeURIComponent(location || DEFAULT_MONITORING_LOCATION)}`

  try {
    const resp = await v2.delete(fullEndpoint)

    if (resp.status === 204) {
      return createSuccessResponse()
    } else if (resp.status === 404) {
      return createFailureResult('SNMP definition not found')
    } else {
      return createFailureResult('Failed to delete SNMP definition')
    }
  } catch (_err) {
    console.error('Error deleting SNMP definition:', _err)
    return createFailureResult('Failed to delete SNMP definition')
  }
}

const saveSnmpProfile = async (profile: SnmpProfile): Promise<ValidationResult> => {
  const fullEndpoint = `${endpoint}/profile`

  try {
    const resp = await v2.post(fullEndpoint, profile)

    if (resp.status === 204) {
      return createSuccessResponse()
    } else if (resp.status === 400) {
      return createFailureResult('Invalid SNMP profile data')
    } else {
      return createFailureResult('Failed to save SNMP profile')
    }
  } catch (_err) {
    console.error('Error saving SNMP profile:', _err)
    return createFailureResult('Failed to save SNMP profile')
  }
}

const deleteSnmpProfile = async (label: string): Promise<ValidationResult> => {
  const fullEndpoint = `${endpoint}/profile?label=${encodeURIComponent(label)}`

  try {
    const resp = await v2.delete(fullEndpoint)

    if (resp.status === 204) {
      return createSuccessResponse()
    } else if (resp.status === 400) {
      return createFailureResult('Invalid SNMP profile data')
    } else if (resp.status === 404) {
      return createFailureResult('SNMP profile not found')
    } else {
      return createFailureResult('Failed to delete SNMP profile')
    }
  } catch (_err) {
    console.error('Error deleting SNMP profile:', _err)
    return createFailureResult('Failed to delete SNMP profile')
  }
}

const downloadSnmpConfig = async (isXml: boolean) => {
  const fullEndpoint = `${endpoint}/download?format=${isXml ? 'xml' : 'json'}`

  try {
    return await v2.get(fullEndpoint, { responseType: 'blob' })
  } catch (_err) {
    console.error('Error downloading SNMP config file:', _err)
    return false
  }
}

const uploadSnmpConfig = async (file: File, isXml: boolean) => {
  const fullEndpoint = isXml ? `${endpoint}/upload/xml` : `${endpoint}/upload`

  try {
    const formData = new FormData()
    formData.append('upload', file)

    const response = await v2.post(fullEndpoint, formData)

    if (response.status !== 200) {
      throw new Error(`Failed to upload file: ${response.statusText}`)
    }

    return true
  } catch (_err) {
    console.error('Error uploading SNMP config file:', _err)
    return false
  }
}

export {
  convertSnmpVersionToNumber,
  convertSnmpVersionToString,
  deleteSnmpDefinition,
  deleteSnmpProfile,
  downloadSnmpConfig,
  getSnmpConfig,
  lookupSnmpConfig,
  saveSnmpConfigDefaultOverrides,
  saveSnmpDefinition,
  saveSnmpProfile,
  uploadSnmpConfig
}
