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
import { Minion, MinionEdit } from '@/types/minionAdmin'
import { createFailureResult, createSuccessResponse, ValidationResult } from '@/types/validation'
import { rest, v2 } from './axiosInstances'

// Uses the existing v2 AbstractDaoRestService CRUD at /api/v2/minions.

const endpoint = '/minions'

// Only surface a server detail if it looks like a short, plain message — a 500
// often returns a servlet HTML error page, which must not be shown verbatim.
const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  if (typeof detail === 'string') {
    const trimmed = detail.trim()
    if (trimmed && trimmed.length <= 200 && !/[<>]/.test(trimmed)) {
      return trimmed
    }
  }
  return fallback
}

// null on failure (not []) so callers can keep showing the previous list
// Bound the fetch rather than limit=0 (unbounded); minions are few in practice.
const LIST_CAP = 2000

const listMinions = async (): Promise<{ minions: Minion[]; totalCount: number } | null> => {
  try {
    const resp = await v2.get(`${endpoint}?limit=${LIST_CAP}&orderBy=label`)
    if (resp.status === 204) {
      return { minions: [], totalCount: 0 }
    }
    const raw = resp.data?.minion ?? []
    const minions = Array.isArray(raw) ? raw : [raw]
    return { minions, totalCount: resp.data?.totalCount ?? minions.length }
  } catch (err) {
    console.error('Error loading minions:', err)
    return null
  }
}

// Each minion auto-registers a requisition node whose foreignId is the minion id.
// Resolve them and key by id+location (ids repeat across locations), mirroring
// the legacy page so a minion's ID can link to its node. Best-effort: the link
// is a convenience, so a failure just yields no links for that chunk.
const minionNodeKey = (id: string, location: string | null) => `${id}\u0000${location ?? ''}`

// One "foreignId==<id>" clause per minion, ~54 bytes each once encoded, so the
// URL stays well inside an 8 KB request line however many minions exist.
export const NODE_LOOKUP_CHUNK = 50

// Ids are free text on the write path (in practice UUIDs). Anything that is
// meaningful to the FIQL grammar (, ; ( ) = ! ~ < > and whitespace) would change
// the query, so such ids are left out of the lookup and simply get no link.
const FIQL_SAFE_ID = /^[A-Za-z0-9._:@+/-]+$/
export const isFiqlSafeId = (id: string): boolean => FIQL_SAFE_ID.test(id)

const lookupNodeIds = async (ids: string[]): Promise<Record<string, number>> => {
  const fiql = '(' + ids.map(id => `foreignId==${id}`).join(',') + ')'
  const resp = await v2.get(`/nodes?limit=${LIST_CAP}&_s=${encodeURIComponent(fiql)}`)
  if (resp.status === 204) {
    return {}
  }
  const raw = resp.data?.node ?? []
  const nodes = Array.isArray(raw) ? raw : [raw]
  const map: Record<string, number> = {}
  for (const n of nodes) {
    if (n.foreignId != null) {
      map[minionNodeKey(String(n.foreignId), n.location ?? null)] = Number(n.id)
    }
  }
  return map
}

const getMinionNodeIds = async (minions: Minion[]): Promise<Record<string, number>> => {
  const ids = [...new Set(minions.map(m => m.id).filter(isFiqlSafeId))]
  const map: Record<string, number> = {}
  for (let i = 0; i < ids.length; i += NODE_LOOKUP_CHUNK) {
    try {
      Object.assign(map, await lookupNodeIds(ids.slice(i, i + NODE_LOOKUP_CHUNK)))
    } catch (err) {
      console.error('Error resolving minion nodes:', err)
    }
  }
  return map
}

// Read-before-write: the v2 PUT is a whole-object saveOrUpdate, so we must send
// the CURRENT server row with only label/location/properties changed — spreading
// a stale list snapshot would revert the server-maintained status/version/date.
const updateMinion = async (edit: MinionEdit): Promise<ValidationResult> => {
  try {
    const current = await v2.get(`${endpoint}/${encodeURIComponent(edit.id)}`)
    const fresh = (current.data ?? {}) as Minion
    const payload: Minion = {
      ...fresh,
      label: edit.label,
      location: edit.location,
      properties: edit.properties
    }
    await v2.put(`${endpoint}/${encodeURIComponent(edit.id)}`, payload)
    return createSuccessResponse()
  } catch (err: any) {
    console.error('Error updating minion:', err)
    return createFailureResult(errorMessage(err, `Failed to update minion '${edit.label ?? edit.id}'.`))
  }
}

const deleteMinion = async (id: string): Promise<ValidationResult> => {
  try {
    await v2.delete(`${endpoint}/${encodeURIComponent(id)}`)
    return createSuccessResponse()
  } catch (err: any) {
    // already gone (another admin deleted it): treat as success so the row clears
    if (err?.response?.status === 404) {
      return createSuccessResponse()
    }
    console.error('Error deleting minion:', err)
    return createFailureResult(errorMessage(err, `Failed to delete minion '${id}'.`))
  }
}

// The core's own version from /rest/info ("37.0.0", no qualifier). Minions report
// VersionBean.toString() ("v37.0.0-SNAPSHOT"), so callers compare through
// lib/version rather than verbatim.
const getCoreVersion = async (): Promise<string | null> => {
  try {
    const resp = await rest.get('/info')
    const version = resp.data?.version
    return typeof version === 'string' && version.trim() ? version.trim() : null
  } catch (err) {
    console.error('Error loading the core version:', err)
    return null
  }
}

export { deleteMinion, getCoreVersion, getMinionNodeIds, listMinions, minionNodeKey, updateMinion }
