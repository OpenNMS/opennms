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

import { MonitoringLocation } from '@/types'
import { createFailureResult, createSuccessResponse, ValidationResult } from '@/types/validation'
import { v2 } from './axiosInstances'

// PrimeVue Manage Monitoring Locations (NMS-20129). Reuses the existing v2
// AbstractDaoRestService CRUD at /api/v2/monitoringLocations — no backend or
// XML change. The v1 REST and the JSON contract stay as they are.

const endpoint = '/monitoringLocations'

// Only surface a server detail from a 4xx that looks like a short, plain message;
// a 500 carries an HTML page or a raw persistence exception, neither for the user.
const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  const status = Number(err?.response?.status ?? 0)
  if (typeof detail === 'string' && status >= 400 && status < 500) {
    const trimmed = detail.trim()
    if (trimmed && trimmed.length <= 200 && !/[<>]/.test(trimmed)) {
      return trimmed
    }
  }
  return fallback
}

// Bound the fetch instead of limit=0 (unbounded). Locations are few in practice,
// so the cap is a safety net; the caller surfaces a note if it is ever hit.
const LIST_CAP = 2000

// null on failure (not []) so callers can keep showing the previous list
const listMonitoringLocations = async (): Promise<{ locations: MonitoringLocation[]; totalCount: number } | null> => {
  try {
    const resp = await v2.get(`${endpoint}?limit=${LIST_CAP}`)
    const raw = resp.data?.location ?? []
    const locations = Array.isArray(raw) ? raw : [raw]
    return { locations, totalCount: resp.data?.totalCount ?? locations.length }
  } catch (err) {
    console.error('Error loading monitoring locations:', err)
    return null
  }
}

const createMonitoringLocation = async (location: MonitoringLocation): Promise<ValidationResult> => {
  try {
    await v2.post(endpoint, location)
    return createSuccessResponse()
  } catch (err: any) {
    console.error('Error creating monitoring location:', err)
    return createFailureResult(errorMessage(err, `Failed to create monitoring location '${location['location-name']}'.`))
  }
}

// The editable fields on this page. The v2 doUpdate does a full replace, so we
// read the current server row first and patch only these — otherwise a stale
// page snapshot would clobber fields this page never edits (e.g. tags) that
// changed concurrently.
const EDITABLE_FIELDS = ['location-name', 'monitoring-area', 'geolocation', 'priority', 'latitude', 'longitude'] as const

// the v2 doUpdate requires a JSON body whose location-name matches the path id
const updateMonitoringLocation = async (location: MonitoringLocation): Promise<ValidationResult> => {
  const name = location['location-name']
  const path = `${endpoint}/${encodeURIComponent(name)}`
  try {
    const current = (await v2.get(path))?.data ?? {}
    const body: Record<string, unknown> = { ...current }
    const source = location as unknown as Record<string, unknown>
    for (const field of EDITABLE_FIELDS) {
      body[field] = source[field]
    }
    await v2.put(path, body)
    return createSuccessResponse()
  } catch (err: any) {
    console.error('Error updating monitoring location:', err)
    return createFailureResult(errorMessage(err, `Failed to update monitoring location '${name}'.`))
  }
}

// node.location has a foreign key without ON DELETE CASCADE, so the server
// rejects the delete (with an HTML 500 page, hidden by the scrubber) while
// any node is still assigned; the fallback message says so.
const deleteMonitoringLocation = async (name: string): Promise<ValidationResult> => {
  try {
    await v2.delete(`${endpoint}/${encodeURIComponent(name)}`)
    return createSuccessResponse()
  } catch (err: any) {
    console.error('Error deleting monitoring location:', err)
    return createFailureResult(errorMessage(err, `Monitoring location '${name}' could not be deleted. Make sure no nodes are assigned to it.`))
  }
}

export {
  createMonitoringLocation,
  deleteMonitoringLocation,
  listMonitoringLocations,
  updateMonitoringLocation
}
