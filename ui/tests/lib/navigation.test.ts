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

import { navigateTo, SAME_PAGE_NAVIGATION_EVENT } from '@/lib/navigation'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

describe('navigateTo', () => {
  const original = window.location
  const assign = vi.fn()
  const current = 'http://localhost:8980/opennms/ui/index.html#/notifications?preset=yourOutstanding'

  beforeEach(() => {
    assign.mockClear()
    Object.defineProperty(window, 'location', { configurable: true, value: { href: current, assign }})
  })

  afterEach(() => {
    Object.defineProperty(window, 'location', { configurable: true, value: original })
  })

  it('navigates to a different page', () => {
    const listener = vi.fn()
    window.addEventListener(SAME_PAGE_NAVIGATION_EVENT, listener)

    navigateTo('http://localhost:8980/opennms/ui/index.html#/notifications?preset=teamOutstanding')

    expect(assign).toHaveBeenCalledWith('http://localhost:8980/opennms/ui/index.html#/notifications?preset=teamOutstanding')
    expect(listener).not.toHaveBeenCalled()
    window.removeEventListener(SAME_PAGE_NAVIGATION_EVENT, listener)
  })

  it('signals the current page instead of a no-op assign when the link is the page already open', () => {
    const listener = vi.fn()
    window.addEventListener(SAME_PAGE_NAVIGATION_EVENT, listener)

    navigateTo(current)

    expect(assign).not.toHaveBeenCalled()
    expect(listener).toHaveBeenCalledTimes(1)
    window.removeEventListener(SAME_PAGE_NAVIGATION_EVENT, listener)
  })
})
