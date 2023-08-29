import { defineStore } from 'pinia'
import API from '@/services'
import { IpInterface, Node, NodeAvailability, Outage, QueryParameters, SnmpInterface } from '@/types'

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

  const getNodes = async (queryParameters?: QueryParameters) => {
    const resp = await API.getNodes(queryParameters)

    if (resp) {
      totalCount.value = resp.totalCount
      nodes.value = resp.node
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

  return {
    nodes,
    totalCount,
    node,
    snmpInterfaces,
    snmpInterfacesTotalCount,
    ipInterfaces,
    ipInterfacesTotalCount,
    availability,
    outages,
    outagesTotalCount,
    getNodes,
    getNodeById,
    getNodeSnmpInterfaces,
    getNodeIpInterfaces,
    getNodeAvailabilityPercentage,
    getNodeOutages
  }
})
