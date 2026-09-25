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
import { DEFAULT_LOG_LINES } from '@/services/pluginManagementService'
import { KarInspection, PluginCatalog, PluginEntry, PluginFetchInput, PluginInstallInput, PluginInstallResult, PluginManagementState, PluginReleases, PluginReleasesQuery, PluginUnloadResult, RestartInstructions } from '@/types/pluginManagement'
import { ValidationResultWithPayload } from '@/types/validation'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const usePluginManagementStore = defineStore('pluginManagementStore', () => {
  const state = ref<PluginManagementState | null>(null)
  const loadError = ref(false)
  const isLoading = ref(false)
  const restartInstructions = ref<RestartInstructions | null>(null)
  // null once a read has failed; undefined until the first read
  const log = ref<string | null | undefined>(undefined)
  const logLines = ref(DEFAULT_LOG_LINES)
  // undefined until read, null once a read has failed
  const catalog = ref<PluginCatalog | null | undefined>(undefined)
  const releases = ref<PluginReleases | null>(null)

  const plugins = computed<PluginEntry[]>(() => state.value?.plugins ?? [])
  const containerAvailable = computed<boolean>(() => state.value?.containerAvailable ?? true)
  const restartRequired = computed<boolean>(() => state.value?.restartRequired ?? false)

  // keeps the previous list on failure so a transient error does not blank the page
  const load = async () => {
    isLoading.value = true
    try {
      const result = await API.getPluginManagement()
      if (result !== null) {
        state.value = result
        loadError.value = false
      } else {
        loadError.value = true
      }
    } finally {
      isLoading.value = false
    }
  }

  const check = (file: File): Promise<ValidationResultWithPayload<KarInspection>> => API.checkPluginKar(file)

  const loadCatalog = async (): Promise<PluginCatalog | null> => {
    catalog.value = await API.getPluginCatalog()
    return catalog.value
  }

  // the previous list is dropped on failure so a stale version list is never offered
  const loadReleases = async (query: PluginReleasesQuery): Promise<ValidationResultWithPayload<PluginReleases>> => {
    const result = await API.getPluginReleases(query)
    releases.value = result.success && result.payload ? result.payload : null
    return result
  }

  const fetchFromRepository = (input: PluginFetchInput): Promise<ValidationResultWithPayload<KarInspection>> => API.fetchPluginFromRepository(input)

  // the list and the log are re-read after a successful action, so the new
  // status and its audit entry show up together
  const install = async (input: PluginInstallInput): Promise<ValidationResultWithPayload<PluginInstallResult>> => {
    const result = await API.installPlugin(input)
    if (result.success) {
      await Promise.all([load(), refreshLog()])
    }
    return result
  }

  const unload = async (karName: string): Promise<ValidationResultWithPayload<PluginUnloadResult>> => {
    const result = await API.unloadPlugin(karName)
    if (result.success) {
      await Promise.all([load(), refreshLog()])
    }
    return result
  }

  const getRestartInstructions = async (): Promise<RestartInstructions | null> => {
    if (restartInstructions.value === null) {
      restartInstructions.value = await API.getPluginRestartInstructions()
    }
    return restartInstructions.value
  }

  const refreshLog = async () => {
    log.value = await API.getPluginManagementLog(logLines.value)
  }

  const setLogLines = async (lines: number) => {
    logLines.value = lines
    await refreshLog()
  }

  // the full file for saving; null when it cannot be read
  const downloadLog = (): Promise<string | null> => API.downloadPluginManagementLog()

  return { state, loadError, isLoading, restartInstructions, log, logLines, catalog, releases, plugins, containerAvailable, restartRequired, load, check, loadCatalog, loadReleases, fetchFromRepository, install, unload, getRestartInstructions, refreshLog, setLogLines, downloadLog }
})
