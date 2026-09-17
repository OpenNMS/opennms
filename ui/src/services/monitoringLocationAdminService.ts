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

import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'
import { MonitoringLocation } from '@/types'
import { v2 } from './axiosInstances'

// PrimeVue Manage Monitoring Locations (NMS-20129). Reuses the existing v2
// AbstractDaoRestService CRUD at /api/v2/monitoringLocations — no backend or
// XML change. The v1 REST and the JSON contract stay as they are.

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/monitoringLocations'

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
// Bound the fetch instead of limit=0 (unbounded). Locations are few in practice,
// so the cap is a safety net; the caller surfaces a note if it is ever hit.
const LIST_CAP = 2000

const listMonitoringLocations = async (): Promise<{ locations: MonitoringLocation[]; totalCount: number } | null> => {
  try {
    startSpinner()
    const resp = await v2.get(`${endpoint}?limit=${LIST_CAP}`)
    const raw = resp.data?.location ?? []
    const locations = Array.isArray(raw) ? raw : [raw]
    return { locations, totalCount: resp.data?.totalCount ?? locations.length }
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load monitoring locations.' })
    return null
  } finally {
    stopSpinner()
  }
}

const createMonitoringLocation = async (location: MonitoringLocation): Promise<string | null> => {
  try {
    startSpinner()
    await v2.post(endpoint, location)
    showSnackBar({ msg: `Monitoring location '${location['location-name']}' created.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to create monitoring location '${location['location-name']}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

// The editable fields on this page. The v2 doUpdate does a full replace, so we
// read the current server row first and patch only these — otherwise a stale
// page snapshot would clobber fields this page never edits (e.g. tags) that
// changed concurrently.
const EDITABLE_FIELDS = ['location-name', 'monitoring-area', 'geolocation', 'priority', 'latitude', 'longitude'] as const

// the v2 doUpdate requires a JSON body whose location-name matches the path id
const updateMonitoringLocation = async (location: MonitoringLocation): Promise<string | null> => {
  const name = location['location-name']
  const path = `${endpoint}/${encodeURIComponent(name)}`
  try {
    startSpinner()
    const current = (await v2.get(path))?.data ?? {}
    const body: Record<string, unknown> = { ...current }
    const source = location as unknown as Record<string, unknown>
    for (const field of EDITABLE_FIELDS) {
      body[field] = source[field]
    }
    await v2.put(path, body)
    showSnackBar({ msg: `Monitoring location '${name}' updated.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to update monitoring location '${name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

const deleteMonitoringLocation = async (name: string): Promise<string | null> => {
  try {
    startSpinner()
    await v2.delete(`${endpoint}/${encodeURIComponent(name)}`)
    showSnackBar({ msg: `Monitoring location '${name}' deleted.` })
    return null
  } catch (err: any) {
    // already gone — the desired end-state holds, so treat it as success
    if (err?.response?.status === 404) {
      showSnackBar({ msg: `Monitoring location '${name}' deleted.` })
      return null
    }
    const msg = errorMessage(err, `Failed to delete monitoring location '${name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

export {
  createMonitoringLocation,
  deleteMonitoringLocation,
  listMonitoringLocations,
  updateMonitoringLocation
}
