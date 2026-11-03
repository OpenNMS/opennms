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
import { ValidationResult } from '@/types/validation'

// UI feedback for store/service calls that return a ValidationResult, so the
// services and stores themselves stay free of spinners and snackbars.
const useActionFeedback = () => {
  const { showSnackBar } = useSnackbar()
  const { startSpinner, stopSpinner } = useSpinner()

  // The spinner is a single flag, not a counter, so wrap a whole user action
  // once rather than each request inside it.
  const withSpinner = async <T>(action: () => Promise<T>): Promise<T> => {
    startSpinner()
    try {
      return await action()
    } finally {
      stopSpinner()
    }
  }

  // Failure: the result's message as an error. Success: the optional success
  // message, then any follow-up errors (e.g. the list refresh after a save
  // failed). Returns result.success.
  const report = (result: ValidationResult, successMsg?: string): boolean => {
    if (!result.success) {
      showSnackBar({ msg: result.message, error: true })
      return false
    }
    if (successMsg) {
      showSnackBar({ msg: successMsg })
    }
    result.errors?.forEach(msg => showSnackBar({ msg, error: true }))
    return true
  }

  const showError = (msg: string) => showSnackBar({ msg, error: true })
  const showSuccess = (msg: string) => showSnackBar({ msg })

  return { withSpinner, report, showError, showSuccess }
}

export default useActionFeedback
