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
import {
  ManagedOnCallRole,
  ManagedOnCallRoleCreate,
  OnCallCalendar,
  OnCallScheduleSettings
} from '@/types/onCallRoleAdmin'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { adminItemConfig, adminItemUrl } from './adminItemUrl'
import { v2 } from './axiosInstances'
import { adminWriteError } from './adminWrite'
import { serverMessage } from './serverMessage'

// Reads return a ValidationResultWithPayload and writes an AdminWriteResult; the
// component that started the action shows any feedback.
const endpoint = '/oncall-roles'

const getManagedOnCallRoles = async (): Promise<ValidationResultWithPayload<ManagedOnCallRole[]>> => {
  try {
    const resp = await v2.get(endpoint)
    return createResultWithPayload(true, '', Array.isArray(resp.data) ? resp.data : [])
  } catch (err: any) {
    return createResultWithPayload<ManagedOnCallRole[]>(false, serverMessage(err, 'Failed to load on-call roles.'))
  }
}

const getOnCallScheduleSettings = async (): Promise<ValidationResultWithPayload<OnCallScheduleSettings>> => {
  try {
    const resp = await v2.get(`${endpoint}/schedule-settings`)
    return createResultWithPayload(true, '', resp.data as OnCallScheduleSettings)
  } catch (err: any) {
    return createResultWithPayload<OnCallScheduleSettings>(false, serverMessage(err, 'Failed to load the schedule settings.'))
  }
}

// the on-call intervals of a role draft (saved or not) in a month, YYYY-MM
const previewOnCallCalendar = async (draft: ManagedOnCallRoleCreate, month: string): Promise<ValidationResultWithPayload<OnCallCalendar>> => {
  try {
    const resp = await v2.post(`${endpoint}/calendar`, draft, { params: { month }})
    return createResultWithPayload(true, '', resp.data as OnCallCalendar)
  } catch (err: any) {
    return createResultWithPayload<OnCallCalendar>(false, serverMessage(err, 'Failed to preview the calendar.'))
  }
}

const createManagedOnCallRole = async (role: ManagedOnCallRoleCreate): Promise<AdminWriteResult> => {
  try {
    await v2.post(endpoint, role)
    return adminWriteSuccess()
  } catch (err: any) {
    return adminWriteError(err, `Failed to create on-call role '${role.name}'.`)
  }
}

const updateManagedOnCallRole = async (role: ManagedOnCallRole): Promise<AdminWriteResult> => {
  try {
    const target = adminItemUrl('oncall-roles', role.name)
    await v2.put(target.url, role, adminItemConfig(target, role.version))
    return adminWriteSuccess()
  } catch (err: any) {
    return adminWriteError(err, `Failed to update on-call role '${role.name}'.`)
  }
}

const renameManagedOnCallRole = async (name: string, newName: string, version: number): Promise<AdminWriteResult> => {
  try {
    const target = adminItemUrl('oncall-roles', name, 'rename')
    await v2.post(target.url, { newName }, adminItemConfig(target, version))
    return adminWriteSuccess()
  } catch (err: any) {
    return adminWriteError(err, `Failed to rename on-call role '${name}'.`)
  }
}

const deleteManagedOnCallRole = async (name: string, version: number): Promise<AdminWriteResult> => {
  try {
    const target = adminItemUrl('oncall-roles', name)
    await v2.delete(target.url, adminItemConfig(target, version))
    return adminWriteSuccess()
  } catch (err: any) {
    return adminWriteError(err, `Failed to delete on-call role '${name}'.`)
  }
}

export {
  createManagedOnCallRole,
  deleteManagedOnCallRole,
  getManagedOnCallRoles,
  getOnCallScheduleSettings,
  previewOnCallCalendar,
  renameManagedOnCallRole,
  updateManagedOnCallRole
}
