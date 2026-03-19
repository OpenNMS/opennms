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

import { SnmpSecurityLevel } from '@/types/trapConfig'

const VALID_SECURITY_LEVELS = [SnmpSecurityLevel.NoAuthNoPriv, SnmpSecurityLevel.AuthNoPriv, SnmpSecurityLevel.AuthPriv]
const MIN_PORT = 1
const MAX_PORT = 65535

export const SecurityLevelSelectionOptions = [
  { _text: 'No Auth (1)', _value: String(SnmpSecurityLevel.NoAuthNoPriv) },
  { _text: 'Auth Only (2)', _value: String(SnmpSecurityLevel.AuthNoPriv) },
  { _text: 'Auth and Privacy (3)', _value: String(SnmpSecurityLevel.AuthPriv) }
]

export const SnmpAuthProtocols = ['MD5', 'SHA', 'SHA-224', 'SHA-256', 'SHA-512']

export const SnmpPrivacyProtocols = ['DES', 'AES', 'AES192', 'AES256']

export const isValidSnmpSecurityLevel = (level: number | undefined): boolean => {
  return level !== undefined && VALID_SECURITY_LEVELS.includes(level)
}
