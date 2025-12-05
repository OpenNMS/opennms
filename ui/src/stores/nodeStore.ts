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

import { defineStore } from 'pinia'
import API from '@/services'
import { IpInterface, Node, NodeAvailability, Outage, QueryParameters, SnmpInterface } from '@/types'
import { getNodeIpInterfaceQuery } from '@/services/ipInterfaceService'

export const useNodeStore = defineStore('nodeStore', () => {
  const nodes = ref([] as Node[])
  const totalCount = ref(0)
  const node = ref({} as Node)
  const snmpInterfaces = ref([] as SnmpInterface[])
  const snmpInterfacesTotalCount = ref(0)
  const ipInterfaces = ref([] as IpInterface[])
  const ipInterfacesTotalCount = ref(0)
  const availability = ref({} as NodeAvailability)
  const outages = ref([] as Outage[])
  const outagesTotalCount = ref(0)
  const nodeQueryParameters = ref({ limit: 20, offset: 0, orderBy: 'label' } as QueryParameters)

  // map of nodeId to IpInterfaces associated with that node
  const nodeToIpInterfaceMap = ref<Map<string, IpInterface[]>>(new Map<string, IpInterface[]>())

  const getNodes = async (queryParameters?: QueryParameters, includeIpInterfaces?: boolean) => {
    const resp = await API.getNodes(queryParameters)

    if (resp) {
      totalCount.value = resp.totalCount
      nodes.value = resp.node

      if (includeIpInterfaces === true) {
        const nodeIds = resp.node.map(n => n.id)
        getIpInterfacesForNodes(nodeIds, false)
      }
    }
  }

  const getNodeById = async (n: Node) => {
    const resp = await API.getNodeById(n.id)

    if (resp) {
      node.value = resp
    }
  }

  const getNodeSnmpInterfaces = async (payload: { id: string; queryParameters?: QueryParameters }) => {
    const resp = await API.getNodeSnmpInterfaces(payload.id, payload.queryParameters)

    if (resp) {
      snmpInterfaces.value = resp.snmpInterface
      snmpInterfacesTotalCount.value = resp.totalCount
    }
  }

  const getNodeIpInterfaces = async (payload: { id: string; queryParameters?: QueryParameters }) => {
    const resp = await API.getNodeIpInterfaces(payload.id, payload.queryParameters)

    if (resp) {
      ipInterfaces.value = resp.ipInterface
      ipInterfacesTotalCount.value = resp.totalCount
    }
  }

  /**
   * Get the IpInterfaces for the given nodes, then update the nodeToIpInterfaceMap.
   */
  const getIpInterfacesForNodes = async (nodeIds: string[], managedOnly: boolean) => {
    if (nodeIds.length === 0) {
      return
    }

    const query = getNodeIpInterfaceQuery(nodeIds, managedOnly)
    const queryParameters = {
      limit: 0,
      _s: query
    } as QueryParameters

    const resp = await API.getIpInterfaces(queryParameters)

    if (resp) {
      // find updated list of IpInterfaces for each node and update the node => ip map
      for (const id of nodeIds) {
        const ipsThisNode = resp.ipInterface.filter(ip => ip.nodeId.toString() === id)
        nodeToIpInterfaceMap.value.set(id, ipsThisNode)
      }
    }
  }

  const getNodeAvailabilityPercentage = async (id: string) => {
    const av = await API.getNodeAvailabilityPercentage(id)

    if (av) {
      availability.value = av
    }
  }

  const getNodeOutages = async (payload: { id: string; queryParameters?: QueryParameters }) => {
    const resp = await API.getNodeOutages(payload.id, payload.queryParameters)

    if (resp) {
      outages.value = resp.outage
      outagesTotalCount.value = resp.totalCount
    }
  }

  const setNodeQueryParameters = async (params: QueryParameters) => {
    nodeQueryParameters.value = {
      ...params
    }
  }

  return {
    nodes,
    totalCount,
    node,
    snmpInterfaces,
    snmpInterfacesTotalCount,
    ipInterfaces,
    ipInterfacesTotalCount,
    availability,
    nodeToIpInterfaceMap,
    nodeQueryParameters,
    outages,
    outagesTotalCount,
    getIpInterfacesForNodes,
    getNodes,
    getNodeById,
    getNodeSnmpInterfaces,
    getNodeIpInterfaces,
    getNodeAvailabilityPercentage,
    getNodeOutages,
    setNodeQueryParameters
  }
})
