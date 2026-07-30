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
import { ManagedUser, ManagedUserCreate } from '@/types/userAdmin'
import { v2 } from './axiosInstances'

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/users'

const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  return typeof detail === 'string' && detail ? detail : fallback
}

const getManagedUsers = async (): Promise<ManagedUser[]> => {
  try {
    startSpinner()
    const resp = await v2.get(endpoint)
    return Array.isArray(resp.data) ? resp.data : []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load users.' })
    return []
  } finally {
    stopSpinner()
  }
}

const getAvailableUserRoles = async (): Promise<string[]> => {
  try {
    const resp = await v2.get(`${endpoint}/available-roles`)
    return Array.isArray(resp.data) ? resp.data : []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load available roles.' })
    return []
  }
}

const createManagedUser = async (user: ManagedUserCreate): Promise<boolean> => {
  try {
    startSpinner()
    await v2.post(endpoint, user)
    showSnackBar({ msg: `User '${user['user-id']}' created.` })
    return true
  } catch (err: any) {
    showSnackBar({ msg: errorMessage(err, `Failed to create user '${user['user-id']}'.`) })
    return false
  } finally {
    stopSpinner()
  }
}

const updateManagedUser = async (user: ManagedUser): Promise<boolean> => {
  try {
    startSpinner()
    await v2.put(`${endpoint}/${encodeURIComponent(user['user-id'])}`, user)
    showSnackBar({ msg: `User '${user['user-id']}' updated.` })
    return true
  } catch (err: any) {
    showSnackBar({ msg: errorMessage(err, `Failed to update user '${user['user-id']}'.`) })
    return false
  } finally {
    stopSpinner()
  }
}

const setManagedUserPassword = async (userId: string, password: string): Promise<boolean> => {
  try {
    startSpinner()
    await v2.put(`${endpoint}/${encodeURIComponent(userId)}/password`, { password })
    showSnackBar({ msg: `Password changed for '${userId}'.` })
    return true
  } catch (err: any) {
    showSnackBar({ msg: errorMessage(err, `Failed to change the password for '${userId}'.`) })
    return false
  } finally {
    stopSpinner()
  }
}

const renameManagedUser = async (userId: string, newUserId: string): Promise<boolean> => {
  try {
    startSpinner()
    await v2.post(`${endpoint}/${encodeURIComponent(userId)}/rename`, { 'new-user-id': newUserId })
    showSnackBar({ msg: `User '${userId}' renamed to '${newUserId}'.` })
    return true
  } catch (err: any) {
    showSnackBar({ msg: errorMessage(err, `Failed to rename user '${userId}'.`) })
    return false
  } finally {
    stopSpinner()
  }
}

const deleteManagedUser = async (userId: string): Promise<boolean> => {
  try {
    startSpinner()
    await v2.delete(`${endpoint}/${encodeURIComponent(userId)}`)
    showSnackBar({ msg: `User '${userId}' deleted.` })
    return true
  } catch (err: any) {
    showSnackBar({ msg: errorMessage(err, `Failed to delete user '${userId}'.`) })
    return false
  } finally {
    stopSpinner()
  }
}

export {
  createManagedUser,
  deleteManagedUser,
  getAvailableUserRoles,
  getManagedUsers,
  renameManagedUser,
  setManagedUserPassword,
  updateManagedUser
}
