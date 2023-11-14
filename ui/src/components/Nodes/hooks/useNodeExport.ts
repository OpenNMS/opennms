import { getNodes } from '@/services/nodeService'
import {
  Node,
  NodeApiResponse,
  NodeColumnSelectionItem,
  QueryParameters
} from '@/types'
import { hasEgressFlow, hasIngressFlow } from '../utils'

export const useNodeExport = () => {
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

    const nodeResponse = resp as NodeApiResponse
    const nodes: Node[] = nodeResponse.node

    const exportableNodes = nodes.map(n => buildExportableNode(columns, n))

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
    return cols.join(',')
  })

  return [header, ...rows]
}
