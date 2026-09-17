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

export interface ResourceFilter {
  field: string
  content?: string
}

export interface BaseThresholdDef {
  relaxed?: boolean
  description?: string
  type: string
  dsType: string
  // Strings, not numbers: the schema also accepts a metadata reference such as ${scv:key:value}, and an
  // OnmsInputNumber would silently destroy a valid configuration.
  value: string
  rearm: string
  trigger: string
  dsLabel?: string
  exprLabel?: string
  triggeredUEI?: string
  rearmedUEI?: string
  filterOperator?: string
  resourceFilters: ResourceFilter[]
}

export interface Threshold extends BaseThresholdDef {
  dsName: string
}

export interface Expression extends BaseThresholdDef {
  expression: string
}

export type ThresholdDefinition = Threshold | Expression

export interface ThresholdGroup {
  name: string
  rrdRepository: string
  thresholds: Threshold[]
  expressions: Expression[]
  readOnly?: boolean
  /** Entity tag of the group as read; sent back as If-Match so a concurrent change is not clobbered. */
  version?: string
}

export interface ThresholdGroupSummary {
  name: string
  rrdRepository: string
  thresholdCount: number
  expressionCount: number
  readOnly?: boolean
}

export interface ThresholdDsType {
  name: string
  label: string
}

export interface ThresholdingMetadata {
  dsTypes: ThresholdDsType[]
  thresholdTypes: string[]
  filterOperators: string[]
}

export interface ThreshdParameter {
  key: string
  value: string
}

export interface ThreshdAddressRange {
  begin: string
  end: string
}

export interface ThreshdService {
  name: string
  interval: number
  userDefined?: boolean
  status?: string
  parameters: ThreshdParameter[]
}

export interface ThreshdPackage {
  name: string
  filter: string
  specifics: string[]
  includeRanges: ThreshdAddressRange[]
  excludeRanges: ThreshdAddressRange[]
  includeUrls: string[]
  services: ThreshdService[]
  outageCalendars: string[]
  version?: string
}

export interface ThreshdPackageSummary {
  name: string
  filter: string
  serviceCount: number
  outageCalendars: string[]
}

export interface ThreshdConfiguration {
  packages: ThreshdPackage[]
  version?: string
}

export interface ThresholdDefinitionErrors {
  type?: string
  dsName?: string
  expression?: string
  dsType?: string
  value?: string
  rearm?: string
  trigger?: string
  triggeredUEI?: string
  rearmedUEI?: string
  filterOperator?: string
}

export interface ThresholdGroupErrors {
  name?: string
  rrdRepository?: string
}

export interface ResourceFilterErrors {
  field?: string
  content?: string
}

export interface ThreshdPackageErrors {
  name?: string
  filter?: string
}

export interface ThreshdServiceErrors {
  name?: string
  interval?: string
  thresholdingGroup?: string
}
