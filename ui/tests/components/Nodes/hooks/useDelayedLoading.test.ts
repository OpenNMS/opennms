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

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { effectScope } from 'vue'
import { LOADING_DELAY_MS, useDelayedLoading } from '@/components/Nodes/hooks/useDelayedLoading'

describe('useDelayedLoading', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('marks a fetch as running straight away', () => {
    const { isFetching, showSpinner, start } = useDelayedLoading()

    start()

    expect(isFetching.value).toBe(true)
    expect(showSpinner.value).toBe(false)
  })

  // The point of the delay: a fetch that returns promptly never flashes a spinner.
  it('shows no spinner for a fetch that finishes inside the delay', () => {
    const { showSpinner, start, stop } = useDelayedLoading()

    start()
    vi.advanceTimersByTime(LOADING_DELAY_MS - 25)
    stop()
    vi.advanceTimersByTime(LOADING_DELAY_MS * 2)

    expect(showSpinner.value).toBe(false)
  })

  it('shows the spinner once the fetch outlives the delay', () => {
    const { isFetching, showSpinner, start } = useDelayedLoading()

    start()
    vi.advanceTimersByTime(LOADING_DELAY_MS)

    expect(showSpinner.value).toBe(true)
    expect(isFetching.value).toBe(true)
  })

  it('clears both when the fetch finishes', () => {
    const { isFetching, showSpinner, start, stop } = useDelayedLoading()

    start()
    vi.advanceTimersByTime(LOADING_DELAY_MS)
    stop()

    expect(showSpinner.value).toBe(false)
    expect(isFetching.value).toBe(false)
  })

  // What the app-wide useSpinner gets wrong: its single boolean lets whichever caller finishes
  // first hide everyone's spinner.
  describe('overlapping fetches', () => {
    it('keeps waiting until the last one finishes', () => {
      const { isFetching, showSpinner, start, stop } = useDelayedLoading()

      start()
      start()
      vi.advanceTimersByTime(LOADING_DELAY_MS)
      stop()

      expect(showSpinner.value).toBe(true)
      expect(isFetching.value).toBe(true)

      stop()

      expect(showSpinner.value).toBe(false)
      expect(isFetching.value).toBe(false)
    })

    // The second fetch must not restart the clock, or a rapid series would defer the spinner
    // indefinitely.
    it('does not restart the delay for a fetch begun midway through it', () => {
      const { showSpinner, start } = useDelayedLoading()

      start()
      vi.advanceTimersByTime(LOADING_DELAY_MS - 25)
      start()
      vi.advanceTimersByTime(25)

      expect(showSpinner.value).toBe(true)
    })

    // More stops than starts must not leave the count negative, or the next fetch never shows.
    it('survives an unmatched stop', () => {
      const { showSpinner, start, stop } = useDelayedLoading()

      stop()
      start()
      vi.advanceTimersByTime(LOADING_DELAY_MS)

      expect(showSpinner.value).toBe(true)
    })
  })

  it('drops a pending timer when its scope is disposed', () => {
    const scope = effectScope()
    const state = scope.run(() => useDelayedLoading())!

    state.start()
    scope.stop()
    vi.advanceTimersByTime(LOADING_DELAY_MS * 2)

    expect(state.showSpinner.value).toBe(false)
  })
})
