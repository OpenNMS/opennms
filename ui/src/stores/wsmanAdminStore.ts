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
import { WsmanConfig, WsmanConfigInput, WsmanDataCollection } from '@/types/wsmanAdmin'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useWsmanAdminStore = defineStore('wsmanAdminStore', () => {
  const config = ref<WsmanConfig | null>(null)
  const loadError = ref(false)
  const isLoading = ref(false)
  const dataCollection = ref<WsmanDataCollection | null>(null)
  const dataCollectionError = ref(false)

  const getConfig = async () => {
    isLoading.value = true
    try {
      const result = await API.getWsmanConfig()
      if (result !== null) {
        config.value = result
        loadError.value = false
      } else {
        loadError.value = true
      }
    } finally {
      isLoading.value = false
    }
  }

  // null on success, else the reason to show. The config is re-read either
  // way: on success for the new state, on failure (e.g. a 409 because another
  // admin saved first) so the next attempt is built from the current file.
  const saveConfig = async (input: WsmanConfigInput): Promise<string | null> => {
    const error = await API.updateWsmanConfig(input)
    await getConfig()
    return error
  }

  const getDataCollection = async () => {
    const result = await API.getWsmanDataCollection()
    if (result !== null) {
      dataCollection.value = result
      dataCollectionError.value = false
    } else {
      dataCollectionError.value = true
    }
  }

  return { config, loadError, isLoading, getConfig, saveConfig, dataCollection, dataCollectionError, getDataCollection }
})
