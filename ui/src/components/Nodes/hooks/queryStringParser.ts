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
  SetOperator
} from '@/types'
import { isIP } from 'is-ip'

/** Parse node label from a vue-router route.query object */
export const parseNodeLabel = (queryObject: any) => {
  return queryObject.nodename as string || queryObject.nodeLabel as string || ''
}

/**
 * Parse categories from a vue-router route.query object.
 * The route.query 'categories' string can be a comma- or semicolon-separated list of either
 * numeric Category ids or names.
 * comma: Union; semicolon: Intersection
 * 
 * @returns The category mode and categories parsed from the queryObject. If 'selectedCategories' is empty,
 * it means no categories were present.
 */
export const parseCategories = (queryObject: any, categories: Category[]) => {
  let categoryMode: SetOperator = SetOperator.Union
  const selectedCategories: Category[] = []

  const queryCategories = queryObject.categories as string ?? ''

  if (categories.length > 0) {
    categoryMode = queryCategories.includes(';') ? SetOperator.Intersection : SetOperator.Union

    const cats: string[] = queryCategories.replace(/;/g, ',').split(',')

    // add any valid categories
    cats.forEach(c => {
      if (/\d+/.test(c)) {
        // category id number
        const id = parseInt(c)

        const item = categories.find(x => x.id === id)

        if (item) {
          selectedCategories.push(item)
        }
      } else {
        // category name, case insensitive
        const item = categories.find(x => x.name.toLowerCase() === c.toLowerCase())

        if (item) {
          selectedCategories.push(item)
        }
      }
    })
  }

  return {
    categoryMode,
    selectedCategories
  }
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
  const foreignSource = queryObject.foreignSource || ''
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
