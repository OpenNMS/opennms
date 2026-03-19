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

import { SnmpBaseConfiguration, SnmpFieldInfo } from '@/types/snmpConfig'

/**
 * Augments each field's hint with the current default value, unless the field
 * has `skipDefaultHint: true` (used for sensitive fields such as passphrases)
 * or the default value is absent/empty.
 */
export const withDefaultHints = (fields: SnmpFieldInfo[], defaults: SnmpBaseConfiguration): SnmpFieldInfo[] => {
  const defaultsMap = defaults as Record<string, unknown>
  return fields.map(field => {
    const value = defaultsMap[field.key]
    if (value === undefined || value === null || value === '' || field.skipDefaultHint) {
      return field
    }
    return {
      ...field,
      hint: field.hint ? `${field.hint}. Current default value is: '${value}'.` : `Current default value is: '${value}'.`
    }
  })
}
