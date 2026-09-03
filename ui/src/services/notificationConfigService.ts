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
import { DestinationPath, EventNotification, NotifdStatus, NotificationCommand, PathOutage, PathOutagePreview, PathOutageRequest, RuleValidation, UeiSuggestion } from '@/types/notificationConfig'
import { rest, v2 } from './axiosInstances'

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/notification-config'

const getNotificationConfigStatus = async (): Promise<NotifdStatus | null> => {
  try {
    startSpinner()
    const resp = await v2.get(`${endpoint}/status`)
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
    await v2.put(`${endpoint}/status`, { status })
    showSnackBar({ msg: `Notifications turned ${status}.` })
    return true
  } catch (_err) {
    showSnackBar({ msg: 'Failed to update notification status.' })
    return false
  } finally {
    stopSpinner()
  }
}

const getEventNotifications = async (): Promise<EventNotification[] | null> => {
  try {
    startSpinner()
    const resp = await v2.get(`${endpoint}/event-notifications`)
    return resp.data?.notification ?? []
  } catch (_err) {
    // null (not []) so a failed load is distinguishable from an empty one and the
    // tab loader can retry instead of latching
    showSnackBar({ msg: 'Failed to load event notifications.' })
    return null
  } finally {
    stopSpinner()
  }
}

const setEventNotificationStatus = async (name: string, status: NotifdStatus): Promise<boolean> => {
  try {
    startSpinner()
    await v2.put(`${endpoint}/event-notifications/${encodeURIComponent(name)}/status`, { status })
    showSnackBar({ msg: `Event notification '${name}' turned ${status}.` })
    return true
  } catch (_err) {
    showSnackBar({ msg: `Failed to update event notification '${name}'.` })
    return false
  } finally {
    stopSpinner()
  }
}

const addEventNotification = async (notification: EventNotification): Promise<boolean> => {
  try {
    startSpinner()
    await v2.post(`${endpoint}/event-notifications`, notification)
    showSnackBar({ msg: `Event notification '${notification.name}' added.` })
    return true
  } catch (err: any) {
    const detail = err?.response?.data
    showSnackBar({ msg: typeof detail === 'string' && detail ? detail : `Failed to add event notification '${notification.name}'.` })
    return false
  } finally {
    stopSpinner()
  }
}

const updateEventNotification = async (originalName: string, notification: EventNotification): Promise<boolean> => {
  try {
    startSpinner()
    await v2.put(`${endpoint}/event-notifications/${encodeURIComponent(originalName)}`, notification)
    showSnackBar({ msg: `Event notification '${notification.name}' updated.` })
    return true
  } catch (err: any) {
    const detail = err?.response?.data
    showSnackBar({ msg: typeof detail === 'string' && detail ? detail : `Failed to update event notification '${notification.name}'.` })
    return false
  } finally {
    stopSpinner()
  }
}

// Type-ahead UEI suggestions from the event configuration (DB-backed eventconf REST).
const searchEventConfUeis = async (query: string): Promise<UeiSuggestion[]> => {
  try {
    const resp = await v2.get(`/eventconf/filter?uei=${encodeURIComponent(query)}&limit=25&offset=0`)
    const items = Array.isArray(resp.data) ? resp.data : []
    return items
      .filter((item: any) => !!item?.uei)
      .map((item: any) => ({ uei: item.uei, eventLabel: item.eventLabel ?? '' }))
  } catch (_err) {
    // suggestions are best-effort; free-text UEIs remain valid
    return []
  }
}

const deleteEventNotification = async (name: string): Promise<boolean> => {
  try {
    startSpinner()
    await v2.delete(`${endpoint}/event-notifications/${encodeURIComponent(name)}`)
    showSnackBar({ msg: `Event notification '${name}' deleted.` })
    return true
  } catch (err: any) {
    // surface the server's reason (e.g. the last notification cannot be deleted)
    // instead of a generic failure
    const detail = err?.response?.data
    showSnackBar({ msg: typeof detail === 'string' && detail ? detail : `Failed to delete event notification '${name}'.` })
    return false
  } finally {
    stopSpinner()
  }
}

const getDestinationPaths = async (): Promise<DestinationPath[] | null> => {
  try {
    startSpinner()
    const resp = await v2.get(`${endpoint}/destination-paths`)
    return resp.data?.path ?? []
  } catch (_err) {
    // null (not []) so a failed load is distinguishable from an empty one and the
    // tab loader can retry instead of latching
    showSnackBar({ msg: 'Failed to load destination paths.' })
    return null
  } finally {
    stopSpinner()
  }
}

const deleteDestinationPath = async (name: string): Promise<boolean> => {
  try {
    startSpinner()
    await v2.delete(`${endpoint}/destination-paths/${encodeURIComponent(name)}`)
    showSnackBar({ msg: `Destination path '${name}' deleted.` })
    return true
  } catch (err: any) {
    const detail = err?.response?.data
    showSnackBar({ msg: typeof detail === 'string' && detail ? detail : `Failed to delete destination path '${name}'.` })
    return false
  } finally {
    stopSpinner()
  }
}

const testDestinationPath = async (name: string): Promise<boolean> => {
  try {
    startSpinner()
    // the trigger endpoint lives on the v1 resource (NotificationRestService), not v2
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
    await v2.post(`${endpoint}/destination-paths`, path)
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
    await v2.put(`${endpoint}/destination-paths/${encodeURIComponent(originalName)}`, path)
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
const getNotificationUsers = async (): Promise<string[] | null> => {
  try {
    const resp = await v2.get('/users?limit=0')
    const users = resp.data?.user ?? []
    return (Array.isArray(users) ? users : [users]).map((u: any) => u['user-id']).filter(Boolean)
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load users.' })
    return null
  }
}

const getNotificationGroups = async (): Promise<string[] | null> => {
  try {
    const resp = await v2.get('/groups?limit=0')
    const groups = resp.data?.group ?? []
    return (Array.isArray(groups) ? groups : [groups]).map((g: any) => g.name).filter(Boolean)
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load groups.' })
    return null
  }
}

const getOnCallRoles = async (): Promise<string[] | null> => {
  try {
    const resp = await v2.get(`${endpoint}/on-call-roles`)
    return Array.isArray(resp.data) ? resp.data : []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load on-call roles.' })
    return null
  }
}

const getNotificationCommands = async (): Promise<NotificationCommand[] | null> => {
  try {
    startSpinner()
    const resp = await v2.get(`${endpoint}/commands`)
    return resp.data ?? []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load notification commands.' })
    return null
  } finally {
    stopSpinner()
  }
}

const getPathOutages = async (): Promise<PathOutage[] | null> => {
  try {
    startSpinner()
    const resp = await v2.get(`${endpoint}/path-outages`)
    return resp.data ?? []
  } catch (_err) {
    // null (not []) so the caller can tell a failed load from an empty one and retry
    showSnackBar({ msg: 'Failed to load path outages.' })
    return null
  } finally {
    stopSpinner()
  }
}

const getNotificationServices = async (): Promise<string[] | null> => {
  try {
    const resp = await v2.get(`${endpoint}/services`)
    return Array.isArray(resp.data) ? resp.data : []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load the service list.' })
    return null
  }
}

// preview builds the (potentially large) match list; the save path leaves it
// false so validation costs only a rule parse on the server.
const validateNotificationRule = async (rule: string, preview = false): Promise<RuleValidation | null> => {
  try {
    const resp = await v2.post(`${endpoint}/rule/validate`, { rule, preview })
    return resp.data ?? null
  } catch (_err) {
    showSnackBar({ msg: 'Failed to validate the rule.' })
    return null
  }
}

const previewPathOutageRule = async (rule: string): Promise<PathOutagePreview | null> => {
  try {
    startSpinner()
    const resp = await v2.get(`${endpoint}/path-outages/preview?rule=${encodeURIComponent(rule)}`)
    return resp.data ?? null
  } catch (err: any) {
    const detail = err?.response?.data
    showSnackBar({ msg: typeof detail === 'string' && detail ? detail : 'Failed to validate the filter rule.' })
    return null
  } finally {
    stopSpinner()
  }
}

const applyPathOutage = async (request: PathOutageRequest): Promise<boolean> => {
  try {
    startSpinner()
    await v2.post(`${endpoint}/path-outages`, request)
    showSnackBar({ msg: request.criticalIp ? 'Critical path applied.' : 'Critical path cleared.' })
    return true
  } catch (err: any) {
    const detail = err?.response?.data
    showSnackBar({ msg: typeof detail === 'string' && detail ? detail : 'Failed to apply the critical path.' })
    return false
  } finally {
    stopSpinner()
  }
}

const deletePathOutage = async (nodeId: number): Promise<boolean> => {
  try {
    startSpinner()
    await v2.delete(`${endpoint}/path-outages/${nodeId}`)
    showSnackBar({ msg: 'Critical path removed.' })
    return true
  } catch (err: any) {
    const detail = err?.response?.data
    showSnackBar({ msg: typeof detail === 'string' && detail ? detail : 'Failed to remove critical path.' })
    return false
  } finally {
    stopSpinner()
  }
}

export {
  addDestinationPath,
  addEventNotification,
  applyPathOutage,
  deleteDestinationPath,
  deleteEventNotification,
  deletePathOutage,
  getDestinationPaths,
  getEventNotifications,
  getNotificationCommands,
  getNotificationConfigStatus,
  getNotificationGroups,
  getNotificationServices,
  getNotificationUsers,
  getOnCallRoles,
  getPathOutages,
  previewPathOutageRule,
  searchEventConfUeis,
  setEventNotificationStatus,
  setNotificationConfigStatus,
  testDestinationPath,
  updateDestinationPath,
  updateEventNotification,
  validateNotificationRule
}
