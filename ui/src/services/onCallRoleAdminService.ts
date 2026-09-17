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
import { OnCallCalendar, OnCallRole } from '@/types/onCallRoleAdmin'
import { rest, v2 } from './axiosInstances'

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/on-call-roles'

const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  return typeof detail === 'string' && detail ? detail : fallback
}

// null on failure (not []) so callers can keep showing the previous list
const listOnCallRoles = async (): Promise<OnCallRole[] | null> => {
  try {
    startSpinner()
    const resp = await v2.get(endpoint)
    return Array.isArray(resp.data) ? resp.data : []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load on-call roles.' })
    return null
  } finally {
    stopSpinner()
  }
}

const getOnCallRole = async (name: string): Promise<OnCallRole | null> => {
  try {
    startSpinner()
    const resp = await v2.get(`${endpoint}/${encodeURIComponent(name)}`)
    return resp.data ?? null
  } catch (_err) {
    showSnackBar({ msg: `Failed to load on-call role '${name}'.` })
    return null
  } finally {
    stopSpinner()
  }
}

const getOnCallCalendar = async (name: string, year: number, month: number): Promise<OnCallCalendar | null> => {
  try {
    startSpinner()
    const resp = await v2.get(`${endpoint}/${encodeURIComponent(name)}/calendar?year=${year}&month=${month}`)
    return resp.data ?? null
  } catch (_err) {
    showSnackBar({ msg: `Failed to load the on-call calendar for '${name}'.` })
    return null
  } finally {
    stopSpinner()
  }
}

const createOnCallRole = async (role: OnCallRole): Promise<string | null> => {
  try {
    startSpinner()
    await v2.post(endpoint, role)
    showSnackBar({ msg: `On-call role '${role.name}' created.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to create on-call role '${role.name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

const updateOnCallRole = async (role: OnCallRole): Promise<string | null> => {
  try {
    startSpinner()
    await v2.put(`${endpoint}/${encodeURIComponent(role.name)}`, role)
    showSnackBar({ msg: `On-call role '${role.name}' updated.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to update on-call role '${role.name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

const renameOnCallRole = async (name: string, newName: string): Promise<string | null> => {
  try {
    startSpinner()
    await v2.post(`${endpoint}/${encodeURIComponent(name)}/rename`, { 'new-name': newName })
    showSnackBar({ msg: `On-call role '${name}' renamed to '${newName}'.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to rename on-call role '${name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

const deleteOnCallRole = async (name: string): Promise<string | null> => {
  try {
    startSpinner()
    await v2.delete(`${endpoint}/${encodeURIComponent(name)}`)
    showSnackBar({ msg: `On-call role '${name}' deleted.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to delete on-call role '${name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

// picker options via the v1 endpoints that exist on every install
const getOnCallSupervisorCandidates = async (): Promise<string[]> => {
  try {
    const resp = await rest.get('/users?limit=0')
    const users = resp.data?.user ?? []
    return (Array.isArray(users) ? users : [users]).map((u: any) => u['user-id']).filter(Boolean)
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load users.' })
    return []
  }
}

const getOnCallGroupCandidates = async (): Promise<Record<string, string[]>> => {
  try {
    const resp = await rest.get('/groups?limit=0')
    const groups = resp.data?.group ?? []
    const result: Record<string, string[]> = {}
    for (const group of Array.isArray(groups) ? groups : [groups]) {
      if (group?.name) {
        const users = group.user ?? []
        result[group.name] = Array.isArray(users) ? users : [users]
      }
    }
    return result
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load groups.' })
    return {}
  }
}

export {
  createOnCallRole,
  deleteOnCallRole,
  getOnCallCalendar,
  getOnCallGroupCandidates,
  getOnCallRole,
  getOnCallSupervisorCandidates,
  listOnCallRoles,
  renameOnCallRole,
  updateOnCallRole
}
