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

import { rest } from './axiosInstances'
import { UsageStatisticsData, UsageStatisticsMetadata, UsageStatisticsStatus } from '@/types/usageStatistics'

const endpoint = '/datachoices'

const getUsageStatistics = async (): Promise<UsageStatisticsData | false> => {
  try {
    const url = `${endpoint}`
    const resp = await rest.get(url)
    return resp.data
  } catch (err) {
    return false
  }
}

const getUsageStatisticsMetadata = async (): Promise<UsageStatisticsMetadata | false> => {
  try {
    const url = `${endpoint}/meta`
    const resp = await rest.get(url)
    return resp.data
  } catch (err) {
    return false
  }
}

const getUsageStatisticsStatus = async (): Promise<UsageStatisticsStatus | false> => {
  try {
    const url = `${endpoint}/status`
    const resp = await rest.get(url)
    return resp.data
  } catch (err) {
    return false
  }
}

const setUsageStatisticsStatus = async (enabled: boolean) : Promise<any | false> => {
  try {
    const status = {
      enabled
    }
    const url = `${endpoint}/status`
    const resp = await rest.post(url, status)
    return resp
  } catch (err) {
    return false
  }
}

export {
  getUsageStatistics,
  getUsageStatisticsMetadata,
  getUsageStatisticsStatus,
  setUsageStatisticsStatus
}
