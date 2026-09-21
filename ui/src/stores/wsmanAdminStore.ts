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
import { WsmanConfig, WsmanConfigInput, WsmanDataCollection, WsmanDataCollectionFileInput, WsmanReadiness, WsmanStatus, WsmanSyncResult } from '@/types/wsmanAdmin'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useWsmanAdminStore = defineStore('wsmanAdminStore', () => {
  const config = ref<WsmanConfig | null>(null)
  const loadError = ref(false)
  const isLoading = ref(false)
  const dataCollection = ref<WsmanDataCollection | null>(null)
  const dataCollectionError = ref(false)
  const status = ref<WsmanStatus | null>(null)
  const readiness = ref<WsmanReadiness | null>(null)

  // the status is keyed by definition position, so it is refreshed together
  // with the config it describes
  const getConfig = async () => {
    isLoading.value = true
    try {
      const [result, currentStatus, currentReadiness] = await Promise.all([API.getWsmanConfig(), API.getWsmanStatus(), API.getWsmanReadiness()])
      if (result !== null) {
        config.value = result
        loadError.value = false
      } else {
        loadError.value = true
      }
      status.value = currentStatus
      readiness.value = currentReadiness
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

  // null on success, else the reason; re-read either way so the next attempt
  // carries the file's current version
  const saveDataCollectionFile = async (file: string, input: WsmanDataCollectionFileInput): Promise<string | null> => {
    const error = await API.updateWsmanDataCollectionFile(file, input)
    await getDataCollection()
    return error
  }

  // the result on success (and the status is re-read), else the reason
  const syncDefinition = async (index: number): Promise<WsmanSyncResult | string> => {
    const result = await API.syncWsmanDefinition(index)
    if (typeof result !== 'string') {
      status.value = await API.getWsmanStatus()
    }
    return result
  }

  // enable polling or rescan; null on success (readiness and status re-read), else the reason
  const runReadinessAction = async (action: 'enable-polling' | 'rescan'): Promise<string | null> => {
    const result = await API.runWsmanReadinessAction(action)
    if (typeof result === 'string') {
      return result
    }
    readiness.value = result
    status.value = await API.getWsmanStatus()
    return null
  }

  // null on success (the view is replaced), else the reason
  const resetDataCollection = async (): Promise<string | null> => {
    const result = await API.resetWsmanDataCollection()
    if (typeof result === 'string') {
      return result
    }
    dataCollection.value = result
    dataCollectionError.value = false
    return null
  }

  return { config, loadError, isLoading, status, readiness, getConfig, saveConfig, dataCollection, dataCollectionError, getDataCollection, saveDataCollectionFile, syncDefinition, runReadinessAction, resetDataCollection }
})
