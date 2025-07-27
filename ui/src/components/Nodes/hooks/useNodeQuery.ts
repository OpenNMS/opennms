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

import { isConvertibleToInteger } from '@/lib/utils'
import {
  Category,
  MonitoringLocation,
  NodeQueryFilter,
  NodeQueryForeignSourceParams,
  NodeQuerySnmpParams,
  NodeQuerySysParams,
  QueryParameters,
  SetOperator
} from '@/types'
import {
  parseCategories,
  parseFlows,
  parseForeignSource,
  parseIplike,
  parseMonitoringLocation,
  parseNodeLabel,
  parseSnmpParams,
  parseSysParams
} from './queryStringParser'

export const useNodeQuery = () => {
  const getDefaultNodeQueryForeignSourceParams = () => {
    return {
      foreignId: '',
      foreignSource: '',
      foreignSourceId: ''
    } as NodeQueryForeignSourceParams
  }

  const getDefaultNodeQuerySnmpParams = () => {
    return {
      snmpIfAlias: '',
      snmpIfDescription: '',
      snmpIfIndex: '',
      snmpIfName: '',
      snmpIfType: ''
    } as NodeQuerySnmpParams
  }

  const getDefaultNodeQuerySysParams = () => {
    return {
      sysContact: '',
      sysDescription: '',
      sysLocation: '',
      sysName: '',
      sysObjectId: ''
    } as NodeQuerySysParams
  }

  const getDefaultNodeQueryExtendedSearchParams = () => {
    return {
      ipAddress: '',
      foreignSourceParams: getDefaultNodeQueryForeignSourceParams(),
      snmpParams: getDefaultNodeQuerySnmpParams(),
      sysParams: getDefaultNodeQuerySysParams()
    }
  }

  const getDefaultNodeQueryFilter = () => {
    return {
      searchTerm: '',
      categoryMode: SetOperator.Union,
      selectedCategories: [] as Category[],
      selectedFlows: [] as string[],
      selectedMonitoringLocations: [] as MonitoringLocation[],
      extendedSearch: getDefaultNodeQueryExtendedSearchParams()
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

  /**
   * Query string search parameters tracked/accepted by the Node Structure page.
   */
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
    'snmpphysaddr',
    'foreignSource',
    'foreignId',
    'fsfid',
    'sysContact',
    'sysDescription',
    'sysLocation',
    'sysName',
    'sysObjectId'
  ])

  /**
   * Check if vue-router route.query object has any query string values we are tracking.
   */
  const queryStringHasTrackedValues = (queryObject: any) => {
    return Object.getOwnPropertyNames(queryObject).some(x => trackedNodeQueryStringProperties.has(x) && !!queryObject[x])
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
      filter.extendedSearch.ipAddress = ip
    }

    const snmpParams = parseSnmpParams(queryObject)

    if (snmpParams) {
      filter.extendedSearch.snmpParams = snmpParams
    }

    const sysParams = parseSysParams(queryObject)

    if (sysParams) {
      filter.extendedSearch.sysParams = sysParams
    }

    const fsParams = parseForeignSource(queryObject)

    if (fsParams) {
      filter.extendedSearch.foreignSourceParams = fsParams
    }

    return filter
  }

  return {
    buildNodeQueryFilterFromQueryString,
    buildUpdatedNodeStructureQueryParameters,
    getDefaultNodeQueryFilter,
    getDefaultNodeQueryExtendedSearchParams,
    getDefaultNodeQueryForeignSourceParams,
    getDefaultNodeQuerySnmpParams,
    getDefaultNodeQuerySysParams,
    queryStringHasTrackedValues
  }
}

/**
 * Build a FIQL query for the Node Rest service from a NodeQueryFilter.
 */
const buildNodeStructureQuery = (filter: NodeQueryFilter) => {
  const searchQuery = buildSearchQuery(filter.searchTerm)
  const ipAddressQuery = buildIpAddressQuery(filter.extendedSearch.ipAddress)
  const categoryQuery = buildCategoryQuery(filter.selectedCategories, filter.categoryMode)
  const flowsQuery = buildFlowsQuery(filter.selectedFlows)
  const locationQuery = buildLocationsQuery(filter.selectedMonitoringLocations)
  const foreignSourceQuery = buildForeignSourceQuery(filter.extendedSearch.foreignSourceParams)
  const snmpQuery = buildSnmpQuery(filter.extendedSearch.snmpParams)
  const sysQuery = buildSysQuery(filter.extendedSearch.sysParams)

  // TODO: filter on regex to screen out bad FIQL characters like ',', ';', etc.
  // and/or restrict characters in the FeatherInput above
  const query = [searchQuery, ipAddressQuery, foreignSourceQuery, snmpQuery, sysQuery, categoryQuery, flowsQuery, locationQuery].filter(s => s.length > 0).join(';')

  return query
}

const buildSearchQuery = (searchTerm: string) => {
  if (searchTerm?.length > 0) {
    const startStar = searchTerm.startsWith('*') ? '' : '*'
    const endStar = searchTerm.endsWith('*') ? '' : '*'
    return `label==${startStar}${searchTerm}${endStar},ipInterface.ipAddress==${startStar}${searchTerm}${endStar},ipInterface.ipHostName==${startStar}${searchTerm}${endStar},ipInterface.isManaged==${searchTerm}`
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

const isValidFsFidParam = (value: string) => {
  if (isValidParam(value)) {
    const arr = value.split(':')
    return arr && arr.length === 2 && arr[0].length > 0 && arr[1].length > 0
  }

  return false
}

const makeWildcard = (value: string) => {
  const s = value.replace('*', '')

  return `*${s}*`
}

/**
 * For now, can only search on FS, FID or FS:FID, but not combinations of these.
 */
const buildForeignSourceQuery = (fsParams?: NodeQueryForeignSourceParams) => {
  if (fsParams) {
    if (isValidFsFidParam(fsParams.foreignSourceId)) {
      const arr = fsParams.foreignSourceId.split(':')
      return `(node.foreignSource==${arr[0]};node.foreignId==${arr[1]})`
    } else if (isValidParam(fsParams.foreignSource)) {
      return `node.foreignSource==${fsParams.foreignSource}`
    } else if (isValidParam(fsParams.foreignId)) {
      return `node.foreignId==${fsParams.foreignId}`
    }
  }

  return ''
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

    if (isValidIntegerParam('' + snmpParams.snmpIfIndex)) {
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

const buildSysQuery = (sysParams?: NodeQuerySysParams) => {
  if (sysParams) {
    const props = ['sysContact', 'sysDescription', 'sysLocation', 'sysName', 'sysObjectId']
    const arr: string[] = []

    props.forEach(p => {
      const value = (sysParams as any)[p]
      if (isValidParam(value)) {
        arr.push(`node.${p}==${makeWildcard(value)}`)
      }
    })

    if (arr.length > 0) {
      return arr.join(';')
    }
  }

  return ''
}
