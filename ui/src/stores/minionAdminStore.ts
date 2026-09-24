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
import { Minion, MinionEdit } from '@/types/minionAdmin'
import { minionNodeKey } from '@/services/minionAdminService'
import { ValidationResult } from '@/types/validation'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useMinionAdminStore = defineStore('minionAdminStore', () => {
  const minions = ref([] as Minion[])
  const loadError = ref(false)
  const isLoading = ref(false)
  const totalCount = ref(0)
  const truncated = computed(() => minions.value.length < totalCount.value)
  // minion id+location -> its requisition node id, for the ID -> node link
  const nodeIdByMinion = ref<Record<string, number>>({})
  // the core's version, null until loaded or when /rest/info is unreachable
  const coreVersion = ref<string | null>(null)

  // minions grouped by location name, for the Monitoring locations tab
  const byLocation = computed<Record<string, Minion[]>>(() => {
    const groups: Record<string, Minion[]> = {}
    for (const minion of minions.value) {
      const key = minion.location ?? ''
      ;(groups[key] ??= []).push(minion)
    }
    return groups
  })

  // false when the load failed; the previous list is kept
  const getMinions = async (): Promise<boolean> => {
    isLoading.value = true
    try {
      const result = await API.listMinions()
      if (result !== null) {
        minions.value = result.minions
        totalCount.value = result.totalCount
        loadError.value = false
        nodeIdByMinion.value = await API.getMinionNodeIds(result.minions)
      } else {
        loadError.value = true
      }
      return result !== null
    } finally {
      isLoading.value = false
    }
  }

  const nodeIdFor = (minion: Minion): number | undefined =>
    nodeIdByMinion.value[minionNodeKey(minion.id, minion.location)]

  const getCoreVersion = async (): Promise<string | null> => {
    coreVersion.value = await API.getCoreVersion()
    return coreVersion.value
  }

  const updateMinion = async (edit: MinionEdit): Promise<ValidationResult> => {
    const result = await API.updateMinion(edit)
    if (result.success) {
      await getMinions()
    }
    return result
  }

  const getAlarmCount = (id: string) => API.getAlarmCountForMinion(id)

  const deleteMinion = async (id: string): Promise<ValidationResult> => {
    const result = await API.deleteMinion(id)
    if (result.success) {
      await getMinions()
    }
    return result
  }

  return {
    minions,
    loadError,
    isLoading,
    totalCount,
    truncated,
    coreVersion,
    byLocation,
    nodeIdFor,
    getMinions,
    getCoreVersion,
    updateMinion,
    getAlarmCount,
    deleteMinion
  }
})
