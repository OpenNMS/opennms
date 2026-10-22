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

import { rest } from './axiosInstances'

// Front-page RTC availability per category (legacy categories-box), from
// GET /rest/availability → { section: [{ name, categories: { category: [...] } }] }.
export interface AvailabilityCategory {
  name: string
  outageText: string
  availabilityText: string
  availability: number
  availabilityClass: string
  outageClass: string
}

export interface AvailabilitySection {
  name: string
  categories: AvailabilityCategory[]
}

// JAXB-style JSON collapses single-element arrays to objects; normalize.
const toArray = <T>(v: T | T[] | undefined | null): T[] => (v == null ? [] : Array.isArray(v) ? v : [v])

// null distinguishes a data-source error (RTC disconnected / fetch failed) from a
// genuinely empty set ([]) so the panel can show "Waiting for availability data"
// like the legacy categories box, instead of a bare "No availability data".
export const getAvailability = async (): Promise<AvailabilitySection[] | null> => {
  try {
    const resp = await rest.get('/availability', { headers: { Accept: 'application/json' }})
    if (resp.status === 204 || !resp.data?.section) {
      return []
    }
    return toArray<any>(resp.data.section).map(s => ({
      name: s.name,
      categories: toArray<any>(s.categories?.category).map(c => ({
        name: c.name,
        outageText: c['outage-text'] ?? '',
        availabilityText: c['availability-text'] ?? '',
        availability: Number(c.availability ?? 0),
        availabilityClass: c['availability-class'] ?? 'Normal',
        outageClass: c['outage-class'] ?? 'Normal'
      }))
    }))
  } catch {
    return null
  }
}
