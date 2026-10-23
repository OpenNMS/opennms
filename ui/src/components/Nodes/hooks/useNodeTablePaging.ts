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

import { ref } from 'vue'
import { NodePage } from '@/types'

/**
 * Paging for a Node Details table whose rows are a page of a store's node slice.
 *
 * The paginator moves when the user picks a page, before the rows arrive. When the latest fetch
 * settles, it is put back on the page the slice holds for this node, so a failed fetch -- which
 * leaves the previous page in the slice -- never shows that page under the new page number. A
 * fetch that finds the page past the end (rows gone since) steps back to the last page.
 *
 * - `load` fetches the given page for the node on screen.
 * - `shownPage` is the page the slice holds, or undefined when it is not this node's.
 * - `total` is this node's row count.
 */
const useNodeTablePaging = (options: {
  pageSize: number
  load: (page: NodePage) => Promise<unknown>
  shownPage: () => NodePage | undefined
  total: () => number
}) => {
  const first = ref(0)
  const rows = ref(options.pageSize)

  // Only the latest fetch settles the paginator: an older one answering late knows less.
  let latestFetch = 0

  const fetchPage = async (): Promise<void> => {
    const thisFetch = ++latestFetch

    await options.load({ offset: first.value, limit: rows.value })

    if (thisFetch !== latestFetch) {
      return
    }

    const shown = options.shownPage()

    // Nothing of this node's yet: the table shows its loading or failure message, on page one.
    if (!shown) {
      return
    }

    if (shown.offset !== first.value || shown.limit !== rows.value) {
      first.value = shown.offset
      rows.value = shown.limit || options.pageSize

      return
    }

    const total = options.total()

    if (first.value > 0 && first.value >= total) {
      first.value = Math.max(0, Math.floor((total - 1) / rows.value) * rows.value)
      await fetchPage()
    }
  }

  const onPage = (event: { first: number, rows: number }) => {
    first.value = event.first
    rows.value = event.rows

    return fetchPage()
  }

  // For a new node: the page the user was on says nothing about it.
  const firstPage = () => {
    first.value = 0

    return fetchPage()
  }

  return { first, rows, fetchPage, onPage, firstPage }
}

export default useNodeTablePaging
