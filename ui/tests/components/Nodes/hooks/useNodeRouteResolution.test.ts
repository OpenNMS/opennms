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

import { useNodeRouteResolution } from '@/components/Nodes/hooks/useNodeRouteResolution'
import API from '@/services'
import { useNodeStore } from '@/stores/nodeStore'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h, ref } from 'vue'

const routerReplace = vi.fn()
const routeQuery: Record<string, string> = {}
vi.mock('vue-router', () => ({
  useRoute: () => ({ query: routeQuery }),
  useRouter: () => ({ replace: routerReplace })
}))

vi.mock('@/services', () => ({
  default: { resolveNodeId: vi.fn() }
}))

const resolveNodeId = vi.mocked(API.resolveNodeId)

/** A promise the test resolves itself, to land responses out of order. */
const deferred = <T>() => {
  let resolve: (value: T) => void = () => {}
  const promise = new Promise<T>((r) => {
    resolve = r
  })
  return { promise, resolve }
}

// Any route builder will do: the hook must use the one it is given, not Node Details'.
const routeFor = (id: string | number) => ({ name: 'Some Node Page', params: { id: String(id) }})

type Resolution = ReturnType<typeof useNodeRouteResolution>

describe('useNodeRouteResolution', () => {
  let wrapper: VueWrapper<any>
  let resolution: Resolution
  const load = vi.fn<(id: string) => Promise<void>>()
  const id = ref('42')

  const mountHook = (initial: string) => {
    id.value = initial
    const Host = defineComponent({
      setup() {
        resolution = useNodeRouteResolution(() => id.value, routeFor, load)
        return () => h('div')
      }
    })
    wrapper = mount(Host, {
      global: { plugins: [createTestingPinia({ stubActions: false })] }
    })
    return resolution
  }

  beforeEach(() => {
    vi.clearAllMocks()
    load.mockResolvedValue(undefined)
    for (const key of Object.keys(routeQuery)) {
      delete routeQuery[key]
    }
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('loads a numeric id and shows the node', async () => {
    const { pageState, lookupValue } = mountHook('42')
    await flushPromises()

    expect(load).toHaveBeenCalledWith('42')
    expect(resolveNodeId).not.toHaveBeenCalled()
    expect(pageState.value).toBe('node')
    expect(lookupValue.value).toBe('42')
  })

  // Started before the first render, so the store clears an earlier visit's failure in time.
  it('starts the load during setup', () => {
    mountHook('42')

    expect(load).toHaveBeenCalledWith('42')
  })

  it('loads again when the id changes', async () => {
    mountHook('42')
    await flushPromises()

    id.value = '99'
    await flushPromises()

    expect(load).toHaveBeenCalledTimes(2)
    expect(load).toHaveBeenLastCalledWith('99')
  })

  it('replaces a non-canonical numeric id with the canonical one, keeping the query', async () => {
    routeQuery.tab = 'x'
    mountHook('007')
    await flushPromises()

    expect(routerReplace).toHaveBeenCalledWith({ name: 'Some Node Page', params: { id: '7' }, query: { tab: 'x' }})
    expect(load).not.toHaveBeenCalled()
  })

  it('resolves foreignSource:foreignId and replaces the route with the given route for the node id', async () => {
    routeQuery.tab = 'x'
    resolveNodeId.mockResolvedValue(createResultWithPayload(true, '', '153'))
    const { pageState, lookupValue } = mountHook('fs:fid')

    expect(pageState.value).toBe('resolving')
    expect(lookupValue.value).toBe('fs:fid')
    await flushPromises()

    expect(resolveNodeId).toHaveBeenCalledWith('fs:fid')
    expect(routerReplace).toHaveBeenCalledWith({ name: 'Some Node Page', params: { id: '153' }, query: { tab: 'x' }})
    expect(load).not.toHaveBeenCalled()
  })

  it('says notFound when the foreign id resolves to no node', async () => {
    resolveNodeId.mockResolvedValue(createResultWithPayload(true, '', null))
    const { pageState } = mountHook('fs:nope')
    await flushPromises()

    expect(pageState.value).toBe('notFound')
    expect(routerReplace).not.toHaveBeenCalled()
  })

  it('says error when the foreign id lookup fails', async () => {
    resolveNodeId.mockResolvedValue(createResultWithPayload<string | null>(false, 'Failed'))
    const { pageState } = mountHook('fs:1')
    await flushPromises()

    expect(pageState.value).toBe('error')
    expect(routerReplace).not.toHaveBeenCalled()
  })

  it('says invalid, and asks nothing, for an id that is neither form', async () => {
    const { pageState, lookupValue } = mountHook('abc')
    await flushPromises()

    expect(pageState.value).toBe('invalid')
    expect(lookupValue.value).toBe('abc')
    expect(load).not.toHaveBeenCalled()
    expect(resolveNodeId).not.toHaveBeenCalled()
    expect(routerReplace).not.toHaveBeenCalled()
  })

  // The user moved on to another foreign id before the first lookup answered: the late answer must
  // not send them to the first node, nor overwrite the second lookup's state.
  it('ignores a lookup that a later one has superseded', async () => {
    const first = deferred<ValidationResultWithPayload<string | null>>()
    const second = deferred<ValidationResultWithPayload<string | null>>()
    resolveNodeId.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise)

    const { pageState } = mountHook('fs:a')
    await flushPromises()
    id.value = 'fs:b'
    await flushPromises()

    second.resolve(createResultWithPayload<string | null>(true, '', null))
    await flushPromises()
    first.resolve(createResultWithPayload<string | null>(true, '', '1'))
    await flushPromises()

    expect(routerReplace).not.toHaveBeenCalled()
    expect(pageState.value).toBe('notFound')
  })

  it('reports the node store\'s load error for a numeric id', async () => {
    const { pageState } = mountHook('42')
    await flushPromises()
    const nodeStore = useNodeStore()

    nodeStore.nodeLoadError = 'notFound'
    expect(pageState.value).toBe('notFound')

    nodeStore.nodeLoadError = 'failed'
    expect(pageState.value).toBe('error')
  })

  it('looks the foreign id up again on followRoute', async () => {
    resolveNodeId.mockResolvedValue(createResultWithPayload<string | null>(false, 'Failed'))
    const { followRoute } = mountHook('fs:1')
    await flushPromises()

    await followRoute()

    expect(resolveNodeId).toHaveBeenCalledTimes(2)
  })
})
