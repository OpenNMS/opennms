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

import { onUnmounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/stores/dashboardStore'

// Owns the single global refresh timer (NMS-4404). Panels react to
// store.refreshTick rather than each holding their own timer. Auto-refresh is
// suspended while paused, while in edit mode, or when the interval is "off".
export const useDashboardRefresh = () => {
  const store = useDashboardStore()
  const { refreshSeconds, isPaused, editMode } = storeToRefs(store)
  let timer: ReturnType<typeof setInterval> | null = null

  const clear = () => {
    if (timer !== null) {
      clearInterval(timer)
      timer = null
    }
  }

  const restart = () => {
    clear()
    if (isPaused.value || editMode.value || refreshSeconds.value <= 0) {
      return
    }
    timer = setInterval(() => store.tickRefresh(), refreshSeconds.value * 1000)
  }

  watch([refreshSeconds, isPaused, editMode], restart, { immediate: true })
  onUnmounted(clear)
}
