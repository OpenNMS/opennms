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
  SnmpAgentConfig,
  SnmpConfig,
  SnmpConfigInfoDto,
  SnmpDefinition,
  SnmpProfile
} from '@/types/snmpConfig'
import { isNumber, isString } from '@/lib/utils'
import { createFailureResult, createSuccessResponse, ValidationResult } from '@/types/validation'

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
        definitions: [] as SnmpDefinition[],
        snmpProfiles: {
          snmpProfiles: [] as SnmpProfile[]
        }
      }
    }

    return resp.data as SnmpConfig
  } catch (err) {
    return false
  }
}

const lookupSnmpConfig = async (ipAddress: string, location: string): Promise<SnmpAgentConfig | false> => {
  const fullEndpoint = `${endpoint}/lookup?ipAddress=${encodeURIComponent(ipAddress)}&location=${encodeURIComponent(location ?? 'Default')}`

  try {
    const resp = await v2.get(fullEndpoint)

    if (resp.status !== 200) {
      return false
    }

    // The Lookup API returns the SNMP version as a number, but the UI expects a string, so we need to convert it here.
    const data = {
      ...resp.data,
      location: location ?? 'Default',
      version: convertSnmpVersionToString(resp.data.version)
    } as SnmpAgentConfig

    return data
  } catch (err) {
    return false
  }
}

const saveSnmpDefinition = async (config: SnmpConfigInfoDto): Promise<ValidationResult> => {
  const fullEndpoint = `${endpoint}/definition`

  try {
    const resp = await v2.put(fullEndpoint, config)

    if (resp.status === 201) {
      return createSuccessResponse()
    } else if (resp.status === 400) {
      return createFailureResult('Invalid SNMP configuration data')
    } else {
      return createFailureResult('Failed to save SNMP configuration')
    }
  } catch (err) {
    console.error('Error saving SNMP definition:', err)
    return createFailureResult('Failed to save SNMP configuration')
  }
}

const deleteSnmpDefinition = async (ipAddress: string, location: string): Promise<ValidationResult> => {
  const fullEndpoint = `${endpoint}/definition?ipAddress=${encodeURIComponent(ipAddress)}&location=${encodeURIComponent(location || 'Default')}`

  try {
    const resp = await v2.delete(fullEndpoint)

    if (resp.status === 204) {
      return createSuccessResponse()
    } else if (resp.status === 404) {
      return createFailureResult('SNMP definition not found')
    } else {
      return createFailureResult('Failed to delete SNMP definition')
    }
  } catch (err) {
    console.error('Error deleting SNMP definition:', err)
    return createFailureResult('Failed to delete SNMP definition')
  }
}

export {
  convertSnmpVersionToNumber,
  convertSnmpVersionToString,
  deleteSnmpDefinition,
  getSnmpConfig,
  lookupSnmpConfig,
  saveSnmpDefinition
}
