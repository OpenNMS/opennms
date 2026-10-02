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

import { onActivated, onDeactivated, ref, watch } from 'vue'
import { useDocumentVisibility, useIntervalFn } from '@vueuse/core'

/**
 * Call `fn` every `intervalMs`, but only while someone can see the component calling this: not
 * while the browser tab is hidden, and not while a KeepAlive has switched the component away
 * (it stays alive, and so would a plain timer). Coming back from a KeepAlive switch calls `fn`
 * straight away, since the data may be a long way out of date by then.
 *
 * It does not make the first call -- callers fetch on mount or on an id change themselves -- and
 * the timer stops when the component unmounts. Call it from `setup`.
 */
const useVisiblePolling = (fn: () => unknown, intervalMs: number) => {
  const visibility = useDocumentVisibility()
  const { pause, resume } = useIntervalFn(fn, intervalMs, { immediate: false })
  const active = ref(true)

  watch([active, visibility], ([isActive, vis]) => {
    if (isActive && vis === 'visible') {
      resume()
    } else {
      pause()
    }
  }, { immediate: true })

  onActivated(() => {
    if (!active.value) {
      fn()
    }

    active.value = true
  })

  onDeactivated(() => {
    active.value = false
  })
}

export default useVisiblePolling
