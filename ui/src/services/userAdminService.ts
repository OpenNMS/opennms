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

import { adminWriteSuccess, AdminWriteResult } from '@/types/adminWrite'
import { ManagedUser, ManagedUserCreate } from '@/types/userAdmin'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { adminItemConfig, adminItemUrl } from './adminItemUrl'
import { v2 } from './axiosInstances'
import { adminWriteError } from './adminWrite'
import { serverMessage } from './serverMessage'

// Reads return a ValidationResultWithPayload and writes an AdminWriteResult; the
// component that started the action shows any feedback.
const endpoint = '/users'

const getManagedUsers = async (): Promise<ValidationResultWithPayload<ManagedUser[]>> => {
  try {
    const resp = await v2.get(endpoint)
    return createResultWithPayload(true, '', Array.isArray(resp.data) ? resp.data : [])
  } catch (err: any) {
    return createResultWithPayload<ManagedUser[]>(false, serverMessage(err, 'Failed to load users.'))
  }
}

const getAvailableUserRoles = async (): Promise<ValidationResultWithPayload<string[]>> => {
  try {
    const resp = await v2.get(`${endpoint}/available-roles`)
    return createResultWithPayload(true, '', Array.isArray(resp.data) ? resp.data : [])
  } catch (err: any) {
    return createResultWithPayload<string[]>(false, serverMessage(err, 'Failed to load available roles.'))
  }
}

const createManagedUser = async (user: ManagedUserCreate): Promise<AdminWriteResult> => {
  try {
    await v2.post(endpoint, user)
    return adminWriteSuccess()
  } catch (err: any) {
    return adminWriteError(err, `Failed to create user '${user.userId}'.`)
  }
}

const updateManagedUser = async (user: ManagedUser): Promise<AdminWriteResult> => {
  try {
    const target = adminItemUrl('users', user.userId)
    await v2.put(target.url, user, adminItemConfig(target, user.version))
    return adminWriteSuccess()
  } catch (err: any) {
    return adminWriteError(err, `Failed to update user '${user.userId}'.`)
  }
}

const setManagedUserPassword = async (userId: string, password: string, version: number): Promise<AdminWriteResult> => {
  try {
    const target = adminItemUrl('users', userId, 'password')
    await v2.put(target.url, { password }, adminItemConfig(target, version))
    return adminWriteSuccess()
  } catch (err: any) {
    return adminWriteError(err, `Failed to change the password for '${userId}'.`)
  }
}

const renameManagedUser = async (userId: string, newUserId: string, version: number): Promise<AdminWriteResult> => {
  try {
    const target = adminItemUrl('users', userId, 'rename')
    await v2.post(target.url, { newUserId }, adminItemConfig(target, version))
    return adminWriteSuccess()
  } catch (err: any) {
    return adminWriteError(err, `Failed to rename user '${userId}'.`)
  }
}

const deleteManagedUser = async (userId: string, version: number): Promise<AdminWriteResult> => {
  try {
    const target = adminItemUrl('users', userId)
    await v2.delete(target.url, adminItemConfig(target, version))
    return adminWriteSuccess()
  } catch (err: any) {
    return adminWriteError(err, `Failed to delete user '${userId}'.`)
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
