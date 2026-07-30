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
import { ManagedUser, ManagedUserCreate } from '@/types/userAdmin'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserAdminStore = defineStore('userAdminStore', () => {
  const users = ref([] as ManagedUser[])
  const availableRoles = ref([] as string[])

  const getUsers = async () => {
    users.value = await API.getManagedUsers()
  }

  const getAvailableRoles = async () => {
    availableRoles.value = await API.getAvailableUserRoles()
  }

  const createUser = async (user: ManagedUserCreate) => {
    const ok = await API.createManagedUser(user)
    if (ok) {
      await getUsers()
    }
    return ok
  }

  const updateUser = async (user: ManagedUser) => {
    const ok = await API.updateManagedUser(user)
    if (ok) {
      await getUsers()
    }
    return ok
  }

  const setPassword = async (userId: string, password: string) => {
    return await API.setManagedUserPassword(userId, password)
  }

  const renameUser = async (userId: string, newUserId: string) => {
    const ok = await API.renameManagedUser(userId, newUserId)
    if (ok) {
      await getUsers()
    }
    return ok
  }

  const deleteUser = async (userId: string) => {
    const ok = await API.deleteManagedUser(userId)
    if (ok) {
      await getUsers()
    }
    return ok
  }

  const populate = async () => {
    await Promise.all([getUsers(), getAvailableRoles()])
  }

  return {
    users,
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
