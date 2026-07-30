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
import { DestinationPath, NotifdStatus } from '@/types/notificationConfig'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationConfigStore = defineStore('notificationConfigStore', () => {
  const notifdStatus = ref<NotifdStatus | null>(null)
  const destinationPaths = ref([] as DestinationPath[])

  const getStatus = async () => {
    notifdStatus.value = await API.getNotificationConfigStatus()
  }

  const setStatus = async (status: NotifdStatus) => {
    const ok = await API.setNotificationConfigStatus(status)
    if (ok) {
      notifdStatus.value = status
    }
    return ok
  }

  const getDestinationPaths = async () => {
    destinationPaths.value = await API.getDestinationPaths()
  }

  const populate = async () => {
    await Promise.all([getStatus(), getDestinationPaths()])
  }

  return {
    notifdStatus,
    destinationPaths,
    getStatus,
    setStatus,
    getDestinationPaths,
    populate
  }
})
