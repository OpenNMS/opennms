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
    vals.forEach(c => {
      if (!c) return
      if (/^\d+$/.test(c)) {
        const item = categories.find(x => x.id === parseInt(c))
        if (item) result.push(item)
      } else {
        const item = categories.find(x => x.name.toLowerCase() === c.toLowerCase())
        if (item) result.push(item)
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
  }

  // TODO: we don't yet have support for excluding flows, i.e. if queryObject.flows === 'false'

  return []
}

/**
 * Currently this accepts anything in any valid IPv4 or IPv6 format (see `is-ip`), but
 * some formats may not actually be supported by our FIQL search.
 */
export const parseIplike = (queryObject: any) => {
  const ip = queryObject.iplike as string || queryObject.ipAddress as string || ''

  if (ip && isIP(ip)) {
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
 * suitable for wildcard FIQL matching against snmpInterface.physAddr.
 * Colons and dashes are stripped to match the format stored in the database.
 */
export const parseMaclike = (queryObject: any): string | null => {
  const mac = queryObject.maclike as string || queryObject.snmpphysaddr as string || ''

  if (!mac) {
    return null
  }

  return mac.replace(/[:-]/g, '').toLowerCase()
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
  if (!raw) return []

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
