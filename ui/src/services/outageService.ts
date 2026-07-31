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

// OnmsOutage exposes flat nodeId/nodeLabel/ipAddress/locationName; the service
// name is nested under monitoredService.serviceType.name.
export interface CurrentOutage {
  id?: number
  nodeId?: number
  nodeLabel?: string
  ipAddress?: string
  locationName?: string
  ifLostService?: number | string
  serviceName?: string
  monitoredService?: { serviceName?: string; serviceType?: { name?: string } }
}

// Currently-open outages: ifRegainedService is the epoch sentinel (== null).
const OPEN_FIQL = 'outage.ifRegainedService==1970-01-01T00:00:00.000-0000'

export const getCurrentOutages = async (limit = 12, extraFiql: string[] = []): Promise<CurrentOutage[]> => {
  try {
    const fiql = encodeURIComponent([OPEN_FIQL, ...extraFiql].join(';'))
    const resp = await v2.get(`/outages?_s=${fiql}&limit=${limit}`)
    if (resp.status === 204) {
      return []
    }
    return (resp.data?.outage ?? []) as CurrentOutage[]
  } catch (err) {
    return []
  }
}

export const outageServiceName = (o: CurrentOutage): string =>
  o.serviceName ?? o.monitoredService?.serviceName ?? o.monitoredService?.serviceType?.name ?? 'service'
