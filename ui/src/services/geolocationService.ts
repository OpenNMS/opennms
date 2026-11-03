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

import { v2 } from './axiosInstances'
import { GeolocationConfigItem } from '@/types'

const geolocationEndpoint = 'geolocation'
const geolocationConfigEndpoint = 'config'

/**
 * The tile server is an install-wide setting (opennms.properties), so the answer
 * is shared rather than refetched per caller. Callers that mount repeatedly --
 * the topology inspector's location map, once per node selection -- would
 * otherwise issue a request every time.
 *
 * A failure is not cached, so a briefly unavailable server is retried.
 */
let configPromise: Promise<GeolocationConfigItem | false> | null = null

const getGeolocationConfig = async (): Promise<GeolocationConfigItem | false> => {
  if (configPromise) {
    return configPromise
  }
  const endpoint = `${geolocationEndpoint}/${geolocationConfigEndpoint}`

  configPromise = v2.get(endpoint)
    .then(resp => resp.data as GeolocationConfigItem)
    .catch(() => {
      configPromise = null
      return false as const
    })
  return configPromise
}

export {
  getGeolocationConfig
}
