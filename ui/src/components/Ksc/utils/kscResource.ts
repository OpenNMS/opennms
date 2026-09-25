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

// Legacy KSC configs may store the resource id percent-encoded, occasionally
// twice (NMS-10309). Decode while the value still looks encoded, matching
// DefaultKscReportService.getResourceIdForGraph, and guard malformed sequences
// so a bad value falls back to the raw string rather than throwing.
export const decodeResourceId = (raw?: string | null): string => {
  if (!raw) {
    return ''
  }
  let value = raw
  for (let i = 0; i < 2 && /%[0-9a-fA-F]{2}/.test(value); i++) {
    try {
      const decoded = decodeURIComponent(value)
      if (decoded === value) {
        break
      }
      value = decoded
    } catch {
      break
    }
  }
  return value
}
