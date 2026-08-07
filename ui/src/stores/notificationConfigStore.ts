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
import { DestinationPath, EventNotification, NotifdStatus } from '@/types/notificationConfig'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationConfigStore = defineStore('notificationConfigStore', () => {
  const notifdStatus = ref<NotifdStatus | null>(null)
  const eventNotifications = ref([] as EventNotification[])
  // the event-notification editor's destination picker needs the path list
  const destinationPaths = ref([] as DestinationPath[])

  const getStatus = async (): Promise<boolean> => {
    notifdStatus.value = await API.getNotificationConfigStatus()
    return notifdStatus.value !== null
  }

  const setStatus = async (status: NotifdStatus) => {
    const ok = await API.setNotificationConfigStatus(status)
    if (ok) {
      notifdStatus.value = status
    }
    return ok
  }

  const getEventNotifications = async (): Promise<boolean> => {
    const result = await API.getEventNotifications()
    if (result === null) {
      return false
    }
    eventNotifications.value = result
    return true
  }

  const setEventNotificationStatus = async (name: string, status: NotifdStatus) => {
    const ok = await API.setEventNotificationStatus(name, status)
    if (ok) {
      const notification = eventNotifications.value.find(n => n.name === name)
      if (notification) {
        notification.status = status
      }
    }
    return ok
  }

  const addEventNotification = async (notification: EventNotification) => {
    const ok = await API.addEventNotification(notification)
    if (ok) {
      await getEventNotifications()
    }
    return ok
  }

  const updateEventNotification = async (originalName: string, notification: EventNotification) => {
    const ok = await API.updateEventNotification(originalName, notification)
    if (ok) {
      await getEventNotifications()
    }
    return ok
  }

  const deleteEventNotification = async (name: string) => {
    const ok = await API.deleteEventNotification(name)
    if (ok) {
      await getEventNotifications()
    }
    return ok
  }

  const getDestinationPaths = async (): Promise<boolean> => {
    const result = await API.getDestinationPaths()
    if (result === null) {
      return false
    }
    destinationPaths.value = result
    return true
  }

  return {
    notifdStatus,
    eventNotifications,
    destinationPaths,
    getStatus,
    setStatus,
    getEventNotifications,
    setEventNotificationStatus,
    addEventNotification,
    updateEventNotification,
    deleteEventNotification,
    getDestinationPaths
  }
})
