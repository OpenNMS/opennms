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
  QueryParameters,
  SnmpInterfaceApiResponse
} from '@/types'
import { queryParametersHandler } from './serviceHelpers'

const endpoint = '/snmpinterfaces'

export const getSnmpInterfaces = async (queryParameters?: QueryParameters): Promise<SnmpInterfaceApiResponse | false> => {
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
  }

  try {
    const resp = await v2.get(endpointWithQueryString || endpoint)
    return resp.data as SnmpInterfaceApiResponse
  } catch (_err) {
    return false
  }
}

/**
 * Construct the '_s' part of the getSnmpInterfaces query string with the given node ids and an
 * optional narrowing FIQL expression (e.g. 'physAddr==*aabb*').
 *
 * The node-id OR-group and the narrowing expression are each wrapped in their own parens and
 * joined with ';' (AND): '(nodeIdOrList);(narrowing)'. This is intentional and NOT the same
 * shape as getNodeIpInterfaceQuery's single-group form: FIQL ';' (AND) binds tighter than ','
 * (OR), so folding narrowing into the same group as the node-id OR-list would incorrectly bind
 * it to only the last node id (e.g. 'node.id==1,node.id==2;physAddr==*aa*' means
 * 'node.id==1 OR (node.id==2 AND physAddr==*aa*)'). Two separate parenthesized groups ensure the
 * narrowing term applies to the whole OR-group.
 * Use this in QueryParameters passed to getSnmpInterfaces.
 */
export const getNodeSnmpInterfaceQuery = (nodeIds: string[], narrowing?: string) => {
  const ids = nodeIds.map(id => `node.id==${id}`).join(',')
  const nodeIdGroup = `(${ids})`

  if (narrowing) {
    return `${nodeIdGroup};(${narrowing})`
  }

  return nodeIdGroup
}
