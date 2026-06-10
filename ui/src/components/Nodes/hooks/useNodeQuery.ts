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
  ExtendedSearchValue,
  MatchType,
  MonitoringLocation,
  NodeQueryExtendedSearchParams,
  NodeQueryFilter,
  NodeQueryForeignSourceParams,
  NodeQuerySnmpParams,
  NodeQuerySysParams,
  QueryParameters,
  ServiceType,
  SetOperator
} from '@/types'
import {
  parseCategories,
  parseFlows,
  parseForeignSource,
  parseIplike,
  parseMaclike,
  parseMib2Params,
  parseMonitoredServices,
  parseMonitoringLocation,
  parseNodeLabel,
  parseSnmpParams,
  parseSnmpParmParams,
  parseSysParams
} from './queryStringParser'
import { isIP } from 'is-ip'

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
      selectedCategories2: [] as Category[],
      selectedServices: [] as string[],
      selectedFlows: [] as string[],
      selectedMonitoringLocations: [] as MonitoringLocation[],
      ipAddress: '',
      topology: '',
      extendedSearch: getDefaultNodeQueryExtendedSearchParams()
    } as NodeQueryFilter
  }

  const EXTENDED_SEARCH_DISPLAY_NAMES: Record<string, string> = {
    foreignId: 'Foreign ID',
    foreignSource: 'Foreign Source',
    foreignSourceId: 'Foreign Source:Foreign ID',
    snmpIfAlias: 'SNMP Alias',
    snmpIfDescription: 'SNMP Description',
    snmpIfIndex: 'SNMP Index',
    snmpIfName: 'SNMP Name',
    snmpIfType: 'SNMP Type',
    physAddr: 'MAC Address',
    sysContact: 'Sys Contact',
    sysDescription: 'Sys Description',
    sysLocation: 'Sys Location',
    sysName: 'Sys Name',
    sysObjectId: 'Sys Object ID'
  }

  // match-type qualifier fields — not search terms, excluded from chips
  const EXTENDED_SEARCH_SKIP_FIELDS = new Set(['snmpMatchType', 'sysMatchType'])

  const getExtendedSearchValues = (extendedSearch?: NodeQueryExtendedSearchParams): ExtendedSearchValue[] => {
    if (!extendedSearch) {
      return []
    }

    const values: ExtendedSearchValue[] = []

    const addGroupValues = (group: keyof NodeQueryExtendedSearchParams, obj: Record<string, unknown> | undefined) => {
      if (!obj) {
        return
      }
      for (const key of Object.keys(obj)) {
        if (EXTENDED_SEARCH_SKIP_FIELDS.has(key)) {
          continue
        }
        const val = String(obj[key] || '')
        if (val) {
          values.push({ name: EXTENDED_SEARCH_DISPLAY_NAMES[key] ?? key, value: val, group, key })
        }
      }
    }

    addGroupValues('foreignSourceParams', extendedSearch.foreignSourceParams as unknown as Record<string, unknown>)
    addGroupValues('snmpParams', extendedSearch.snmpParams as unknown as Record<string, unknown>)
    addGroupValues('sysParams', extendedSearch.sysParams as unknown as Record<string, unknown>)

    return values
  }

  const addIpAddressToQueryFilter = (filter: NodeQueryFilter, ipAddress: string) => {
    const ip = parseIplike(ipAddress)

    if (ip) {
      return {
        ...filter,
        ipAddress: ip
      }
    }

    return filter
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
    'category1',
    'category2',
    'flows',
    'foreignsource',
    'ipAddress',
    'iplike',
    'listInterfaces',
    'maclike',
    'mib2Parm',
    'mib2ParmValue',
    'mib2ParmMatchType',
    'monitoredService',
    'monitoringLocation',
    'nodeLabel',
    'nodename',
    'service',
    'snmpifalias',
    'snmpifdescription',
    'snmpifindex',
    'snmpifname',
    'snmpiftype',
    'snmpMatchType',
    'snmpphysaddr',
    'snmpParm',
    'snmpParmValue',
    'snmpParmMatchType',
    'foreignSource',
    'foreignId',
    'fsfid',
    'sysContact',
    'sysDescription',
    'sysLocation',
    'sysName',
    'sysObjectId',
    'topology'
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
  const buildNodeQueryFilterFromQueryString = (queryObject: any, categories: Category[], monitoringLocations: MonitoringLocation[], serviceTypes: ServiceType[] = []) => {
    const filter: NodeQueryFilter = getDefaultNodeQueryFilter()

    filter.searchTerm = parseNodeLabel(queryObject)

    const { categoryMode, selectedCategories, selectedCategories2 } = parseCategories(queryObject, categories)
    if (selectedCategories.length > 0) {
      filter.categoryMode = categoryMode
      filter.selectedCategories = selectedCategories
      filter.selectedCategories2 = selectedCategories2
    }

    const location = parseMonitoringLocation(queryObject, monitoringLocations)
    if (location) {
      filter.selectedMonitoringLocations.push(location)
    }

    filter.selectedFlows = parseFlows(queryObject)

    const ip = parseIplike(queryObject)
    if (ip) {
      filter.ipAddress = ip
    }

    // Individual SNMP params take priority over legacy snmpParm/snmpParmValue
    const snmpParams = parseSnmpParams(queryObject)
    if (snmpParams) {
      filter.extendedSearch.snmpParams = snmpParams
    } else {
      const snmpParmParams = parseSnmpParmParams(queryObject)
      if (snmpParmParams) {
        filter.extendedSearch.snmpParams = snmpParmParams
      }
    }

    // Individual sys params take priority over legacy mib2Parm/mib2ParmValue
    const sysParams = parseSysParams(queryObject)
    if (sysParams) {
      filter.extendedSearch.sysParams = sysParams
    } else {
      const mib2Params = parseMib2Params(queryObject)
      if (mib2Params) {
        filter.extendedSearch.sysParams = mib2Params
      }
    }

    const fsParams = parseForeignSource(queryObject)
    if (fsParams) {
      filter.extendedSearch.foreignSourceParams = fsParams
    }

    // physAddr (MAC address) — set independently of snmpParm/snmpParams blocks
    const macAddr = parseMaclike(queryObject)
    if (macAddr) {
      if (!filter.extendedSearch.snmpParams) {
        filter.extendedSearch.snmpParams = getDefaultNodeQuerySnmpParams()
      }
      filter.extendedSearch.snmpParams.physAddr = macAddr
    }

    const serviceNames = parseMonitoredServices(queryObject, serviceTypes)
    if (serviceNames.length > 0) {
      filter.selectedServices = serviceNames
    }

    if (queryObject.topology) {
      filter.topology = queryObject.topology
    }

    // listInterfaces: intentionally not handled — the Vue node list page has no interface-listing mode,
    // and already displays the primary interface in the node table.
    // service=<id>: numeric service ID is not resolved here; PR 3 pages will send monitoredService=<name>.
    // NOTE nodeId: not handled here. Once Vue Node Details (/node/:id) has parity with element/node.jsp,
    //   update quicksearch-box.jsp to link to the Vue route instead of element/node.jsp?node={id}.

    return filter
  }

  return {
    addIpAddressToQueryFilter,
    buildNodeQueryFilterFromQueryString,
    buildUpdatedNodeStructureQueryParameters,
    getDefaultNodeQueryFilter,
    getDefaultNodeQueryExtendedSearchParams,
    getDefaultNodeQueryForeignSourceParams,
    getDefaultNodeQuerySnmpParams,
    getDefaultNodeQuerySysParams,
    getExtendedSearchValues,
    queryStringHasTrackedValues
  }
}

/**
 * Build a FIQL query for the Node Rest service from a NodeQueryFilter.
 */
const buildNodeStructureQuery = (filter: NodeQueryFilter) => {
  const searchTerm = sanitizeSearchTerm(filter.searchTerm)
  const ipAddress = sanitizeSearchTerm(filter.ipAddress)

  const searchQuery = buildSearchQuery(searchTerm)
  const ipAddressQuery = buildIpAddressQuery(ipAddress)
  const categoryQuery = buildCategoryQuery(filter.selectedCategories, filter.categoryMode, filter.selectedCategories2)
  const flowsQuery = buildFlowsQuery(filter.selectedFlows)
  const locationQuery = buildLocationsQuery(filter.selectedMonitoringLocations)
  const foreignSourceQuery = buildForeignSourceQuery(filter.extendedSearch.foreignSourceParams)
  const snmpQuery = buildSnmpQuery(filter.extendedSearch.snmpParams)
  const sysQuery = buildSysQuery(filter.extendedSearch.sysParams)
  const serviceQuery = buildServiceQuery(filter.selectedServices ?? [])
  const topologyQuery = buildTopologyQuery(filter.topology)

  // TODO: May need more search term sanitizing and/or restrict characters in the FeatherInput above
  const querySeparator = getFiqlSetOperator(SetOperator.Intersection)
  const query = [searchQuery, ipAddressQuery, foreignSourceQuery, snmpQuery, sysQuery, categoryQuery, flowsQuery, locationQuery, serviceQuery, topologyQuery].filter(s => s.length > 0).join(querySeparator)

  // additional fields to search on for main searchTerm
  // these will be added as SetOperator.Union (i.e. 'or')
  // for now, just ipAddress - but only if user does not specify ipAddress in extended search
  if (!ipAddress && isIP(searchTerm)) {
    const ipQuery = buildIpAddressQuery(searchTerm)
    const separator = getFiqlSetOperator(SetOperator.Union)

    return `${query}${separator}${ipQuery}`
  }

  return query
}

const buildSearchQuery = (searchTerm: string) => {
  if (searchTerm?.length > 0) {
    const startStar = searchTerm.startsWith('*') ? '' : '*'
    const endStar = searchTerm.endsWith('*') ? '' : '*'
    return `label==${startStar}${searchTerm}${endStar}`
  }

  return ''
}

const buildIpAddressQuery = (ipAddress?: string) => {
  if (ipAddress) {
    return `ipInterface.ipAddress==${ipAddress}`
  }

  return ''
}

const buildCategoryQuery = (selectedCategories: Category[], categoryMode: SetOperator, selectedCategories2?: Category[]) => {
  if (selectedCategories2 && selectedCategories2.length > 0) {
    // Grouped mode: union within each group, intersection between groups
    // In this case, we ignore categoryMode
    const buildGroup = (cats: Category[]) => {
      const items = cats.map(cat => `category.id==${cat.id}`)
      if (items.length === 0) return ''
      if (items.length === 1) return items[0]
      return `(${items.join(',')})`
    }
    const group1 = buildGroup(selectedCategories)
    const group2 = buildGroup(selectedCategories2)
    if (group1 && group2) return `${group1};${group2}`
    return group1 || group2
  }

  // Single category group: use categoryMode to determine union or intersection
  const categoryItems = selectedCategories.map(cat => `category.id==${cat.id}`)
  if (categoryItems.length === 1) return `${categoryItems[0]}`
  if (categoryItems.length > 1) {
    const separator = getFiqlSetOperator(categoryMode)
    return `(${categoryItems.join(separator)})`
  }
  return ''
}

const buildFlowsQuery = (selectedFlows: string[]) => {
  if (selectedFlows.some(f => f === 'No Flows')) {
    return 'lastIngressFlow==null;lastEgressFlow==null'
  }

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

const getSnmpSearchTerm = (name: string, field: any, wildcard = false) => {
  const fieldStr = (field as string) || ''
  const searchValue = wildcard ? makeWildcard(fieldStr) : fieldStr
  return `snmpInterface.${name}==${searchValue}`
}

const isValidParam = (value: string | undefined) => {
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

const buildServiceQuery = (selectedServices: string[]) => {
  if (!selectedServices || selectedServices.length === 0) {
    return ''
  }

  const items = selectedServices
    .filter(name => name && name.trim().length > 0)
    .map(name => `serviceType.name==${name}`)

  if (items.length === 1) {
    return items[0]
  }

  return `(${items.join(',')})`
}

const buildSnmpQuery = (snmpParams?: NodeQuerySnmpParams) => {
  if (snmpParams) {
    const arr: string[] = []
    const wildcard = snmpParams.snmpMatchType === MatchType.Contains

    if (isValidParam(snmpParams.snmpIfAlias)) {
      arr.push(getSnmpSearchTerm('ifAlias', snmpParams.snmpIfAlias, wildcard))
    }

    if (isValidParam(snmpParams.snmpIfDescription)) {
      arr.push(getSnmpSearchTerm('ifDescr', snmpParams.snmpIfDescription, wildcard))
    }

    if (isValidIntegerParam('' + snmpParams.snmpIfIndex)) {
      arr.push(getSnmpSearchTerm('ifIndex', snmpParams.snmpIfIndex))
    }

    if (isValidParam(snmpParams.snmpIfName)) {
      arr.push(getSnmpSearchTerm('ifName', snmpParams.snmpIfName, wildcard))
    }

    if (isValidIntegerParam(snmpParams.snmpIfType)) {
      arr.push(getSnmpSearchTerm('ifType', snmpParams.snmpIfType))
    }

    if (isValidParam(snmpParams.physAddr)) {
      arr.push(`snmpInterface.physAddr==*${snmpParams.physAddr!}*`)
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
        const searchValue = sysParams.sysMatchType === MatchType.Equals ? value : makeWildcard(value)
        arr.push(`node.${p}==${searchValue}`)
      }
    })

    if (arr.length > 0) {
      return arr.join(';')
    }
  }

  return ''
}

const buildTopologyQuery = (topology?: string) => {
  const term = sanitizeSearchTerm(topology)
  if (term.length > 0) {
    const startStar = term.startsWith('*') ? '' : '*'
    const endStar = term.endsWith('*') ? '' : '*'
    return `topology==${startStar}${term}${endStar}`
  }
  return ''
}

/**
 * Remove any FIQL characters from a search term.
 * May need to do additional replacements here.
 */
export const sanitizeSearchTerm = (s?: string) => {
  return (s || '').replace(/[,;]/g, ' ')
}

export const getFiqlSetOperator = (op: SetOperator) => {
  return op === SetOperator.Union ? ',' : ';'
}
