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

// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore - no type declarations published for this entry point
import ToastEventBus from 'primevue/toasteventbus'

// Toasts are rendered by OnmsToastHost (two PrimeVue Toast outlets). We emit on
// PrimeVue's toast event bus rather than calling useToast(): useToast() depends
// on an inject context, but this composable is also used at module scope
// (Pinia stores, the router, services), where inject is unavailable. The event
// bus is a module singleton and works everywhere.
export const ONMS_TOAST_GROUP_CENTER = 'onms-toast-center'
export const ONMS_TOAST_GROUP_START = 'onms-toast-start'

const DEFAULT_TIMEOUT = 4000

export type OnmsToastSeverity = 'success' | 'info' | 'warn' | 'error'

export interface OnmsToastOptions {
  message: string
  severity?: OnmsToastSeverity
  center?: boolean
  timeout?: number
}

// Tracks toasts that are currently displayed (keyed by severity + group +
// message) so that identical toasts are not stacked: a burst of identical
// messages (e.g. one validation-error toast per invalid field) would otherwise
// produce a pile of duplicates. Distinct messages still stack normally.
const activeKeys = new Map<string, number>()

export const useOnmsToast = () => {
  const showToast = (options: OnmsToastOptions) => {
    const severity = options.severity ?? 'success'
    const group = (options.center ?? true) ? ONMS_TOAST_GROUP_CENTER : ONMS_TOAST_GROUP_START
    const life = options.timeout ?? DEFAULT_TIMEOUT
    const key = `${severity}::${group}::${options.message}`

    // Suppress an identical toast while one is still visible.
    if (activeKeys.has(key)) {
      return
    }

    ToastEventBus.emit('add', {
      severity,
      detail: options.message,
      life,
      closable: true,
      group
    })

    activeKeys.set(key, window.setTimeout(() => activeKeys.delete(key), life))
  }

  const hideAllToasts = () => {
    ToastEventBus.emit('remove-all-groups')
    activeKeys.forEach(timer => window.clearTimeout(timer))
    activeKeys.clear()
  }

  return {
    showToast,
    hideAllToasts
  }
}
