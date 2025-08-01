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

import { getIpInterfaces, getNodeIpInterfaceQuery } from '@/services/ipInterfaceService'
import { getNodes } from '@/services/nodeService'
import {
  IpInterface,
  Node,
  NodeApiResponse,
  NodeColumnSelectionItem,
  QueryParameters
} from '@/types'
import { hasEgressFlow, hasIngressFlow } from '../utils'
import { useIpInterfaceQuery } from './useIpInterfaceQuery'

export const useNodeExport = () => {

  const getIpInterfacesForNodes = async (nodeIds: string[], managedOnly: boolean): Promise<Map<string, IpInterface[]> | boolean> => {
    if (nodeIds.length === 0) {
      return false
    }
    const query = getNodeIpInterfaceQuery(nodeIds, managedOnly)
    const queryParameters = {
      limit: 0,
      _s: query
    } as QueryParameters
    const resp = await getIpInterfaces(queryParameters)
    const nodeToIpInterfaceMap = new Map<string, IpInterface[]>()
    if (resp) {
      // find updated list of IpInterfaces for each node and update the node => ip map
      for (const id of nodeIds) {
        const ipsThisNode = resp.ipInterface.filter(ip => ip.nodeId.toString() === id)
        nodeToIpInterfaceMap.set(id, ipsThisNode)
      }
      return nodeToIpInterfaceMap
    }
    return false
  }
  /**
   * Create Node export data as a string, with given query parameters/filters and currently configured columns.
   * @param format Export format, either 'csv' or 'json'
   */
  const getExportData = async (format: string, queryParams: QueryParameters, columns: NodeColumnSelectionItem[]) => {
    const updatedParams = {
      ...queryParams,
      offset: 0,
      limit: 0
    }

    const resp = await getNodes(updatedParams)

    if (!resp || !resp.node || resp.node.length === 0) {
      console.error('Invalid response from getNodes, or no nodes found for the given search')
      return ''
    }

    const nodeResponse: NodeApiResponse = resp
    const nodes: Node[] = nodeResponse.node
    const nodeIds = nodes.map(n => n.id)
    const interfacesMap = await getIpInterfacesForNodes(nodeIds, false)
    const nodeToIpInterfaceMap = interfacesMap as Map<string, IpInterface[]>
    const exportableNodes = [] as any[]
    const { getBestIpInterfaceForNode } = useIpInterfaceQuery()
    nodes.forEach((node) => {
      const exportableNode = buildExportableNode(columns, node)
      exportableNode.ipaddress = getBestIpInterfaceForNode(node.id, nodeToIpInterfaceMap).label
      exportableNodes.push(exportableNode)
    })

    if (format === 'json') {
      return JSON.stringify(exportableNodes, null, 2)
    }

    if (format === 'csv') {
      const csvRows = buildCsvExport(columns, exportableNodes)
      return csvRows.join('\n')
    }

    return ''
  }

  /**
   * Generate a blob for the given text and content type.
   */
  const generateBlob = (data: string, contentType: string): Blob => {
    return new Blob([data], { type: contentType })
  }

  /**
   * Create and call the target <a/> element
   * Note, should probably call window.URL.revokeObjectURL() to clean up.
   */
  const generateDownload = (blob: Blob, name: string): void => {
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = name
    a.click()
  }

  return {
    generateBlob,
    generateDownload,
    getExportData
  }
}

/**
 * Create an object used to export data which contains only the fields from the currently selected columns for the given Node.
 */
const buildExportableNode = (columns: NodeColumnSelectionItem[], node: Node) => {
  const obj: any = {}
  const selectedColumns = columns.filter(col => col.selected)

  for (const col of selectedColumns) {
    let val: string | null = null

    if (col.id === 'flows') {
      const hasIngress = hasIngressFlow(node)
      const hasEgress = hasEgressFlow(node)

      if (hasIngress && hasEgress) {
        val = 'Ingress, Egress'
      } else if (hasIngress) {
        val = 'Ingress'
      } else if (hasEgress) {
        val = 'Egress'
      }
    } else {
      val = (node as any)[col.id]
    }

    if (val !== null) {
      obj[col.id] = val
    }
  }

  return obj
}

const getCsvString = (val: any) => {
  if (val === null || val === undefined) {
    return ''
  } else {
    let s: string = val.toString()

    if (s.includes(',')) {
      s = `"${s}"`
    }

    return s
  }
}

const buildCsvExport = (columns: NodeColumnSelectionItem[], nodes: any[]): string[] => {
  const selectedColumns = columns.filter(c => c.selected)

  const header = selectedColumns.map(c => c.label).join(',')

  const rows = nodes.map(node => {
    const cols: string[] = selectedColumns.map(col => getCsvString(node[col.id]))
    const joined = cols.join(',')

    if (joined.endsWith(',')) {
      return joined.slice(0, joined.length - 1)
    }
    return joined
  })

  return [header, ...rows]
}