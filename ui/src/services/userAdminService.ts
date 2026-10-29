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

import { AdminWriteError } from '@/types/adminWrite'
import { ManagedUser, ManagedUserCreate } from '@/types/userAdmin'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { AdminItemTarget, adminItemUrl } from './adminItemUrl'
import { ifMatch, v2 } from './axiosInstances'

// Reads return a ValidationResultWithPayload and writes an AdminWriteError (null on
// success); the component that started the action shows any feedback.
const endpoint = '/users'

// the server's plain-text reason for a rejected request, else the fallback
const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  return typeof detail === 'string' && detail ? detail : fallback
}

// A 412 is reported by flag only; the store words the stale message once the list has been reloaded.
const writeError = (err: any, fallback: string): AdminWriteError => {
  if (err?.response?.status === 412) {
    return { message: '', stale: true }
  }
  return { message: errorMessage(err, fallback), stale: false }
}

// the If-Match header, plus the query of the /item form when the name needs it
const itemConfig = (target: AdminItemTarget, version: number) =>
  target.params ? { ...ifMatch(version), params: target.params } : ifMatch(version)

const getManagedUsers = async (): Promise<ValidationResultWithPayload<ManagedUser[]>> => {
  try {
    const resp = await v2.get(endpoint)
    return createResultWithPayload(true, '', Array.isArray(resp.data) ? resp.data : [])
  } catch (err: any) {
    return createResultWithPayload<ManagedUser[]>(false, errorMessage(err, 'Failed to load users.'))
  }
}

const getAvailableUserRoles = async (): Promise<ValidationResultWithPayload<string[]>> => {
  try {
    const resp = await v2.get(`${endpoint}/available-roles`)
    return createResultWithPayload(true, '', Array.isArray(resp.data) ? resp.data : [])
  } catch (err: any) {
    return createResultWithPayload<string[]>(false, errorMessage(err, 'Failed to load available roles.'))
  }
}

const createManagedUser = async (user: ManagedUserCreate): Promise<AdminWriteError | null> => {
  try {
    await v2.post(endpoint, user)
    return null
  } catch (err: any) {
    return writeError(err, `Failed to create user '${user.userId}'.`)
  }
}

const updateManagedUser = async (user: ManagedUser): Promise<AdminWriteError | null> => {
  try {
    const target = adminItemUrl('users', user.userId)
    await v2.put(target.url, user, itemConfig(target, user.version))
    return null
  } catch (err: any) {
    return writeError(err, `Failed to update user '${user.userId}'.`)
  }
}

const setManagedUserPassword = async (userId: string, password: string, version: number): Promise<AdminWriteError | null> => {
  try {
    const target = adminItemUrl('users', userId, 'password')
    await v2.put(target.url, { password }, itemConfig(target, version))
    return null
  } catch (err: any) {
    return writeError(err, `Failed to change the password for '${userId}'.`)
  }
}

const renameManagedUser = async (userId: string, newUserId: string, version: number): Promise<AdminWriteError | null> => {
  try {
    const target = adminItemUrl('users', userId, 'rename')
    await v2.post(target.url, { newUserId }, itemConfig(target, version))
    return null
  } catch (err: any) {
    return writeError(err, `Failed to rename user '${userId}'.`)
  }
}

const deleteManagedUser = async (userId: string, version: number): Promise<AdminWriteError | null> => {
  try {
    const target = adminItemUrl('users', userId)
    await v2.delete(target.url, itemConfig(target, version))
    return null
  } catch (err: any) {
    return writeError(err, `Failed to delete user '${userId}'.`)
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
