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
import { SEARCH_DEBOUNCE_MS, useDebouncedSearch } from '@/components/Nodes/hooks/useDebouncedSearch'

describe('useDebouncedSearch', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('follows the input immediately but applies on a delay', () => {
    const { searchTerm, appliedTerm, onSearch } = useDebouncedSearch()

    onSearch('eth')
    expect(searchTerm.value).toBe('eth')
    expect(appliedTerm.value).toBe('')

    vi.advanceTimersByTime(SEARCH_DEBOUNCE_MS)
    expect(appliedTerm.value).toBe('eth')
  })

  it('coalesces a burst of typing into one applied term', () => {
    const { appliedTerm, onSearch } = useDebouncedSearch()

    onSearch('e')
    vi.advanceTimersByTime(50)
    onSearch('et')
    vi.advanceTimersByTime(50)
    onSearch('eth')
    expect(appliedTerm.value).toBe('')

    vi.advanceTimersByTime(SEARCH_DEBOUNCE_MS)
    expect(appliedTerm.value).toBe('eth')
  })

  // Normalised once here rather than per row per keystroke.
  it('trims and lowercases the applied term', () => {
    const { appliedTerm, onSearch } = useDebouncedSearch()

    onSearch('  UPLink  ')
    vi.advanceTimersByTime(SEARCH_DEBOUNCE_MS)

    expect(appliedTerm.value).toBe('uplink')
  })

  it('treats an undefined value as empty', () => {
    const { searchTerm, onSearch } = useDebouncedSearch()

    onSearch(undefined)

    expect(searchTerm.value).toBe('')
  })

  // The user is asking to see everything again, so it takes effect at once.
  it('clears without waiting for the debounce', () => {
    const { searchTerm, appliedTerm, onSearch, clearSearch } = useDebouncedSearch()

    onSearch('eth')
    vi.advanceTimersByTime(SEARCH_DEBOUNCE_MS)
    expect(appliedTerm.value).toBe('eth')

    clearSearch()
    expect(searchTerm.value).toBe('')
    expect(appliedTerm.value).toBe('')
  })

  // Without cancelling, the apply queued by those keystrokes would land after the clear and
  // re-filter the table.
  it('drops a pending apply when cleared mid-type', () => {
    const { appliedTerm, onSearch, clearSearch } = useDebouncedSearch()

    onSearch('eth')
    clearSearch()
    vi.advanceTimersByTime(SEARCH_DEBOUNCE_MS * 2)

    expect(appliedTerm.value).toBe('')
  })
})
