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
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore - no type declarations published for this entry point
import ToastEventBus from 'primevue/toasteventbus'

// Snackbars are rendered by a PrimeVue Toast (see Common/Snackbar.vue). We emit
// on PrimeVue's toast event bus rather than calling useToast(): useToast()
// depends on an inject context, but this composable is also used at module scope
// (Pinia stores, the router, services), where inject is unavailable. The event
// bus is a module singleton and works everywhere.
export const SNACKBAR_GROUP_CENTER = 'snackbar-center'
export const SNACKBAR_GROUP_START = 'snackbar-start'

const DEFAULT_TIMEOUT = 4000

const useSnackbar = () => {
  const showSnackBar = (snackbarProps: SnackbarProps) => {
    const { center, error, msg, timeout } = snackbarProps

    ToastEventBus.emit('add', {
      severity: error ? 'error' : 'success',
      detail: msg,
      life: timeout ?? DEFAULT_TIMEOUT,
      closable: true,
      group: (isDefined(center) ? center : true) ? SNACKBAR_GROUP_CENTER : SNACKBAR_GROUP_START
    })
  }

  const hideSnackbar = () => {
    ToastEventBus.emit('remove-all-groups')
  }

  return {
    showSnackBar,
    hideSnackbar
  }
}

export default useSnackbar
