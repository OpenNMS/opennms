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
import { AdminEditMode, AdminWriteError } from '@/types/adminWrite'
import { ManagedGroup, ManagedGroupCreate, STALE_GROUP_MESSAGE, STALE_GROUP_RELOAD_FAILED_MESSAGE } from '@/types/groupAdmin'
import { createSuccessResponse, ValidationResult } from '@/types/validation'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useGroupAdminStore = defineStore('groupAdminStore', () => {
  const groups = ref([] as ManagedGroup[])
  const memberCandidates = ref([] as string[])
  const availableCategories = ref([] as string[])
  // why the list reload after the last successful write failed; '' when it succeeded.
  // The write itself still returns null, so the caller reports both.
  const reloadError = ref('')

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

  // a failed read keeps the previous list on screen
  const getGroups = async (): Promise<ValidationResult> => {
    const result = await API.getManagedGroups()
    if (result.success) {
      groups.value = result.payload ?? []
    }
    return result
  }

  const getMemberCandidates = async (): Promise<ValidationResult> => {
    const result = await API.getGroupMemberCandidates()
    if (result.success) {
      memberCandidates.value = result.payload ?? []
    }
    return result
  }

  const getAvailableCategories = async (): Promise<ValidationResult> => {
    const result = await API.getAvailableGroupCategories()
    if (result.success) {
      availableCategories.value = result.payload ?? []
    }
    return result
  }

  // After a write: a success reloads the list (and notes a failed reload in reloadError);
  // a stale write means the list on screen is out of date, so it reloads too and words
  // the stale message by whether that reload worked. The caller shows the result.
  const afterWrite = async (error: AdminWriteError | null): Promise<AdminWriteError | null> => {
    reloadError.value = ''
    if (error === null) {
      const reloaded = await getGroups()
      reloadError.value = reloaded.success ? '' : reloaded.message
      return null
    }
    if (error.stale) {
      const reloaded = await getGroups()
      return { ...error, message: reloaded.success ? STALE_GROUP_MESSAGE : STALE_GROUP_RELOAD_FAILED_MESSAGE }
    }
    return error
  }

  const createGroup = async (group: ManagedGroupCreate) => afterWrite(await API.createManagedGroup(group))

  const updateGroup = async (group: ManagedGroup) => afterWrite(await API.updateManagedGroup(group))

  const renameGroup = async (name: string, newName: string, version: number) =>
    afterWrite(await API.renameManagedGroup(name, newName, version))

  const deleteGroup = async (name: string, version: number) => afterWrite(await API.deleteManagedGroup(name, version))

  // fails with the message of the first read that failed
  const populate = async (): Promise<ValidationResult> => {
    const results = await Promise.all([getGroups(), getMemberCandidates(), getAvailableCategories()])
    return results.find(r => !r.success) ?? createSuccessResponse()
  }

  return {
    groups,
    editMode,
    currentGroup,
    openGroupEditor,
    closeGroupEditor,
    memberCandidates,
    availableCategories,
    reloadError,
    getGroups,
    getMemberCandidates,
    getAvailableCategories,
    createGroup,
    updateGroup,
    renameGroup,
    deleteGroup,
    populate
  }
})
