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

import { KarInspection, PluginEntry, PluginInstallInput, PluginInstallResult, PluginManagementState, PluginUnloadResult, RestartInstructions } from '@/types/pluginManagement'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { rest, v2 } from './axiosInstances'

// Plugin Management (NMS-20365): KAR plugins through /api/v2/plugin-management.
// No spinner or toast here: the page owns every surface these results land on.

const endpoint = '/plugin-management'
const jsonAccept = { headers: { Accept: 'application/json' }}

// /rest/logs/contents clamps n to this many lines.
export const MAX_LOG_LINES = 10000
export const LOG_LINE_OPTIONS = [200, 1000, 5000, 10000]
export const DEFAULT_LOG_LINES = 1000

const logUrl = (lines: number, reverse: boolean) =>
  `/logs/contents?f=plugin-management.log&reverse=${reverse}&n=${Math.min(MAX_LOG_LINES, Math.max(1, Math.floor(lines)))}`

const readLog = async (lines: number, reverse: boolean): Promise<string | null> => {
  try {
    const resp = await rest.get(logUrl(lines, reverse), { headers: { Accept: 'text/plain' }})
    return typeof resp.data === 'string' ? resp.data : ''
  } catch (_err) {
    return null
  }
}

// Only surface a server detail from a 4xx that looks like a short, plain
// message; a 5xx often carries a servlet HTML error page.
const errorMessage = (err: any, fallback: string): string => {
  const status = Number(err?.response?.status)
  const detail = err?.response?.data
  if (status >= 400 && status < 500 && typeof detail === 'string') {
    const trimmed = detail.trim()
    if (trimmed && trimmed.length <= 300 && !/[<>]/.test(trimmed)) {
      return trimmed
    }
  }
  return fallback
}

const asState = (data: any): PluginManagementState | null =>
  data && typeof data.containerAvailable === 'boolean' && Array.isArray(data.plugins)
    ? {
      containerAvailable: data.containerAvailable,
      opennmsHome: String(data.opennmsHome ?? ''),
      deployDir: String(data.deployDir ?? ''),
      restartRequired: data.restartRequired === true,
      plugins: data.plugins as PluginEntry[]
    }
    : null

// null on failure (not an empty list) so the page can show an error state
const getPluginManagement = async (): Promise<PluginManagementState | null> => {
  try {
    const resp = await v2.get(endpoint, jsonAccept)
    return asState(resp.data)
  } catch (_err) {
    return null
  }
}

// Inspects the KAR without writing anything; the inspection on success, else
// the reason to show in the card.
const checkPluginKar = async (file: File): Promise<ValidationResultWithPayload<KarInspection>> => {
  const formData = new FormData()
  formData.append('upload', file)
  try {
    const resp = await v2.post(`${endpoint}/check`, formData, jsonAccept)
    const data = resp.data
    if (!data || typeof data.uploadToken !== 'string' || !Array.isArray(data.checks)) {
      return createResultWithPayload<KarInspection>(false, 'The server returned an unexpected answer.')
    }
    return createResultWithPayload(true, '', {
      karName: String(data.karName ?? file.name),
      size: Number(data.size ?? file.size),
      sha256: String(data.sha256 ?? ''),
      uploadToken: data.uploadToken,
      manifest: data.manifest ?? {},
      features: Array.isArray(data.features) ? data.features : [],
      bundles: Array.isArray(data.bundles) ? data.bundles : [],
      checks: data.checks
    })
  } catch (err: any) {
    return createResultWithPayload<KarInspection>(false, errorMessage(err, `Failed to check ${file.name}.`))
  }
}

// Stages the inspected KAR; 409 carries the failed or unacknowledged check,
// 503 means the container is unavailable.
const installPlugin = async (input: PluginInstallInput): Promise<ValidationResultWithPayload<PluginInstallResult>> => {
  try {
    const resp = await v2.post(`${endpoint}/install`, input, jsonAccept)
    const data = resp.data
    if (!data || !data.plugin || !data.restartInstructions) {
      return createResultWithPayload<PluginInstallResult>(false, 'The server returned an unexpected answer.')
    }
    return createResultWithPayload(true, '', data as PluginInstallResult)
  } catch (err: any) {
    const status = Number(err?.response?.status)
    const fallback = status === 503 ? 'The plugin container is not available; the plugin was not loaded.' : 'Failed to load the plugin.'
    return createResultWithPayload<PluginInstallResult>(false, errorMessage(err, fallback))
  }
}

// Removes the KAR, its boot file and every featuresBoot.d line waiting for it.
const unloadPlugin = async (karName: string): Promise<ValidationResultWithPayload<PluginUnloadResult>> => {
  try {
    const resp = await v2.delete(`${endpoint}/${encodeURIComponent(karName)}`, jsonAccept)
    const data = resp.data
    if (!data || !data.plugin || typeof data.plugin.karName !== 'string') {
      return createResultWithPayload<PluginUnloadResult>(false, 'The server returned an unexpected answer.')
    }
    return createResultWithPayload(true, '', {
      plugin: data.plugin as PluginEntry,
      restartRequired: data.restartRequired === true,
      bootFilesRemoved: Array.isArray(data.bootFilesRemoved) ? data.bootFilesRemoved.map(String) : [],
      restartInstructions: data.restartInstructions as RestartInstructions
    })
  } catch (err: any) {
    return createResultWithPayload<PluginUnloadResult>(false, errorMessage(err, `Failed to unload ${karName}.`))
  }
}

// null on failure: the page then omits the commands rather than inventing them
const getPluginRestartInstructions = async (): Promise<RestartInstructions | null> => {
  try {
    const resp = await v2.get(`${endpoint}/restart-instructions`, jsonAccept)
    const data = resp.data
    if (!data || typeof data.packages !== 'string' || typeof data.container !== 'string') {
      return null
    }
    return {
      packages: data.packages,
      container: data.container,
      healthCheck: String(data.healthCheck ?? ''),
      note: String(data.note ?? '')
    }
  } catch (_err) {
    return null
  }
}

// The last `lines` entries of the audit log, newest first; null when it
// cannot be read (an empty string is a readable, empty log).
const getPluginManagementLog = (lines = DEFAULT_LOG_LINES): Promise<string | null> => readLog(lines, true)

// The whole file in its own order, for saving; null when it cannot be read.
const downloadPluginManagementLog = (): Promise<string | null> => readLog(MAX_LOG_LINES, false)

export { getPluginManagement, checkPluginKar, installPlugin, unloadPlugin, getPluginRestartInstructions, getPluginManagementLog, downloadPluginManagementLog }
