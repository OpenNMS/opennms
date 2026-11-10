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

// The DTOs of /api/v2/httpcollection, the HTTP collector configuration (formerly http-datacollection-config.xml).

export interface HttpParameter {
  key: string
  value: string
}

export interface HttpHeader {
  name: string
  value: string
}

export interface HttpAttrib {
  alias: string
  matchGroup: number | null
  type: string
}

/** A null or absent field takes its schema default; the server returns every default filled in. */
export interface HttpUrl {
  method?: string | null
  httpVersion?: string | null
  userAgent?: string | null
  virtualHost?: string | null
  scheme?: string | null
  userInfo?: string | null
  host?: string | null
  port?: number | null
  path: string
  query?: string | null
  fragment?: string | null
  matches?: string | null
  responseRange?: string | null
  canonicalEquivalence?: boolean | null
  caseInsensitive?: boolean | null
  comments?: boolean | null
  dotall?: boolean | null
  literal?: boolean | null
  multiline?: boolean | null
  unicodeCase?: boolean | null
  unixLines?: boolean | null
  parameters?: HttpParameter[]
  headers?: HttpHeader[]
}

export interface HttpUri {
  name: string
  url: HttpUrl
  attributes?: HttpAttrib[]
}

export interface HttpRrd {
  step: number | null
  rras: string[]
}

export interface HttpCollection {
  name: string
  rrd: HttpRrd
  uris: HttpUri[]
  /** ETag of the collection as last read; sent back as If-Match on a write. */
  version?: string
}

export interface HttpCollectionSummary {
  name: string
  rrdStep?: number
  uriCount?: number
  attributeCount?: number
}

export interface HttpCollectionSettings {
  rrdRepository: string
  version?: string
}

export interface HttpCollectionMetadata {
  attributeTypes: string[]
  methods: string[]
  httpVersions: string[]
  schemes: string[]
  defaultRrd: HttpRrd
}

export interface HttpCollectionErrors {
  name?: string
  step?: string
  rras?: string
}

export interface HttpUriErrors {
  name?: string
  path?: string
  port?: string
  responseRange?: string
  matches?: string
  parameters?: string
  headers?: string
  attributes?: string
}

export interface HttpSettingsErrors {
  rrdRepository?: string
}
