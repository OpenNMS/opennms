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
import { MonitoringLocationApiResponse } from '@/types'

const endpoint = '/monitoringLocations'

export const getMonitoringLocations = async (): Promise<MonitoringLocationApiResponse| false> => {
  try {
    const resp = await v2.get(endpoint)

    const data = resp.data as MonitoringLocationApiResponse

    // map these to typed fields for clarity in calling code
    data.location.forEach(loc => {
      loc.name = loc['location-name']
      loc.area = loc['monitoring-area']
    })

    return data
  } catch (err) {
    return false
  }
}
