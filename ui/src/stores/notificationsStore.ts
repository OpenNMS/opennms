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
import { useAuthStore } from '@/stores/authStore'
import { NotificationBrowseResult, NotificationQueryPreset, OnmsNotification } from '@/types/notifications'
import { createFailureResult, createResultWithPayload, createSuccessResponse, ValidationResult, ValidationResultWithPayload } from '@/types/validation'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useNotificationsStore = defineStore('notificationsStore', () => {
  const authStore = useAuthStore()

  const preset = ref<NotificationQueryPreset>('yourOutstanding')
  const userFilter = ref<string | null>(null)
  const notifications = ref([] as OnmsNotification[])
  const totalCount = ref(0)
  const rows = ref(10)
  const first = ref(0)
  const loading = ref(false)

  const currentUser = computed<string>(() => authStore.whoAmI.id)

  const acktype = computed<'unack' | 'ack'>(() => (preset.value === 'allAcknowledged' ? 'ack' : 'unack'))

  const effectiveUser = computed<string | null>(() => {
    if (preset.value === 'yourOutstanding') {
      return currentUser.value || null
    }
    if (preset.value === 'userSearch') {
      return userFilter.value
    }
    return null
  })

  // User search chosen but no user entered yet (e.g. the search was cleared):
  // there is nothing to query, which is not the same as a failed whoami.
  const awaitingUser = computed<boolean>(() => preset.value === 'userSearch' && !userFilter.value)

  const effectiveExcludeUser = computed<string | null>(() =>
    preset.value === 'teamOutstanding' ? currentUser.value || null : null)

  const title = computed<string>(() => {
    switch (preset.value) {
      case 'yourOutstanding':
        return 'Your Outstanding Notifications'
      case 'teamOutstanding':
        return 'Outstanding Notifications Assigned to Anyone but You'
      case 'allOutstanding':
        return 'All Outstanding Notifications'
      case 'allAcknowledged':
        return 'All Acknowledged Notifications'
      case 'userSearch':
        return userFilter.value ? `Outstanding Notifications for '${userFilter.value}'` : 'Outstanding Notifications for User'
    }
    return 'Notifications'
  })

  // No snackbars here: actions return a ValidationResult and the calling
  // component shows the message. A failed whoami leaves no user id, and
  // widening a user-scoped query to everyone's notifications would be silently
  // wrong, so user-scoped presets refuse instead.
  const needsUser = computed<boolean>(() =>
    preset.value === 'yourOutstanding' || preset.value === 'userSearch' || preset.value === 'teamOutstanding')
  const missingUser = computed<boolean>(() => needsUser.value && !effectiveUser.value && !effectiveExcludeUser.value)

  // Loads can overlap (preset buttons, Refresh, paging); only the latest may
  // write the table, or a slow earlier response could land under a newer
  // preset's title. Every call, including the ones that return without
  // querying, supersedes any request still in flight.
  let latestLoad = 0

  const load = async (): Promise<ValidationResult> => {
    const thisLoad = ++latestLoad
    if (awaitingUser.value || missingUser.value) {
      notifications.value = []
      totalCount.value = 0
      loading.value = false
      return awaitingUser.value
        ? createSuccessResponse()
        : createFailureResult('Cannot determine the current user; showing no notifications. Reload the page to retry.')
    }
    loading.value = true
    try {
      const result = await API.browseNotifications({
        acktype: acktype.value,
        user: effectiveUser.value,
        excludeUser: effectiveExcludeUser.value,
        limit: rows.value,
        offset: first.value
      })
      if (thisLoad !== latestLoad) {
        // superseded: a newer load owns the table (and any message to show)
        return createSuccessResponse()
      }
      // a failed load clears the table rather than leaving stale rows
      notifications.value = result.payload?.notifications ?? []
      totalCount.value = result.payload?.totalCount ?? 0
      return result
    } finally {
      if (thisLoad === latestLoad) {
        loading.value = false
      }
    }
  }

  const applyPreset = async (newPreset: NotificationQueryPreset, user?: string): Promise<ValidationResult> => {
    preset.value = newPreset
    userFilter.value = newPreset === 'userSearch' ? (user ?? null) : null
    first.value = 0
    return await load()
  }

  const onPage = async (newFirst: number, newRows: number): Promise<ValidationResult> => {
    first.value = newFirst
    rows.value = newRows
    return await load()
  }

  // Export/print path: same user guard as load() so a failed whoami never
  // widens a user-scoped export to everyone's notifications.
  // Same guard as load(); the teamOutstanding preset is user-scoped via
  // excludeUser, so a failed whoami must block it too rather than widen.
  const fetchForExport = async (limit: number): Promise<ValidationResultWithPayload<NotificationBrowseResult>> => {
    if (awaitingUser.value) {
      return createResultWithPayload(true, '', { notifications: [], totalCount: 0 })
    }
    if (missingUser.value) {
      return createResultWithPayload<NotificationBrowseResult>(false, 'Cannot determine the current user; nothing to export. Reload the page to retry.')
    }
    return await API.browseNotifications({
      acktype: acktype.value,
      user: effectiveUser.value,
      excludeUser: effectiveExcludeUser.value,
      limit,
      offset: 0
    })
  }

  // A failed reload after a successful acknowledge comes back in `errors`.
  const acknowledge = async (notification: OnmsNotification): Promise<ValidationResult> => {
    const ok = await API.acknowledgeNotification(notification.id, true)
    if (!ok) {
      return createFailureResult(`Failed to acknowledge notification ${notification.id}.`)
    }
    let reload = await load()
    // acknowledging the last row of the last page leaves the offset past
    // the end; clamp to the last valid page instead of stranding the user
    if (reload.success && !notifications.value.length && first.value > 0) {
      first.value = totalCount.value > 0 ? Math.floor((totalCount.value - 1) / rows.value) * rows.value : 0
      reload = await load()
    }
    return reload.success ? createSuccessResponse() : { ...createSuccessResponse(), errors: [reload.message] }
  }

  return {
    preset,
    userFilter,
    awaitingUser,
    notifications,
    totalCount,
    rows,
    first,
    loading,
    currentUser,
    title,
    load,
    applyPreset,
    onPage,
    fetchForExport,
    acknowledge
  }
})
