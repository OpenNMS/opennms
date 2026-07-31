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
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMonitoringLocationAdminStore = defineStore('monitoringLocationAdminStore', () => {
  const locations = ref([] as MonitoringLocation[])
  const loadError = ref(false)

  const getLocations = async () => {
    const result = await API.listMonitoringLocations()
    if (result !== null) {
      locations.value = result
      loadError.value = false
    } else {
      loadError.value = true
    }
  }

  const createLocation = async (location: MonitoringLocation) => {
    const error = await API.createMonitoringLocation(location)
    if (error === null) {
      await getLocations()
    }
    return error
  }

  const updateLocation = async (location: MonitoringLocation) => {
    const error = await API.updateMonitoringLocation(location)
    if (error === null) {
      await getLocations()
    }
    return error
  }

  const deleteLocation = async (name: string) => {
    const error = await API.deleteMonitoringLocation(name)
    if (error === null) {
      await getLocations()
    }
    return error
  }

  return {
    locations,
    loadError,
    getLocations,
    createLocation,
    updateLocation,
    deleteLocation
  }
})
