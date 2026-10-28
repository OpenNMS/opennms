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
import { AdminWriteError } from '@/types/adminWrite'
import { ManagedGroup, ManagedGroupCreate } from '@/types/groupAdmin'
import { ifMatch, rest, v2 } from './axiosInstances'

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/groups'

// A 412 is reported by flag only; the store shows the stale message once the list has been reloaded.
const writeError = (err: any, fallback: string): AdminWriteError => {
  if (err?.response?.status === 412) {
    return { message: '', stale: true }
  }
  const detail = err?.response?.data
  return { message: typeof detail === 'string' && detail ? detail : fallback, stale: false }
}

// null on failure (not []) so callers can keep showing the previous list
const getManagedGroups = async (): Promise<ManagedGroup[] | null> => {
  try {
    startSpinner()
    const resp = await v2.get(endpoint)
    return Array.isArray(resp.data) ? resp.data : []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load groups.' })
    return null
  } finally {
    stopSpinner()
  }
}

// member picker options; the v1 users endpoint exists on every install
const getGroupMemberCandidates = async (): Promise<string[]> => {
  try {
    const resp = await rest.get('/users?limit=0')
    // v1 OnmsUserList serializes the array under "user" (@JsonProperty("user")),
    // and a single-element list may deserialize as one object, not an array
    const users = resp.data?.user ?? []
    return (Array.isArray(users) ? users : [users]).map((u: any) => u['user-id']).filter(Boolean)
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load users.' })
    return []
  }
}

const createManagedGroup = async (group: ManagedGroupCreate): Promise<AdminWriteError | null> => {
  try {
    startSpinner()
    await v2.post(endpoint, group)
    showSnackBar({ msg: `Group '${group.name}' created.` })
    return null
  } catch (err: any) {
    const error = writeError(err, `Failed to create group '${group.name}'.`)
    if (!error.stale) {
      showSnackBar({ msg: error.message, error: true })
    }
    return error
  } finally {
    stopSpinner()
  }
}

const updateManagedGroup = async (group: ManagedGroup): Promise<AdminWriteError | null> => {
  try {
    startSpinner()
    await v2.put(`${endpoint}/${encodeURIComponent(group.name)}`, group, ifMatch(group.version))
    showSnackBar({ msg: `Group '${group.name}' updated.` })
    return null
  } catch (err: any) {
    const error = writeError(err, `Failed to update group '${group.name}'.`)
    if (!error.stale) {
      showSnackBar({ msg: error.message, error: true })
    }
    return error
  } finally {
    stopSpinner()
  }
}

const renameManagedGroup = async (name: string, newName: string, version: number): Promise<AdminWriteError | null> => {
  try {
    startSpinner()
    await v2.post(`${endpoint}/${encodeURIComponent(name)}/rename`, { newName }, ifMatch(version))
    showSnackBar({ msg: `Group '${name}' renamed to '${newName}'.` })
    return null
  } catch (err: any) {
    const error = writeError(err, `Failed to rename group '${name}'.`)
    if (!error.stale) {
      showSnackBar({ msg: error.message, error: true })
    }
    return error
  } finally {
    stopSpinner()
  }
}

const deleteManagedGroup = async (name: string, version: number): Promise<AdminWriteError | null> => {
  try {
    startSpinner()
    await v2.delete(`${endpoint}/${encodeURIComponent(name)}`, ifMatch(version))
    showSnackBar({ msg: `Group '${name}' deleted.` })
    return null
  } catch (err: any) {
    const error = writeError(err, `Failed to delete group '${name}'.`)
    if (!error.stale) {
      showSnackBar({ msg: error.message, error: true })
    }
    return error
  } finally {
    stopSpinner()
  }
}

export {
  createManagedGroup,
  deleteManagedGroup,
  getGroupMemberCandidates,
  getManagedGroups,
  renameManagedGroup,
  updateManagedGroup
}
