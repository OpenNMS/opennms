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
import { reloadAfterWrite } from '@/services/adminWrite'
import { AdminEditMode, AdminWriteResult } from '@/types/adminWrite'
import { ManagedGroup, ManagedGroupCreate, STALE_GROUP_MESSAGE, STALE_GROUP_RELOAD_FAILED_MESSAGE } from '@/types/groupAdmin'
import { createSuccessResponse, ValidationResult } from '@/types/validation'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useGroupAdminStore = defineStore('groupAdminStore', () => {
  const groups = ref([] as ManagedGroup[])
  const memberCandidates = ref([] as string[])
  const availableCategories = ref([] as string[])

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

  // reloads the list after a write, and words a stale write's message by whether that reload worked
  const afterWrite = (result: AdminWriteResult) => reloadAfterWrite(result, getGroups,
    { reloaded: STALE_GROUP_MESSAGE, reloadFailed: STALE_GROUP_RELOAD_FAILED_MESSAGE })

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
