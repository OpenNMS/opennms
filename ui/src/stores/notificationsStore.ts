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
import useSnackbar from '@/composables/useSnackbar'
import { useAuthStore } from '@/stores/authStore'
import { NotificationQueryPreset, OnmsNotification } from '@/types/notifications'
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
        return `Outstanding Notifications for '${userFilter.value}'`
    }
    return 'Notifications'
  })

  const { showSnackBar } = useSnackbar()


  const load = async () => {
    // a failed whoami leaves no user id; widening a user-scoped query to
    // everyone's notifications would be silently wrong, so refuse instead
    const needsUser = preset.value === 'yourOutstanding' || preset.value === 'userSearch' || preset.value === 'teamOutstanding'
    if (needsUser && !effectiveUser.value && !effectiveExcludeUser.value) {
      notifications.value = []
      totalCount.value = 0
      showSnackBar({ msg: 'Cannot determine the current user; showing no notifications. Reload the page to retry.', error: true })
      return
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
      notifications.value = result.notifications
      totalCount.value = result.totalCount
    } finally {
      loading.value = false
    }
  }

  const applyPreset = async (newPreset: NotificationQueryPreset, user?: string) => {
    preset.value = newPreset
    userFilter.value = newPreset === 'userSearch' ? (user ?? null) : null
    first.value = 0
    await load()
  }

  const onPage = async (newFirst: number, newRows: number) => {
    first.value = newFirst
    rows.value = newRows
    await load()
  }

  // Export/print path: same user guard as load() so a failed whoami never
  // widens a user-scoped export to everyone's notifications.
  const fetchForExport = async (limit: number): Promise<{ notifications: OnmsNotification[], totalCount: number }> => {
    // Same guard as load(); the teamOutstanding preset is user-scoped via
    // excludeUser, so a failed whoami must block it too rather than widen.
    const needsUser = preset.value === 'yourOutstanding' || preset.value === 'userSearch' || preset.value === 'teamOutstanding'
    if (needsUser && !effectiveUser.value && !effectiveExcludeUser.value) {
      showSnackBar({ msg: 'Cannot determine the current user; nothing to export. Reload the page to retry.', error: true })
      return { notifications: [], totalCount: 0 }
    }
    const result = await API.browseNotifications({
      acktype: acktype.value,
      user: effectiveUser.value,
      excludeUser: effectiveExcludeUser.value,
      limit,
      offset: 0
    })
    return { notifications: result.notifications, totalCount: result.totalCount }
  }

  const acknowledge = async (notification: OnmsNotification) => {
    const ok = await API.acknowledgeNotification(notification.id, true)
    if (ok) {
      await load()
      // acknowledging the last row of the last page leaves the offset past
      // the end; clamp to the last valid page instead of stranding the user
      if (!notifications.value.length && first.value > 0) {
        first.value = totalCount.value > 0 ? Math.floor((totalCount.value - 1) / rows.value) * rows.value : 0
        await load()
      }
    }
    return ok
  }

  return {
    preset,
    userFilter,
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
