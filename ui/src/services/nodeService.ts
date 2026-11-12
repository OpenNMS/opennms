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
  Node,
  NodeApiResponse,
  NodeFilterRuleResult,
  SnmpInterfaceApiResponse,
  QueryParameters,
  IpInterfaceApiResponse,
  NodeAlarmStatus,
  NodeAvailability,
  NodeCriticalPath
} from '@/types'
import { queryParametersHandler } from './serviceHelpers'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'

const endpoint = '/nodes'

/**
 * The nodes matching an OpenNMS filter rule (`catincRouters & location='Default'`),
 * via the v1 endpoint: the filter engine lives there, not in v2.
 *
 * One request, bounded. A rule can match an entire estate, and the v1 endpoint
 * serialises full nodes with every join, so it is asked for at most `max + 1`
 * nodes; the endpoint counts the whole match regardless of the limit, and that
 * count says whether the rule fits. One node past the limit is enough to know
 * without a second round trip through the filter engine.
 *
 * The one failure worth telling apart is a rule the engine cannot parse, which the
 * server reports as 400 with a fixed message; everything else is `failed`. That
 * includes the server failing on a match too large to bind as an `IN` list, which
 * no request from here can avoid: the caller words that case accordingly.
 */
const getNodesByFilterRule = async (rule: string, max: number): Promise<NodeFilterRuleResult> => {
  try {
    const resp = await rest.get(endpoint, {
      params: { filterRule: rule, limit: max + 1, orderBy: 'label', order: 'asc' }
    })

    if (resp.status === 204 || !resp.data) {
      return { nodes: [] }
    }

    const data = resp.data as NodeApiResponse
    const nodes = data.node ?? []
    const total = Math.max(data.totalCount ?? 0, nodes.length)

    if (total > max) {
      return { error: 'too-many', count: total }
    }

    return { nodes }
  } catch (err) {
    const status = (err as { response?: { status?: number }})?.response?.status
    return { error: status === 400 ? 'invalid' : 'failed' }
  }
}

const getNodes = async (queryParameters?: QueryParameters): Promise<NodeApiResponse | false> => {
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
  }

  try {
    const resp = await v2.get(endpointWithQueryString || endpoint)

    // no content from server
    if (resp.status === 204) {
      return { node: [], totalCount: 0, count: 0, offset: 0 }
    }

    return resp.data
  } catch (_err) {
    return false
  }
}

const getNodeById = async (id: string): Promise<any> => {
  try {
    const resp = await v2.get(`${endpoint}/${id}`)
    return resp.data
  } catch (_err) {
    return false
  }
}

// Looks a node up by database id. v2 /nodes/{id} also resolves foreignSource:foreignId
// (NodeDao.get(String)), but a foreign id holding '/' cannot travel in the path: the server
// decodes %2F into a separator and answers 404. Callers that may hold a foreign id should use
// resolveNodeId. A 404 is an answer -- no such node -- and is told apart from a failed lookup.
const lookupNodeById = async (idOrForeignId: string): Promise<ValidationResultWithPayload<Node | null>> => {
  try {
    const resp = await v2.get(`${endpoint}/${encodeURIComponent(idOrForeignId)}`)
    return createResultWithPayload<Node | null>(true, '', resp.data ?? null)
  } catch (err) {
    if ((err as { response?: { status?: number }})?.response?.status === 404) {
      return createResultWithPayload<Node | null>(true, '', null)
    }
    return createResultWithPayload<Node | null>(false, 'Failed to look up the node.')
  }
}

const isNullFilterValue = (value: string) => ['null', 'notnull'].includes(value.toLowerCase())

/**
 * Turns a database id or foreignSource:foreignId into the database id; null when no such node.
 * A foreign id is resolved through query parameters on the v1 API, split at the first ':' only,
 * because a path segment cannot carry an encoded '/'. axios encodes the parameters, so '/', ';',
 * ',', '#' and spaces are safe, and v1 matches them exactly.
 */
const resolveNodeId = async (idOrForeignId: string): Promise<ValidationResultWithPayload<string | null>> => {
  const colon = idOrForeignId.indexOf(':')

  if (colon < 0) {
    const result = await lookupNodeById(idOrForeignId)
    if (!result.success) {
      return createResultWithPayload<string | null>(false, result.message)
    }
    const id = result.payload?.id
    return createResultWithPayload<string | null>(true, '', id === undefined || id === null ? null : String(id))
  }

  const foreignSource = idOrForeignId.substring(0, colon)
  const foreignId = idOrForeignId.substring(colon + 1)
  const matches = (node: { foreignSource?: string, foreignId?: string } | undefined | null) => {
    return node?.foreignSource === foreignSource && node?.foreignId === foreignId
  }
  const toId = (node: { id?: string | number } | undefined | null) => {
    const id = node?.id
    return id === undefined || id === null ? null : String(id)
  }

  // v1 reads a value of null or notnull (any case) as an IS NULL or IS NOT NULL filter, not as the
  // literal text. v2 matches the literal value, so use it for those. A foreign source holding '/'
  // together with such a foreign id is out of scope: a path segment cannot carry the '/'.
  if (isNullFilterValue(foreignSource) || isNullFilterValue(foreignId)) {
    const result = await lookupNodeById(idOrForeignId)
    if (!result.success) {
      return createResultWithPayload<string | null>(false, result.message)
    }
    return createResultWithPayload<string | null>(true, '', matches(result.payload) ? toId(result.payload) : null)
  }

  try {
    const resp = await rest.get(endpoint, { params: { foreignSource, foreignId, limit: 1 }})
    const nodes = resp?.status === 204 ? undefined : resp?.data?.node
    const first = Array.isArray(nodes) ? nodes[0] : nodes
    return createResultWithPayload<string | null>(true, '', matches(first) ? toId(first) : null)
  } catch (_err) {
    return createResultWithPayload<string | null>(false, 'Failed to look up the node.')
  }
}

const getNodeSnmpInterfaces = async (
  id: string,
  queryParameters?: QueryParameters
): Promise<SnmpInterfaceApiResponse | false> => {
  const snmpInterfaceEndpoint = `${endpoint}/${id}/snmpinterfaces`
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, snmpInterfaceEndpoint)
  }

  try {
    const resp = await v2.get(`${endpointWithQueryString || snmpInterfaceEndpoint}`)

    // no content from server
    if (resp.status === 204) {
      return { snmpInterface: [], totalCount: 0, count: 0, offset: 0 }
    }

    return resp.data
  } catch (_err) {
    return false
  }
}

const getNodeIpInterfaces = async (
  id: string,
  queryParameters?: QueryParameters
): Promise<IpInterfaceApiResponse | false> => {
  const ipInterfaceEndpoint = `${endpoint}/${id}/ipinterfaces`
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, ipInterfaceEndpoint)
  }

  try {
    const resp = await v2.get(`${endpointWithQueryString || ipInterfaceEndpoint}`)

    // no content from server
    if (resp.status === 204) {
      return { ipInterface: [], totalCount: 0, count: 0, offset: 0 }
    }

    return resp.data
  } catch (_err) {
    return false
  }
}

/**
 * Node availability by interface and service. With `startMs`/`endMs` the figures cover that window;
 * without them they cover the last 24 hours, which is the legacy behaviour and still what every
 * caller but the availability panel wants.
 *
 * `page` asks for one page of the node's interfaces (in address order, as the server sorts them):
 * only that page's figures are computed, each being a query of its own. `ipinterfaceCount` in the
 * answer is the node's total, for a paginator. `withServices` counts and pages only the interfaces
 * with monitored services.
 */
const getNodeAvailabilityPercentage = async (
  id: string,
  startMs?: number,
  endMs?: number,
  page?: { limit: number, offset: number, withServices?: boolean }
): Promise<NodeAvailability | false> => {
  try {
    const params: Record<string, number> = {}

    if (startMs !== undefined && endMs !== undefined) {
      params.start = startMs
      params.end = endMs
    }

    if (page) {
      params.limit = page.limit
      params.offset = page.offset
    }

    const query: Record<string, number | boolean> = page?.withServices ? { ...params, withServices: true } : params

    const resp: { data: NodeAvailability } = await rest.get(`/availability/nodes/${id}`, Object.keys(query).length ? { params: query } : undefined)

    return resp.data
  } catch (_err) {
    return false
  }
}

/**
 * The node's critical path, for the Path Outage panel. The payload is null when the node has none
 * of its own (204); a failed request is a failed result, so the two can be told apart.
 */
const getNodeCriticalPath = async (id: string): Promise<ValidationResultWithPayload<NodeCriticalPath | null>> => {
  try {
    const resp = await v2.get(`/nodes/${id}/criticalPath`)

    return createResultWithPayload(true, '', resp.status === 204 || !resp.data ? null : resp.data as NodeCriticalPath)
  } catch (_err) {
    return createResultWithPayload<NodeCriticalPath | null>(false, `Unable to load the critical path for node ${id}`)
  }
}

/**
 * A summary of the node's problem alarms for the status banner, counted on the server, so it stays
 * small however many alarms the node has.
 */
const getNodeAlarmStatus = async (id: string): Promise<ValidationResultWithPayload<NodeAlarmStatus>> => {
  try {
    const resp = await v2.get(`/nodes/${id}/alarmStatus`)

    return createResultWithPayload(true, '', resp.data as NodeAlarmStatus)
  } catch (_err) {
    return createResultWithPayload<NodeAlarmStatus>(false, `Unable to load the alarm status for node ${id}`)
  }
}

export {
  getNodes,
  getNodesByFilterRule,
  getNodeAlarmStatus,
  getNodeCriticalPath,
  getNodeById,
  lookupNodeById,
  resolveNodeId,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage
}
