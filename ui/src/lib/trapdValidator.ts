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

import { SecurityLevel, AuthProtocol, PrivacyProtocol } from '@/types/trapConfig'
import { ISelectItemType } from '@featherds/select'

export const MIN_PORT = 1
export const MAX_PORT = 65535

const VALID_SECURITY_LEVELS = [SecurityLevel.NoAuthNoPriv, SecurityLevel.AuthNoPriv, SecurityLevel.AuthPriv]

export const SECURITY_LEVEL_OPTIONS: ISelectItemType[] = [
  { _text: 'No Auth (1)', _value: String(SecurityLevel.NoAuthNoPriv) },
  { _text: 'Auth Only (2)', _value: String(SecurityLevel.AuthNoPriv) },
  { _text: 'Auth and Privacy (3)', _value: String(SecurityLevel.AuthPriv) }
]

export const AuthProtocols = [
  AuthProtocol.MD5,
  AuthProtocol.SHA,
  AuthProtocol.SHA224,
  AuthProtocol.SHA256,
  AuthProtocol.SHA512
]

export const PrivacyProtocols = [
  PrivacyProtocol.DES,
  PrivacyProtocol.AES,
  PrivacyProtocol.AES192,
  PrivacyProtocol.AES256
]

export const isValidSnmpSecurityLevel = (level: number | undefined): boolean => {
  return level !== undefined && VALID_SECURITY_LEVELS.includes(level)
}

export const isValidIP = (ip: string): boolean => {
  const parts = ip.split('.')
  if (parts.length !== 4) return false
  return parts.every((part) => {
    const num = parseInt(part, 10)
    return !isNaN(num) && num >= 0 && num <= 255
  })
}

export const isValidPort = (port: number | undefined): boolean => {
  return port !== undefined && !isNaN(port) && port >= MIN_PORT && port <= MAX_PORT
}

export const AUTH_PROTOCOL_OPTIONS: ISelectItemType[] = AuthProtocols.map((protocol) => ({
  _text: protocol,
  _value: protocol
}))

export const PRIVACY_PROTOCOL_OPTIONS: ISelectItemType[] = PrivacyProtocols.map((protocol) => ({
  _text: protocol,
  _value: protocol
}))

export const isEqual = (obj1: any, obj2: any): boolean => {
  return JSON.stringify(obj1) === JSON.stringify(obj2)
}
