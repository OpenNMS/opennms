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

import { v2, rest } from './axiosInstances'
import {
  QueryParameters,
  GraphNodesApiResponse,
  ResourceDefinitionsApiResponse,
  PreFabGraph,
  GraphMetricsPayload,
  GraphMetricsResponse
} from '@/types'
import { queryParametersHandler } from './serviceHelpers'

const endpoint = '/graphs/nodes/nodes'

const getGraphNodesNodes = async (queryParameters?: QueryParameters): Promise<GraphNodesApiResponse | false> => {
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
  }

  try {
    const resp = await v2.get(endpointWithQueryString || endpoint)

    // no content from server
    if (resp.status === 204) {
      return { vertices: [], edges: [] }
    }

    return resp.data
  } catch (err) {
    return false
  }
}

const getGraphDefinitionsByResourceId = async (id: string): Promise<ResourceDefinitionsApiResponse> => {
  try {
    const resp = await rest.get(`/graphs/for/${id}`)
    return resp.data
  } catch (err) {
    return (<unknown>{ name: [] }) as ResourceDefinitionsApiResponse
  }
}

const getPreFabGraphs = async (node: string): Promise<PreFabGraph[]> => {
  try {
    const resp = await rest.get(`/graphs/fornode/${node}`)
    return resp.data['prefab-graphs']['prefab-graph']
  } catch (err) {
    return []
  }
}

const getDefinitionData = async (definition: string): Promise<PreFabGraph | null> => {
  try {
    const resp = await rest.get(`/graphs/${definition}`)
    return resp.data
  } catch (err) {
    return null
  }
}

const getGraphMetrics = async (payload: GraphMetricsPayload): Promise<GraphMetricsResponse | null> => {
  try {
    const resp = await rest.post('/measurements', payload)
    return resp.data
  } catch (err) {
    return null
  }
}

export { getGraphNodesNodes, getGraphDefinitionsByResourceId, getDefinitionData, getGraphMetrics, getPreFabGraphs }
