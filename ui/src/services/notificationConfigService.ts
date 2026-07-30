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
import { DestinationPath, NotifdStatus, NotificationCommand } from '@/types/notificationConfig'
import { rest } from './axiosInstances'

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/notification-config'

const getNotificationConfigStatus = async (): Promise<NotifdStatus | null> => {
  try {
    startSpinner()
    const resp = await rest.get(`${endpoint}/status`)
    return resp.data?.status ?? null
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load notification status.' })
    return null
  } finally {
    stopSpinner()
  }
}

const setNotificationConfigStatus = async (status: NotifdStatus): Promise<boolean> => {
  try {
    startSpinner()
    await rest.put(`${endpoint}/status`, { status })
    showSnackBar({ msg: `Notifications turned ${status}.` })
    return true
  } catch (_err) {
    showSnackBar({ msg: 'Failed to update notification status.' })
    return false
  } finally {
    stopSpinner()
  }
}

const getDestinationPaths = async (): Promise<DestinationPath[]> => {
  try {
    startSpinner()
    const resp = await rest.get(`${endpoint}/destination-paths`)
    return resp.data?.path ?? []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load destination paths.' })
    return []
  } finally {
    stopSpinner()
  }
}

const deleteDestinationPath = async (name: string): Promise<boolean> => {
  try {
    startSpinner()
    await rest.delete(`${endpoint}/destination-paths/${encodeURIComponent(name)}`)
    showSnackBar({ msg: `Destination path '${name}' deleted.` })
    return true
  } catch (_err) {
    showSnackBar({ msg: `Failed to delete destination path '${name}'.` })
    return false
  } finally {
    stopSpinner()
  }
}

const testDestinationPath = async (name: string): Promise<boolean> => {
  try {
    startSpinner()
    await rest.post(`/notifications/destination-paths/${encodeURIComponent(name)}/trigger`)
    showSnackBar({ msg: `Test notification triggered for '${name}'.` })
    return true
  } catch (_err) {
    showSnackBar({ msg: `Failed to trigger test notification for '${name}'.` })
    return false
  } finally {
    stopSpinner()
  }
}

const addDestinationPath = async (path: DestinationPath): Promise<boolean> => {
  try {
    startSpinner()
    await rest.post(`${endpoint}/destination-paths`, path)
    showSnackBar({ msg: `Destination path '${path.name}' added.` })
    return true
  } catch (err: any) {
    const detail = err?.response?.data
    showSnackBar({ msg: typeof detail === 'string' && detail ? detail : `Failed to add destination path '${path.name}'.` })
    return false
  } finally {
    stopSpinner()
  }
}

const updateDestinationPath = async (originalName: string, path: DestinationPath): Promise<boolean> => {
  try {
    startSpinner()
    await rest.put(`${endpoint}/destination-paths/${encodeURIComponent(originalName)}`, path)
    showSnackBar({ msg: `Destination path '${path.name}' updated.` })
    return true
  } catch (err: any) {
    const detail = err?.response?.data
    showSnackBar({ msg: typeof detail === 'string' && detail ? detail : `Failed to update destination path '${path.name}'.` })
    return false
  } finally {
    stopSpinner()
  }
}

// Users and groups for destination path target pickers (v1 users/groups REST).
const getNotificationUsers = async (): Promise<string[]> => {
  try {
    const resp = await rest.get('/users?limit=0')
    const users = resp.data?.user ?? []
    return (Array.isArray(users) ? users : [users]).map((u: any) => u['user-id']).filter(Boolean)
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load users.' })
    return []
  }
}

const getNotificationGroups = async (): Promise<string[]> => {
  try {
    const resp = await rest.get('/groups?limit=0')
    const groups = resp.data?.group ?? []
    return (Array.isArray(groups) ? groups : [groups]).map((g: any) => g.name).filter(Boolean)
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load groups.' })
    return []
  }
}

const getOnCallRoles = async (): Promise<string[]> => {
  try {
    const resp = await rest.get(`${endpoint}/on-call-roles`)
    return Array.isArray(resp.data) ? resp.data : []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load on-call roles.' })
    return []
  }
}

const getNotificationCommands = async (): Promise<NotificationCommand[]> => {
  try {
    startSpinner()
    const resp = await rest.get(`${endpoint}/commands`)
    return resp.data ?? []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load notification commands.' })
    return []
  } finally {
    stopSpinner()
  }
}

export {
  addDestinationPath,
  deleteDestinationPath,
  getDestinationPaths,
  getNotificationCommands,
  getNotificationConfigStatus,
  getNotificationGroups,
  getNotificationUsers,
  getOnCallRoles,
  setNotificationConfigStatus,
  testDestinationPath,
  updateDestinationPath
}
