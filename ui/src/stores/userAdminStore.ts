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

import useSnackbar from '@/composables/useSnackbar'
import API from '@/services'
import { AdminEditMode, AdminWriteError } from '@/types/adminWrite'
import { ManagedUser, ManagedUserCreate, STALE_USER_MESSAGE, STALE_USER_RELOAD_FAILED_MESSAGE } from '@/types/userAdmin'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserAdminStore = defineStore('userAdminStore', () => {
  const { showSnackBar } = useSnackbar()
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

  const getUsers = async () => {
    const result = await API.getManagedUsers()
    if (result !== null) {
      users.value = result
    }
    return result !== null
  }

  const getAvailableRoles = async () => {
    availableRoles.value = await API.getAvailableUserRoles()
  }

  // A stale write means the list on screen is out of date: reload it, and say so only
  // once the reload has succeeded. The caller gets the flag, not the text.
  const reloadIfStale = async (error: AdminWriteError | null) => {
    if (error?.stale) {
      const reloaded = await getUsers()
      const message = reloaded ? STALE_USER_MESSAGE : STALE_USER_RELOAD_FAILED_MESSAGE
      showSnackBar({ msg: message, error: true })
      return { ...error, message }
    }
    return error
  }

  const createUser = async (user: ManagedUserCreate) => {
    const error = await API.createManagedUser(user)
    if (error === null) {
      await getUsers()
    }
    return error
  }

  const updateUser = async (user: ManagedUser) => {
    const error = await API.updateManagedUser(user)
    if (error === null) {
      await getUsers()
    }
    return await reloadIfStale(error)
  }

  const setPassword = async (userId: string, password: string, version: number) => {
    const error = await API.setManagedUserPassword(userId, password, version)
    if (error === null) {
      // the password PUT bumps the version; reload so the next action sends the new one
      await getUsers()
    }
    return await reloadIfStale(error)
  }

  const renameUser = async (userId: string, newUserId: string, version: number) => {
    const error = await API.renameManagedUser(userId, newUserId, version)
    if (error === null) {
      await getUsers()
    }
    return await reloadIfStale(error)
  }

  const deleteUser = async (userId: string, version: number) => {
    const error = await API.deleteManagedUser(userId, version)
    if (error === null) {
      await getUsers()
    }
    return await reloadIfStale(error)
  }

  const populate = async () => {
    await Promise.all([getUsers(), getAvailableRoles()])
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
