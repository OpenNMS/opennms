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
import type { ChartData, ChartSummary } from '@/types/charts'

// null on failure, as opposed to an empty list, so panels can tell a fetch
// failure from a configuration without charts
export const getCharts = async (): Promise<ChartSummary[] | null> => {
  try {
    const resp = await v2.get('/charts')
    return resp.status === 204 || !Array.isArray(resp.data) ? [] : (resp.data as ChartSummary[])
  } catch (_err) {
    return null
  }
}

export const getChart = async (name: string): Promise<ChartData | null> => {
  try {
    const resp = await v2.get(`/charts/${encodeURIComponent(name)}`)
    return resp.data as ChartData
  } catch (_err) {
    return null
  }
}
