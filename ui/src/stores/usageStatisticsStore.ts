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
import API from '@/services'
import {
  UsageStatisticsData,
  UsageStatisticsMetadata,
  UsageStatisticsMetadataItem,
  UsageStatisticsStatus
} from '@/types/usageStatistics'
import { ref } from 'vue'

export const useUsageStatisticsStore = defineStore('usageStatisticsStore', () => {
  const status = ref({ enabled: false } as UsageStatisticsStatus)
  const metadata = ref({ metadata: [] as UsageStatisticsMetadataItem[] } as UsageStatisticsMetadata)
  const statistics = ref({} as UsageStatisticsData)

  const getStatistics = async () => {
    const data = await API.getUsageStatistics()

    if (data) {
      statistics.value = data
    }
  }

  const getMetadata = async () => {
    const resp = await API.getUsageStatisticsMetadata()

    if (resp) {
      metadata.value = resp
    }
  }

  const getStatus = async () => {
    const resp = await API.getUsageStatisticsStatus()

    if (resp) {
      status.value = resp
    }
  }

  const enableSharing = async (): Promise<boolean> => {
    const success = await updateSharing(true)

    if (success) {
      await getStatus()
    }

    return success
  }

  const disableSharing = async (): Promise<boolean> => {
    const success = await updateSharing(false)

    if (success) {
      await getStatus()
    }

    return success
  }

  const updateSharing = async (enable: boolean) => {
    const resp = await API.setUsageStatisticsStatus(enable)

    return !!(resp && (resp.status === 200 || resp.status === 202))
  }

  return {
    status,
    metadata,
    statistics,
    getStatistics,
    getMetadata,
    getStatus,
    enableSharing,
    disableSharing
  }
})
