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

import {
  AssetFilter,
  Category,
  MatchType,
  MonitoringLocation,
  NodeQueryForeignSourceParams,
  NodeQuerySnmpParams,
  NodeQuerySysParams,
  ServiceType,
  SetOperator
} from '@/types'
import { isIP } from 'is-ip'

/** Parse node label from a vue-router route.query object */
export const parseNodeLabel = (queryObject: any) => {
  return queryObject.nodename as string || queryObject.nodeLabel as string || ''
}

/**
 * Parse categories from a vue-router route.query object.
 *
 * Two formats are supported:
 * - 'categories': flat comma- or semicolon-separated list (comma=Union, semicolon=Intersection).
 *   Returns a single group in selectedCategories with selectedCategories2 empty.
 * - 'category1' / 'category2': legacy format from surveillance-view links. Each param may be a
 *   single string or an array (vue-router repeats params as arrays). Each group is union internally;
 *   when both groups are non-empty, intersection is applied between them via selectedCategories2.
 *   If only one of the two is present, all items go into selectedCategories.
 *
 * @returns categoryMode, selectedCategories (group 1), selectedCategories2 (group 2, may be empty)
 */
export const parseCategories = (queryObject: any, categories: Category[]): {
  categoryMode: SetOperator
  selectedCategories: Category[]
  selectedCategories2: Category[]
} => {
  let categoryMode: SetOperator = SetOperator.Union
  const selectedCategories: Category[] = []
  const selectedCategories2: Category[] = []

  if (categories.length === 0) {
    return { categoryMode, selectedCategories, selectedCategories2 }
  }

  const resolveCategory = (vals: string[]): Category[] => {
    const result: Category[] = []
    vals.forEach((c) => {
      if (!c) {
        return
      }
      if (/^\d+$/.test(c)) {
        const item = categories.find(x => x.id === parseInt(c))
        if (item) {
          result.push(item)
        }
      } else {
        const item = categories.find(x => x.name.toLowerCase() === c.toLowerCase())
        if (item) {
          result.push(item)
        }
      }
    })
    return result
  }

  if (queryObject.categories) {
    // Flat 'categories' string: comma=union, semicolon=intersection
    const queryCategories = queryObject.categories as string
    categoryMode = queryCategories.includes(';') ? SetOperator.Intersection : SetOperator.Union
    const cats = queryCategories.replace(/;/g, ',').split(',')
    selectedCategories.push(...resolveCategory(cats))
  } else if (queryObject.category1 || queryObject.category2) {
    // Legacy category1/category2: union within each group, intersection between groups.
    // Vue-router gives a string for a single occurrence, array for multiple occurrences.
    const toArray = (v: any): string[] =>
      Array.isArray(v) ? (v as string[]) : v ? [v as string] : []

    const group1 = resolveCategory(toArray(queryObject.category1))
    const group2 = resolveCategory(toArray(queryObject.category2))

    if (group1.length > 0 && group2.length > 0) {
      selectedCategories.push(...group1)
      selectedCategories2.push(...group2)
    } else {
      // Only one group present — treat as a flat union (backwards compat)
      selectedCategories.push(...group1, ...group2)
    }
    categoryMode = SetOperator.Union
  }

  return { categoryMode, selectedCategories, selectedCategories2 }
}

export const parseMonitoringLocation = (queryObject: any, monitoringLocations: MonitoringLocation[]) => {
  const locationName = queryObject.monitoringLocation as string || ''

  if (locationName) {
    return monitoringLocations.find(x => x.name.toLowerCase() === locationName.toLowerCase()) ?? null
  }

  return null
}

export const parseFlows = (queryObject: any) => {
  const flows = (queryObject.flows as string || '').toLowerCase()

  if (flows === 'true') {
    return ['Ingress', 'Egress']
  } else if (flows === 'ingress') {
    return ['Ingress']
  } else if (flows === 'egress') {
    return ['Egress']
  } else if (flows === 'false') {
    return ['No Flows']
  }

  return []
}

/**
 * Returns true if value looks like an IPv4 or IPv6 iplike pattern (wildcard, range, or list).
 *
 * IPv4: 1-4 dot-separated segments, each being * | N | N-M | N,M | N-M,P-Q,...
 *   Examples: 192.168.1.*, 10.9.1-3.*, 10.0.0.1-255, 192.168.0,1,2.*
 * IPv6: 1-8 colon-separated hextets, each being * | H | H-H | H,H,...  (hex values)
 *   Examples: 2001:db8:*:*:*:*:*:*, fe80:*:*:*:*:*:*:*, 2001:0-ffff:*:*:*:*:*:*
 *
 * Returns false for plain exact IPs (use isIP() for those) and for garbage.
 * Compressed IPv6 notation (::) is not supported in patterns — only in exact addresses.
 * Ranges are per-segment only; cross-segment notation like 10.0.0.1-10.0.0.255 is invalid.
 */
export const isIplikePattern = (value: string): boolean => {
  // Normalize spaces around commas so "1, 2, 3" is treated the same as "1,2,3"
  const v = value.replace(/\s*,\s*/g, ',')
  // Must contain at least one pattern character to be an iplike pattern (not a plain IP)
  if (!v.includes('*') && !v.includes('-') && !v.includes(',')) {
    return false
  }
  if (v.includes('.')) {
    // IPv4: each octet is * | N | N-M | list of those
    const seg = '(\\*|\\d+(?:-\\d+)?(?:,\\d+(?:-\\d+)?)*)'
    return new RegExp(`^${seg}(\\.${seg}){0,3}$`).test(v)
  }
  if (v.includes(':')) {
    // IPv6: each hextet is * | H | H-H | list of those (hex digits only)
    const seg = '(\\*|[0-9a-fA-F]{1,4}(?:-[0-9a-fA-F]{1,4})?(?:,[0-9a-fA-F]{1,4}(?:-[0-9a-fA-F]{1,4})?)*)'
    return new RegExp(`^${seg}(:${seg}){0,7}$`).test(v)
  }
  return false
}

/**
 * Parses an IP address or iplike pattern from a URL query object.
 * Accepts exact IPv4/IPv6 addresses and IPv4 iplike wildcard patterns like 192.168.1.*.
 * Priority: `iplike` param over `ipAddress` param.
 */
export const parseIplike = (queryObject: any) => {
  const ip = queryObject.iplike as string || queryObject.ipAddress as string || ''

  if (!ip) {
    return null
  }

  if (isIP(ip) || isIplikePattern(ip)) {
    return ip
  }

  return null
}

export const parseForeignSource = (queryObject: any) => {
  const foreignSource = queryObject.foreignSource || queryObject.foreignsource || ''
  const foreignId = queryObject.foreignId || ''
  const foreignSourceId = queryObject.foreignSourceId || queryObject.fsfid || ''

  if (foreignSource || foreignId || foreignSourceId) {
    return {
      foreignSource,
      foreignId,
      foreignSourceId
    } as NodeQueryForeignSourceParams
  }

  return null
}

export const parseSnmpParams = (queryObject: any) => {
  const snmpIfAlias = queryObject.snmpifalias as string || ''
  const snmpIfDescription = queryObject.snmpifdescription as string || ''
  const snmpIfIndex = queryObject.snmpifindex as string || ''
  const snmpIfName = queryObject.snmpifname as string || ''
  const snmpIfType = queryObject.snmpiftype as string || ''
  const snmpMatchType = (queryObject.snmpMatchType as string) === 'contains' ? MatchType.Contains : MatchType.Equals

  if (snmpIfAlias || snmpIfDescription || snmpIfIndex || snmpIfName || snmpIfType) {
    return {
      snmpIfAlias,
      snmpIfDescription,
      snmpIfIndex,
      snmpIfName,
      snmpIfType,
      snmpMatchType
    } as NodeQuerySnmpParams
  }

  return null
}

export const parseSysParams = (queryObject: any) => {
  const sysContact = queryObject.sysContact as string || ''
  const sysDescription = queryObject.sysDescription as string || ''
  const sysLocation = queryObject.sysLocation as string || ''
  const sysName = queryObject.sysName as string || ''
  const sysObjectId = queryObject.sysObjectId as string || ''

  if (sysContact || sysDescription || sysLocation || sysName || sysObjectId) {
    return {
      sysContact,
      sysDescription,
      sysLocation,
      sysName,
      sysObjectId
    } as NodeQuerySysParams
  }

  return null
}

const snmpParmToFieldMap: Record<string, keyof NodeQuerySnmpParams> = {
  ifAlias: 'snmpIfAlias',
  ifName: 'snmpIfName',
  ifDescr: 'snmpIfDescription'
}

/**
 * Maps legacy snmpParm/snmpParmValue/snmpParmMatchType params to NodeQuerySnmpParams.
 * snmpParm must be one of: ifAlias, ifName, ifDescr.
 * snmpParmMatchType=contains applies wildcard matching; default is exact match.
 */
export const parseSnmpParmParams = (queryObject: any): NodeQuerySnmpParams | null => {
  const parm = queryObject.snmpParm as string || ''
  const value = queryObject.snmpParmValue as string || ''
  const matchTypeStr = (queryObject.snmpParmMatchType as string || '').toLowerCase()

  if (!parm || !value || !snmpParmToFieldMap[parm]) {
    return null
  }

  const snmpMatchType = matchTypeStr === 'contains' ? MatchType.Contains : MatchType.Equals

  return {
    snmpIfAlias: '',
    snmpIfDescription: '',
    snmpIfIndex: '',
    snmpIfName: '',
    snmpIfType: '',
    snmpMatchType,
    [snmpParmToFieldMap[parm]]: value
  } as NodeQuerySnmpParams
}

/**
 * Maps the legacy mib2Parm/mib2ParmValue/mib2ParmMatchType params to NodeQuerySysParams.
 * mib2Parm must be one of: sysDescription, sysObjectId, sysContact, sysName, sysLocation.
 */
export const parseMib2Params = (queryObject: any): NodeQuerySysParams | null => {
  const parm = queryObject.mib2Parm as string || ''
  const value = queryObject.mib2ParmValue as string || ''
  const matchTypeStr = (queryObject.mib2ParmMatchType as string || '').toLowerCase()

  const validParms: Array<keyof NodeQuerySysParams> = [
    'sysContact', 'sysDescription', 'sysLocation', 'sysName', 'sysObjectId'
  ]

  if (!parm || !value || !validParms.includes(parm as keyof NodeQuerySysParams)) {
    return null
  }

  const sysMatchType = matchTypeStr === 'equals' ? MatchType.Equals : MatchType.Contains

  return {
    sysContact: '',
    sysDescription: '',
    sysLocation: '',
    sysName: '',
    sysObjectId: '',
    sysMatchType,
    [parm]: value
  } as NodeQuerySysParams
}

/**
 * Maps legacy maclike/snmpphysaddr params to a stripped, lowercase MAC address string
 * suitable for matching against snmpInterface.physAddr.
 * All non-hex characters (separators like ':' and '-', plus any stray FIQL characters such as
 * ',' or ';') are stripped, both to match the format stored in the database and to keep the value
 * safe for the FIQL expression.
 */
export const parseMaclike = (queryObject: any): string | null => {
  const mac = queryObject.maclike as string || queryObject.snmpphysaddr as string || ''

  if (!mac) {
    return null
  }

  return mac.replace(/[^0-9a-fA-F]/g, '').toLowerCase()
}

/**
 * OnmsAssetRecord string columns that can be filtered directly via `assetRecord.<col>` FIQL,
 * with human-readable labels for the asset-filter dropdown. Geolocation-backed fields
 * (city/state/zip/country) are intentionally excluded — they are not direct assetRecord properties,
 * so an `assetRecord.city` query would be invalid.
 */
export const ASSET_COLUMN_OPTIONS: { value: string, label: string }[] = [
  { value: 'building', label: 'Building' },
  { value: 'floor', label: 'Floor' },
  { value: 'room', label: 'Room' },
  { value: 'rack', label: 'Rack' },
  { value: 'region', label: 'Region' },
  { value: 'division', label: 'Division' },
  { value: 'department', label: 'Department' },
  { value: 'category', label: 'Category' },
  { value: 'displayCategory', label: 'Display Category' },
  { value: 'circuitId', label: 'Circuit ID' }
]

/** Set of allowed asset column keys, used to validate inbound `assetColumn` params. */
export const ALLOWED_ASSET_COLUMNS = new Set(ASSET_COLUMN_OPTIONS.map(o => o.value))

/** Display label for an asset column key (falls back to the key itself). */
export const getAssetColumnLabel = (column: string): string =>
  ASSET_COLUMN_OPTIONS.find(o => o.value === column)?.label ?? column

/**
 * Returns true if the `nodesWithDownAggregateStatus` query param requests down-only nodes.
 */
export const parseDownAggregateStatus = (queryObject: any): boolean => {
  return String(queryObject.nodesWithDownAggregateStatus ?? '').toLowerCase() === 'true'
}

/**
 * Returns true if the `nodesWithAssets` query param requests only nodes that have asset info.
 */
export const parseNodesWithAssets = (queryObject: any): boolean => {
  return String(queryObject.nodesWithAssets ?? '').toLowerCase() === 'true'
}

/**
 * Parses asset-field filters from `assetColumn` + `assetValue` query params.
 * Each param may be a single value (vue-router string) or repeated (array); the two are paired
 * by index. Pairs with an empty value or a column not in ALLOWED_ASSET_COLUMNS are skipped, and
 * duplicate columns keep the last value. Returns [] when none are valid.
 */
export const parseAssetFilters = (queryObject: any): AssetFilter[] => {
  const toArray = (v: any): string[] =>
    Array.isArray(v) ? (v as string[]) : v ? [v as string] : []

  const columns = toArray(queryObject.assetColumn)
  const values = toArray(queryObject.assetValue)

  const byColumn = new Map<string, string>()
  const count = Math.min(columns.length, values.length)
  for (let i = 0; i < count; i++) {
    const column = columns[i]
    const value = values[i]
    if (column && value && ALLOWED_ASSET_COLUMNS.has(column)) {
      byColumn.set(column, value)
    }
  }

  return Array.from(byColumn, ([column, value]) => ({ column, value }))
}

/**
 * Maps monitoredService or service query params to canonical service names for FIQL filtering.
 * Resolves by exact or case-insensitive name match, or by numeric ID lookup via serviceTypes.
 * monitoredService takes precedence over service when both are present.
 */
export const parseMonitoredServices = (
  queryObject: any,
  serviceTypes: ServiceType[]
): string[] => {
  const raw = queryObject.monitoredService ?? queryObject.service
  if (!raw) {
    return []
  }

  const resolve = (val: string): string | null => {
    if (/^\d+$/.test(val)) {
      const found = serviceTypes.find(s => s.id === parseInt(val))
      return found ? found.name : null
    }
    const lower = val.toLowerCase()
    const found = serviceTypes.find(s => s.name.toLowerCase() === lower)
    return found ? found.name : null
  }

  const values: string[] = Array.isArray(raw) ? raw : [raw as string]
  return values.map(v => resolve(v)).filter((n): n is string => n !== null)
}
