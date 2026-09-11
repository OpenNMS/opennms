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

import {
  WsmanCollectionInfo,
  WsmanDataCollection,
  WsmanDataCollectionFileInput,
  WsmanGroupInfo,
  WsmanSystemDefinitionInfo
} from '@/types/wsmanAdmin'

export type DataCollectionKind = 'collection' | 'group' | 'systemDefinition'

export const ROOT_FILE = 'wsman-datacollection-config.xml'

export const ATTRIBUTE_TYPES = ['gauge', 'counter', 'string']

// The content of one source file, rebuilt from the flattened view, with the
// version the server handed out for it. A file that does not exist yet has no
// version, which is how the server knows to create it.
export const fileInput = (dc: WsmanDataCollection, file: string): WsmanDataCollectionFileInput => ({
  version: dc.versions[file] ?? null,
  rrdRepository: file === ROOT_FILE ? dc.rrdRepository : null,
  collections: dc.collections.filter(c => c.source === file).map(c => ({
    name: c.name,
    rrdStep: c.rrdStep,
    rras: [...c.rras],
    includeAllSystemDefinitions: c.includeAllSystemDefinitions,
    includedSystemDefinitions: [...c.includedSystemDefinitions]
  })),
  groups: dc.groups.filter(g => g.source === file).map(g => ({
    name: g.name,
    resourceType: g.resourceType,
    resourceUri: g.resourceUri,
    dialect: g.dialect,
    filter: g.filter,
    attributes: g.attributes.map(a => ({ name: a.name, alias: a.alias, type: a.type ?? 'gauge', indexOf: a.indexOf, filter: a.filter }))
  })),
  systemDefinitions: dc.systemDefinitions.filter(s => s.source === file).map(s => ({
    name: s.name,
    rules: [...s.rules],
    includedGroups: [...s.includedGroups]
  }))
})

// Replaces (by original name) or appends one object in a file's input.
export const upsert = <T extends { name: string }>(list: T[], originalName: string | null, item: T): T[] => {
  const index = originalName === null ? -1 : list.findIndex(x => x.name === originalName)
  if (index < 0) {
    return [...list, item]
  }
  return list.map((x, i) => (i === index ? item : x))
}

export const remove = <T extends { name: string }>(list: T[], name: string): T[] => list.filter(x => x.name !== name)

export const isRra = (s: string): boolean => /^RRA:(AVERAGE|MIN|MAX|LAST):[0-9.]+:[0-9]+:[0-9]+$/i.test(s.trim())

// Whether a name is already taken by another object of the same kind, in any file.
export const nameTaken = (dc: WsmanDataCollection, kind: DataCollectionKind, name: string, originalName: string | null): boolean => {
  const list: { name: string }[] = kind === 'collection' ? dc.collections : kind === 'group' ? dc.groups : dc.systemDefinitions
  return list.some(x => x.name === name.trim() && x.name !== originalName)
}

export type EditableObject = WsmanCollectionInfo | WsmanGroupInfo | WsmanSystemDefinitionInfo
