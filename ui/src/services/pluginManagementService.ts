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

import { KarFeature, KarInspection, PluginCatalog, PluginEntry, PluginFetchInput, PluginInstallInput, PluginInstallResult, PluginManagementState, PluginReleases, PluginReleasesQuery, PluginUnloadResult, RestartInstructions } from '@/types/pluginManagement'
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

// Only surface a server detail that looks like a short, plain message; a 5xx
// often carries a servlet HTML error page, so those are skipped unless the
// endpoint is known to answer text/plain (the GitHub-backed ones send a 502).
const errorMessage = (err: any, fallback: string, serverErrors = false): string => {
  const status = Number(err?.response?.status)
  const detail = err?.response?.data
  if (status >= 400 && (serverErrors || status < 500) && typeof detail === 'string') {
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
      plugins: data.plugins as PluginEntry[],
      ...(typeof data.tempDir === 'string' ? { tempDir: data.tempDir } : {}),
      ...(Number.isFinite(Number(data.tempBytes)) && data.tempBytes !== null ? { tempBytes: Number(data.tempBytes) } : {}),
      ...(Number.isFinite(Number(data.tempFiles)) && data.tempFiles !== null ? { tempFiles: Number(data.tempFiles) } : {})
    }
    : null

// a feature without the topLevel flag comes from an older server and is offered as a choice
const asFeature = (f: any): KarFeature => ({
  name: String(f?.name ?? ''),
  version: String(f?.version ?? ''),
  description: typeof f?.description === 'string' && f.description ? f.description : null,
  topLevel: f?.topLevel !== false,
  dependencies: Array.isArray(f?.dependencies) ? f.dependencies : []
})

const asInspection = (data: any, fallbackName: string, fallbackSize: number): KarInspection | null =>
  data && typeof data.uploadToken === 'string' && Array.isArray(data.checks)
    ? {
      karName: String(data.karName ?? fallbackName),
      size: Number(data.size ?? fallbackSize),
      sha256: String(data.sha256 ?? ''),
      uploadToken: data.uploadToken,
      manifest: data.manifest ?? {},
      features: Array.isArray(data.features) ? data.features.map(asFeature) : [],
      bundles: Array.isArray(data.bundles) ? data.bundles : [],
      checks: data.checks,
      suggestedFeatures: Array.isArray(data.suggestedFeatures) ? data.suggestedFeatures.map(String) : [],
      ...(data.source && typeof data.source.repository === 'string'
        ? { source: { repository: data.source.repository, tag: String(data.source.tag ?? ''), assetName: String(data.source.assetName ?? ''), url: String(data.source.url ?? '') }}
        : {})
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
    const inspection = asInspection(resp.data, file.name, file.size)
    return inspection
      ? createResultWithPayload(true, '', inspection)
      : createResultWithPayload<KarInspection>(false, 'The server returned an unexpected answer.')
  } catch (err: any) {
    return createResultWithPayload<KarInspection>(false, errorMessage(err, `Failed to check ${file.name}.`))
  }
}

// null on failure so the repository tab can say the catalog is unavailable
const getPluginCatalog = async (): Promise<PluginCatalog | null> => {
  try {
    const resp = await v2.get(`${endpoint}/catalog`, jsonAccept)
    const data = resp.data
    if (!data || !Array.isArray(data.entries)) {
      return null
    }
    return {
      entries: data.entries
        .filter((e: any) => e && typeof e.id === 'string' && typeof e.repository === 'string')
        .map((e: any) => ({
          id: e.id,
          name: String(e.name ?? e.id),
          description: String(e.description ?? ''),
          repository: e.repository,
          docsUrl: typeof e.docsUrl === 'string' && e.docsUrl ? e.docsUrl : null
        })),
      customAllowed: data.customAllowed === true
    }
  } catch (_err) {
    return null
  }
}

// The releases the server read from GitHub for a catalog entry or an
// arbitrary owner/name; the server's text/plain reason (a 502 for a GitHub
// rate limit, for instance) is passed on as the message.
const getPluginReleases = async (query: PluginReleasesQuery): Promise<ValidationResultWithPayload<PluginReleases>> => {
  const url = 'catalogId' in query
    ? `${endpoint}/catalog/${encodeURIComponent(query.catalogId)}/releases`
    : `${endpoint}/releases?repository=${encodeURIComponent(query.repository)}`
  try {
    const resp = await v2.get(url, jsonAccept)
    const data = resp.data
    if (!data || !Array.isArray(data.releases)) {
      return createResultWithPayload<PluginReleases>(false, 'The server returned an unexpected answer.')
    }
    return createResultWithPayload(true, '', {
      repository: String(data.repository ?? ('repository' in query ? query.repository : '')),
      releases: data.releases
        .filter((r: any) => r && typeof r.tag === 'string')
        .map((r: any) => ({
          tag: r.tag,
          name: String(r.name ?? r.tag),
          publishedAt: String(r.publishedAt ?? ''),
          prerelease: r.prerelease === true,
          notes: String(r.notes ?? ''),
          assets: Array.isArray(r.assets)
            ? r.assets.filter((a: any) => a && typeof a.name === 'string').map((a: any) => ({ name: a.name, size: Number(a.size ?? 0), url: String(a.url ?? '') }))
            : []
        })),
      fetchedAt: String(data.fetchedAt ?? ''),
      cached: data.cached === true
    })
  } catch (err: any) {
    return createResultWithPayload<PluginReleases>(false, errorMessage(err, 'Failed to read the releases from GitHub.', true))
  }
}

// Downloads the release asset on the server and inspects it like an upload.
const fetchPluginFromRepository = async (input: PluginFetchInput): Promise<ValidationResultWithPayload<KarInspection>> => {
  try {
    const resp = await v2.post(`${endpoint}/fetch`, input, jsonAccept)
    const inspection = asInspection(resp.data, input.assetName, 0)
    return inspection
      ? createResultWithPayload(true, '', inspection)
      : createResultWithPayload<KarInspection>(false, 'The server returned an unexpected answer.')
  } catch (err: any) {
    return createResultWithPayload<KarInspection>(false, errorMessage(err, `Failed to download ${input.assetName}.`, true))
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

export { getPluginManagement, checkPluginKar, getPluginCatalog, getPluginReleases, fetchPluginFromRepository, installPlugin, unloadPlugin, getPluginRestartInstructions, getPluginManagementLog, downloadPluginManagementLog }
