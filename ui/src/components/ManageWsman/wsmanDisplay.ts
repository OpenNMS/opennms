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

import { WsmanAgentSettings } from '@/types/wsmanAdmin'

// Human labels for the agent settings, in display order. A null value means
// "not set here": the built-in default for the root, or inherited from the
// defaults for a definition.
export const SETTING_ROWS: { key: keyof WsmanAgentSettings, label: string }[] = [
  { key: 'username', label: 'Username' },
  { key: 'hasPassword', label: 'Password' },
  { key: 'ssl', label: 'Use SSL' },
  { key: 'strictSsl', label: 'Strict SSL' },
  { key: 'port', label: 'Port' },
  { key: 'path', label: 'Path' },
  { key: 'timeout', label: 'Timeout (ms)' },
  { key: 'retry', label: 'Retries' },
  { key: 'maxElements', label: 'Max elements' },
  { key: 'gssAuth', label: 'GSS authentication' },
  { key: 'productVendor', label: 'Product vendor' },
  { key: 'productVersion', label: 'Product version' }
]

export const NOT_SET = '—'

export const formatSetting = (settings: WsmanAgentSettings, key: keyof WsmanAgentSettings): string => {
  if (key === 'hasPassword') {
    return settings.hasPassword ? 'Set' : 'Not set'
  }
  const value = settings[key]
  if (value === null || value === undefined || value === '') {
    return NOT_SET
  }
  if (typeof value === 'boolean') {
    return value ? 'Yes' : 'No'
  }
  return String(value)
}
