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

import { onScopeDispose, ref } from 'vue'

// Long enough that a fetch which returns promptly never flashes a spinner, short enough that a
// slow one is acknowledged before the user wonders whether anything happened.
export const LOADING_DELAY_MS = 175

/**
 * Loading state for a fetch, in two parts.
 *
 * `isFetching` is true for the whole fetch. `showSpinner` turns on only once the fetch has been
 * running longer than LOADING_DELAY_MS, so the common fast fetch shows nothing at all.
 *
 * Both are needed: the tables clear their rows when a fetch starts, so without `isFetching` the
 * empty state would claim 'No results found.' during the delay, which is a statement about the
 * data rather than about still waiting for it.
 *
 * Reference counted, so overlapping fetches -- a node changed twice in quick succession -- cannot
 * turn each other off. This is exactly what the app-wide useSpinner does not do: its single
 * boolean means whichever caller finishes first hides everyone's spinner.
 */
export const useDelayedLoading = () => {
  const isFetching = ref(false)
  const showSpinner = ref(false)

  let inFlight = 0
  let timer: ReturnType<typeof setTimeout> | undefined

  const clearTimer = () => {
    if (timer !== undefined) {
      clearTimeout(timer)
      timer = undefined
    }
  }

  const start = () => {
    inFlight += 1
    isFetching.value = true

    if (timer === undefined && !showSpinner.value) {
      timer = setTimeout(() => {
        timer = undefined
        showSpinner.value = true
      }, LOADING_DELAY_MS)
    }
  }

  const stop = () => {
    inFlight = Math.max(0, inFlight - 1)

    if (inFlight > 0) {
      return
    }

    clearTimer()
    isFetching.value = false
    showSpinner.value = false
  }

  // A fetch still running at unmount would otherwise leave its timer to fire into nothing.
  onScopeDispose(() => {
    inFlight = 0
    clearTimer()
  })

  return { isFetching, showSpinner, start, stop }
}
