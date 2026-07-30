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
import { ManagedGroup } from '@/types/groupAdmin'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useGroupAdminStore = defineStore('groupAdminStore', () => {
  const groups = ref([] as ManagedGroup[])
  const memberCandidates = ref([] as string[])

  const getGroups = async () => {
    const result = await API.getManagedGroups()
    if (result !== null) {
      groups.value = result
    }
  }

  const getMemberCandidates = async () => {
    memberCandidates.value = await API.getGroupMemberCandidates()
  }

  const createGroup = async (group: ManagedGroup) => {
    const ok = await API.createManagedGroup(group)
    if (ok) {
      await getGroups()
    }
    return ok
  }

  const updateGroup = async (group: ManagedGroup) => {
    const ok = await API.updateManagedGroup(group)
    if (ok) {
      await getGroups()
    }
    return ok
  }

  const renameGroup = async (name: string, newName: string) => {
    const ok = await API.renameManagedGroup(name, newName)
    if (ok) {
      await getGroups()
    }
    return ok
  }

  const deleteGroup = async (name: string) => {
    const ok = await API.deleteManagedGroup(name)
    if (ok) {
      await getGroups()
    }
    return ok
  }

  const populate = async () => {
    await Promise.all([getGroups(), getMemberCandidates()])
  }

  return {
    groups,
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
