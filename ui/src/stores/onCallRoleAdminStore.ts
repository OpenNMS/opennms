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
import { OnCallCalendar, OnCallRole } from '@/types/onCallRoleAdmin'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useOnCallRoleAdminStore = defineStore('onCallRoleAdminStore', () => {
  const roles = ref([] as OnCallRole[])
  const supervisorCandidates = ref([] as string[])
  const groupMembers = ref({} as Record<string, string[]>)

  const getRoles = async () => {
    const result = await API.listOnCallRoles()
    if (result !== null) {
      roles.value = result
    }
  }

  const getPickerData = async () => {
    const [users, groups] = await Promise.all([API.getOnCallSupervisorCandidates(), API.getOnCallGroupCandidates()])
    supervisorCandidates.value = users
    groupMembers.value = groups
  }

  const getRole = async (name: string): Promise<OnCallRole | null> => {
    return await API.getOnCallRole(name)
  }

  const getCalendar = async (name: string, year: number, month: number): Promise<OnCallCalendar | null> => {
    return await API.getOnCallCalendar(name, year, month)
  }

  const createRole = async (role: OnCallRole) => {
    const ok = await API.createOnCallRole(role)
    if (ok) {
      await getRoles()
    }
    return ok
  }

  const updateRole = async (role: OnCallRole) => {
    const ok = await API.updateOnCallRole(role)
    if (ok) {
      await getRoles()
    }
    return ok
  }

  const renameRole = async (name: string, newName: string) => {
    const ok = await API.renameOnCallRole(name, newName)
    if (ok) {
      await getRoles()
    }
    return ok
  }

  const deleteRole = async (name: string) => {
    const ok = await API.deleteOnCallRole(name)
    if (ok) {
      await getRoles()
    }
    return ok
  }

  const populate = async () => {
    await Promise.all([getRoles(), getPickerData()])
  }

  return {
    roles,
    supervisorCandidates,
    groupMembers,
    getRoles,
    getPickerData,
    getRole,
    getCalendar,
    createRole,
    updateRole,
    renameRole,
    deleteRole,
    populate
  }
})
