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
import { NoticeQueryPreset, OnmsNotice } from '@/types/notices'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useNoticesStore = defineStore('noticesStore', () => {
  const authStore = useAuthStore()

  const preset = ref<NoticeQueryPreset>('yourOutstanding')
  const userFilter = ref<string | null>(null)
  const notices = ref([] as OnmsNotice[])
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

  const title = computed<string>(() => {
    switch (preset.value) {
      case 'yourOutstanding':
        return 'Your Outstanding Notices'
      case 'allOutstanding':
        return 'All Outstanding Notices'
      case 'allAcknowledged':
        return 'All Acknowledged Notices'
      case 'userSearch':
        return `Outstanding Notices for '${userFilter.value}'`
    }
    return 'Notices'
  })

  const { showSnackBar } = useSnackbar()

  const load = async () => {
    // a failed whoami leaves no user id; widening a user-scoped query to
    // everyone's notices would be silently wrong, so refuse instead
    const needsUser = preset.value === 'yourOutstanding' || preset.value === 'userSearch'
    if (needsUser && !effectiveUser.value) {
      notices.value = []
      totalCount.value = 0
      showSnackBar({ msg: 'Cannot determine the current user; showing no notices. Reload the page to retry.', error: true })
      return
    }
    loading.value = true
    try {
      const result = await API.browseNotices({
        acktype: acktype.value,
        user: effectiveUser.value,
        limit: rows.value,
        offset: first.value
      })
      notices.value = result.notices
      totalCount.value = result.totalCount
    } finally {
      loading.value = false
    }
  }

  const applyPreset = async (newPreset: NoticeQueryPreset, user?: string) => {
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

  const acknowledge = async (notice: OnmsNotice) => {
    const ok = await API.acknowledgeNotice(notice.id, true)
    if (ok) {
      await load()
      // acknowledging the last row of the last page leaves the offset past
      // the end; clamp to the last valid page instead of stranding the user
      if (!notices.value.length && first.value > 0) {
        first.value = totalCount.value > 0 ? Math.floor((totalCount.value - 1) / rows.value) * rows.value : 0
        await load()
      }
    }
    return ok
  }

  return {
    preset,
    userFilter,
    notices,
    totalCount,
    rows,
    first,
    loading,
    currentUser,
    title,
    load,
    applyPreset,
    onPage,
    acknowledge
  }
})
