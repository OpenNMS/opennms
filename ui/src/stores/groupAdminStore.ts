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
import { ManagedGroup, ManagedGroupCreate, STALE_GROUP_MESSAGE, STALE_GROUP_RELOAD_FAILED_MESSAGE } from '@/types/groupAdmin'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useGroupAdminStore = defineStore('groupAdminStore', () => {
  const { showSnackBar } = useSnackbar()
  const groups = ref([] as ManagedGroup[])
  const memberCandidates = ref([] as string[])

  const editMode = ref(AdminEditMode.Table)
  // the group being edited; null while creating or on the table
  const currentGroup = ref<ManagedGroup | null>(null)

  // null opens the editor on a new group
  const openGroupEditor = (group: ManagedGroup | null) => {
    currentGroup.value = group
    editMode.value = group ? AdminEditMode.Edit : AdminEditMode.Create
  }

  const closeGroupEditor = () => {
    currentGroup.value = null
    editMode.value = AdminEditMode.Table
  }

  const getGroups = async () => {
    const result = await API.getManagedGroups()
    if (result !== null) {
      groups.value = result
    }
    return result !== null
  }

  const getMemberCandidates = async () => {
    memberCandidates.value = await API.getGroupMemberCandidates()
  }

  // A stale write means the list on screen is out of date: reload it, and say so only
  // once the reload has succeeded. The caller gets the flag, not the text.
  const reloadIfStale = async (error: AdminWriteError | null) => {
    if (error?.stale) {
      const reloaded = await getGroups()
      const message = reloaded ? STALE_GROUP_MESSAGE : STALE_GROUP_RELOAD_FAILED_MESSAGE
      showSnackBar({ msg: message, error: true })
      return { ...error, message }
    }
    return error
  }

  const createGroup = async (group: ManagedGroupCreate) => {
    const error = await API.createManagedGroup(group)
    if (error === null) {
      await getGroups()
    }
    return error
  }

  const updateGroup = async (group: ManagedGroup) => {
    const error = await API.updateManagedGroup(group)
    if (error === null) {
      await getGroups()
    }
    return await reloadIfStale(error)
  }

  const renameGroup = async (name: string, newName: string, version: number) => {
    const error = await API.renameManagedGroup(name, newName, version)
    if (error === null) {
      await getGroups()
    }
    return await reloadIfStale(error)
  }

  const deleteGroup = async (name: string, version: number) => {
    const error = await API.deleteManagedGroup(name, version)
    if (error === null) {
      await getGroups()
    }
    return await reloadIfStale(error)
  }

  const populate = async () => {
    await Promise.all([getGroups(), getMemberCandidates()])
  }

  return {
    groups,
    editMode,
    currentGroup,
    openGroupEditor,
    closeGroupEditor,
    memberCandidates,
    getGroups,
    getMemberCandidates,
    createGroup,
    updateGroup,
    renameGroup,
    deleteGroup,
    populate
  }
})
