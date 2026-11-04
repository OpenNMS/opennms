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

import API from '@/services'
import { reloadAfterWrite } from '@/services/adminWrite'
import { AdminEditMode, AdminWriteResult } from '@/types/adminWrite'
import { ManagedGroup } from '@/types/groupAdmin'
import {
  ManagedOnCallRole,
  ManagedOnCallRoleCreate,
  OnCallScheduleSettings,
  STALE_ONCALL_ROLE_MESSAGE,
  STALE_ONCALL_ROLE_RELOAD_FAILED_MESSAGE
} from '@/types/onCallRoleAdmin'
import { ManagedUser } from '@/types/userAdmin'
import { createSuccessResponse, ValidationResult } from '@/types/validation'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useOnCallRoleAdminStore = defineStore('onCallRoleAdminStore', () => {
  const roles = ref([] as ManagedOnCallRole[])
  const users = ref([] as ManagedUser[])
  const groups = ref([] as ManagedGroup[])
  const scheduleSettings = ref<OnCallScheduleSettings | null>(null)

  const editMode = ref(AdminEditMode.Table)
  // the role being edited; null while creating or on the table
  const currentRole = ref<ManagedOnCallRole | null>(null)

  // null opens the editor on a new role
  const openRoleEditor = (role: ManagedOnCallRole | null) => {
    currentRole.value = role
    editMode.value = role ? AdminEditMode.Edit : AdminEditMode.Create
  }

  const closeRoleEditor = () => {
    currentRole.value = null
    editMode.value = AdminEditMode.Table
  }

  // the display name of a user, if the user has one
  const fullName = (userId: string): string | undefined =>
    users.value.find(u => u.userId === userId)?.fullName || undefined

  // the user ids of a group, in the group's order
  const groupMembers = (groupName: string): string[] =>
    groups.value.find(g => g.name === groupName)?.users ?? []

  // a failed read keeps the previous list on screen, and only the latest read sets it: a slow first
  // load must not replace the list read after a write
  let latestRead = 0
  const getRoles = async (): Promise<ValidationResult> => {
    const read = ++latestRead
    const result = await API.getManagedOnCallRoles()
    if (result.success && read === latestRead) {
      roles.value = result.payload ?? []
    }
    return result
  }

  const getUsers = async (): Promise<ValidationResult> => {
    const result = await API.getManagedUsers()
    if (result.success) {
      users.value = result.payload ?? []
    }
    return result
  }

  const getGroups = async (): Promise<ValidationResult> => {
    const result = await API.getManagedGroups()
    if (result.success) {
      groups.value = result.payload ?? []
    }
    return result
  }

  const getScheduleSettings = async (): Promise<ValidationResult> => {
    const result = await API.getOnCallScheduleSettings()
    if (result.success) {
      scheduleSettings.value = result.payload ?? null
    }
    return result
  }

  // reloads the list after a write, and words a stale write's message by whether that reload worked
  const afterWrite = (result: AdminWriteResult) => reloadAfterWrite(result, getRoles,
    { reloaded: STALE_ONCALL_ROLE_MESSAGE, reloadFailed: STALE_ONCALL_ROLE_RELOAD_FAILED_MESSAGE })

  const createRole = async (role: ManagedOnCallRoleCreate) => afterWrite(await API.createManagedOnCallRole(role))

  const updateRole = async (role: ManagedOnCallRole) => afterWrite(await API.updateManagedOnCallRole(role))

  const renameRole = async (name: string, newName: string, version: number) =>
    afterWrite(await API.renameManagedOnCallRole(name, newName, version))

  const deleteRole = async (name: string, version: number) => afterWrite(await API.deleteManagedOnCallRole(name, version))

  // fails with the message of the first read that failed
  const populate = async (): Promise<ValidationResult> => {
    const results = await Promise.all([getRoles(), getUsers(), getGroups(), getScheduleSettings()])
    return results.find(r => !r.success) ?? createSuccessResponse()
  }

  return {
    roles,
    users,
    groups,
    scheduleSettings,
    editMode,
    currentRole,
    openRoleEditor,
    closeRoleEditor,
    fullName,
    groupMembers,
    getRoles,
    getUsers,
    getGroups,
    getScheduleSettings,
    createRole,
    updateRole,
    renameRole,
    deleteRole,
    populate
  }
})
