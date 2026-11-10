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
import { getNodeSnmpInterfaceQuery } from '@/services/snmpInterfaceService'
import { ref } from 'vue'

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
  const nodeQueryParameters = ref({ limit: 50, offset: 0, orderBy: 'label' } as QueryParameters)

  // map of nodeId to IpInterfaces associated with that node
  const nodeToIpInterfaceMap = ref<Map<string, IpInterface[]>>(new Map<string, IpInterface[]>())

  // map of nodeId to SnmpInterfaces associated with that node
  const nodeToSnmpInterfaceMap = ref<Map<string, SnmpInterface[]>>(new Map<string, SnmpInterface[]>())

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

  // Monotonic id sequencing getIpInterfacesForNodes requests, mirroring
  // getSnmpInterfacesForNodes below: a response only applies if no newer call has started since
  // its request was issued, so a slow response from a superseded request can never overwrite the
  // map after the latest one (or an empty-ids reset) has run. Needed because getNodes(...) fires
  // this off without awaiting it (see getNodes above), so two overlapping getNodes calls (e.g. a
  // fast page-2-then-page-3 click) can race their getIpInterfacesForNodes calls on the wire.
  let ipInterfacesRequestId = 0

  /**
   * Get the IpInterfaces for the given nodes, then REPLACE nodeToIpInterfaceMap wholesale with the
   * newly grouped result (mirroring getSnmpInterfacesForNodes's replace-wholesale semantics, so a
   * shallow watcher on the map ref reliably fires once the batch resolves).
   */
  const getIpInterfacesForNodes = async (nodeIds: string[], managedOnly: boolean) => {
    const requestId = ++ipInterfacesRequestId

    if (nodeIds.length === 0) {
      nodeToIpInterfaceMap.value = new Map<string, IpInterface[]>()
      return
    }

    const query = getNodeIpInterfaceQuery(nodeIds, managedOnly)
    const queryParameters = {
      limit: 0,
      _s: query
    } as QueryParameters

    const resp = await API.getIpInterfaces(queryParameters)

    if (requestId !== ipInterfacesRequestId) {
      return
    }

    const grouped = new Map<string, IpInterface[]>()

    if (resp) {
      // Group the response by nodeId (mirroring getSnmpInterfacesForNodes's grouping below): a
      // node with no returned interfaces simply has no entry, rather than an explicit empty-array
      // entry — callers already treat a missing key the same as an empty array (`?? []`), and an
      // empty response should leave the map empty (size 0), not one empty-array entry per
      // requested id.
      for (const ip of resp.ipInterface) {
        const key = ip.nodeId.toString()
        const ipsThisNode = grouped.get(key) ?? []
        ipsThisNode.push(ip)
        grouped.set(key, ipsThisNode)
      }
    }

    nodeToIpInterfaceMap.value = grouped
  }

  // Monotonic id sequencing getSnmpInterfacesForNodes requests: a response only applies if no
  // newer call has started since its request was issued, so a slow response from a superseded
  // request can never overwrite the map after the latest one (or an empty-ids reset) has run.
  let snmpInterfacesRequestId = 0

  /**
   * Get the SnmpInterfaces for the given nodes, then replace nodeToSnmpInterfaceMap with the
   * newly grouped result (grouped by String(nodeId)).
   */
  const getSnmpInterfacesForNodes = async (nodeIds: string[], narrowing?: string) => {
    const requestId = ++snmpInterfacesRequestId

    if (nodeIds.length === 0) {
      nodeToSnmpInterfaceMap.value = new Map<string, SnmpInterface[]>()
      return
    }

    const query = getNodeSnmpInterfaceQuery(nodeIds, narrowing)
    const queryParameters = {
      limit: 0,
      _s: query
    } as QueryParameters

    const resp = await API.getSnmpInterfaces(queryParameters)

    if (requestId !== snmpInterfacesRequestId) {
      return
    }

    const grouped = new Map<string, SnmpInterface[]>()

    if (resp) {
      for (const snmp of resp.snmpInterface) {
        const key = String(snmp.nodeId)
        const snmpsThisNode = grouped.get(key) ?? []
        snmpsThisNode.push(snmp)
        grouped.set(key, snmpsThisNode)
      }
    }

    nodeToSnmpInterfaceMap.value = grouped
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
    nodeToSnmpInterfaceMap,
    nodeQueryParameters,
    outages,
    outagesTotalCount,
    getIpInterfacesForNodes,
    getSnmpInterfacesForNodes,
    getNodes,
    getNodeById,
    getNodeSnmpInterfaces,
    getNodeIpInterfaces,
    getNodeAvailabilityPercentage,
    getNodeOutages,
    setNodeQueryParameters
  }
})
