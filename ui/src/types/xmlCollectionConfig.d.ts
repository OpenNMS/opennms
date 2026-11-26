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

// The XML collector configuration, as served by /api/v2/xml-collection (formerly xml-datacollection-config.xml
// and xml-datacollection/*.xml).

export interface XmlKeyValue {
  key: string
  value: string
}

export interface XmlMapping {
  /** Omitted: the mapping applies to every value without a mapping of its own. */
  from?: string | null
  to: string
}

export interface XmlObject {
  name: string
  type: string
  xpath: string
  mappings: XmlMapping[]
}

export interface XmlGroup {
  name: string
  resourceType: string
  resourceXpath: string
  keyXpath?: string | null
  timestampXpath?: string | null
  timestampFormat?: string | null
  resourceKeys: string[]
  objects: XmlObject[]
}

export interface XmlRequest {
  method: string
  parameters: XmlKeyValue[]
  headers: XmlKeyValue[]
  contentType?: string | null
  content?: string | null
}

export interface XmlSource {
  url: string
  request?: XmlRequest | null
  importGroups: string[]
  groups: XmlGroup[]
}

export interface XmlRrd {
  step: number | null
  rras: string[]
}

export interface XmlCollection {
  name: string
  rrd: XmlRrd
  sources: XmlSource[]
  /** The ETag of the collection as read; sent back as If-Match. */
  version?: string
}

export interface XmlCollectionSummary {
  name: string
  step?: number | null
  sourceCount: number
  groupCount: number
  importGroups: string[]
}

export interface XmlGroupFile {
  id: string
  groups: XmlGroup[]
  version?: string
}

export interface XmlGroupFileSummary {
  id: string
  groupCount: number
  usedBy: string[]
}

export interface XmlCollectionSettings {
  rrdRepository: string
}

export interface XmlResourceType {
  name: string
  label: string
}

export interface XmlCollectionMetadata {
  attributeTypes: string[]
  resourceTypes: XmlResourceType[]
  groupFiles: string[]
}

export interface XmlUploadResult {
  type: 'collections' | 'groupFile'
  id?: string
  count?: number
}
