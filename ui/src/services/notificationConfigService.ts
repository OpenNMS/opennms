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
import { DestinationPath, NotifdStatus } from '@/types/notificationConfig'
import { rest } from './axiosInstances'

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

const getDestinationPaths = async (): Promise<DestinationPath[]> => {
  try {
    startSpinner()
    const resp = await rest.get(`${endpoint}/destination-paths`)
    return resp.data?.path ?? []
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load destination paths.' })
    return []
  } finally {
    stopSpinner()
  }
}


export {
  getDestinationPaths,
  getNotificationConfigStatus,
  setNotificationConfigStatus
}
