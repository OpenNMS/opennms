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

import { QueryParameters } from '.'

export interface DeviceConfigBackup {
  id: number
  ipInterfaceId: number
  ipAddress: string
  deviceName: string
  location: string
  lastBackupDate: number
  lastUpdatedDate: number
  lastSucceededDate: number
  lastFailedDate: number
  backupStatus: string
  scheduledInterval: Record<string, string>
  fileName: string
  failureReason: string
  encoding: string
  configType: string
  configName: string
  nodeId: number
  nodeLabel: string
  operatingSystem: string
  isSuccessfulBackup: boolean
  nextScheduledBackupDate: number
  config: string
  monitoredServiceId: number
  serviceName: string
}

export interface DeviceConfigQueryParams extends QueryParameters {
  deviceName?: string
  ipAddress?: string
  ipInterfaceId?: number
  configType?: string
  createdAfter?: number
  createdBefore?: number
  location?: string
  status?: status
  pageEnter?: boolean
}

export type status = 'NONE' | 'SUCCESS' | 'FAILED'
