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

/** A key/value parameter of a strategy. A key may occur more than once. */
export interface ResourceTypeParameter {
  key: string
  value: string
}

/** A persistence selector or storage strategy: a Java class and its parameters. */
export interface ResourceTypeStrategy {
  className: string
  parameters: ResourceTypeParameter[]
}

export interface ResourceTypeDefinition {
  name: string
  label: string
  resourceLabel?: string
  persistenceSelectorStrategy: ResourceTypeStrategy
  storageStrategy: ResourceTypeStrategy
}

/** A resource types configuration, the counterpart of one former file in etc/resource-types.d/. */
export interface ResourceTypesConfig {
  id: string
  resourceTypes: ResourceTypeDefinition[]
  /** The ETag of the GET, sent back as If-Match. */
  version?: string
}

export interface ResourceTypesConfigSummary {
  id: string
  resourceTypeNames: string[]
}

export interface ResourceTypesMetadata {
  storageStrategies: string[]
  persistenceSelectorStrategies: string[]
  /** Defined in datacollection-config.xml; a stored type of the same name overrides them. */
  dataCollectionTypeNames: string[]
  /** Provided by extensions; they override a stored type of the same name. */
  extensionTypeNames: string[]
}

export interface ResourceTypesConfigErrors {
  id?: string
}

export interface ResourceTypeErrors {
  name?: string
  label?: string
  persistenceSelectorStrategy?: string
  storageStrategy?: string
  persistenceSelectorParameters?: string
  storageParameters?: string
}
