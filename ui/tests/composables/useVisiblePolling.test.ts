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

import useVisiblePolling from '@/composables/useVisiblePolling'
import { enableAutoUnmount, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h, KeepAlive, nextTick, ref } from 'vue'

enableAutoUnmount(afterEach)

const INTERVAL = 60_000

const setVisibility = async (state: 'visible' | 'hidden') => {
  Object.defineProperty(document, 'visibilityState', { value: state, configurable: true })
  document.dispatchEvent(new Event('visibilitychange'))
  await nextTick()
}

describe('useVisiblePolling', () => {
  let fn: ReturnType<typeof vi.fn<() => void>>

  const Poller = defineComponent({
    setup() {
      useVisiblePolling(fn, INTERVAL)

      return () => h('div')
    }
  })

  beforeEach(async () => {
    vi.useFakeTimers()
    fn = vi.fn<() => void>()
    await setVisibility('visible')
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('calls on each interval, but not on mount', async () => {
    mount(Poller)
    expect(fn).not.toHaveBeenCalled()

    await vi.advanceTimersByTimeAsync(INTERVAL * 2)

    expect(fn).toHaveBeenCalledTimes(2)
  })

  it('does not call while the browser tab is hidden', async () => {
    mount(Poller)
    await setVisibility('hidden')

    await vi.advanceTimersByTimeAsync(INTERVAL * 3)

    expect(fn).not.toHaveBeenCalled()
  })

  // The data may be hours old by then, so it does not wait for the next tick.
  it('calls at once when the browser tab is shown again, then keeps to the interval', async () => {
    mount(Poller)
    await setVisibility('hidden')
    await vi.advanceTimersByTimeAsync(INTERVAL * 3)

    await setVisibility('visible')
    expect(fn).toHaveBeenCalledTimes(1)

    await vi.advanceTimersByTimeAsync(INTERVAL)
    expect(fn).toHaveBeenCalledTimes(2)
  })

  describe('in a KeepAlive', () => {
    const show = ref(true)
    const Host = defineComponent({
      setup: () => () => h(KeepAlive, null, [show.value ? h(Poller) : h('div')])
    })

    beforeEach(() => {
      show.value = true
    })

    it('does not call while switched away, and calls once when switched back', async () => {
      mount(Host)
      show.value = false
      await nextTick()
      await vi.advanceTimersByTimeAsync(INTERVAL * 3)
      expect(fn).not.toHaveBeenCalled()

      show.value = true
      await nextTick()

      expect(fn).toHaveBeenCalledTimes(1)
    })

    // Hidden by the KeepAlive, so the browser tab coming back is not a reason to call.
    it('does not call when the browser tab is shown while it is switched away', async () => {
      mount(Host)
      show.value = false
      await nextTick()
      await setVisibility('hidden')

      await setVisibility('visible')

      expect(fn).not.toHaveBeenCalled()
    })
  })
})
