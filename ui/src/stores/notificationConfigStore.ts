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
import { DestinationPath, NotifdStatus, NotificationCommand } from '@/types/notificationConfig'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationConfigStore = defineStore('notificationConfigStore', () => {
  const notifdStatus = ref<NotifdStatus | null>(null)
  const destinationPaths = ref([] as DestinationPath[])
  const commands = ref([] as NotificationCommand[])
  const users = ref([] as string[])
  const groups = ref([] as string[])
  const roles = ref([] as string[])

  const getStatus = async (): Promise<boolean> => {
    notifdStatus.value = await API.getNotificationConfigStatus()
    return notifdStatus.value !== null
  }

  const setStatus = async (status: NotifdStatus) => {
    const ok = await API.setNotificationConfigStatus(status)
    if (ok) {
      notifdStatus.value = status
    }
    return ok
  }

  const getDestinationPaths = async (): Promise<boolean> => {
    const result = await API.getDestinationPaths()
    if (result === null) {
      return false
    }
    destinationPaths.value = result
    return true
  }

  const addDestinationPath = async (path: DestinationPath) => {
    const ok = await API.addDestinationPath(path)
    if (ok) {
      await getDestinationPaths()
    }
    return ok
  }

  const updateDestinationPath = async (originalName: string, path: DestinationPath) => {
    const ok = await API.updateDestinationPath(originalName, path)
    if (ok) {
      await getDestinationPaths()
    }
    return ok
  }

  const getUsersAndGroups = async (): Promise<boolean> => {
    const [u, g, r] = await Promise.all([API.getNotificationUsers(), API.getNotificationGroups(), API.getOnCallRoles()])
    if (u) {
      users.value = u
    }
    if (g) {
      groups.value = g
    }
    if (r) {
      roles.value = r
    }
    return u !== null && g !== null && r !== null
  }

  const deleteDestinationPath = async (name: string) => {
    const ok = await API.deleteDestinationPath(name)
    if (ok) {
      await getDestinationPaths()
    }
    return ok
  }

  const testDestinationPath = async (name: string) => {
    return await API.testDestinationPath(name)
  }

  const getCommands = async (): Promise<boolean> => {
    const result = await API.getNotificationCommands()
    if (result === null) {
      return false
    }
    commands.value = result
    return true
  }

  return {
    notifdStatus,
    destinationPaths,
    commands,
    getStatus,
    setStatus,
    getDestinationPaths,
    addDestinationPath,
    updateDestinationPath,
    deleteDestinationPath,
    testDestinationPath,
    getCommands,
    users,
    groups,
    roles,
    getUsersAndGroups
  }
})
