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

import { CreateEditMode } from '.'

export interface TrapConfigStoreState {
  isLoading: boolean
  trapdConfig: TrapConfig
  snmpV3Users: SnmpV3User[]
  activeTab: number
  credentialDrawerState: {
    visible: boolean,
    key: string | null
  }
  createUserDrawerState: {
    visible: boolean
    mode: CreateEditMode
    selectedUserIndex: number
  }
}

export interface TrapConfig {
  snmpTrapAddress: string
  snmpTrapPort: number
  newSuspectOnTrap: boolean
  includeRawMessage: boolean
  threads: number
  queueSize: number
  batchSize: number
  batchInterval: number
  useAddressFromVarbind: boolean
  snmpv3User: SnmpV3User[]
}

export interface SnmpV3User {
  engineId: string | null
  securityName: string
  securityLevel: number
  authProtocol: string | null
  authPassphrase: string | null
  privacyProtocol: string | null
  privacyPassphrase: string | null
}

export interface TrapdConfigurationError {
  port?: string
  bindAddress?: string
  threads?: string
  queueSize?: string
  batchSize?: string
  batchInterval?: string
  snmpv3User?: string
}

export interface SnmpV3UserError {
  engineId?: string
  securityName?: string
  securityLevel?: string
  authProtocol?: string
  authPassphrase?: string
  privacyProtocol?: string
  privacyPassphrase?: string
}

export interface XmlValidationError {
  field: string
  message: string
}

export interface XmlValidationResult {
  valid: boolean
  errors: XmlValidationError[]
}

