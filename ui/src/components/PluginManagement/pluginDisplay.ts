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

import { formatInDisplayZone } from '@/lib/displayTimeZone'
import { isValid, parseISO } from 'date-fns'
import { KarCheckLevel, PluginEntry, PluginFetchSource, PluginRelease, PluginStatus } from '@/types/pluginManagement'
import { type OnmsTagSeverity } from '@opennms/onms-ui'

export const NOT_SET = '—'

export const CONTAINER_UNAVAILABLE = 'The plugin container is not available, so plugins cannot be loaded or unloaded.'

export const formatSize = (bytes: number | null | undefined): string => {
  if (bytes === null || bytes === undefined || !Number.isFinite(bytes) || bytes < 0) {
    return NOT_SET
  }
  if (bytes < 1024) {
    return `${bytes} B`
  }
  const units = ['KB', 'MB', 'GB']
  let value = bytes / 1024
  let unit = 0
  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024
    unit++
  }
  return `${value.toFixed(value >= 100 ? 0 : 1)} ${units[unit]}`
}

export const shortSha = (sha256: string | null | undefined): string => sha256 ? sha256.slice(0, 12) : NOT_SET

// epoch milliseconds, a numeric string, or an ISO-8601 timestamp
export const formatUploadedAt = (value: number | string | null | undefined): string => {
  if (value === null || value === undefined || value === '') {
    return NOT_SET
  }
  const date = typeof value === 'number' ? new Date(value) : /^\d+$/.test(value) ? new Date(Number(value)) : parseISO(value)
  return isValid(date) ? formatInDisplayZone(date) : NOT_SET
}

// "<owner>/<repository>" as GitHub accepts it; ".." is refused as on the server
export const REPOSITORY_PATTERN = /^[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+$/

export const isRepository = (value: string | null | undefined): boolean =>
  !!value && REPOSITORY_PATTERN.test(value) && !value.includes('..')

export const formatReleaseDate = (iso: string | null | undefined): string => {
  if (!iso) {
    return NOT_SET
  }
  const date = parseISO(iso)
  return isValid(date) ? formatInDisplayZone(date, 'yyyy-MM-dd') : NOT_SET
}

export const releaseLabel = (release: PluginRelease): string =>
  `${release.tag} · ${formatReleaseDate(release.publishedAt)}${release.prerelease ? ' · pre-release' : ''}`

const publishedMillis = (release: PluginRelease): number => {
  const date = parseISO(release.publishedAt ?? '')
  return isValid(date) ? date.getTime() : 0
}

// newest first, every pre-release after the last release
export const sortReleases = (releases: PluginRelease[]): PluginRelease[] =>
  [...releases].sort((a, b) => Number(a.prerelease) - Number(b.prerelease) || publishedMillis(b) - publishedMillis(a))

export const defaultRelease = (releases: PluginRelease[]): PluginRelease | null => {
  const sorted = sortReleases(releases)
  return sorted.find(r => !r.prerelease) ?? sorted[0] ?? null
}

export const karAssets = (release: PluginRelease | null | undefined) =>
  (release?.assets ?? []).filter(a => a.name.toLowerCase().endsWith('.kar'))

export const fetchedSourceLine = (source: PluginFetchSource, size: number | null | undefined): string =>
  `Fetched from ${source.repository} ${source.tag} (${source.assetName}, ${formatSize(size)})`

export const uploadedSourceLine = (fileName: string, size: number | null | undefined): string =>
  `Uploaded ${fileName} (${formatSize(size)})`

export interface SourcePresentation {
  label: string
  title: string
}

// "github:<owner>/<repository>@<tag>" reads as "<owner>/<repository>@<tag>";
// anything else that is not "upload" is shown as sent
export const sourceOf = (source: string | null | undefined): SourcePresentation => {
  if (!source || source === 'upload') {
    return { label: 'Uploaded file', title: source ?? '' }
  }
  const github = /^github:(.+)$/.exec(source)
  return { label: github ? github[1] : source, title: source }
}

export const CHECK_TAG: Record<KarCheckLevel, OnmsTagSeverity> = { PASS: 'success', WARN: 'warn', FAIL: 'danger' }

export interface StatusPresentation {
  severity: OnmsTagSeverity
  label: string
  title: string
}

export const STATUS_TAG: Record<PluginStatus, StatusPresentation> = {
  installed: { severity: 'success', label: 'Loaded', title: 'loaded and its features are started' },
  staged: { severity: 'warn', label: 'Load pending restart', title: 'written to deploy/; its features start on the next restart' },
  failed: { severity: 'danger', label: 'Failed to start', title: 'one or more features did not start; see karaf.log' },
  unloaded: { severity: 'secondary', label: 'Unloaded', title: 'removed from deploy/' },
  unmanaged: { severity: 'info', label: 'Not managed here', title: 'found in deploy/ but not loaded through this page' },
  unknown: { severity: 'secondary', label: 'Unknown', title: 'the container could not be reached, so the state of this plugin is not known' }
}

const UNLOAD_PENDING: StatusPresentation = { severity: 'warn', label: 'Unload pending restart', title: 'removed from deploy/; the next restart completes the removal' }

export const statusOf = (status: string, pendingRestart = false): StatusPresentation => {
  if (status === 'unloaded' && pendingRestart) {
    return UNLOAD_PENDING
  }
  return STATUS_TAG[status as PluginStatus] ?? { severity: 'secondary', label: status, title: '' }
}

export interface RestartCounts {
  toLoad: number
  toUnload: number
}

export const restartCounts = (plugins: PluginEntry[]): RestartCounts => ({
  toLoad: plugins.filter(p => p.status === 'staged' || (p.pendingRestart && p.status !== 'unloaded')).length,
  toUnload: plugins.filter(p => p.status === 'unloaded' && p.pendingRestart).length
})

export const restartSummary = (plugins: PluginEntry[]): string => {
  const { toLoad, toUnload } = restartCounts(plugins)
  return `A restart is required to finish loading or unloading plugins: ${toLoad} to load, ${toUnload} to unload.`
}
