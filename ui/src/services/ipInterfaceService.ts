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
import {
  IpInterfaceApiResponse,
  QueryParameters
} from '@/types'
import { queryParametersHandler } from './serviceHelpers'

const endpoint = '/ipinterfaces'

export const getIpInterfaces = async (queryParameters?: QueryParameters): Promise<IpInterfaceApiResponse | false> => {
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
  }

  try {
    const resp = await v2.get(endpointWithQueryString || endpoint)
    return resp.data as IpInterfaceApiResponse
  } catch (err) {
    return false
  }
}

/**
 * Construct the '_s' part of the getIpInterfaces query string with the given node ids and whether
 * to return only managed interfaces or all.
 * Use this in QueryParameters passed to getIpInterfaces.
 */
export const getNodeIpInterfaceQuery = (nodeIds: string[], managedOnly: boolean) => {
  const ids = nodeIds.map(id => `node.id==${id}`).join(',')

  const managedQuery = managedOnly ? ';isManaged==M' : ''

  return `(${ids}${managedQuery})`
}
