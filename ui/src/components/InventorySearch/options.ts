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

// Asset field columns (label -> asset property key), from AssetModel.getColumns()
export const ASSET_FIELDS: Option[] = [
  { label: 'Address 1', value: 'address1' },
  { label: 'Address 2', value: 'address2' },
  { label: 'Asset Number', value: 'assetNumber' },
  { label: 'Building', value: 'building' },
  { label: 'Circuit ID', value: 'circuitId' },
  { label: 'City', value: 'city' },
  { label: 'Comments', value: 'comment' },
  { label: 'Date Installed', value: 'dateInstalled' },
  { label: 'Department', value: 'department' },
  { label: 'Description', value: 'description' },
  { label: 'Display Category', value: 'displayCategory' },
  { label: 'Division', value: 'division' },
  { label: 'Floor', value: 'floor' },
  { label: 'Lease', value: 'lease' },
  { label: 'Lease Expires', value: 'leaseExpires' },
  { label: 'Maint Contract', value: 'maintcontract' },
  { label: 'Maint Contract Expires', value: 'maintContractExpiration' },
  { label: 'Maint Phone', value: 'supportPhone' },
  { label: 'Manufacturer', value: 'manufacturer' },
  { label: 'Model Number', value: 'modelNumber' },
  { label: 'Notification Category', value: 'notifyCategory' },
  { label: 'Operating System', value: 'operatingSystem' },
  { label: 'Poller Category', value: 'pollerCategory' },
  { label: 'Rack', value: 'rack' },
  { label: 'Region', value: 'region' },
  { label: 'Room', value: 'room' },
  { label: 'Serial Number', value: 'serialNumber' },
  { label: 'Slot', value: 'slot' },
  { label: 'State', value: 'state' },
  { label: 'Threshold Category', value: 'thresholdCategory' },
  { label: 'Vendor', value: 'vendor' },
  { label: 'Vendor Asset Number', value: 'vendorAssetNumber' },
  { label: 'ZIP Code', value: 'zip' }
]
