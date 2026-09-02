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
import { WsmanConfig } from '@/types/wsmanAdmin'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useWsmanAdminStore = defineStore('wsmanAdminStore', () => {
  const config = ref<WsmanConfig | null>(null)
  const loadError = ref(false)
  const isLoading = ref(false)

  const getConfig = async () => {
    isLoading.value = true
    try {
      const result = await API.getWsmanConfig()
      if (result !== null) {
        config.value = result
        loadError.value = false
      } else {
        loadError.value = true
      }
    } finally {
      isLoading.value = false
    }
  }

  return { config, loadError, isLoading, getConfig }
})
