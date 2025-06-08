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
  ZenithConnectRegistrationResponse,
  ZenithConnectRegistration,
  ZenithConnectRegistrations
} from '@/types/zenithConnect'

export const useZenithConnectStore = defineStore('zenithConnectStore', () => {
  const registerResponse = ref<ZenithConnectRegistrationResponse>()
  const registrations = ref<ZenithConnectRegistrations>()
  const currentRegistration = ref<ZenithConnectRegistration>()

  const resetRegistration = () => {
    const regs = registrations.value?.registrations ?? []

    currentRegistration.value = regs && regs.length > 0 ? regs[0] : undefined
  }

  const addRegistration = async (registration: ZenithConnectRegistration) => {
    const resp = await API.addZenithRegistration(registration)

    if (resp) {
      const newRegistration = resp as ZenithConnectRegistration

      if (newRegistration) {
        registrations.value = {
          registrations: [newRegistration]
        }
      
        return true
      }
    }

    return false
  }

  const fetchRegistrations = async () => {
    const resp = await API.getZenithRegistrations()

    if (resp) {
      const newRegistrations = resp as ZenithConnectRegistrations

      if (newRegistrations) {
        registrations.value = newRegistrations
        resetRegistration()

        return true
      }
    }

    return false
  }

  const setRegistrationResponse = (response: ZenithConnectRegistrationResponse) => {
    registerResponse.value = response
  }

  // TODO: remove
  const clearRegistrations = () => {
    registrations.value = {
      registrations: []
    }
  }

  return {
    currentRegistration,
    registerResponse,
    registrations,
    addRegistration,
    clearRegistrations,
    fetchRegistrations,
    resetRegistration,
    setRegistrationResponse
  }
})
