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

import { ISelectItemType } from '@featherds/select'

export const DEFAULT_MONITORING_LOCATION = 'Default'

export const SCV_GET_ALL_ALIAS = '_all'

// SNMP Defaults
export const DEFAULT_SNMP_VERSION = 'v2c'
export const DEFAULT_SNMP_TIMEOUT = 3000
export const DEFAULT_SNMP_RETRIES = 1
export const DEFAULT_SNMP_PORT = 161
export const DEFAULT_SNMP_TTL = 1
export const DEFAULT_SNMP_MAX_REQUEST_SIZE = 65535
export const DEFAULT_SNMP_MAX_VARS_PER_PDU = 10
export const DEFAULT_SNMP_MAX_REPETITIONS = 2
export const DEFAULT_SNMP_READ_COMMUNITY_STRING = 'public'
export const DEFAULT_SNMP_WRITE_COMMUNITY_STRING = 'private'
export const DEFAULT_SNMP_V3_SECURITY_NAME = 'opennmsUser'
export const DEFAULT_SNMP_V3_SECURITY_LEVEL = 1
export const DEFAULT_SNMP_V3_SECURITY_LEVEL_STRING = 'noAuthNoPriv'
export const DEFAULT_SNMP_V3_AUTH_PASSPHRASE = '0p3nNMSv3'
export const DEFAULT_SNMP_V3_AUTH_PROTOCOL = 'MD5'
export const DEFAULT_SNMP_V3_PRIVACY_PASSPHRASE = '0p3nNMSv3'
export const DEFAULT_SNMP_V3_PRIVACY_PROTOCOL = 'DES'

// SNMP Data Collection Constants
const OID_TYPE_SINGLE = 'single'
const OID_TYPE_MASK = 'mask'

export const OID_TYPE_OPTIONS = [
  { name: 'Single', value: OID_TYPE_SINGLE },
  { name: 'Mask', value: OID_TYPE_MASK }
]

export const DEFAULT_OID_TYPE = OID_TYPE_SINGLE

export const STATUS_OPTIONS = [
  { name: 'Enabled', value: true },
  { name: 'Disabled', value: false }
]

export const DEFAULT_STATUS = true

export const KEY_PATTERN = /^[\w-]+$/

export const OID_PATTERN = /^\.?\d+(\.\d+)*$/

export const OID_MASK_PATTERN = /^\.?\d+(\.\d+)*\.?$/

const IF_TYPE_ALL = 'all'
const IF_TYPE_IGNORE = 'ignore'

export const DEFAULT_IF_TYPE_FILTER: ISelectItemType = { _text: 'Ignore', _value: IF_TYPE_IGNORE }

export const IF_TYPE_FILTERS_OPTIONS: ISelectItemType[] = [
  { _text: 'Ignore', _value: IF_TYPE_IGNORE },
  { _text: 'All', _value: IF_TYPE_ALL }
]

export const VALID_MIB_OBJ_TYPES = [
  'counter',
  'counter32',
  'counter64',
  'gauge',
  'gauge32',
  'gauge64',
  'integer',
  'integer32',
  'timeticks',
  'string',
  'octetstring',
  'opaque'
]

export const DEFAULT_MIB_OBJ_TYPE: ISelectItemType = { _text: 'gauge', _value: 'gauge' }

export const MIB_OBJECT_DATA_TYPE_OPTIONS: ISelectItemType[] = [
  ...VALID_MIB_OBJ_TYPES.map(type => ({ _text: type, _value: type }))
]

export const PERSISTENCE_SELECTOR_STRATEGY_OPTIONS: string[] = [
  'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy',
  'org.opennms.netmgt.collectd.PersistRegexSelectorStrategy'
]

export const STORAGE_STRATEGY_OPTIONS: string[] = [
  'org.opennms.netmgt.collection.support.IndexStorageStrategy',
  'org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy'
]

// Trapd Defaults
export const DEFAULT_TRAPD_PORT = 10162
export const DEFAULT_TRAPD_BIND_ADDRESS = '*'
export const DEFAULT_TRAPD_THREADS = 0
export const DEFAULT_TRAPD_QUEUE_SIZE = 10000
export const DEFAULT_TRAPD_BATCH_SIZE = 1000
export const DEFAULT_TRAPD_BATCH_INTERVAL = 500
export const DEFAULT_TRAPD_USE_ADDRESS_FROM_VARBIND = false
export const DEFAULT_TRAPD_INCLUDE_RAW_MESSAGE = false
export const DEFAULT_TRAPD_NEW_SUSPECT_ON_TRAP = false
