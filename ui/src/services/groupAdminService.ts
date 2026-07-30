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
import { ManagedGroup } from '@/types/groupAdmin'
import { rest, v2 } from './axiosInstances'

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/groups'

const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  return typeof detail === 'string' && detail ? detail : fallback
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
    const users = resp.data?.user ?? []
    return (Array.isArray(users) ? users : [users]).map((u: any) => u['user-id']).filter(Boolean)
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load users.' })
    return []
  }
}

const createManagedGroup = async (group: ManagedGroup): Promise<string | null> => {
  try {
    startSpinner()
    await v2.post(endpoint, group)
    showSnackBar({ msg: `Group '${group.name}' created.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to create group '${group.name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

const updateManagedGroup = async (group: ManagedGroup): Promise<string | null> => {
  try {
    startSpinner()
    await v2.put(`${endpoint}/${encodeURIComponent(group.name)}`, group)
    showSnackBar({ msg: `Group '${group.name}' updated.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to update group '${group.name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

const renameManagedGroup = async (name: string, newName: string): Promise<string | null> => {
  try {
    startSpinner()
    await v2.post(`${endpoint}/${encodeURIComponent(name)}/rename`, { 'new-name': newName })
    showSnackBar({ msg: `Group '${name}' renamed to '${newName}'.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to rename group '${name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

const deleteManagedGroup = async (name: string): Promise<string | null> => {
  try {
    startSpinner()
    await v2.delete(`${endpoint}/${encodeURIComponent(name)}`)
    showSnackBar({ msg: `Group '${name}' deleted.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to delete group '${name}'.`)
    showSnackBar({ msg, error: true })
    return msg
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
