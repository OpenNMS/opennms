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
  SnmpDefinition,
  SnmpProfile
} from '@/types/snmpConfig'

const endpoint = '/snmp-config'

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
  } catch (err) {
    return false
  }
}

const lookupSnmpConfig = async (ipAddress: string, location: string): Promise<SnmpAgentConfig | false> => {
  const fullEndpoint = `${endpoint}/lookup?ipAddress=${ipAddress}&llocation=${location ?? 'Default'}`  

  try {
    const resp = await v2.get(fullEndpoint)

    // no content from server
    if (resp.status === 204) {
      return { }
    }

    return resp.data as SnmpAgentConfig
  } catch (err) {
    return false
  }
}

export {
  getSnmpConfig,
  lookupSnmpConfig
}
