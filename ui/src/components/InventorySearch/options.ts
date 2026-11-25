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

// Static option lists for the inventory (node) search form. Values mirror the
// legacy element/index.jsp so the query strings the Node List page already
// tracks (mib2Parm, snmpParm, assetColumn, …) are produced unchanged.

export interface Option {
  label: string
  value: string
}

// SNMP MIB-2 system attributes (the "System attribute" search)
export const SYSTEM_ATTRIBUTES: Option[] = [
  { label: 'sysDescription', value: 'sysDescription' },
  { label: 'sysObjectId', value: 'sysObjectId' },
  { label: 'sysContact', value: 'sysContact' },
  { label: 'sysName', value: 'sysName' },
  { label: 'sysLocation', value: 'sysLocation' }
]

// SNMP interface attributes (the "Interface attribute" search)
export const INTERFACE_ATTRIBUTES: Option[] = [
  { label: 'ifAlias', value: 'ifAlias' },
  { label: 'ifName', value: 'ifName' },
  { label: 'ifDescr', value: 'ifDescr' }
]

export const MATCH_TYPES: Option[] = [
  { label: 'contains', value: 'contains' },
  { label: 'equals', value: 'equals' }
]

export const FLOW_OPTIONS: Option[] = [
  { label: 'Nodes with flow data', value: 'true' },
  { label: 'Nodes without flow data', value: 'false' }
]

// Asset "category" field values (distinct from surveillance categories)
export const ASSET_CATEGORIES: Option[] = [
  'Unspecified', 'Infrastructure', 'Server', 'Desktop', 'Laptop', 'Printer', 'Telephony', 'Other'
].map(c => ({ label: c, value: c }))
