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
import { DestinationPath, EventNotification, NotifdStatus, RuleValidation, UeiSuggestion } from '@/types/notificationConfig'
import { rest, v2 } from './axiosInstances'

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

const getEventNotifications = async (): Promise<EventNotification[] | null> => {
  try {
    startSpinner()
    const resp = await rest.get(`${endpoint}/event-notifications`)
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
    await rest.put(`${endpoint}/event-notifications/${encodeURIComponent(name)}/status`, { status })
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
    await rest.post(`${endpoint}/event-notifications`, notification)
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
    await rest.put(`${endpoint}/event-notifications/${encodeURIComponent(originalName)}`, notification)
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
    await rest.delete(`${endpoint}/event-notifications/${encodeURIComponent(name)}`)
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
    const resp = await rest.get(`${endpoint}/destination-paths`)
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


const getNotificationServices = async (): Promise<string[] | null> => {
  try {
    const resp = await rest.get(`${endpoint}/services`)
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
    const resp = await rest.post(`${endpoint}/rule/validate`, { rule, preview })
    return resp.data ?? null
  } catch (_err) {
    showSnackBar({ msg: 'Failed to validate the rule.' })
    return null
  }
}

export {
  addEventNotification,
  deleteEventNotification,
  getDestinationPaths,
  getEventNotifications,
  getNotificationConfigStatus,
  getNotificationServices,
  searchEventConfUeis,
  setEventNotificationStatus,
  setNotificationConfigStatus,
  updateEventNotification,
  validateNotificationRule
}
