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

import { rest, v2 } from './axiosInstances'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { QueryParameters, IfServiceApiResponse, NodeLinkService } from '@/types'
import { queryParametersHandler } from './serviceHelpers'

const endpoint = '/ifservices'

const getNodeIfServices = async (queryParameters?: QueryParameters): Promise<IfServiceApiResponse | false> => {
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
  }

  try {
    const resp = await rest.get(endpointWithQueryString || endpoint)

    if (resp.status === 204) {
      return { 'monitored-service': [], totalCount: 0, count: 0, offset: 0 }
    }

    return resp.data
  } catch (_err) {
    return false
  }
}

/**
 * The node's monitored services of the given types, with the address each runs on -- for the
 * Node Details Services menu. One v2 query for all of them.
 */
const getNodeServicesByName = async (nodeId: string, serviceNames: string[]): Promise<ValidationResultWithPayload<NodeLinkService[]>> => {
  const names = serviceNames.map(name => `serviceType.name==${name}`).join(',')

  try {
    const resp = await v2.get(`${endpoint}?limit=0&_s=node.id==${nodeId};(${names})`)

    if (resp.status === 204 || !resp.data) {
      return createResultWithPayload(true, '', [])
    }

    const services: NodeLinkService[] = (resp.data.service ?? [])
      .map((s: any) => ({ serviceName: s.serviceType?.name, ipAddress: s.ipAddress }))
      .filter((s: NodeLinkService) => s.serviceName && s.ipAddress)

    return createResultWithPayload(true, '', services)
  } catch (_err) {
    return createResultWithPayload<NodeLinkService[]>(false, `Unable to load the services on node ${nodeId}`)
  }
}

export { getNodeIfServices, getNodeServicesByName }
