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

export type PluginStatus = 'installed' | 'staged' | 'failed' | 'unloaded' | 'unmanaged' | 'unknown'

export type KarCheckLevel = 'PASS' | 'WARN' | 'FAIL'

export interface PluginEntry {
  karName: string
  fileName: string
  sha256: string
  size: number
  uploadedBy: string | null
  // epoch milliseconds, or an ISO-8601 timestamp
  uploadedAt: number | string | null
  features: string[]
  bootFile: string | null
  autoStart: boolean
  status: PluginStatus
  pendingRestart: boolean
  // "github:<owner>/<repository>@<tag>" or "upload"
  source: string
}

export interface PluginManagementState {
  containerAvailable: boolean
  opennmsHome: string
  deployDir: string
  restartRequired: boolean
  plugins: PluginEntry[]
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
}

export interface RestartInstructions {
  packages: string
  container: string
  healthCheck: string
  note: string
}

export interface PluginInstallResult {
  plugin: PluginEntry
  // true only when the KAR manifest sets Karaf-Feature-Start: false
  restartRequired: boolean
  restartInstructions: RestartInstructions
}

export interface PluginUnloadResult {
  plugin: PluginEntry
  restartRequired: boolean
  // featuresBoot.d files, relative to OPENNMS_HOME, edited or deleted with the KAR
  bootFilesRemoved: string[]
  restartInstructions: RestartInstructions
}
