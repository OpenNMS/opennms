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

export type PluginStatus = 'installed' | 'staged' | 'unloaded' | 'unmanaged' | 'unknown'

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
}

export interface PluginManagementState {
  containerAvailable: boolean
  opennmsHome: string
  deployDir: string
  restartRequired: boolean
  plugins: PluginEntry[]
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
  restartRequired: boolean
  restartInstructions: RestartInstructions
}
