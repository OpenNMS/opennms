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

import { DestinationPath, EventNotification, NotifdStatus, NotificationCommand, PathOutage, PathOutagePreview, PathOutageRequest, RuleValidation, UeiSuggestion } from '@/types/notificationConfig'
import {
  createFailureResult,
  createResultWithPayload,
  createSuccessResponse,
  ValidationResult,
  ValidationResultWithPayload
} from '@/types/validation'
import { rest, v2 } from './axiosInstances'

// API calls only: no spinner or snackbar here. Callers (the notification config
// store, or a component directly) decide what to show.
//  - Reads return a ValidationResultWithPayload, so a failed load (success false,
//    with a message) is distinguishable from an empty one.
//  - Mutations whose failure reason comes from the server (e.g. "the last
//    notification cannot be deleted") return a ValidationResult carrying it.
//  - Other mutations return a plain boolean; the caller words the message.
const endpoint = '/notification-config'

// the server's plain-text reason for a rejected request, else the fallback
const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  return typeof detail === 'string' && detail ? detail : fallback
}

const loaded = <T>(payload: T): ValidationResultWithPayload<T> => createResultWithPayload(true, '', payload)
const loadFailed = <T>(message: string): ValidationResultWithPayload<T> => createResultWithPayload<T>(false, message)

const getNotificationConfigStatus = async (): Promise<ValidationResultWithPayload<NotifdStatus>> => {
  try {
    const resp = await v2.get(`${endpoint}/status`)
    const status = resp.data?.status
    return status ? loaded<NotifdStatus>(status) : loadFailed('Failed to load notification status.')
  } catch (_err) {
    return loadFailed('Failed to load notification status.')
  }
}

const setNotificationConfigStatus = async (status: NotifdStatus): Promise<boolean> => {
  try {
    await v2.put(`${endpoint}/status`, { status })
    return true
  } catch (_err) {
    return false
  }
}

const getEventNotifications = async (): Promise<ValidationResultWithPayload<EventNotification[]>> => {
  try {
    const resp = await v2.get(`${endpoint}/event-notifications`)
    return loaded<EventNotification[]>(resp.data?.notification ?? [])
  } catch (_err) {
    return loadFailed('Failed to load event notifications.')
  }
}

const setEventNotificationStatus = async (name: string, status: NotifdStatus): Promise<boolean> => {
  try {
    await v2.put(`${endpoint}/event-notifications/${encodeURIComponent(name)}/status`, { status })
    return true
  } catch (_err) {
    return false
  }
}

const addEventNotification = async (notification: EventNotification): Promise<ValidationResult> => {
  try {
    await v2.post(`${endpoint}/event-notifications`, notification)
    return createSuccessResponse()
  } catch (err: any) {
    return createFailureResult(errorMessage(err, `Failed to add event notification '${notification.name}'.`))
  }
}

const updateEventNotification = async (originalName: string, notification: EventNotification): Promise<ValidationResult> => {
  try {
    await v2.put(`${endpoint}/event-notifications/${encodeURIComponent(originalName)}`, notification)
    return createSuccessResponse()
  } catch (err: any) {
    return createFailureResult(errorMessage(err, `Failed to update event notification '${notification.name}'.`))
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

const deleteEventNotification = async (name: string): Promise<ValidationResult> => {
  try {
    await v2.delete(`${endpoint}/event-notifications/${encodeURIComponent(name)}`)
    return createSuccessResponse()
  } catch (err: any) {
    // the server's reason (e.g. the last notification cannot be deleted) beats a generic failure
    return createFailureResult(errorMessage(err, `Failed to delete event notification '${name}'.`))
  }
}

const getDestinationPaths = async (): Promise<ValidationResultWithPayload<DestinationPath[]>> => {
  try {
    const resp = await v2.get(`${endpoint}/destination-paths`)
    return loaded<DestinationPath[]>(resp.data?.path ?? [])
  } catch (_err) {
    return loadFailed('Failed to load destination paths.')
  }
}

const deleteDestinationPath = async (name: string): Promise<ValidationResult> => {
  try {
    await v2.delete(`${endpoint}/destination-paths/${encodeURIComponent(name)}`)
    return createSuccessResponse()
  } catch (err: any) {
    return createFailureResult(errorMessage(err, `Failed to delete destination path '${name}'.`))
  }
}

const testDestinationPath = async (name: string): Promise<boolean> => {
  try {
    // the trigger endpoint lives on the v1 resource (NotificationRestService), not v2
    await rest.post(`/notifications/destination-paths/${encodeURIComponent(name)}/trigger`)
    return true
  } catch (_err) {
    return false
  }
}

const addDestinationPath = async (path: DestinationPath): Promise<ValidationResult> => {
  try {
    await v2.post(`${endpoint}/destination-paths`, path)
    return createSuccessResponse()
  } catch (err: any) {
    return createFailureResult(errorMessage(err, `Failed to add destination path '${path.name}'.`))
  }
}

const updateDestinationPath = async (originalName: string, path: DestinationPath): Promise<ValidationResult> => {
  try {
    await v2.put(`${endpoint}/destination-paths/${encodeURIComponent(originalName)}`, path)
    return createSuccessResponse()
  } catch (err: any) {
    return createFailureResult(errorMessage(err, `Failed to update destination path '${path.name}'.`))
  }
}

// Users and groups for destination path target pickers. v2 (not v1) because
// v1 /users also serializes each user's password hash. v2 returns plain arrays
// of UserDTO/GroupDTO, keyed userId/name (not v1's {user: [{'user-id'}]}).
const getNotificationUsers = async (): Promise<ValidationResultWithPayload<string[]>> => {
  try {
    const resp = await v2.get('/users?limit=0')
    const users = Array.isArray(resp.data) ? resp.data : []
    return loaded<string[]>(users.map((u: any) => u.userId).filter(Boolean))
  } catch (_err) {
    return loadFailed('Failed to load users.')
  }
}

const getNotificationGroups = async (): Promise<ValidationResultWithPayload<string[]>> => {
  try {
    const resp = await v2.get('/groups?limit=0')
    const groups = Array.isArray(resp.data) ? resp.data : []
    return loaded<string[]>(groups.map((g: any) => g.name).filter(Boolean))
  } catch (_err) {
    return loadFailed('Failed to load groups.')
  }
}

const getOnCallRoles = async (): Promise<ValidationResultWithPayload<string[]>> => {
  try {
    const resp = await v2.get(`${endpoint}/on-call-roles`)
    return loaded<string[]>(Array.isArray(resp.data) ? resp.data : [])
  } catch (_err) {
    return loadFailed('Failed to load on-call roles.')
  }
}

const getNotificationCommands = async (): Promise<ValidationResultWithPayload<NotificationCommand[]>> => {
  try {
    const resp = await v2.get(`${endpoint}/commands`)
    return loaded<NotificationCommand[]>(resp.data ?? [])
  } catch (_err) {
    return loadFailed('Failed to load notification commands.')
  }
}

const getPathOutages = async (): Promise<ValidationResultWithPayload<PathOutage[]>> => {
  try {
    const resp = await v2.get(`${endpoint}/path-outages`)
    return loaded<PathOutage[]>(resp.data ?? [])
  } catch (_err) {
    return loadFailed('Failed to load path outages.')
  }
}

const getNotificationServices = async (): Promise<ValidationResultWithPayload<string[]>> => {
  try {
    const resp = await v2.get(`${endpoint}/services`)
    return loaded<string[]>(Array.isArray(resp.data) ? resp.data : [])
  } catch (_err) {
    return loadFailed('Failed to load the service list.')
  }
}

// preview builds the (potentially large) match list; the save path leaves it
// false so validation costs only a rule parse on the server.
const validateNotificationRule = async (rule: string, preview = false): Promise<ValidationResultWithPayload<RuleValidation>> => {
  try {
    const resp = await v2.post(`${endpoint}/rule/validate`, { rule, preview })
    return resp.data ? loaded<RuleValidation>(resp.data) : loadFailed('Failed to validate the rule.')
  } catch (_err) {
    return loadFailed('Failed to validate the rule.')
  }
}

const previewPathOutageRule = async (rule: string): Promise<ValidationResultWithPayload<PathOutagePreview>> => {
  try {
    const resp = await v2.get(`${endpoint}/path-outages/preview?rule=${encodeURIComponent(rule)}`)
    return resp.data ? loaded<PathOutagePreview>(resp.data) : loadFailed('Failed to validate the filter rule.')
  } catch (err: any) {
    return loadFailed(errorMessage(err, 'Failed to validate the filter rule.'))
  }
}

const applyPathOutage = async (request: PathOutageRequest): Promise<ValidationResult> => {
  try {
    await v2.post(`${endpoint}/path-outages`, request)
    return createSuccessResponse()
  } catch (err: any) {
    return createFailureResult(errorMessage(err, 'Failed to apply the critical path.'))
  }
}

const deletePathOutage = async (nodeId: number): Promise<ValidationResult> => {
  try {
    await v2.delete(`${endpoint}/path-outages/${nodeId}`)
    return createSuccessResponse()
  } catch (err: any) {
    return createFailureResult(errorMessage(err, 'Failed to remove critical path.'))
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
