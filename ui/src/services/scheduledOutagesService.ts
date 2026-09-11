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

import { OutageApplicability, PackageRef, ScheduledOutage } from '@/types/scheduledOutage'
import { rest, v2 } from './axiosInstances'

const endpoint = '/sched-outages'

// v1/JAXB JSON collapses a single-element list to one object; normalize to []
export const asArray = <T>(value: T | T[] | undefined | null): T[] => {
  if (value == null) {
    return []
  }
  return Array.isArray(value) ? value : [value]
}

export const scheduledOutageErrorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  return typeof detail === 'string' && detail ? detail : fallback
}

const seg = (value: string): string => encodeURIComponent(value)

// Strip FIQL/URL metacharacters so a typed query can't break out of the
// `node.label==*<term>*` filter or inject extra query params. Node labels and
// IP addresses only contain these characters anyway.
const sanitizeSearchTerm = (query: string): string => query.replace(/[^a-zA-Z0-9._:-]/g, '')

// null (not []) on failure so the list page can keep the previous rows
export const getScheduledOutages = async (): Promise<ScheduledOutage[] | null> => {
  try {
    const resp = await rest.get(endpoint)
    return asArray<ScheduledOutage>(resp.data?.outage).map(normalizeOutage)
  } catch (_err) {
    return null
  }
}

export const getScheduledOutage = async (name: string): Promise<ScheduledOutage | null> => {
  try {
    const resp = await rest.get(`${endpoint}/${seg(name)}`)
    return normalizeOutage(resp.data)
  } catch (_err) {
    return null
  }
}

// POST adds a new outage or updates an existing one (matched by name).
export const saveScheduledOutage = async (outage: ScheduledOutage): Promise<void> => {
  await rest.post(endpoint, outage)
}

export const deleteScheduledOutage = async (name: string): Promise<void> => {
  await rest.delete(`${endpoint}/${seg(name)}`)
}

export const getOutageApplicability = async (name?: string): Promise<OutageApplicability | null> => {
  try {
    const path = name ? `${endpoint}/${seg(name)}/applies-to` : `${endpoint}/applies-to`
    const resp = await rest.get(path)
    const packages = (value: unknown): PackageRef[] =>
      asArray<any>(value).map(p => ({
        name: p?.name,
        applied: !!p?.applied,
        calendars: asArray<string>(p?.calendars)
      }))
    return {
      notifications: !!resp.data?.notifications,
      notificationCalendars: asArray<string>(resp.data?.['notification-calendars']),
      pollers: packages(resp.data?.pollers),
      collectors: packages(resp.data?.collectors),
      thresholders: packages(resp.data?.thresholders)
    }
  } catch (_err) {
    return null
  }
}

type Subsystem = 'pollerd' | 'collectd' | 'threshd'

export const setPackageMembership = async (
  subsystem: Subsystem,
  outageName: string,
  packageName: string,
  applied: boolean
): Promise<void> => {
  const path = `${endpoint}/${seg(outageName)}/${subsystem}/${seg(packageName)}`
  if (applied) {
    await rest.put(path)
  } else {
    await rest.delete(path)
  }
}

export const setNotificationMembership = async (outageName: string, applied: boolean): Promise<void> => {
  const path = `${endpoint}/${seg(outageName)}/notifd`
  if (applied) {
    await rest.put(path)
  } else {
    await rest.delete(path)
  }
}

// Node label autocomplete for the node picker (id + label). The v1 endpoint is
// used because its ilike comparator gives a case-insensitive substring match,
// which v2 FIQL wildcards (case-sensitive LIKE) cannot express.
export const searchOutageNodes = async (query: string): Promise<{ id: number, label: string }[]> => {
  try {
    const term = sanitizeSearchTerm(query)
    const filter = term ? `&comparator=ilike&label=${encodeURIComponent(`%${term}%`)}` : ''
    const resp = await rest.get(`/nodes?limit=200${filter}`)
    return asArray<any>(resp.data?.node)
      .map(n => ({ id: Number(n.id), label: n.label as string }))
      .filter(n => !Number.isNaN(n.id))
  } catch (_err) {
    return []
  }
}

// A complete IPv4 or IPv6 address, i.e. something ipAddress== can match exactly.
export const isCompleteIpAddress = (s: string): boolean => {
  if (/^(\d{1,3}\.){3}\d{1,3}$/.test(s)) {
    return s.split('.').every(octet => Number(octet) <= 255)
  }
  return s.includes(':') && s.length >= 3 && /^[0-9a-fA-F:]+$/.test(s)
}

// A partial dotted IPv4 (192, 192.168., 192.168.1) as the iplike pattern that
// matches every address under it; null for anything else.
export const ipv4PrefixPattern = (term: string): string | null => {
  if (!/^\d{1,3}(\.\d{1,3}){0,2}\.?$/.test(term)) {
    return null
  }
  const octets = term.split('.').filter(o => o !== '')
  if (octets.some(o => Number(o) > 255)) {
    return null
  }
  return [...octets, ...Array(4 - octets.length).fill('*')].join('.')
}

// IP interface autocomplete for the interface picker. A complete IP queries
// ipAddress exactly and is always offered as a suggestion itself, since
// poll-outages accepts any valid address whether or not it is in inventory.
// A partial IPv4 also searches ipAddress as an iplike pattern, because
// ipHostName holds the hostname wherever reverse DNS resolved.
export const searchOutageInterfaces = async (query: string): Promise<{ address: string, nodeLabel: string }[]> => {
  const term = sanitizeSearchTerm(query)
  const isExactIp = isCompleteIpAddress(term)
  try {
    // there is no v1 ipinterfaces endpoint (no ilike), so hostname matching
    // ORs the raw and lowercased term — DNS names are stored lowercase, which
    // makes this case-insensitive in practice
    const hostFilters = [...new Set([term.toLowerCase(), term])]
      .map(t => `ipHostName==*${t}*`).join(',')
    const prefix = isExactIp ? null : ipv4PrefixPattern(term)
    const partialFilters = prefix ? `ipAddress==${prefix},${hostFilters}` : hostFilters
    const filter = term ? (isExactIp ? `&_s=ipAddress==${term}` : `&_s=${partialFilters}`) : ''
    const resp = await v2.get(`/ipinterfaces?limit=200${filter}`)
    const found = asArray<any>(resp.data?.ipInterface)
      .map(i => ({ address: i.ipAddress as string, nodeLabel: i.nodeLabel ?? '' }))
      .filter(i => !!i.address)
    if (isExactIp && !found.some(i => i.address === term)) {
      found.unshift({ address: term, nodeLabel: '' })
    }
    return found
  } catch (_err) {
    return isExactIp ? [{ address: term, nodeLabel: '' }] : []
  }
}

// Resolve node labels for the ids referenced by outages (single OR query).
export const getNodeLabels = async (ids: number[]): Promise<Record<number, string>> => {
  if (!ids.length) {
    return {}
  }
  try {
    const filter = ids.map(id => `id==${id}`).join(',')
    const resp = await v2.get(`/nodes?limit=${ids.length}&_s=${filter}`)
    const labels: Record<number, string> = {}
    for (const n of asArray<any>(resp.data?.node)) {
      labels[Number(n.id)] = n.label
    }
    return labels
  } catch (_err) {
    return {}
  }
}

// The v1 payload may arrive with single-element lists collapsed to objects.
const normalizeOutage = (raw: any): ScheduledOutage => ({
  name: raw?.name,
  type: raw?.type,
  time: asArray(raw?.time),
  node: asArray(raw?.node),
  interface: asArray(raw?.interface)
})
