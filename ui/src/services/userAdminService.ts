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
import { ManagedUser, ManagedUserCreate } from '@/types/userAdmin'
import { ifMatch, v2 } from './axiosInstances'

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/users'

// A 412 is reported by flag only; the store shows the stale message once the list has been reloaded.
const writeError = (err: any, fallback: string): AdminWriteError => {
  if (err?.response?.status === 412) {
    return { message: '', stale: true }
  }
  const detail = err?.response?.data
  return { message: typeof detail === 'string' && detail ? detail : fallback, stale: false }
}

// null on failure (not []) so callers can keep showing the previous list
const getManagedUsers = async (): Promise<ManagedUser[] | null> => {
  try {
    startSpinner()
    const resp = await v2.get(endpoint)
    return Array.isArray(resp.data) ? resp.data : []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load users.' })
    return null
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

const createManagedUser = async (user: ManagedUserCreate): Promise<AdminWriteError | null> => {
  try {
    startSpinner()
    await v2.post(endpoint, user)
    showSnackBar({ msg: `User '${user.userId}' created.` })
    return null
  } catch (err: any) {
    const error = writeError(err, `Failed to create user '${user.userId}'.`)
    if (!error.stale) {
      showSnackBar({ msg: error.message, error: true })
    }
    return error
  } finally {
    stopSpinner()
  }
}

const updateManagedUser = async (user: ManagedUser): Promise<AdminWriteError | null> => {
  try {
    startSpinner()
    await v2.put(`${endpoint}/${encodeURIComponent(user.userId)}`, user, ifMatch(user.version))
    showSnackBar({ msg: `User '${user.userId}' updated.` })
    return null
  } catch (err: any) {
    const error = writeError(err, `Failed to update user '${user.userId}'.`)
    if (!error.stale) {
      showSnackBar({ msg: error.message, error: true })
    }
    return error
  } finally {
    stopSpinner()
  }
}

const setManagedUserPassword = async (userId: string, password: string, version: number): Promise<AdminWriteError | null> => {
  try {
    startSpinner()
    await v2.put(`${endpoint}/${encodeURIComponent(userId)}/password`, { password }, ifMatch(version))
    showSnackBar({ msg: `Password changed for '${userId}'.` })
    return null
  } catch (err: any) {
    const error = writeError(err, `Failed to change the password for '${userId}'.`)
    if (!error.stale) {
      showSnackBar({ msg: error.message, error: true })
    }
    return error
  } finally {
    stopSpinner()
  }
}

const renameManagedUser = async (userId: string, newUserId: string, version: number): Promise<AdminWriteError | null> => {
  try {
    startSpinner()
    await v2.post(`${endpoint}/${encodeURIComponent(userId)}/rename`, { newUserId }, ifMatch(version))
    showSnackBar({ msg: `User '${userId}' renamed to '${newUserId}'.` })
    return null
  } catch (err: any) {
    const error = writeError(err, `Failed to rename user '${userId}'.`)
    if (!error.stale) {
      showSnackBar({ msg: error.message, error: true })
    }
    return error
  } finally {
    stopSpinner()
  }
}

const deleteManagedUser = async (userId: string, version: number): Promise<AdminWriteError | null> => {
  try {
    startSpinner()
    await v2.delete(`${endpoint}/${encodeURIComponent(userId)}`, ifMatch(version))
    showSnackBar({ msg: `User '${userId}' deleted.` })
    return null
  } catch (err: any) {
    const error = writeError(err, `Failed to delete user '${userId}'.`)
    if (!error.stale) {
      showSnackBar({ msg: error.message, error: true })
    }
    return error
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
