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
import { AdminEditMode, AdminWriteResult } from '@/types/adminWrite'
import { ManagedUser, ManagedUserCreate, STALE_USER_MESSAGE, STALE_USER_RELOAD_FAILED_MESSAGE } from '@/types/userAdmin'
import { createSuccessResponse, ValidationResult } from '@/types/validation'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserAdminStore = defineStore('userAdminStore', () => {
  const users = ref([] as ManagedUser[])
  const availableRoles = ref([] as string[])

  const editMode = ref(AdminEditMode.Table)
  // the user being edited; null while creating or on the table
  const currentUser = ref<ManagedUser | null>(null)

  // null opens the editor on a new user
  const openUserEditor = (user: ManagedUser | null) => {
    currentUser.value = user
    editMode.value = user ? AdminEditMode.Edit : AdminEditMode.Create
  }

  const closeUserEditor = () => {
    currentUser.value = null
    editMode.value = AdminEditMode.Table
  }

  // a failed read keeps the previous list on screen
  const getUsers = async (): Promise<ValidationResult> => {
    const result = await API.getManagedUsers()
    if (result.success) {
      users.value = result.payload ?? []
    }
    return result
  }

  const getAvailableRoles = async (): Promise<ValidationResult> => {
    const result = await API.getAvailableUserRoles()
    if (result.success) {
      availableRoles.value = result.payload ?? []
    }
    return result
  }

  // After a write: a success reloads the list (a failed reload goes in the result's errors);
  // a stale write means the list on screen is out of date, so it reloads too and words
  // the stale message by whether that reload worked. The caller shows the result.
  const afterWrite = async (result: AdminWriteResult): Promise<AdminWriteResult> => {
    if (result.success) {
      const reloaded = await getUsers()
      return reloaded.success ? result : { ...result, errors: [reloaded.message] }
    }
    if (result.stale) {
      const reloaded = await getUsers()
      return { ...result, message: reloaded.success ? STALE_USER_MESSAGE : STALE_USER_RELOAD_FAILED_MESSAGE }
    }
    return result
  }

  const createUser = async (user: ManagedUserCreate) => afterWrite(await API.createManagedUser(user))

  const updateUser = async (user: ManagedUser) => afterWrite(await API.updateManagedUser(user))

  // the password PUT bumps the version; the reload makes the next action send the new one
  const setPassword = async (userId: string, password: string, version: number) =>
    afterWrite(await API.setManagedUserPassword(userId, password, version))

  const renameUser = async (userId: string, newUserId: string, version: number) =>
    afterWrite(await API.renameManagedUser(userId, newUserId, version))

  const deleteUser = async (userId: string, version: number) => afterWrite(await API.deleteManagedUser(userId, version))

  // fails with the message of the first read that failed
  const populate = async (): Promise<ValidationResult> => {
    const results = await Promise.all([getUsers(), getAvailableRoles()])
    return results.find(r => !r.success) ?? createSuccessResponse()
  }

  return {
    users,
    editMode,
    currentUser,
    openUserEditor,
    closeUserEditor,
    availableRoles,
    getUsers,
    getAvailableRoles,
    createUser,
    updateUser,
    setPassword,
    renameUser,
    deleteUser,
    populate
  }
})
