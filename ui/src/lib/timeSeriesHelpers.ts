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

import { RRA } from '@/types/timeSeries'

export const rraToString = (rra: RRA): string => {
  return `RRA:${rra.cf}:${rra.xff}:${rra.steps}:${rra.rows}`
}

export const rraFromString = (rraStr: string): RRA => {
  const parts = rraStr.split(':')

  if (parts.length !== 5 || parts[0] !== 'RRA') {
    throw new Error(`Invalid RRA string: ${rraStr}`)
  }

  const xff = parseFloat(parts[2])
  const steps = parseInt(parts[3], 10)
  const rows = parseInt(parts[4], 10)

  // Validate all numeric values
  if (isNaN(xff) || !Number.isFinite(xff) || xff < 0 || xff > 1 ||
      isNaN(steps) || !Number.isFinite(steps) || steps <= 0 ||
      isNaN(rows) || !Number.isFinite(rows) || rows <= 0) {
    throw new Error(`Invalid RRA string: ${rraStr}`)
  }

  return {
    cf: parts[1] as any,
    xff,
    steps,
    rows
  } as RRA
}
