import { Category, IpInterface, MonitoringLocation, Node, NodeApiResponse, NodeColumnSelectionItem, QueryParameters, SetOperator } from '@/types'
import { isNumber } from '@/lib/utils'
import { getNodes } from '@/services/nodeService'

export interface NodeStructureQueryParams {
  searchVal: string,
  selectedCategories: Category[],
  categoryMode: SetOperator,
  selectedFlows: string[],
  selectedLocations: MonitoringLocation[]
}

/**
 * Construct an array of Feather Table CSS classes for the given configured node table columns. 
 * These start with 't', then ('l', 'r', 'c') for (left, right, center), then the 1 based column index.
 * e.g. 'tl1': left-align 1st column
 * 'tr7': right-align 7th colunn
 */
export const getTableCssClasses = (columns: NodeColumnSelectionItem[]) => {
  const classes: string[] = columns.filter(col => col.selected).map((col, i) => {
    let t = 'tl'

    if (col.id === 'id') {
      t = 'tr'
    } else if (col.id === 'flows') {
      t = 'tc'
    }

    // +2 : one since Feather table column classes are 1-based, one for the first action column which isn't in 'columns'
    return `${t}${i + 2}`
  })

  // add 'action' column
  return ['tl1', ...classes]
}

const buildSearchQuery = (searchVal: string) => {
  if (searchVal.length > 0) {
    const startStar = searchVal.startsWith('*') ? '' : '*'
    const endStar = searchVal.endsWith('*') ? '' : '*'
    return `node.label==${startStar}${searchVal}${endStar}`
  }

  return ''
}

const buildCategoryQuery = (selectedCategories: Category[], categoryMode: SetOperator) => {
  const categoryItems = selectedCategories.map(cat => `category.id==${cat.id}`)

  if (categoryItems.length === 1) {
    return `${categoryItems[0]}`
  } else if (categoryItems.length > 1) {
    const separator = categoryMode === SetOperator.Intersection ? ';' : ','
    return `(${categoryItems.join(separator)})`
  }

  return ''
}

const buildFlowsQuery = (selectedFlows: string[]) => {
  const hasIngress = selectedFlows.some(f => f === 'Ingress')
  const hasEgress = selectedFlows.some(f => f === 'Egress')

  const flowItems = [
    hasIngress ? 'lastIngressFlow=gt=0' : '',
    hasEgress ? 'lastEgressFlow=gt=0' : ''
  ].filter(x => x)

  if (flowItems.length === 1) {
    return `${flowItems[0]}`
  } else if (flowItems.length > 1) {
    return `(${flowItems.join(',')})`
  }

  return ''
}

const buildLocationsQuery = (selectedLocations: MonitoringLocation[]) => {
  const locationItems = selectedLocations.map(loc => `node.location.locationName==${loc.name}`)

  if (locationItems.length === 1) {
    return `${locationItems[0]}`
  } else if (locationItems.length > 1) {
    return `(${locationItems.join(',')})`
  }

  return ''
}

export const buildNodeStructureQuery = (
  { searchVal, selectedCategories, categoryMode, selectedFlows, selectedLocations }: NodeStructureQueryParams) => {
  const searchQuery = buildSearchQuery(searchVal)
  const categoryQuery = buildCategoryQuery(selectedCategories, categoryMode)
  const flowsQuery = buildFlowsQuery(selectedFlows)
  const locationQuery = buildLocationsQuery(selectedLocations)

  // TODO: filter on regex to screen out bad FIQL characters like ',', ';', etc.
  // and/or restrict characters in the FeatherInput above
  const query = [searchQuery, categoryQuery, flowsQuery, locationQuery].filter(s => s.length > 0).join(';')

  return query
}

export const buildUpdatedNodeStructureQueryParams = (queryParameters: QueryParameters, newParams: NodeStructureQueryParams) => {
  const searchQuery = buildNodeStructureQuery(newParams)
  const searchQueryParam: QueryParameters = { _s: searchQuery }
  const updatedParams = { ...queryParameters, ...searchQueryParam }

  // if there is no search query, remove the '_s' property entirely so it doesn't
  // get put into the API request query string
  if (!searchQuery) {
    delete updatedParams._s
  }

  return updatedParams as QueryParameters
}

export interface IpInterfaceInfo {
  label: string
  managed: boolean
  primaryLabel: string
  primaryType: string
}

const getSnmpPrimaryLabel = (primaryType: string) => {
  switch (primaryType) {
    case 'P': return 'Primary'
    case 'S': return 'Secondary'
    case 'N': return 'Not Eligible'
    default: return ''
  }
}

/**
 * Find the 'best' IP for the given node id.
 * If there is only one, return that one. Otherwise try to find the primary SNMP interface, or the otherwise best one.
 *
 * @returns a object with the IP address plus a modifier: 'M' for managed, 'P' for primary, 'S' for secondary, 'N' for not eligible
 */
export const getBestIpInterfaceForNode = (nodeId: string, nodeToIpInterfaceMap: Map<string, IpInterface[]>): IpInterfaceInfo => {
  if (nodeToIpInterfaceMap.has(nodeId)) {
    const ipInterfaces = nodeToIpInterfaceMap.get(nodeId) || []

    let intf: IpInterface | null = null

    if (ipInterfaces.length === 1) {
      intf = ipInterfaces[0]
    } else if (ipInterfaces.length > 1) {
      // try to get SNMP primary (even if unmanaged), or else the first managed interface, or else just the first interface
      intf = ipInterfaces.find(x => x.snmpPrimary === 'P') || ipInterfaces.find(x => x.isManaged === 'M') || ipInterfaces[0]
    }

    if (intf) {
      const managed = intf.isManaged === 'M'
      const primaryType = intf.snmpPrimary || ''
      const primaryLabel = getSnmpPrimaryLabel(primaryType)

      return {
        label: intf.ipAddress,
        managed,
        primaryLabel,
        primaryType
      } as IpInterfaceInfo
    }
  }

  return {
    label: '',
    managed: false,
    primaryLabel: '',
    primaryType: ''
  } as IpInterfaceInfo
}

export const hasIngressFlow = (node: Node) => {
  return node.lastIngressFlow && isNumber(node.lastIngressFlow)
}

export const hasEgressFlow = (node: Node) => {
  return node.lastEgressFlow && isNumber(node.lastEgressFlow)
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

export const buildCsvExport = (columns: NodeColumnSelectionItem[], nodes: any[]): string[] => {
  const selectedColumns = columns.filter(c => c.selected)

  const header = selectedColumns.map(c => c.label).join(',')

  const rows = nodes.map(node => {
    const cols: string[] = selectedColumns.map(col => getCsvString(node[col.id]))
    return cols.join(',')
  })

  return [header, ...rows]
}

/**
 * Create Node export data as a string, with given query parameters/filters and currently configured columns.
 * @param format Export format, either 'csv' or 'json'
 */
export const getExportData = async (format: string,
  queryParams: NodeStructureQueryParams,
  initialQueryParams: QueryParameters,
  columns: NodeColumnSelectionItem[]) => {

  const updatedParams = {
    ...buildUpdatedNodeStructureQueryParams(initialQueryParams, queryParams),
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
export const generateBlob = (data: string, contentType: string): Blob => {
  return new Blob([data], { type: contentType })
}

/**
 * Create and call the target <a/> element
 * Note, should probably call window.URL.revokeObjectURL() to clean up.
 */
export const generateDownload = (blob: Blob, name: string): void => {
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = name
  a.click()
}
