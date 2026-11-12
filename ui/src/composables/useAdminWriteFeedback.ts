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

import useActionFeedback from '@/composables/useActionFeedback'
import { AdminWriteResult } from '@/types/adminWrite'

export interface AdminWriteHandlers {
  // closes the panel or dialog; called on success and on a stale write
  close?: () => void
  // shows a non-stale error inside the panel or dialog; without it the error is a snackbar
  banner?: (msg: string) => void
}

// Feedback for a user or group write:
//  - success: the success message, then any follow-up errors (a failed list reload); close
//  - stale: the stale text (the store has already reloaded the list) as one error snackbar; close,
//    so the editor reopens on the fresh object and version
//  - anything else: the banner when there is one, else an error snackbar; stay open
// Returns result.success.
const useAdminWriteFeedback = () => {
  const { report, showError } = useActionFeedback()

  const reportWrite = (result: AdminWriteResult, successMsg: string, handlers: AdminWriteHandlers = {}): boolean => {
    if (result.success) {
      report(result, successMsg)
      handlers.close?.()
      return true
    }
    if (result.stale) {
      showError(result.message)
      handlers.close?.()
    } else if (handlers.banner) {
      handlers.banner(result.message)
    } else {
      showError(result.message)
    }
    return false
  }

  return { reportWrite }
}

export default useAdminWriteFeedback
