import { isConvertibleToInteger } from '@/lib/utils'
import {
  Category,
  MonitoringLocation,
  NodeQueryFilter,
  NodeQuerySnmpParams,
  QueryParameters,
  SetOperator
} from '@/types'
import {
  parseCategories,
  parseFlows,
  parseIplike,
  parseMonitoringLocation,
  parseNodeLabel,
  parseSnmpParams
} from './queryStringParser'

export const useNodeQuery = () => {
  const getDefaultNodeQuerySnmpParams = () => {
    return {
      snmpIfAlias: '',
      snmpIfDescription: '',
      snmpIfIndex: '',
      snmpIfName: '',
      snmpIfType: ''
    } as NodeQuerySnmpParams
  }

  const getDefaultNodeQueryFilter = () => {
    return {
      searchTerm: '',
      categoryMode: SetOperator.Union,
      selectedCategories: [] as Category[],
      selectedFlows: [] as string[],
      selectedMonitoringLocations: [] as MonitoringLocation[],
      ipAddress: '',
      snmpParams: getDefaultNodeQuerySnmpParams()
    } as NodeQueryFilter
  }

  /**
   * Build new QueryParameters based on existing QueryParameters (which contain e.g. limit, offset and similar), 
   * combined with the given NodeQueryFilter.
   */
  const buildUpdatedNodeStructureQueryParameters = (queryParameters: QueryParameters, filter: NodeQueryFilter) => {
    const searchQuery = buildNodeStructureQuery(filter)
    const searchQueryParam: QueryParameters = { _s: searchQuery }
    const updatedParams = { ...queryParameters, ...searchQueryParam }

    // if there is no search query, remove the '_s' property entirely so it doesn't
    // get put into the API request query string
    if (!searchQuery) {
      delete updatedParams._s
    }

    return updatedParams as QueryParameters
  }

  const trackedNodeQueryStringProperties = new Set([
    'categories',
    'flows',
    'ipAddress',
    'iplike',
    'listInterfaces',
    'monitoredService',
    'monitoringLocation',
    'nodeLabel',
    'nodename',
    'snmpifalias',
    'snmpifdescription',
    'snmpifindex',
    'snmpifname',
    'snmpMatchType',
    'snmpphysaddr'
  ])

  /**
   * Check if vue-router route.query object has any query string values we are tracking.
   */
  const queryStringHasTrackedValues = (queryObject: any) => {
    return Object.getOwnPropertyNames(queryObject).some(x => trackedNodeQueryStringProperties.has(x))
  }

  /**
   * Build a node query from a query string coming from another page. queryObject is from vue-router route.query.
   * Used to set the new search query as well as NodeFilterPreferences, which will
   * replace the ones user had previously set.
   * Note, this creates a new filter, should only be called when queryObject has one or more values we are tracking.
   *
   * @param query query object from vue-router route.query
   */
  const buildNodeQueryFilterFromQueryString = (queryObject: any, categories: Category[], monitoringLocations: MonitoringLocation[]) => {
    const filter: NodeQueryFilter = getDefaultNodeQueryFilter()

    filter.searchTerm = parseNodeLabel(queryObject)

    const { categoryMode, selectedCategories } = parseCategories(queryObject, categories)

    if (selectedCategories.length > 0) {
      filter.categoryMode = categoryMode
      filter.selectedCategories = selectedCategories
    }

    const location = parseMonitoringLocation(queryObject, monitoringLocations)

    if (location) {
      filter.selectedMonitoringLocations.push(location)
    }

    filter.selectedFlows = parseFlows(queryObject)

    // TODO: Implement ipaddress or iplike filtering
    const ip = parseIplike(queryObject)

    if (ip) {
      filter.ipAddress = ip
    }

    const snmpParams = parseSnmpParams(queryObject)

    if (snmpParams) {
      filter.snmpParams = snmpParams
    }

    return filter
  }

  return {
    buildNodeQueryFilterFromQueryString,
    buildUpdatedNodeStructureQueryParameters,
    getDefaultNodeQueryFilter,
    getDefaultNodeQuerySnmpParams,
    queryStringHasTrackedValues
  }
}

/**
 * Build a FIQL query for the Node Rest service from a NodeQueryFilter.
 */
const buildNodeStructureQuery = (filter: NodeQueryFilter) => {
  const searchQuery = buildSearchQuery(filter.searchTerm)
  const ipAddressQuery = buildIpAddressQuery(filter.ipAddress)
  const categoryQuery = buildCategoryQuery(filter.selectedCategories, filter.categoryMode)
  const flowsQuery = buildFlowsQuery(filter.selectedFlows)
  const locationQuery = buildLocationsQuery(filter.selectedMonitoringLocations)
  const snmpQuery = buildSnmpQuery(filter.snmpParams)

  // TODO: filter on regex to screen out bad FIQL characters like ',', ';', etc.
  // and/or restrict characters in the FeatherInput above
  const query = [searchQuery, ipAddressQuery, snmpQuery, categoryQuery, flowsQuery, locationQuery].filter(s => s.length > 0).join(';')

  return query
}

const buildSearchQuery = (searchTerm: string) => {
  if (searchTerm?.length > 0) {
    const startStar = searchTerm.startsWith('*') ? '' : '*'
    const endStar = searchTerm.endsWith('*') ? '' : '*'
    return `node.label==${startStar}${searchTerm}${endStar}`
  }

  return ''
}

const buildIpAddressQuery = (ipAddress?: string) => {
  if (ipAddress) {
    return `ipInterface.ipAddress==${ipAddress}`
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

const getSnmpSearchTerm = (name: string, field: any) => {
  const fieldStr = (field as string) || ''

  return `snmpInterface.${name}==${fieldStr}`
}

const isValidParam = (value: string) => {
  return !!value && !!value.trim()
}

const isValidIntegerParam = (value: string) => {
  return isValidParam(value) && isConvertibleToInteger(value.trim())
}

/**
 * Note, FIQL / SNMP search does not currently support 'like' or 'contains' matches, so we always
 * do an 'equals' (exact match) search.
 */
const buildSnmpQuery = (snmpParams?: NodeQuerySnmpParams) => {
  if (snmpParams) {
    const arr: string[] = []

    if (isValidParam(snmpParams.snmpIfAlias)) {
      arr.push(getSnmpSearchTerm('ifAlias', snmpParams.snmpIfAlias))
    }

    if (isValidParam(snmpParams.snmpIfDescription)) {
      arr.push(getSnmpSearchTerm('ifDescr', snmpParams.snmpIfDescription))
    }

    if (isValidIntegerParam(snmpParams.snmpIfIndex)) {
      arr.push(getSnmpSearchTerm('ifIndex', snmpParams.snmpIfIndex))
    }

    if (isValidParam(snmpParams.snmpIfName)) {
      arr.push(getSnmpSearchTerm('ifName', snmpParams.snmpIfName))
    }

    if (isValidIntegerParam(snmpParams.snmpIfType)) {
      arr.push(getSnmpSearchTerm('ifType', snmpParams.snmpIfType))
    }

    if (arr.length > 0) {
      return arr.join(';')
    }
  }
  
  return ''
}
