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
import { ApplicationMembersUpdate, ApplicationSummary } from '@/types/applicationAdmin'
import { ValidationResult, ValidationResultWithPayload } from '@/types/validation'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useApplicationAdminStore = defineStore('applicationAdminStore', () => {
  const applications = ref([] as ApplicationSummary[])
  const loadError = ref(false)
  const loading = ref(false)
  let loadRequest = 0

  // false when the load failed; the previous list is kept. Only the newest load
  // commits, so a slow earlier one cannot overwrite the list after a change
  const getApplications = async (): Promise<boolean> => {
    const request = ++loadRequest
    loading.value = true
    const result = await API.listApplicationSummaries()
    if (request !== loadRequest) {
      return result !== null
    }
    loading.value = false
    if (result !== null) {
      applications.value = result
      loadError.value = false
    } else {
      loadError.value = true
    }
    return result !== null
  }

  const createApplication = async (name: string): Promise<ValidationResultWithPayload<number>> => {
    const result = await API.createApplication(name)
    if (result.success) {
      await getApplications()
    }
    return result
  }

  const deleteApplication = async (application: ApplicationSummary): Promise<ValidationResult> => {
    const result = await API.deleteApplication(application.id, application.name)
    if (result.success) {
      await getApplications()
    }
    return result
  }

  const updateMembers = async (application: { id: number; name: string }, update: ApplicationMembersUpdate): Promise<ValidationResult> => {
    const result = await API.updateApplicationMembers(application.id, application.name, update)
    if (result.success) {
      await getApplications()
    }
    return result
  }

  const getMembers = (id: number) => API.getApplicationMembers(id)

  const searchServices = (search: string, limit: number) => API.searchServiceCandidates(search, limit)

  return {
    applications,
    loadError,
    loading,
    getApplications,
    createApplication,
    deleteApplication,
    updateMembers,
    getMembers,
    searchServices
  }
})
