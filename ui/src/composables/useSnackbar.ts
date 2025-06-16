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

import { SnackbarProps } from '@/types'
import { isDefined } from '@vueuse/core'

const isDisplayed = ref(false)
const isCentered = ref<boolean | undefined>(true)
const hasError = ref<boolean | undefined>(false)
const message = ref('')
const setTimeout = ref<number | undefined>(4000)

const useSnackbar = () => {
  const showSnackBar = (snackbarProps: SnackbarProps) => {
    const { center, error, msg, timeout } = snackbarProps
    isDisplayed.value = true
    isCentered.value = isDefined(center) ? center : true
    hasError.value = error
    message.value = msg
    setTimeout.value = timeout
  }

  const hideSnackbar = () => {
    isDisplayed.value = false
    message.value = ''
  }

  return {
    showSnackBar,
    hideSnackbar,
    isDisplayed: isDisplayed,
    isCentered: readonly(isCentered),
    hasError: readonly(hasError),
    message: readonly(message),
    setTimeout: readonly(setTimeout)
  }
}

export default useSnackbar
