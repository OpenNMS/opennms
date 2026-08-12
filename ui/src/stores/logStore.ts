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

import { defineStore } from 'pinia'
import API from '@/services'
import { ref } from 'vue'

export const useLogStore = defineStore('logStore', () => {
  const logs = ref([] as string[])
  const log = ref('')
  const selectedLog = ref('')
  const reverseLog = ref(false)

  const getLog = async (name: string) => {
    const resp = await API.getLog(name, reverseLog.value)

    log.value = resp
    selectedLog.value = name
  }

  const getLogs = async () => {
    const resp = await API.getLogs()
    logs.value = resp
  }

  const setReverseLog = async (reverse: boolean) => {
    reverseLog.value = reverse
  }

  return {
    logs,
    log,
    selectedLog,
    reverseLog,
    getLog,
    getLogs,
    setReverseLog
  }
})
