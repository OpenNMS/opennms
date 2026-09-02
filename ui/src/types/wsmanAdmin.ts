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
}
