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
import { NotifdStatus, PathOutage, PathOutagePreview, PathOutageRequest } from '@/types/notificationConfig'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationConfigStore = defineStore('notificationConfigStore', () => {
  const notifdStatus = ref<NotifdStatus | null>(null)
  const pathOutages = ref([] as PathOutage[])

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

  const getPathOutages = async (): Promise<boolean> => {
    const result = await API.getPathOutages()
    if (result === null) {
      return false
    }
    pathOutages.value = result
    return true
  }

  const previewPathOutageRule = async (rule: string): Promise<PathOutagePreview | null> => {
    return await API.previewPathOutageRule(rule)
  }

  const applyPathOutage = async (request: PathOutageRequest) => {
    const ok = await API.applyPathOutage(request)
    if (ok) {
      await getPathOutages()
    }
    return ok
  }

  const deletePathOutage = async (nodeId: number) => {
    const ok = await API.deletePathOutage(nodeId)
    if (ok) {
      await getPathOutages()
    }
    return ok
  }

  return {
    notifdStatus,
    pathOutages,
    getStatus,
    setStatus,
    getPathOutages,
    previewPathOutageRule,
    applyPathOutage,
    deletePathOutage
  }
})
