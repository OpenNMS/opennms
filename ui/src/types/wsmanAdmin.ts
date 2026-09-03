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

// Wire shape of GET /api/v2/wsman-config: the agent defaults on the root of
// wsman-config.xml plus each definition. A definition inherits any setting it
// leaves null from the defaults. Passwords are reported as present/absent only.
export interface WsmanAgentSettings {
  retry: number | null
  timeout: number | null
  username: string | null
  hasPassword: boolean
  port: number | null
  maxElements: number | null
  ssl: boolean | null
  strictSsl: boolean | null
  path: string | null
  productVendor: string | null
  productVersion: string | null
  gssAuth: boolean | null
}

export interface WsmanRange {
  begin: string
  end: string
}

export interface WsmanDefinition extends WsmanAgentSettings {
  ranges: WsmanRange[]
  specifics: string[]
  ipMatches: string[]
}

export interface WsmanConfig {
  defaults: WsmanAgentSettings
  definitions: WsmanDefinition[]
  // content hash of the file; a save must present it and is refused if the file changed
  version: string
}

// Request body of PUT /api/v2/wsman-config. password null keeps the stored
// one (never shown to the client); clearPassword removes it. sourceIndex is
// the index the definition was loaded from, so its stored password follows
// it through edits and reordering; null for a new definition.
export interface WsmanSettingsInput extends Omit<WsmanAgentSettings, 'hasPassword'> {
  password: string | null
  clearPassword: boolean
}

export interface WsmanDefinitionInput extends WsmanSettingsInput {
  ranges: WsmanRange[]
  specifics: string[]
  ipMatches: string[]
  sourceIndex: number | null
}

export interface WsmanConfigInput {
  version: string
  defaults: WsmanSettingsInput
  definitions: WsmanDefinitionInput[]
}

// Wire shape of GET /api/v2/wsman-config/data-collection: every object across
// wsman-datacollection-config.xml and wsman-datacollection.d, tagged with the
// file it came from. Read-only for now.
export interface WsmanCollectionInfo {
  name: string
  source: string
  rrdStep: number | null
  rras: string[]
  includeAllSystemDefinitions: boolean
  includedSystemDefinitions: string[]
}

export interface WsmanAttributeInfo {
  name: string
  alias: string
  type: string | null
  indexOf: string | null
  filter: string | null
}

export interface WsmanGroupInfo {
  name: string
  source: string
  resourceType: string
  resourceUri: string
  dialect: string | null
  filter: string | null
  attributes: WsmanAttributeInfo[]
}

export interface WsmanSystemDefinitionInfo {
  name: string
  source: string
  rules: string[]
  includedGroups: string[]
}

export interface WsmanDataCollection {
  rrdRepository: string | null
  sources: string[]
  // content hash per source file; a save of that file must present it
  versions: Record<string, string>
  collections: WsmanCollectionInfo[]
  groups: WsmanGroupInfo[]
  systemDefinitions: WsmanSystemDefinitionInfo[]
}

// Request body of PUT /api/v2/wsman-config/data-collection?file=<name>: the
// whole content of one source file. version is omitted to create a new file.
export interface WsmanCollectionInput {
  name: string
  rrdStep: number | null
  rras: string[]
  includeAllSystemDefinitions: boolean
  includedSystemDefinitions: string[]
}

export interface WsmanAttributeInput {
  name: string
  alias: string
  type: string
  indexOf: string | null
  filter: string | null
}

export interface WsmanGroupInput {
  name: string
  resourceType: string
  resourceUri: string
  dialect: string | null
  filter: string | null
  attributes: WsmanAttributeInput[]
}

export interface WsmanSystemDefinitionInput {
  name: string
  rules: string[]
  includedGroups: string[]
}

export interface WsmanDataCollectionFileInput {
  version: string | null
  rrdRepository: string | null
  collections: WsmanCollectionInput[]
  groups: WsmanGroupInput[]
  systemDefinitions: WsmanSystemDefinitionInput[]
}

// Wire shape of GET /api/v2/wsman-config/status: what the poller sees for the
// servers each definition matches. lastResponse is an epoch millis timestamp.
export interface WsmanStatusBucket {
  servers: number
  responding: number
  down: number
  lastResponse: number | null
}

export interface WsmanDefinitionStatus extends WsmanStatusBucket {
  index: number
}

export interface WsmanStatus {
  serviceName: string
  servers: number
  definitions: WsmanDefinitionStatus[]
  defaults: WsmanStatusBucket
}
