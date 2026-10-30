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
import { ManagedGroup, ManagedGroupCreate } from '@/types/groupAdmin'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { AdminItemTarget, adminItemUrl } from './adminItemUrl'
import { ifMatch, v2 } from './axiosInstances'

// Reads return a ValidationResultWithPayload and writes an AdminWriteResult; the
// component that started the action shows any feedback.
const endpoint = '/groups'

// the server's plain-text reason for a rejected request, else the fallback
const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  return typeof detail === 'string' && detail ? detail : fallback
}

// A 412 is reported by flag only; the store words the stale message once the list has been reloaded.
const writeError = (err: any, fallback: string): AdminWriteResult => {
  if (err?.response?.status === 412) {
    return { success: false, message: '', stale: true }
  }
  return { success: false, message: errorMessage(err, fallback), stale: false }
}

// the If-Match header, plus the query of the /item form when the name needs it
const itemConfig = (target: AdminItemTarget, version: number) =>
  target.params ? { ...ifMatch(version), params: target.params } : ifMatch(version)

const getManagedGroups = async (): Promise<ValidationResultWithPayload<ManagedGroup[]>> => {
  try {
    const resp = await v2.get(endpoint)
    return createResultWithPayload(true, '', Array.isArray(resp.data) ? resp.data : [])
  } catch (err: any) {
    return createResultWithPayload<ManagedGroup[]>(false, errorMessage(err, 'Failed to load groups.'))
  }
}

// member picker options: the user ids of the v2 users list
const getGroupMemberCandidates = async (): Promise<ValidationResultWithPayload<string[]>> => {
  try {
    const resp = await v2.get('/users')
    const users: { userId?: string }[] = Array.isArray(resp.data) ? resp.data : []
    return createResultWithPayload(true, '', users.map(u => u.userId).filter((id): id is string => !!id))
  } catch (err: any) {
    return createResultWithPayload<string[]>(false, errorMessage(err, 'Failed to load users.'))
  }
}

// category picker options: the names of the categories a group can be limited to
const getAvailableGroupCategories = async (): Promise<ValidationResultWithPayload<string[]>> => {
  try {
    const resp = await v2.get(`${endpoint}/available-categories`)
    return createResultWithPayload(true, '', Array.isArray(resp.data) ? resp.data : [])
  } catch (err: any) {
    return createResultWithPayload<string[]>(false, errorMessage(err, 'Failed to load available categories.'))
  }
}

const createManagedGroup = async (group: ManagedGroupCreate): Promise<AdminWriteResult> => {
  try {
    await v2.post(endpoint, group)
    return adminWriteSuccess()
  } catch (err: any) {
    return writeError(err, `Failed to create group '${group.name}'.`)
  }
}

const updateManagedGroup = async (group: ManagedGroup): Promise<AdminWriteResult> => {
  try {
    const target = adminItemUrl('groups', group.name)
    await v2.put(target.url, group, itemConfig(target, group.version))
    return adminWriteSuccess()
  } catch (err: any) {
    return writeError(err, `Failed to update group '${group.name}'.`)
  }
}

const renameManagedGroup = async (name: string, newName: string, version: number): Promise<AdminWriteResult> => {
  try {
    const target = adminItemUrl('groups', name, 'rename')
    await v2.post(target.url, { newName }, itemConfig(target, version))
    return adminWriteSuccess()
  } catch (err: any) {
    return writeError(err, `Failed to rename group '${name}'.`)
  }
}

const deleteManagedGroup = async (name: string, version: number): Promise<AdminWriteResult> => {
  try {
    const target = adminItemUrl('groups', name)
    await v2.delete(target.url, itemConfig(target, version))
    return adminWriteSuccess()
  } catch (err: any) {
    return writeError(err, `Failed to delete group '${name}'.`)
  }
}

export {
  createManagedGroup,
  deleteManagedGroup,
  getAvailableGroupCategories,
  getGroupMemberCandidates,
  getManagedGroups,
  renameManagedGroup,
  updateManagedGroup
}
