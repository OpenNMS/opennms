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
    return {
      notifications: !!resp.data?.notifications,
      pollers: asArray<PackageRef>(resp.data?.pollers),
      collectors: asArray<PackageRef>(resp.data?.collectors),
      thresholders: asArray<PackageRef>(resp.data?.thresholders)
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

// Node label autocomplete for the node picker (id + label).
export const searchOutageNodes = async (query: string): Promise<{ id: number, label: string }[]> => {
  try {
    const term = sanitizeSearchTerm(query)
    const filter = term ? `&_s=node.label==*${term}*` : ''
    const resp = await v2.get(`/nodes?limit=200${filter}`)
    return asArray<any>(resp.data?.node)
      .map(n => ({ id: Number(n.id), label: n.label as string }))
      .filter(n => !Number.isNaN(n.id))
  } catch (_err) {
    return []
  }
}

// IP interface autocomplete for the interface picker.
export const searchOutageInterfaces = async (query: string): Promise<{ address: string, nodeLabel: string }[]> => {
  try {
    const term = sanitizeSearchTerm(query)
    const filter = term ? `&_s=ipInterface.ipAddress==*${term}*` : ''
    const resp = await v2.get(`/ipinterfaces?limit=200${filter}`)
    return asArray<any>(resp.data?.ipInterface)
      .map(i => ({ address: i.ipAddress as string, nodeLabel: i.nodeLabel ?? '' }))
      .filter(i => !!i.address)
  } catch (_err) {
    return []
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
