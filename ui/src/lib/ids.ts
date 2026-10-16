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

// A database id typed or linked as text: digits only (surrounding whitespace allowed), greater than 0,
// and small enough to be exact in a JS number. Returns the canonical form ('007' -> '7'), or null.
export const parsePositiveIntegerId = (raw: string | null | undefined): string | null => {
  if (typeof raw !== 'string') {
    return null
  }

  const value = raw.trim()
  if (!/^\d+$/.test(value)) {
    return null
  }

  const id = Number(value)
  return id > 0 && Number.isSafeInteger(id) ? String(id) : null
}
