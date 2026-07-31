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
import { Minion } from '@/types/minionAdmin'
import { MinionEdit } from '@/services/minionAdminService'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMinionAdminStore = defineStore('minionAdminStore', () => {
  const minions = ref([] as Minion[])
  const loadError = ref(false)
  const isLoading = ref(false)

  const getMinions = async () => {
    isLoading.value = true
    try {
      const result = await API.listMinions()
      if (result !== null) {
        minions.value = result
        loadError.value = false
      } else {
        loadError.value = true
      }
    } finally {
      isLoading.value = false
    }
  }

  const updateMinion = async (edit: MinionEdit) => {
    const error = await API.updateMinion(edit)
    if (error === null) {
      await getMinions()
    }
    return error
  }

  const deleteMinion = async (id: string) => {
    const error = await API.deleteMinion(id)
    if (error === null) {
      await getMinions()
    }
    return error
  }

  return { minions, loadError, isLoading, getMinions, updateMinion, deleteMinion }
})
