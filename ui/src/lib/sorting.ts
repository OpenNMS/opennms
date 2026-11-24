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

import { SORT } from '@/types'
import { ISortObject } from '@/types'

// Sort predicate function to use when both values are strings or can be cast to strings
export const sortPredicate = (a: any, b: any, currentSort: ISortObject) => {
  const valueA = String(a[currentSort.property] ?? '')
  const valueB = String(b[currentSort.property] ?? '')

  let compareVal = valueA.localeCompare(valueB)

  if (currentSort.value === SORT.DESCENDING && compareVal !== 0) {
    compareVal = -compareVal
  }

  return compareVal
}

const isMissing = (value: unknown) => value === null || value === undefined || (typeof value === 'number' && Number.isNaN(value))

/**
 * Ascending comparator for `Array.prototype.sort`: numbers numerically, strings by code unit (so
 * uppercase sorts before lowercase), and null, undefined and NaN after everything else. Matches the
 * ordering lodash's `orderBy`/`sortBy` used, which these replaced.
 */
export const compareValues = (a: unknown, b: unknown): number => {
  const aMissing = isMissing(a)
  const bMissing = isMissing(b)

  if (aMissing || bMissing) {
    return aMissing === bMissing ? 0 : (aMissing ? 1 : -1)
  }

  return (a as any) < (b as any) ? -1 : ((a as any) > (b as any) ? 1 : 0)
}

/**
 * Comparator that orders objects by one property with `compareValues`, descending when `direction`
 * is `SORT.DESCENDING` ('desc') and ascending otherwise. Descending reverses the whole order, so
 * missing values come first.
 *
 * `[...items].sort(compareBy('name'))`
 */
export const compareBy = <T>(property: string, direction: SORT | string = SORT.ASCENDING) => (a: T, b: T): number => {
  const result = compareValues((a as Record<string, unknown>)[property], (b as Record<string, unknown>)[property])

  return direction === SORT.DESCENDING && result !== 0 ? -result : result
}
