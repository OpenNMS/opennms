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

// Wire shapes of /api/v2/plugin-management (NMS-20365): KAR plugins staged
// into deploy/ with a featuresBoot.d boot file, plus the inspection a
// candidate KAR goes through before it is written anywhere.

export type PluginStatus = 'installed' | 'staged' | 'failed' | 'unloaded' | 'unknown'

export type KarCheckLevel = 'PASS' | 'WARN' | 'FAIL'

// how the container answered when a KAR was deployed or a plugin restarted:
// started, or the reason it is not running yet
export type PluginStartState = 'started' | 'failed' | 'timeout' | 'unavailable' | 'restart-required'

// one health signal of a running plugin: a health check its bundles registered, or a bundle whose container is not up
export interface PluginHealthCheck {
  description: string
  // Success, Starting, Failure, Timeout or Unknown, as org.opennms.core.health.api.Status names them
  status: string
  message: string | null
}

export type PluginHealthState = 'healthy' | 'unhealthy' | 'unknown'

export interface PluginStartOutcome {
  state: PluginStartState
  message: string
  // feature name -> why it did not start; filled for the failed state
  diagnostics: Record<string, string>
  // false when the features are Started but a health signal is not passing
  healthy: boolean
  health: PluginHealthCheck[]
}

export interface PluginEntry {
  karName: string
  // null when only the container knows the KAR and nothing sits in deploy/
  fileName: string | null
  sha256: string | null
  size: number
  uploadedBy: string | null
  // epoch milliseconds, or an ISO-8601 timestamp
  uploadedAt: number | string | null
  // what the boot file starts (or, for a plugin loaded by hand without one, the KAR's top-level features)
  features: string[]
  bootFile: string | null
  autoStart: boolean
  status: PluginStatus
  pendingRestart: boolean
  // "github:<owner>/<repository>@<tag>", "upload", or "manual" for a KAR loaded outside this page
  source: string
  // false for a KAR found in deploy/ or in the container without a record from this page
  managed: boolean
  // feature name -> container state, e.g. Started
  featureStates?: Record<string, string>
  // feature name -> why it did not start; filled for failed plugins
  diagnostics?: Record<string, string>
  // the state the last pluginStarted, pluginFailed or pluginStopped event reported
  lastNotifiedState?: string | null
  // what the last live start, restart or watchdog pass saw in the plugin's health checks
  health?: PluginHealthState
  healthMessages?: string[]
}

export interface PluginManagementState {
  containerAvailable: boolean
  opennmsHome: string
  deployDir: string
  restartRequired: boolean
  plugins: PluginEntry[]
  // set while a container operation that overran its time limit is still running; the container accepts no changes
  containerBusy?: string | null
  // temporary download area, reported when the server has one
  tempDir?: string
  tempBytes?: number
  tempFiles?: number
}

export interface KarCheck {
  id: string
  level: KarCheckLevel
  message: string
}

export interface KarFeatureDependency {
  name: string
  version: string
}

export interface KarFeature {
  name: string
  version: string
  // the features XML description attribute, when the plugin wrote one
  description: string | null
  // not hidden and not pulled in by another feature of the same KAR; only these can go into the boot file
  topLevel: boolean
  dependencies: KarFeatureDependency[]
}

export interface KarBundle {
  symbolicName: string
  version: string
}

export interface KarInspection {
  karName: string
  size: number
  sha256: string
  // names the server-side temporary copy for the install call
  uploadToken: string
  manifest: Record<string, string>
  features: KarFeature[]
  bundles: KarBundle[]
  checks: KarCheck[]
  // top-level features the page pre-selects; empty when the operator has to choose
  suggestedFeatures: string[]
  // set when the KAR was downloaded from a repository rather than uploaded
  source?: PluginFetchSource
}

export interface PluginCatalogEntry {
  id: string
  name: string
  description: string
  // "<owner>/<repository>" on GitHub
  repository: string
  docsUrl: string | null
}

export interface PluginCatalog {
  entries: PluginCatalogEntry[]
  // whether a repository outside the catalog may be looked up
  customAllowed: boolean
}

export interface PluginReleaseAsset {
  name: string
  size: number
  url: string
}

export interface PluginRelease {
  tag: string
  name: string
  // ISO-8601
  publishedAt: string
  prerelease: boolean
  // release notes as written on GitHub: untrusted markdown, shown as text
  notes: string
  assets: PluginReleaseAsset[]
}

export interface PluginReleases {
  repository: string
  releases: PluginRelease[]
  fetchedAt: string
  cached: boolean
}

export type PluginReleasesQuery = { catalogId: string } | { repository: string }

export interface PluginFetchInput {
  catalogId?: string
  repository?: string
  tag: string
  assetName: string
}

export interface PluginFetchSource {
  repository: string
  tag: string
  assetName: string
  url: string
}

export interface PluginInstallInput {
  uploadToken: string
  karName?: string
  acknowledgeWarnings: boolean
  // top-level features to list in the boot file; at least one
  features: string[]
}

export interface RestartInstructions {
  packages: string
  container: string
  healthCheck: string
  note: string
}

export interface PluginInstallResult {
  plugin: PluginEntry
  startOutcome: PluginStartOutcome
  // true whenever the features are not running after the load
  restartRequired: boolean
  restartInstructions: RestartInstructions
}

export interface PluginRestartResult {
  plugin: PluginEntry
  startOutcome: PluginStartOutcome
  restartRequired: boolean
}

export interface PluginUnloadResult {
  plugin: PluginEntry
  restartRequired: boolean
  // set when the container did not drop the KAR in time
  note?: string | null
  // featuresBoot.d files, relative to OPENNMS_HOME, edited or deleted with the KAR
  bootFilesRemoved: string[]
  restartInstructions: RestartInstructions
}
