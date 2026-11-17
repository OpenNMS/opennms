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

import { AdminWriteResult } from '@/types/adminWrite'
import { ValidationResult } from '@/types/validation'
import { serverMessage } from './serverMessage'

// The result of a failed user or group write. A 412 is reported by flag only: the store words
// the stale message once the list has been reloaded.
export const adminWriteError = (err: any, fallback: string): AdminWriteResult => {
  if (err?.response?.status === 412) {
    return { success: false, message: '', stale: true }
  }
  return { success: false, message: serverMessage(err, fallback), stale: false }
}

export interface StaleMessages {
  // the list was reloaded
  reloaded: string
  // the reload failed too, so the list on screen is still out of date
  reloadFailed: string
}

// After a user or group write: a success reloads the list (a failed reload goes in the result's
// errors); a stale write means the list on screen is out of date, so it reloads too and words the
// stale message by whether that reload worked. The caller shows the result.
export const reloadAfterWrite = async (result: AdminWriteResult, reload: () => Promise<ValidationResult>,
  stale: StaleMessages): Promise<AdminWriteResult> => {
  if (result.success) {
    const reloaded = await reload()
    return reloaded.success ? result : { ...result, errors: [reloaded.message] }
  }
  if (result.stale) {
    const reloaded = await reload()
    return { ...result, message: reloaded.success ? stale.reloaded : stale.reloadFailed }
  }
  return result
}
