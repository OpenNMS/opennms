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

// Minions report VersionBean.toString() ("v37.0.0-SNAPSHOT"); /rest/info reports
// "37.0.0" in version and "37.0.0-SNAPSHOT" in displayVersion. Only the
// major.minor.patch triple is compared, so neither the "v" nor the qualifier
// counts as a difference.
const VERSION_TRIPLE = /^v?(\d+\.\d+\.\d+)(?![\d.])/

export const normalizeVersion = (value: unknown): string | null => {
  if (typeof value !== 'string') {
    return null
  }
  const match = VERSION_TRIPLE.exec(value.trim())
  return match ? match[1] : null
}

// false when either side cannot be normalised, so nothing is flagged on a guess
export const sameVersion = (a: unknown, b: unknown): boolean => {
  const left = normalizeVersion(a)
  const right = normalizeVersion(b)
  return left !== null && right !== null && left === right
}
