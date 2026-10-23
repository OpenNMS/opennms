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

import API from '@/services'
import { DestinationPath, EventNotification, NotifdStatus, NotificationCommand, PathOutage, PathOutagePreview, PathOutageRequest } from '@/types/notificationConfig'
import { createSuccessResponse, ValidationResult, ValidationResultWithPayload } from '@/types/validation'
import { defineStore } from 'pinia'
import { ref } from 'vue'

// Event Notifications and Destination Paths tabs: the table, or the editor
// panel in place of it.
export enum NotificationConfigEditMode {
  Table = 'table',
  Edit = 'edit',
  Create = 'create'
}

export const useNotificationConfigStore = defineStore('notificationConfigStore', () => {
  const notifdStatus = ref<NotifdStatus | null>(null)
  const eventNotifications = ref([] as EventNotification[])
  // the event-notification editor's destination picker needs the path list
  const destinationPaths = ref([] as DestinationPath[])
  const commands = ref([] as NotificationCommand[])
  const users = ref([] as string[])
  const groups = ref([] as string[])
  const roles = ref([] as string[])
  const pathOutages = ref([] as PathOutage[])
  const eventNotificationEditMode = ref(NotificationConfigEditMode.Table)
  // the notification being edited; null while creating or on the table
  const currentEventNotification = ref<EventNotification | null>(null)
  const destinationPathEditMode = ref(NotificationConfigEditMode.Table)
  // the path being edited; null while creating or on the table
  const currentDestinationPath = ref<DestinationPath | null>(null)

  // null opens the editor on a new notification
  const openEventNotificationEditor = (notification: EventNotification | null) => {
    currentEventNotification.value = notification
    eventNotificationEditMode.value = notification ? NotificationConfigEditMode.Edit : NotificationConfigEditMode.Create
  }

  const closeEventNotificationEditor = () => {
    currentEventNotification.value = null
    eventNotificationEditMode.value = NotificationConfigEditMode.Table
  }

  // null opens the editor on a new path
  const openDestinationPathEditor = (path: DestinationPath | null) => {
    currentDestinationPath.value = path
    destinationPathEditMode.value = path ? NotificationConfigEditMode.Edit : NotificationConfigEditMode.Create
  }

  const closeDestinationPathEditor = () => {
    currentDestinationPath.value = null
    destinationPathEditMode.value = NotificationConfigEditMode.Table
  }

  // Reads return a ValidationResult: success false carries the message to show.
  // Mutations that refresh a list afterwards report a failed refresh in
  // `errors` of an otherwise successful result, so the caller can show both.
  const refreshed = (result: ValidationResult, refresh: ValidationResult): ValidationResult =>
    refresh.success ? result : { ...result, errors: [refresh.message] }

  const getStatus = async (): Promise<ValidationResult> => {
    const result = await API.getNotificationConfigStatus()
    notifdStatus.value = result.payload ?? null
    return result
  }

  const setStatus = async (status: NotifdStatus): Promise<boolean> => {
    const ok = await API.setNotificationConfigStatus(status)
    if (ok) {
      notifdStatus.value = status
    }
    return ok
  }

  const getEventNotifications = async (): Promise<ValidationResult> => {
    const result = await API.getEventNotifications()
    if (result.success) {
      eventNotifications.value = result.payload ?? []
    }
    return result
  }

  const setEventNotificationStatus = async (name: string, status: NotifdStatus): Promise<boolean> => {
    const ok = await API.setEventNotificationStatus(name, status)
    if (ok) {
      const notification = eventNotifications.value.find(n => n.name === name)
      if (notification) {
        notification.status = status
      }
    }
    return ok
  }

  const getPathOutages = async (): Promise<ValidationResult> => {
    const result = await API.getPathOutages()
    if (result.success) {
      pathOutages.value = result.payload ?? []
    }
    return result
  }

  const previewPathOutageRule = async (rule: string): Promise<ValidationResultWithPayload<PathOutagePreview>> => {
    return await API.previewPathOutageRule(rule)
  }

  const applyPathOutage = async (request: PathOutageRequest): Promise<ValidationResult> => {
    const result = await API.applyPathOutage(request)
    return result.success ? refreshed(result, await getPathOutages()) : result
  }

  const addEventNotification = async (notification: EventNotification): Promise<ValidationResult> => {
    const result = await API.addEventNotification(notification)
    return result.success ? refreshed(result, await getEventNotifications()) : result
  }

  const deletePathOutage = async (nodeId: number): Promise<ValidationResult> => {
    const result = await API.deletePathOutage(nodeId)
    return result.success ? refreshed(result, await getPathOutages()) : result
  }

  const updateEventNotification = async (originalName: string, notification: EventNotification): Promise<ValidationResult> => {
    const result = await API.updateEventNotification(originalName, notification)
    return result.success ? refreshed(result, await getEventNotifications()) : result
  }

  const deleteEventNotification = async (name: string): Promise<ValidationResult> => {
    const result = await API.deleteEventNotification(name)
    return result.success ? refreshed(result, await getEventNotifications()) : result
  }

  const getDestinationPaths = async (): Promise<ValidationResult> => {
    const result = await API.getDestinationPaths()
    if (result.success) {
      destinationPaths.value = result.payload ?? []
    }
    return result
  }

  const addDestinationPath = async (path: DestinationPath): Promise<ValidationResult> => {
    const result = await API.addDestinationPath(path)
    return result.success ? refreshed(result, await getDestinationPaths()) : result
  }

  const updateDestinationPath = async (originalName: string, path: DestinationPath): Promise<ValidationResult> => {
    const result = await API.updateDestinationPath(originalName, path)
    return result.success ? refreshed(result, await getDestinationPaths()) : result
  }

  // One result for the three picker lookups: the first failure, if any. Each
  // list that did load is still kept.
  const getUsersAndGroups = async (): Promise<ValidationResult> => {
    const [u, g, r] = await Promise.all([API.getNotificationUsers(), API.getNotificationGroups(), API.getOnCallRoles()])
    if (u.success) {
      users.value = u.payload ?? []
    }
    if (g.success) {
      groups.value = g.payload ?? []
    }
    if (r.success) {
      roles.value = r.payload ?? []
    }
    return [u, g, r].find(result => !result.success) ?? createSuccessResponse()
  }

  const deleteDestinationPath = async (name: string): Promise<ValidationResult> => {
    const result = await API.deleteDestinationPath(name)
    return result.success ? refreshed(result, await getDestinationPaths()) : result
  }

  const testDestinationPath = async (name: string): Promise<boolean> => {
    return await API.testDestinationPath(name)
  }

  const getCommands = async (): Promise<ValidationResult> => {
    const result = await API.getNotificationCommands()
    if (result.success) {
      commands.value = result.payload ?? []
    }
    return result
  }

  return {
    notifdStatus,
    eventNotifications,
    destinationPaths,
    commands,
    users,
    groups,
    roles,
    pathOutages,
    eventNotificationEditMode,
    currentEventNotification,
    openEventNotificationEditor,
    closeEventNotificationEditor,
    destinationPathEditMode,
    currentDestinationPath,
    openDestinationPathEditor,
    closeDestinationPathEditor,
    getStatus,
    setStatus,
    getEventNotifications,
    setEventNotificationStatus,
    addEventNotification,
    updateEventNotification,
    deleteEventNotification,
    getDestinationPaths,
    addDestinationPath,
    updateDestinationPath,
    deleteDestinationPath,
    testDestinationPath,
    getCommands,
    getUsersAndGroups,
    getPathOutages,
    previewPathOutageRule,
    applyPathOutage,
    deletePathOutage
  }
})
