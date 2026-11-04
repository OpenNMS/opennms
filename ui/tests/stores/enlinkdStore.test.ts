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

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useEnlinkdStore } from '@/stores/enlinkdStore'
import API from '@/services'

vi.mock('@/services', () => ({
  default: { getNodeEnlinkdElements: vi.fn() }
}))

const elements = (sysName: string) => ({ lldp: { lldpSysName: sysName }, bridges: [] })
const ok = (payload: unknown) => ({ success: true, message: '', payload }) as never

describe('enlinkdStore.getNodeElements', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('publishes the node\'s elements, stamped with the node id', async () => {
    vi.mocked(API.getNodeEnlinkdElements).mockResolvedValue(ok(elements('sw1')))
    const store = useEnlinkdStore()

    const result = await store.getNodeElements('144')

    expect(API.getNodeEnlinkdElements).toHaveBeenCalledWith('144')
    expect(result.success).toBe(true)
    expect(store.nodeElements).toEqual(elements('sw1'))
    expect(store.nodeElementsNodeId).toBe('144')
  })

  // What did load is still this node's.
  it('publishes a partial answer too', async () => {
    vi.mocked(API.getNodeEnlinkdElements).mockResolvedValue({ success: false, message: 'some failed', payload: elements('sw1') } as never)
    const store = useEnlinkdStore()

    const result = await store.getNodeElements('144')

    expect(result.success).toBe(false)
    expect(store.nodeElements).toEqual(elements('sw1'))
    expect(store.nodeElementsNodeId).toBe('144')
  })

  it('discards a superseded answer: node 144 answering late does not replace node 161', async () => {
    let resolve144: (value: unknown) => void = () => undefined
    vi.mocked(API.getNodeEnlinkdElements)
      .mockImplementationOnce(() => new Promise((r) => {
        resolve144 = r
      }) as never)
      .mockResolvedValueOnce(ok(elements('sw161')))
    const store = useEnlinkdStore()

    const call144 = store.getNodeElements('144')
    await store.getNodeElements('161')
    resolve144(ok(elements('sw144')))

    expect((await call144).success).toBe(false)
    expect(store.nodeElementsNodeId).toBe('161')
    expect(store.nodeElements).toEqual(elements('sw161'))
  })
})
