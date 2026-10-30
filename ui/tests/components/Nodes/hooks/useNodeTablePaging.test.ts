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

import useNodeTablePaging from '@/components/Nodes/hooks/useNodeTablePaging'
import { NodePage } from '@/types'
import { describe, expect, it, vi } from 'vitest'

// A stand-in store slice: it takes the page it is asked for on success and keeps what it had
// on failure, as the node slices do.
const createSlice = (total = 12) => {
  const slice = {
    page: undefined as NodePage | undefined,
    total,
    fail: false,
    load: vi.fn(async (page: NodePage) => {
      if (!slice.fail) {
        slice.page = page
      }
    })
  }

  return slice
}

const paging = (slice: ReturnType<typeof createSlice>, pageSize = 5) => useNodeTablePaging({
  pageSize,
  load: slice.load,
  shownPage: () => slice.page,
  total: () => slice.total
})

describe('useNodeTablePaging', () => {
  it('starts on the first page and asks for it', async () => {
    const slice = createSlice()
    const { firstPage, first } = paging(slice)

    await firstPage()

    expect(slice.load).toHaveBeenCalledWith({ offset: 0, limit: 5 })
    expect(first.value).toBe(0)
  })

  it('moves to the page and rows the user picks', async () => {
    const slice = createSlice()
    const { firstPage, onPage, first, rows } = paging(slice)
    await firstPage()

    await onPage({ first: 10, rows: 10 })

    expect(slice.load).toHaveBeenLastCalledWith({ offset: 10, limit: 10 })
    expect(first.value).toBe(10)
    expect(rows.value).toBe(10)
  })

  // The slice still holds page one, so the paginator must say page one.
  it('goes back to the page on screen when a page change fails', async () => {
    const slice = createSlice()
    const { firstPage, onPage, first, rows } = paging(slice)
    await firstPage()

    slice.fail = true
    await onPage({ first: 5, rows: 10 })

    expect(first.value).toBe(0)
    expect(rows.value).toBe(5)
  })

  // The newer fetch decides; the older one, settling late, must not move the paginator back.
  it('lets only the latest fetch settle the paginator', async () => {
    const slice = createSlice()
    const { firstPage, onPage, first } = paging(slice)
    await firstPage()

    let release: () => void = () => undefined
    slice.load.mockImplementationOnce(() => new Promise<void>((resolve) => {
      release = resolve
    }))
    const older = onPage({ first: 5, rows: 5 })
    await onPage({ first: 10, rows: 5 })
    release()
    await older

    expect(first.value).toBe(10)
  })

  it('steps back to the last page when the page is past the end', async () => {
    const slice = createSlice()
    const { firstPage, onPage, fetchPage, first } = paging(slice)
    await firstPage()
    await onPage({ first: 10, rows: 5 })

    slice.total = 7
    await fetchPage()

    expect(first.value).toBe(5)
    expect(slice.load).toHaveBeenLastCalledWith({ offset: 5, limit: 5 })
  })

  it('stays on page one when nothing is on hand for the node yet', async () => {
    const slice = createSlice()
    slice.fail = true
    const { firstPage, first } = paging(slice)

    await firstPage()

    expect(first.value).toBe(0)
  })
})
