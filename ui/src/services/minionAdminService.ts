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
import { Minion } from '@/types/minionAdmin'
import { v2 } from './axiosInstances'

// PrimeVue Manage Minions (NMS-20128). Reuses the existing v2
// AbstractDaoRestService CRUD at /api/v2/minions — no backend change; the v1
// REST and the JSON contract are untouched.

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/minions'

// Only surface a server detail if it looks like a short, plain message — a 500
// often returns a servlet HTML error page, which must not be shown verbatim.
const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  if (typeof detail === 'string') {
    const trimmed = detail.trim()
    if (trimmed && trimmed.length <= 200 && !/[<>]/.test(trimmed)) return trimmed
  }
  return fallback
}

// null on failure (not []) so callers can keep showing the previous list
const listMinions = async (): Promise<Minion[] | null> => {
  try {
    startSpinner()
    const resp = await v2.get(`${endpoint}?limit=0&orderBy=label`)
    if (resp.status === 204) {
      return []
    }
    const raw = resp.data?.minion ?? []
    return Array.isArray(raw) ? raw : [raw]
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load minions.' })
    return null
  } finally {
    stopSpinner()
  }
}

export interface MinionEdit {
  id: string
  label: string | null
  location: string
  properties: Record<string, string>
}

// Read-before-write: the v2 PUT is a whole-object saveOrUpdate, so we must send
// the CURRENT server row with only label/location/properties changed — spreading
// a stale list snapshot would revert the server-maintained status/version/date.
const updateMinion = async (edit: MinionEdit): Promise<string | null> => {
  try {
    startSpinner()
    const current = await v2.get(`${endpoint}/${encodeURIComponent(edit.id)}`)
    const fresh = (current.data ?? {}) as Minion
    const payload: Minion = {
      ...fresh,
      label: edit.label,
      location: edit.location,
      properties: edit.properties
    }
    await v2.put(`${endpoint}/${encodeURIComponent(edit.id)}`, payload)
    showSnackBar({ msg: `Minion '${edit.label ?? edit.id}' updated.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to update minion '${edit.label ?? edit.id}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

const deleteMinion = async (id: string): Promise<string | null> => {
  try {
    startSpinner()
    await v2.delete(`${endpoint}/${encodeURIComponent(id)}`)
    showSnackBar({ msg: `Minion '${id}' deleted.` })
    return null
  } catch (err: any) {
    // already gone (another admin deleted it): treat as success so the row clears
    if (err?.response?.status === 404) {
      return null
    }
    const msg = errorMessage(err, `Failed to delete minion '${id}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

export { deleteMinion, listMinions, updateMinion }
