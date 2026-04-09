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
