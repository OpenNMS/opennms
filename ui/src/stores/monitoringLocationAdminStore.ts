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

import API from '@/services'
import { MonitoringLocation } from '@/types'
import { ValidationResult } from '@/types/validation'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useMonitoringLocationAdminStore = defineStore('monitoringLocationAdminStore', () => {
  const locations = ref([] as MonitoringLocation[])
  const loadError = ref(false)
  const loading = ref(false)
  const totalCount = ref(0)
  // true if the server had more rows than we fetched (the safety cap was hit)
  const truncated = computed(() => locations.value.length < totalCount.value)

  // false when the load failed; the previous list is kept
  const getLocations = async (): Promise<boolean> => {
    loading.value = true
    const result = await API.listMonitoringLocations()
    loading.value = false
    if (result !== null) {
      locations.value = result.locations
      totalCount.value = result.totalCount
      loadError.value = false
    } else {
      loadError.value = true
    }
    return result !== null
  }

  const createLocation = async (location: MonitoringLocation): Promise<ValidationResult> => {
    const result = await API.createMonitoringLocation(location)
    if (result.success) {
      await getLocations()
    }
    return result
  }

  const updateLocation = async (location: MonitoringLocation): Promise<ValidationResult> => {
    const result = await API.updateMonitoringLocation(location)
    if (result.success) {
      await getLocations()
    }
    return result
  }

  const deleteLocation = async (name: string): Promise<ValidationResult> => {
    const result = await API.deleteMonitoringLocation(name)
    if (result.success) {
      await getLocations()
    }
    return result
  }

  return {
    locations,
    loadError,
    loading,
    totalCount,
    truncated,
    getLocations,
    createLocation,
    updateLocation,
    deleteLocation
  }
})
