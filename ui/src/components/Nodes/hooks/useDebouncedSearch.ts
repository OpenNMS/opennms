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
import { debounce } from 'lodash'

// Long enough to coalesce a burst of typing, short enough that the table still feels live.
export const SEARCH_DEBOUNCE_MS = 175

/**
 * The search term behind a client-side table filter, in two parts: what the input shows, which
 * follows every keystroke, and what the table filters on, which lags by SEARCH_DEBOUNCE_MS.
 *
 * Unlike the legacy interfaces page there is no minimum length -- a single character filters.
 * On these tables that is the useful case, not a degenerate one: typing '7' in the SNMP table
 * is how you get to ifIndex 7, and a one-character guard makes single-digit indexes unreachable.
 *
 * The applied term is lowercased and trimmed once here rather than per row per keystroke.
 */
export const useDebouncedSearch = () => {
  const searchTerm = ref('')
  const appliedTerm = ref('')

  const apply = debounce((value: string) => {
    appliedTerm.value = value.trim().toLowerCase()
  }, SEARCH_DEBOUNCE_MS)

  const onSearch = (value: string | undefined) => {
    searchTerm.value = value ?? ''
    apply(searchTerm.value)
  }

  // Clearing is not debounced: the user is asking to see everything again, and a pending apply
  // from the keystrokes before the clear would otherwise land afterwards and re-filter.
  const clearSearch = () => {
    apply.cancel()
    searchTerm.value = ''
    appliedTerm.value = ''
  }

  // A keystroke close to unmount would otherwise fire its apply after the owning scope is gone,
  // writing to refs nothing can read any more -- and, under the fake timers the table tests use,
  // into whatever runs next.
  onScopeDispose(() => apply.cancel())

  return { searchTerm, appliedTerm, onSearch, clearSearch }
}
